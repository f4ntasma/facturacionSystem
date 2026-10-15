package pe.altoque.usuario;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.altoque.comprobante.ComprobanteRepository;
import pe.altoque.empresa.CertificadoRepository;
import pe.altoque.empresa.Empresa;
import pe.altoque.empresa.Plan;

import java.time.LocalDate;

/** Lo que la app necesita saber del usuario al arrancar: quién es, su negocio, su plan y cuánto ha usado. */
@Service
public class PerfilService {

    public record PlanVista(String codigo, String nombre, Integer limiteComprobantes, int maxUsuarios, int maxEstablecimientos,
                            boolean notasCredito, boolean reportesAvanzados) {}

    public record EmpresaVista(Long id, String ruc, String razonSocial, String nombreComercial, String direccionFiscal,
                               String ambienteSunat, String estado, LocalDate pruebaHasta, boolean certificadoPropio,
                               boolean solConfigurado, PlanVista plan, long usadosMes) {}

    public record Perfil(Long id, String nombre, String email, Usuario.Rol rol, Long establecimientoId, String establecimiento,
                         EmpresaVista empresa) {}

    private final UsuarioRepository usuarios;
    private final ComprobanteRepository comprobantes;
    private final CertificadoRepository certificados;

    public PerfilService(UsuarioRepository usuarios, ComprobanteRepository comprobantes, CertificadoRepository certificados) {
        this.usuarios = usuarios;
        this.comprobantes = comprobantes;
        this.certificados = certificados;
    }

    @Transactional(readOnly = true)
    public Perfil de(Long usuarioId) {
        Usuario u = usuarios.findById(usuarioId).orElseThrow();
        Empresa e = u.getEmpresa();
        Plan p = e.getPlan();
        LocalDate hoy = LocalDate.now();
        long usados = comprobantes.countByEmpresaIdAndFechaEmisionBetween(e.getId(), hoy.withDayOfMonth(1), hoy.withDayOfMonth(hoy.lengthOfMonth()));
        boolean propio = certificados.findFirstByEmpresaIdAndActivoTrueOrderByIdDesc(e.getId()).map(c -> !c.isAutogenerado()).orElse(false);
        var plan = new PlanVista(p.getCodigo(), p.getNombre(), p.getLimiteComprobantes(), p.getMaxUsuarios(), p.getMaxEstablecimientos(),
                p.isNotasCredito(), p.isReportesAvanzados());
        var empresa = new EmpresaVista(e.getId(), e.getRuc(), e.getRazonSocial(), e.getNombreComercial(), e.getDireccionFiscal(),
                e.getAmbienteSunat().name(), e.getEstado().name(), e.getPruebaHasta(), propio,
                e.getSolUsuario() != null && e.getSolClaveEnc() != null, plan, usados);
        return new Perfil(u.getId(), u.getNombre(), u.getEmail(), u.getRol(),
                u.getEstablecimiento() != null ? u.getEstablecimiento().getId() : null,
                u.getEstablecimiento() != null ? u.getEstablecimiento().getNombre() : null, empresa);
    }
}
