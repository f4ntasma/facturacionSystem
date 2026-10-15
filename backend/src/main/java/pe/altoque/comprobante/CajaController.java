package pe.altoque.comprobante;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pe.altoque.common.ApiException;
import pe.altoque.common.Montos;
import pe.altoque.empresa.EmpresaRepository;
import pe.altoque.empresa.EstablecimientoRepository;
import pe.altoque.seguridad.Sesion;
import pe.altoque.usuario.UsuarioRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Cuadre del día: abre la caja con sencillo, cobra, y al cerrar compara lo contado con lo esperado. */
@RestController
@RequestMapping("/api/caja")
public class CajaController {

    public record MetodoTotal(Pago.Metodo metodo, long cantidad, BigDecimal total) {}

    public record CajaVista(Long id, String estado, String usuario, String establecimiento, BigDecimal montoApertura,
                            BigDecimal efectivoCobrado, BigDecimal montoEsperado, BigDecimal montoDeclarado, BigDecimal diferencia,
                            BigDecimal totalCobrado, List<MetodoTotal> porMetodo, String notas, Instant abiertaAt, Instant cerradaAt) {}

    public record AbrirRequest(@NotNull @DecimalMin(value = "0", message = "El monto no puede ser negativo") BigDecimal montoApertura) {}

    public record CerrarRequest(@NotNull @DecimalMin(value = "0", message = "El monto no puede ser negativo") BigDecimal montoDeclarado,
                                @Size(max = 250) String notas) {}

    private final CajaRepository cajas;
    private final PagoRepository pagos;
    private final EmpresaRepository empresas;
    private final EstablecimientoRepository locales;
    private final UsuarioRepository usuarios;

    public CajaController(CajaRepository cajas, PagoRepository pagos, EmpresaRepository empresas, EstablecimientoRepository locales,
                          UsuarioRepository usuarios) {
        this.cajas = cajas;
        this.pagos = pagos;
        this.empresas = empresas;
        this.locales = locales;
        this.usuarios = usuarios;
    }

    @GetMapping("/actual")
    @Transactional(readOnly = true)
    public CajaVista actual() {
        Sesion s = Sesion.actual();
        return cajas.findFirstByEmpresaIdAndEstablecimientoIdAndEstadoOrderByIdDesc(s.empresaId(), local(s), Caja.Estado.ABIERTA)
                .map(this::vista).orElse(null);
    }

    @PostMapping("/abrir")
    @Transactional
    public CajaVista abrir(@Valid @RequestBody AbrirRequest r) {
        Sesion s = Sesion.actual();
        Long local = local(s);
        if (cajas.findFirstByEmpresaIdAndEstablecimientoIdAndEstadoOrderByIdDesc(s.empresaId(), local, Caja.Estado.ABIERTA).isPresent()) {
            throw ApiException.conflicto("Ya hay una caja abierta en este local.");
        }
        Caja c = new Caja();
        c.setEmpresa(empresas.getReferenceById(s.empresaId()));
        c.setEstablecimiento(locales.getReferenceById(local));
        c.setUsuario(usuarios.getReferenceById(s.usuarioId()));
        c.setMontoApertura(Montos.r2(r.montoApertura()));
        c.setAbiertaAt(Instant.now());
        return vista(cajas.save(c));
    }

    @PostMapping("/cerrar")
    @Transactional
    public CajaVista cerrar(@Valid @RequestBody CerrarRequest r) {
        Sesion s = Sesion.actual();
        Caja c = cajas.findFirstByEmpresaIdAndEstablecimientoIdAndEstadoOrderByIdDesc(s.empresaId(), local(s), Caja.Estado.ABIERTA)
                .orElseThrow(() -> ApiException.invalido("No hay caja abierta."));
        BigDecimal esperado = c.getMontoApertura().add(efectivo(c));
        c.setMontoEsperado(Montos.r2(esperado));
        c.setMontoDeclarado(Montos.r2(r.montoDeclarado()));
        c.setDiferencia(Montos.r2(r.montoDeclarado().subtract(esperado)));
        c.setNotas(r.notas());
        c.setEstado(Caja.Estado.CERRADA);
        c.setCerradaAt(Instant.now());
        return vista(c);
    }

    @GetMapping("/historial")
    @Transactional(readOnly = true)
    public List<CajaVista> historial() {
        return cajas.findByEmpresaIdOrderByIdDesc(Sesion.actual().empresaId(), PageRequest.of(0, 30)).stream().map(this::vista).toList();
    }

    private BigDecimal efectivo(Caja c) {
        return pagos.porMetodoEnCaja(c.getId()).stream().filter(m -> m.getMetodo() == Pago.Metodo.EFECTIVO)
                .map(PagoRepository.PorMetodo::getTotal).findFirst().orElse(BigDecimal.ZERO);
    }

    private CajaVista vista(Caja c) {
        List<MetodoTotal> metodos = c.getId() == null ? List.of() : pagos.porMetodoEnCaja(c.getId()).stream()
                .map(m -> new MetodoTotal(m.getMetodo(), m.getCantidad(), m.getTotal())).toList();
        BigDecimal efectivo = metodos.stream().filter(m -> m.metodo() == Pago.Metodo.EFECTIVO).map(MetodoTotal::total).findFirst().orElse(BigDecimal.ZERO);
        BigDecimal total = metodos.stream().map(MetodoTotal::total).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal esperado = c.getMontoEsperado() != null ? c.getMontoEsperado() : c.getMontoApertura().add(efectivo);
        return new CajaVista(c.getId(), c.getEstado().name(), c.getUsuario().getNombre(), c.getEstablecimiento().getNombre(),
                c.getMontoApertura(), efectivo, esperado, c.getMontoDeclarado(), c.getDiferencia(), total, metodos, c.getNotas(),
                c.getAbiertaAt(), c.getCerradaAt());
    }

    private Long local(Sesion s) {
        if (s.establecimientoId() != null) return s.establecimientoId();
        return locales.findByEmpresaIdAndCodigoSunat(s.empresaId(), "0000").orElseThrow().getId();
    }
}
