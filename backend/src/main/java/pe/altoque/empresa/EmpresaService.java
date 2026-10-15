package pe.altoque.empresa;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.altoque.common.ApiException;
import pe.altoque.common.Catalogos;
import pe.altoque.comprobante.TipoComprobante;
import pe.altoque.config.AltoqueProperties;
import pe.altoque.usuario.Usuario;
import pe.altoque.usuario.UsuarioRepository;

import java.time.LocalDate;
import java.util.Locale;

@Service
public class EmpresaService {

    public record NuevaEmpresa(String ruc, String razonSocial, String nombreComercial, String direccion, String ubigeo,
                               String departamento, String provincia, String distrito, String telefono,
                               String nombreUsuario, String email, String password, String planCodigo) {}

    private final EmpresaRepository empresas;
    private final EstablecimientoRepository establecimientos;
    private final SerieRepository series;
    private final PlanRepository planes;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final AltoqueProperties props;

    public EmpresaService(EmpresaRepository empresas, EstablecimientoRepository establecimientos, SerieRepository series,
                          PlanRepository planes, UsuarioRepository usuarios, PasswordEncoder encoder, AltoqueProperties props) {
        this.empresas = empresas;
        this.establecimientos = establecimientos;
        this.series = series;
        this.planes = planes;
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.props = props;
    }

    /** Alta completa: empresa, local principal (0000), series por defecto y usuario propietario. */
    @Transactional
    public Usuario crear(NuevaEmpresa n) {
        String ruc = n.ruc() == null ? "" : n.ruc().trim();
        if (!Catalogos.rucValido(ruc)) throw ApiException.invalido("El RUC no es válido, pe. Revisa los 11 dígitos.");
        if (empresas.existsByRuc(ruc)) throw ApiException.conflicto("Ese RUC ya tiene una cuenta en Al Toque. Ingresa con tu correo.");
        String email = n.email().trim().toLowerCase(Locale.ROOT);
        if (usuarios.existsByEmailIgnoreCase(email)) throw ApiException.conflicto("Ese correo ya está registrado.");

        Plan plan = planes.findByCodigo(n.planCodigo() == null ? "NEGOCIANTE" : n.planCodigo())
                .orElseGet(() -> planes.findByCodigo("NEGOCIANTE").orElseThrow());

        Empresa e = new Empresa();
        e.setRuc(ruc);
        e.setRazonSocial(n.razonSocial().trim().toUpperCase(Locale.ROOT));
        e.setNombreComercial(blancoANull(n.nombreComercial()));
        e.setDireccionFiscal(n.direccion() == null || n.direccion().isBlank() ? "-" : n.direccion().trim().toUpperCase(Locale.ROOT));
        if (n.ubigeo() != null && n.ubigeo().matches("\\d{6}")) e.setUbigeo(n.ubigeo());
        if (n.departamento() != null && !n.departamento().isBlank()) e.setDepartamento(n.departamento().toUpperCase(Locale.ROOT));
        if (n.provincia() != null && !n.provincia().isBlank()) e.setProvincia(n.provincia().toUpperCase(Locale.ROOT));
        if (n.distrito() != null && !n.distrito().isBlank()) e.setDistrito(n.distrito().toUpperCase(Locale.ROOT));
        e.setTelefono(blancoANull(n.telefono()));
        e.setEmail(email);
        e.setPlan(plan);
        e.setEstado(Empresa.Estado.PRUEBA);
        e.setPruebaHasta(LocalDate.now().plusDays(props.diasPrueba()));
        // Arranca en beta: emite de verdad contra SUNAT de pruebas hasta que suba su certificado.
        e.setAmbienteSunat(Empresa.Ambiente.BETA);
        empresas.save(e);

        Establecimiento local = new Establecimiento();
        local.setEmpresa(e);
        local.setCodigoSunat("0000");
        local.setNombre("Principal");
        local.setDireccion(e.getDireccionFiscal());
        local.setUbigeo(e.getUbigeo());
        establecimientos.save(local);

        crearSerie(e, local, TipoComprobante.FACTURA, "F001");
        crearSerie(e, local, TipoComprobante.BOLETA, "B001");
        crearSerie(e, local, TipoComprobante.NOTA_CREDITO, "FC01");
        crearSerie(e, local, TipoComprobante.NOTA_CREDITO, "BC01");
        crearSerie(e, local, TipoComprobante.NOTA_DEBITO, "FD01");
        crearSerie(e, local, TipoComprobante.NOTA_DEBITO, "BD01");

        Usuario u = new Usuario();
        u.setEmpresa(e);
        u.setEstablecimiento(local);
        u.setNombre(n.nombreUsuario().trim());
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(n.password()));
        u.setRol(Usuario.Rol.PROPIETARIO);
        return usuarios.save(u);
    }

    public Serie crearSerie(Empresa e, Establecimiento local, TipoComprobante tipo, String codigo) {
        Serie s = new Serie();
        s.setEmpresa(e);
        s.setEstablecimiento(local);
        s.setTipoComprobante(tipo);
        s.setSerie(codigo);
        return series.save(s);
    }

    private static String blancoANull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
