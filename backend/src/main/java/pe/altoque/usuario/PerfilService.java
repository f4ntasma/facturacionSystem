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
                         EmpresaVista empresa, int negocios) {}

    private final MiembroRepository miembros;
    private final ComprobanteRepository comprobantes;
    private final CertificadoRepository certificados;

    public PerfilService(MiembroRepository miembros, ComprobanteRepository comprobantes, CertificadoRepository certificados) {
        this.miembros = miembros;
        this.comprobantes = comprobantes;
        this.certificados = certificados;
    }

    @Transactional(readOnly = true)
    public Perfil de(Long usuarioId, Long empresaId) {
        Miembro m = miembros.findByUsuarioIdAndEmpresaId(usuarioId, empresaId).orElseThrow();
        Usuario u = m.getUsuario();
        Empresa e = m.getEmpresa();
        Plan p = e.getPlan();
        LocalDate hoy = LocalDate.now();
        long usados = comprobantes.countByEmpresaIdAndFechaEmisionBetween(e.getId(), hoy.withDayOfMonth(1), hoy.withDayOfMonth(hoy.lengthOfMonth()));
        boolean propio = certificados.findFirstByEmpresaIdAndActivoTrueOrderByIdDesc(e.getId()).map(c -> !c.isAutogenerado()).orElse(false);
        var plan = new PlanVista(p.getCodigo(), p.getNombre(), p.getLimiteComprobantes(), p.getMaxUsuarios(), p.getMaxEstablecimientos(),
                p.isNotasCredito(), p.isReportesAvanzados());
        var empresa = new EmpresaVista(e.getId(), e.getRuc(), e.getRazonSocial(), e.getNombreComercial(), e.getDireccionFiscal(),
                e.getAmbienteSunat().name(), e.getEstado().name(), e.getPruebaHasta(), propio,
                e.solConfigurado(), plan, usados);
        return new Perfil(u.getId(), u.getNombre(), u.getEmail(), m.getRol(),
                m.getEstablecimiento() != null ? m.getEstablecimiento().getId() : null,
                m.getEstablecimiento() != null ? m.getEstablecimiento().getNombre() : null, empresa,
                miembros.activosDe(usuarioId).size());
    }
}
