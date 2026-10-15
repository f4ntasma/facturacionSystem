package pe.altoque.empresa;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.altoque.common.ApiException;
import pe.altoque.common.Catalogos;
import pe.altoque.comprobante.TipoComprobante;
import pe.altoque.config.AltoqueProperties;
import pe.altoque.referencia.Distrito;
import pe.altoque.referencia.DistritoRepository;
import pe.altoque.usuario.Miembro;
import pe.altoque.usuario.MiembroRepository;
import pe.altoque.usuario.Usuario;
import pe.altoque.usuario.UsuarioRepository;

import java.time.LocalDate;
import java.util.Locale;

@Service
public class EmpresaService {

    /** Ubigeo por defecto si no llega ninguno: Lima, Lima, Lima. */
    public static final String UBIGEO_LIMA = "150101";

    public record DatosEmpresa(String ruc, String razonSocial, String nombreComercial, String direccion, String ubigeo,
                               String departamento, String provincia, String distrito, String telefono, String planCodigo) {}

    public record DatosPersona(String nombre, String email, String password) {}

    private final EmpresaRepository empresas;
    private final EstablecimientoRepository establecimientos;
    private final SerieRepository series;
    private final PlanRepository planes;
    private final UsuarioRepository usuarios;
    private final MiembroRepository miembros;
    private final DistritoRepository distritos;
    private final PasswordEncoder encoder;
    private final AltoqueProperties props;

    public EmpresaService(EmpresaRepository empresas, EstablecimientoRepository establecimientos, SerieRepository series,
                          PlanRepository planes, UsuarioRepository usuarios, MiembroRepository miembros,
                          DistritoRepository distritos, PasswordEncoder encoder, AltoqueProperties props) {
        this.empresas = empresas;
        this.establecimientos = establecimientos;
        this.series = series;
        this.planes = planes;
        this.usuarios = usuarios;
        this.miembros = miembros;
        this.distritos = distritos;
        this.encoder = encoder;
        this.props = props;
    }

    /** Registro desde cero: persona nueva + su primer negocio. */
    @Transactional
    public Miembro registrar(DatosEmpresa d, DatosPersona p) {
        String email = p.email().trim().toLowerCase(Locale.ROOT);
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflicto("Ese correo ya tiene cuenta. Ingresa y agrega este negocio desde «Mis negocios».");
        }
        Usuario u = new Usuario();
        u.setNombre(p.nombre().trim());
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(p.password()));
        usuarios.save(u);
        return crearNegocio(u, d, email);
    }

    /** Una persona que ya tiene cuenta suma otro negocio (p. ej. el contador que lleva varias mypes). */
    @Transactional
    public Miembro agregarNegocio(Long usuarioId, DatosEmpresa d) {
        Usuario u = usuarios.findById(usuarioId).orElseThrow(() -> ApiException.noEncontrado("Usuario"));
        return crearNegocio(u, d, u.getEmail());
    }

    /** Alta completa: empresa, su configuración SUNAT, local principal (0000), series y el dueño. */
    private Miembro crearNegocio(Usuario dueno, DatosEmpresa d, String email) {
        String ruc = d.ruc() == null ? "" : d.ruc().trim();
        if (!Catalogos.rucValido(ruc)) throw ApiException.invalido("El RUC no es válido, pe. Revisa los 11 dígitos.");
        if (empresas.existsByRuc(ruc)) throw ApiException.conflicto("Ese RUC ya tiene una cuenta en Al Toque. Pide acceso a su dueño.");

        Plan plan = planes.findByCodigo(d.planCodigo() == null ? "NEGOCIANTE" : d.planCodigo())
                .orElseGet(() -> planes.findByCodigo("NEGOCIANTE").orElseThrow());

        Empresa e = new Empresa();
        e.setRuc(ruc);
        e.setRazonSocial(d.razonSocial().trim().toUpperCase(Locale.ROOT));
        e.setNombreComercial(blancoANull(d.nombreComercial()));
        e.setDireccionFiscal(d.direccion() == null || d.direccion().isBlank() ? "-" : d.direccion().trim().toUpperCase(Locale.ROOT));
        e.setUbicacion(resolverUbigeo(d.ubigeo(), d.distrito(), d.provincia(), d.departamento()));
        e.setTelefono(blancoANull(d.telefono()));
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

        Miembro m = new Miembro();
        m.setUsuario(dueno);
        m.setEmpresa(e);
        m.setEstablecimiento(local);
        m.setRol(Usuario.Rol.PROPIETARIO);
        return miembros.save(m);
    }

    public Serie crearSerie(Empresa e, Establecimiento local, TipoComprobante tipo, String codigo) {
        Serie s = new Serie();
        s.setEmpresa(e);
        s.setEstablecimiento(local);
        s.setTipoComprobante(tipo);
        s.setSerie(codigo);
        return series.save(s);
    }

    /** Acepta el ubigeo de 6 dígitos o, si no llega, los nombres que devuelve la consulta RUC. */
    public Distrito resolverUbigeo(String ubigeo, String distrito, String provincia, String departamento) {
        if (ubigeo != null && ubigeo.matches("\\d{6}")) {
            var d = distritos.findById(ubigeo);
            if (d.isPresent()) return d.get();
        }
        if (distrito != null && provincia != null && departamento != null) {
            var porNombre = distritos.porNombres(distrito.trim(), provincia.trim(), departamento.trim());
            if (!porNombre.isEmpty()) return porNombre.getFirst();
        }
        return distritos.findById(UBIGEO_LIMA).orElseThrow();
    }

    private static String blancoANull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
