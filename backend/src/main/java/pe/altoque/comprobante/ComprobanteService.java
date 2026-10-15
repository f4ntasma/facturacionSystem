package pe.altoque.comprobante;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.altoque.catalogo.Cliente;
import pe.altoque.catalogo.ClienteRepository;
import pe.altoque.catalogo.Producto;
import pe.altoque.catalogo.ProductoRepository;
import pe.altoque.common.ApiException;
import pe.altoque.common.Catalogos;
import pe.altoque.common.Montos;
import pe.altoque.common.NumeroALetras;
import pe.altoque.common.Tokens;
import pe.altoque.comprobante.Comprobante.EstadoPago;
import pe.altoque.comprobante.Comprobante.EstadoSunat;
import pe.altoque.comprobante.Comprobante.FormaPago;
import pe.altoque.comprobante.ComprobanteDtos.*;
import pe.altoque.empresa.*;
import pe.altoque.seguridad.Sesion;
import pe.altoque.sunat.SunatService;
import pe.altoque.usuario.Usuario;
import pe.altoque.usuario.UsuarioRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class ComprobanteService {

    /** A partir de este monto la boleta exige documento del cliente. */
    public static final BigDecimal LIMITE_BOLETA_SIN_DOC = new BigDecimal("700.00");
    /** SUNAT permite anular (baja / resumen) hasta 7 días calendario después de la emisión. */
    public static final int DIAS_PARA_ANULAR = 7;

    private final ComprobanteRepository comprobantes;
    private final SerieRepository series;
    private final EmpresaRepository empresas;
    private final UsuarioRepository usuarios;
    private final ClienteRepository clientes;
    private final ProductoRepository productos;
    private final CajaRepository cajas;
    private final SunatService sunat;
    private final ComprobanteVistas vistas;

    public ComprobanteService(ComprobanteRepository comprobantes, SerieRepository series, EmpresaRepository empresas,
                              UsuarioRepository usuarios, ClienteRepository clientes, ProductoRepository productos,
                              CajaRepository cajas, SunatService sunat, ComprobanteVistas vistas) {
        this.comprobantes = comprobantes;
        this.series = series;
        this.empresas = empresas;
        this.usuarios = usuarios;
        this.clientes = clientes;
        this.productos = productos;
        this.cajas = cajas;
        this.sunat = sunat;
        this.vistas = vistas;
    }

    // ------------------------------------------------------------------ emitir

    @Transactional
    public Comprobante emitir(Sesion s, EmitirRequest req) {
        if (req.tipo() != TipoComprobante.FACTURA && req.tipo() != TipoComprobante.BOLETA) {
            throw ApiException.invalido("Desde aquí solo se emiten boletas y facturas. Las notas se emiten desde el comprobante.");
        }
        Empresa empresa = empresaHabilitada(s.empresaId());
        Usuario usuario = usuarios.getReferenceById(s.usuarioId());
        Serie serie = elegirSerie(s, req.tipo(), req.serieId(), req.tipo() == TipoComprobante.FACTURA ? "F" : "B");

        Comprobante c = base(empresa, usuario, serie);
        c.setTipo(req.tipo());
        c.setObservaciones(limpiar(req.observaciones()));
        c.setFormaPago(req.formaPago() == null ? FormaPago.CONTADO : req.formaPago());
        asignarCliente(c, s.empresaId(), req.cliente());

        Map<Long, Producto> porId = cargarProductos(s.empresaId(), req.items());
        int n = 1;
        for (ItemIn it : req.items()) {
            Producto p = it.productoId() != null ? porId.get(it.productoId()) : null;
            c.agregarDetalle(linea(n++, it, p, empresa.getIgvPorcentaje()));
        }
        Calculadora.totalizar(c);
        validarCliente(c);
        moverStock(c, -1);

        if (c.getTipo() == TipoComprobante.FACTURA && c.getFormaPago() == FormaPago.CREDITO) {
            prepararCredito(c, req);
        } else {
            c.setFechaVencimiento(req.fechaVencimiento());
        }
        registrarPagosIniciales(c, usuario, req.pagos());
        cerrarNumeracion(c);
        comprobantes.save(c);
        sunat.generarXml(c);
        return c;
    }

    private void prepararCredito(Comprobante c, EmitirRequest req) {
        List<CuotaIn> cuotas = req.cuotas() == null ? List.of() : req.cuotas();
        if (cuotas.isEmpty()) {
            LocalDate vence = req.fechaVencimiento() != null ? req.fechaVencimiento() : c.getFechaEmision().plusDays(30);
            cuotas = List.of(new CuotaIn(c.getTotal(), vence));
        }
        BigDecimal pagadoAlEmitir = req.pagos() == null ? BigDecimal.ZERO
                : req.pagos().stream().map(PagoIn::monto).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal suma = cuotas.stream().map(CuotaIn::monto).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (Montos.r2(suma.add(pagadoAlEmitir)).compareTo(c.getTotal()) != 0) {
            throw ApiException.invalido("Las cuotas (S/ %s) más lo pagado hoy deben sumar el total (S/ %s)."
                    .formatted(Montos.txt(suma), Montos.txt(c.getTotal())));
        }
        int i = 1;
        LocalDate ultima = null;
        for (CuotaIn q : cuotas) {
            if (!q.fechaVencimiento().isAfter(c.getFechaEmision())) {
                throw ApiException.invalido("Las cuotas deben vencer después de hoy.");
            }
            ComprobanteCuota cuota = new ComprobanteCuota();
            cuota.setNumero(i++);
            cuota.setMonto(Montos.r2(q.monto()));
            cuota.setFechaVencimiento(q.fechaVencimiento());
            c.agregarCuota(cuota);
            ultima = ultima == null || q.fechaVencimiento().isAfter(ultima) ? q.fechaVencimiento() : ultima;
        }
        c.setFechaVencimiento(ultima);
    }

    // ------------------------------------------------------------------ notas

    @Transactional
    public Comprobante emitirNota(Sesion s, Long refId, NotaRequest req) {
        if (!req.tipo().esNota()) throw ApiException.invalido("Tipo de nota inválido.");
        Empresa empresa = empresaHabilitada(s.empresaId());
        if (req.tipo() == TipoComprobante.NOTA_CREDITO && !empresa.getPlan().isNotasCredito()) {
            throw ApiException.prohibido("Tu plan no incluye notas de crédito.");
        }
        Comprobante ref = buscar(s, refId);
        if (ref.getTipo().esNota()) throw ApiException.invalido("Una nota se emite sobre una boleta o factura, no sobre otra nota.");
        if (ref.getEstadoSunat() != EstadoSunat.ACEPTADO && ref.getEstadoSunat() != EstadoSunat.OBSERVADO) {
            throw ApiException.invalido("Solo puedes emitir notas sobre comprobantes aceptados por SUNAT.");
        }
        Map<String, String> motivos = req.tipo() == TipoComprobante.NOTA_CREDITO ? Catalogos.MOTIVOS_NC : Catalogos.MOTIVOS_ND;
        if (!motivos.containsKey(req.motivoCodigo())) throw ApiException.invalido("Motivo inválido para esta nota.");

        Usuario usuario = usuarios.getReferenceById(s.usuarioId());
        String prefijo = ref.getSerie().substring(0, 1);
        Serie serie = elegirSerie(s, req.tipo(), null, prefijo);

        Comprobante c = base(empresa, usuario, serie);
        c.setTipo(req.tipo());
        c.setComprobanteRef(ref);
        c.setRefTipo(ref.getTipo());
        c.setRefSerie(ref.getSerie());
        c.setRefCorrelativo(ref.getCorrelativo());
        c.setMotivoCodigo(req.motivoCodigo());
        c.setMotivoDescripcion(req.motivoDescripcion().trim());
        c.setCliente(ref.getCliente());
        c.setClienteTipoDoc(ref.getClienteTipoDoc());
        c.setClienteNumDoc(ref.getClienteNumDoc());
        c.setClienteNombre(ref.getClienteNombre());
        c.setClienteDireccion(ref.getClienteDireccion());
        c.setClienteEmail(ref.getClienteEmail());
        c.setClienteTelefono(ref.getClienteTelefono());
        c.setIgvPorcentaje(ref.getIgvPorcentaje());

        boolean total = req.tipo() == TipoComprobante.NOTA_CREDITO && Catalogos.motivoTotal(req.motivoCodigo());
        if (total) {
            for (ComprobanteDetalle d : ref.getDetalles()) c.agregarDetalle(copia(d));
        } else {
            if (req.items() == null || req.items().isEmpty()) throw ApiException.invalido("Indica los ítems y montos de la nota.");
            Map<Long, Producto> porId = cargarProductos(s.empresaId(), req.items());
            int n = 1;
            for (ItemIn it : req.items()) {
                Producto p = it.productoId() != null ? porId.get(it.productoId()) : null;
                c.agregarDetalle(linea(n++, it, p, c.getIgvPorcentaje()));
            }
        }
        Calculadora.totalizar(c);

        if (req.tipo() == TipoComprobante.NOTA_CREDITO) {
            BigDecimal yaAcreditado = comprobantes.findByComprobanteRefIdOrderById(ref.getId()).stream()
                    .filter(n -> n.getTipo() == TipoComprobante.NOTA_CREDITO && n.getEstadoSunat() != EstadoSunat.RECHAZADO
                            && n.getEstadoSunat() != EstadoSunat.ANULADO)
                    .map(Comprobante::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (c.getTotal().add(yaAcreditado).compareTo(ref.getTotal()) > 0) {
                throw ApiException.invalido("La nota supera lo que queda por acreditar de %s (S/ %s)."
                        .formatted(ref.numeroImpreso(), Montos.txt(ref.getTotal().subtract(yaAcreditado))));
            }
            if ("01".equals(req.motivoCodigo()) || "06".equals(req.motivoCodigo()) || "07".equals(req.motivoCodigo())) {
                moverStock(c, +1);
            }
            c.setEstadoPago(EstadoPago.PAGADO);
            c.setMontoPagado(c.getTotal());
        } else {
            c.setEstadoPago(EstadoPago.POR_COBRAR);
        }
        cerrarNumeracion(c);
        comprobantes.save(c);
        sunat.generarXml(c);
        return c;
    }

    // ------------------------------------------------------------------ cobros, envío y anulación

    @Transactional
    public Comprobante registrarPago(Sesion s, Long id, PagoIn in) {
        Comprobante c = buscar(s, id);
        if (c.getTipo() == TipoComprobante.NOTA_CREDITO) throw ApiException.invalido("Las notas de crédito no se cobran.");
        if (c.getEstadoSunat() == EstadoSunat.ANULADO || c.getEstadoSunat() == EstadoSunat.RECHAZADO) {
            throw ApiException.invalido("Este comprobante no es válido; no se le pueden registrar cobros.");
        }
        BigDecimal saldo = c.getTotal().subtract(c.getMontoPagado());
        if (saldo.signum() <= 0) throw ApiException.invalido("Este comprobante ya está pagado completo.");
        if (in.monto().compareTo(saldo) > 0) {
            throw ApiException.invalido("El cobro supera el saldo pendiente (S/ %s).".formatted(Montos.txt(saldo)));
        }
        Pago p = nuevoPago(c, usuarios.getReferenceById(s.usuarioId()), in);
        c.agregarPago(p);
        actualizarEstadoPago(c);
        return c;
    }

    @Transactional
    public Comprobante anular(Sesion s, Long id, String motivo) {
        if (!s.esAdmin()) throw ApiException.prohibido("Solo el dueño o un administrador puede anular comprobantes.");
        Comprobante c = buscar(s, id);
        switch (c.getEstadoSunat()) {
            case ANULADO, POR_ANULAR -> throw ApiException.conflicto("Este comprobante ya está anulado o en proceso de anulación.");
            case PENDIENTE -> throw ApiException.invalido("Aún no llega a SUNAT. Reintenta el envío y luego anúlalo.");
            case RECHAZADO -> {
                // Nunca fue válido para SUNAT: solo se marca y se repone el stock.
                c.setEstadoSunat(EstadoSunat.ANULADO);
                c.setAnulacionMotivo(motivo);
                c.setAnuladoAt(Instant.now());
                if (!c.getTipo().esNota()) moverStock(c, +1);
                return c;
            }
            default -> {
            }
        }
        long dias = ChronoUnit.DAYS.between(c.getFechaEmision(), LocalDate.now());
        if (dias > DIAS_PARA_ANULAR) {
            throw ApiException.invalido("Pasaron más de 7 días desde la emisión. Para revertirlo emite una nota de crédito.");
        }
        boolean tieneNotas = comprobantes.findByComprobanteRefIdOrderById(c.getId()).stream()
                .anyMatch(n -> n.getEstadoSunat() != EstadoSunat.ANULADO && n.getEstadoSunat() != EstadoSunat.RECHAZADO);
        if (tieneNotas) throw ApiException.invalido("Este comprobante tiene notas vigentes. Anula primero las notas.");
        if (!c.getTipo().esNota()) moverStock(c, +1);
        sunat.anular(c, motivo.trim());
        return c;
    }

    @Transactional
    public void marcarMensaje(Long id, String mensaje) {
        comprobantes.findById(id).ifPresent(c -> {
            if (c.getEstadoSunat() == EstadoSunat.PENDIENTE) c.setSunatMensaje(mensaje);
        });
    }

    // ------------------------------------------------------------------ consultas

    @Transactional(readOnly = true)
    public Comprobante buscar(Sesion s, Long id) {
        return comprobantes.findByIdAndEmpresaId(id, s.empresaId()).orElseThrow(() -> ApiException.noEncontrado("Comprobante"));
    }

    @Transactional(readOnly = true)
    public Vista vista(Sesion s, Long id) {
        return vistas.de(buscar(s, id));
    }

    @Transactional(readOnly = true)
    public Page<Fila> listar(Sesion s, String tipo, EstadoSunat estado, EstadoPago estadoPago, LocalDate desde, LocalDate hasta,
                             String q, int pagina, int tamano) {
        Specification<Comprobante> spec = filtros(s.empresaId(), tipo, estado, estadoPago, desde, hasta, q);
        var orden = Sort.by(Sort.Direction.DESC, "fechaEmision", "horaEmision", "id");
        return comprobantes.findAll(spec, PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamano, 1), 100), orden)).map(Fila::de);
    }

    @Transactional(readOnly = true)
    public List<Comprobante> paraExportar(Sesion s, LocalDate desde, LocalDate hasta) {
        var orden = Sort.by("fechaEmision", "tipo", "serie", "correlativo");
        return comprobantes.findAll(filtros(s.empresaId(), null, null, null, desde, hasta, null), orden);
    }

    static Specification<Comprobante> filtros(Long empresaId, String tipo, EstadoSunat estado, EstadoPago estadoPago,
                                              LocalDate desde, LocalDate hasta, String q) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> ps = new ArrayList<>();
            ps.add(cb.equal(root.get("empresa").get("id"), empresaId));
            if (tipo != null && !tipo.isBlank()) {
                List<TipoComprobante> tipos = Arrays.stream(tipo.split(",")).map(String::trim).map(TipoComprobante::deCodigo).toList();
                ps.add(root.get("tipo").in(tipos));
            }
            if (estado != null) ps.add(cb.equal(root.get("estadoSunat"), estado));
            if (estadoPago != null) ps.add(cb.equal(root.get("estadoPago"), estadoPago));
            if (desde != null) ps.add(cb.greaterThanOrEqualTo(root.get("fechaEmision"), desde));
            if (hasta != null) ps.add(cb.lessThanOrEqualTo(root.get("fechaEmision"), hasta));
            if (q != null && !q.isBlank()) {
                String t = q.trim();
                var numero = t.toUpperCase().matches("[A-Z0-9]{4}-\\d+");
                if (numero) {
                    String[] partes = t.toUpperCase().split("-");
                    ps.add(cb.equal(root.get("serie"), partes[0]));
                    ps.add(cb.equal(root.get("correlativo"), Integer.parseInt(partes[1])));
                } else {
                    String like = "%" + t.toLowerCase() + "%";
                    ps.add(cb.or(cb.like(cb.lower(root.get("clienteNombre")), like), cb.like(root.get("clienteNumDoc"), t + "%")));
                }
            }
            return cb.and(ps.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }

    // ------------------------------------------------------------------ apoyo

    private Empresa empresaHabilitada(Long empresaId) {
        Empresa e = empresas.findWithPlanById(empresaId).orElseThrow(() -> ApiException.noEncontrado("Empresa"));
        if (e.getEstado() == Empresa.Estado.SUSPENDIDA) {
            throw new ApiException(HttpStatus.PAYMENT_REQUIRED, "Tu cuenta está suspendida. Renueva tu plan para seguir emitiendo.");
        }
        if (e.getEstado() == Empresa.Estado.PRUEBA && e.getPruebaHasta() != null && LocalDate.now().isAfter(e.getPruebaHasta())) {
            throw new ApiException(HttpStatus.PAYMENT_REQUIRED, "Se acabó tu prueba gratis, causa. Elige un plan para seguir emitiendo.");
        }
        Integer limite = e.getPlan().getLimiteComprobantes();
        if (limite != null) {
            LocalDate hoy = LocalDate.now();
            long usados = comprobantes.countByEmpresaIdAndFechaEmisionBetween(e.getId(), hoy.withDayOfMonth(1), hoy.withDayOfMonth(hoy.lengthOfMonth()));
            if (usados >= limite) {
                throw new ApiException(HttpStatus.PAYMENT_REQUIRED,
                        "Llegaste a los %d comprobantes de tu plan este mes. Sube de plan para seguir emitiendo.".formatted(limite));
            }
        }
        return e;
    }

    private Serie elegirSerie(Sesion s, TipoComprobante tipo, Long serieId, String prefijo) {
        Serie serie;
        if (serieId != null) {
            serie = series.findByIdAndEmpresaId(serieId, s.empresaId()).orElseThrow(() -> ApiException.noEncontrado("Serie"));
            if (serie.getTipoComprobante() != tipo || !serie.isActivo() || !serie.getSerie().startsWith(prefijo)) {
                throw ApiException.invalido("La serie elegida no corresponde a este comprobante.");
            }
        } else {
            Long local = s.establecimientoId();
            List<Serie> candidatas = local == null ? List.of() : series.activas(s.empresaId(), local, tipo, prefijo);
            if (candidatas.isEmpty()) {
                candidatas = series.listar(s.empresaId()).stream()
                        .filter(x -> x.getTipoComprobante() == tipo && x.isActivo() && x.getSerie().startsWith(prefijo)).toList();
            }
            if (candidatas.isEmpty()) {
                throw ApiException.invalido("No tienes una serie activa para %s. Créala en Ajustes › Series.".formatted(tipo.nombre().toLowerCase()));
            }
            serie = candidatas.getFirst();
        }
        return series.bloquear(serie.getId()).orElseThrow();
    }

    private Comprobante base(Empresa empresa, Usuario usuario, Serie serie) {
        Comprobante c = new Comprobante();
        c.setEmpresa(empresa);
        c.setUsuario(usuario);
        c.setSerieRef(serie);
        c.setEstablecimiento(serie.getEstablecimiento());
        c.setSerie(serie.getSerie());
        c.setFechaEmision(LocalDate.now());
        c.setHoraEmision(LocalTime.now().withNano(0));
        c.setIgvPorcentaje(empresa.getIgvPorcentaje());
        c.setEstadoSunat(EstadoSunat.PENDIENTE);
        c.setTokenPublico(Tokens.nuevo(20));
        cajas.findFirstByEmpresaIdAndEstablecimientoIdAndEstadoOrderByIdDesc(empresa.getId(), serie.getEstablecimiento().getId(), Caja.Estado.ABIERTA)
                .ifPresent(c::setCaja);
        return c;
    }

    /** Toma el correlativo al final, con la serie ya bloqueada, para no dejar huecos si algo falla antes. */
    private void cerrarNumeracion(Comprobante c) {
        c.setCorrelativo(c.getSerieRef().siguiente());
        c.setTotalLetras(NumeroALetras.convertir(c.getTotal(), c.getMoneda()));
        c.setNombreArchivo("%s-%s-%s-%d".formatted(c.getEmpresa().getRuc(), c.getTipo().codigo(), c.getSerie(), c.getCorrelativo()));
    }

    private void asignarCliente(Comprobante c, Long empresaId, ClienteIn in) {
        if (in == null || ((in.numeroDocumento() == null || in.numeroDocumento().isBlank()) && in.id() == null)) {
            c.setClienteTipoDoc("0");
            c.setClienteNumDoc("-");
            c.setClienteNombre("CLIENTES VARIOS");
            return;
        }
        Cliente guardado = null;
        if (in.id() != null) {
            guardado = clientes.findByIdAndEmpresaId(in.id(), empresaId).orElseThrow(() -> ApiException.noEncontrado("Cliente"));
        }
        String tipo = in.tipoDocumento() != null ? in.tipoDocumento() : guardado != null ? guardado.getTipoDocumento() : "1";
        String numero = in.numeroDocumento() != null && !in.numeroDocumento().isBlank() ? in.numeroDocumento().trim()
                : guardado != null ? guardado.getNumeroDocumento() : null;
        String nombre = in.nombre() != null && !in.nombre().isBlank() ? in.nombre().trim() : guardado != null ? guardado.getNombre() : null;
        if (nombre == null) throw ApiException.invalido("Falta el nombre o razón social del cliente.");
        if (!Catalogos.documentoValido(tipo, numero)) {
            throw ApiException.invalido(switch (tipo) {
                case "6" -> "El RUC no es válido, pe. Revisa los 11 dígitos.";
                case "1" -> "El DNI debe tener 8 dígitos.";
                default -> "Revisa el número de documento del cliente.";
            });
        }
        if (guardado == null) {
            guardado = clientes.findByEmpresaIdAndTipoDocumentoAndNumeroDocumento(empresaId, tipo, numero).orElse(null);
        }
        String direccion = limpiar(in.direccion() != null ? in.direccion() : guardado != null ? guardado.getDireccion() : null);
        String email = limpiar(in.email() != null ? in.email() : guardado != null ? guardado.getEmail() : null);
        String telefono = limpiar(in.telefono() != null ? in.telefono() : guardado != null ? guardado.getTelefono() : null);
        if (guardado == null && in.guardar() && !"0".equals(tipo)) {
            guardado = new Cliente();
            guardado.setEmpresa(c.getEmpresa());
            guardado.setTipoDocumento(tipo);
            guardado.setNumeroDocumento(numero);
            guardado.setNombre(nombre);
            guardado.setDireccion(direccion);
            guardado.setEmail(email);
            guardado.setTelefono(telefono);
            guardado = clientes.save(guardado);
        }
        c.setCliente(guardado);
        c.setClienteTipoDoc(tipo);
        c.setClienteNumDoc(numero);
        c.setClienteNombre(nombre);
        c.setClienteDireccion(direccion);
        c.setClienteEmail(email);
        c.setClienteTelefono(telefono);
    }

    private void validarCliente(Comprobante c) {
        if (c.getTipo() == TipoComprobante.FACTURA) {
            if (!"6".equals(c.getClienteTipoDoc())) {
                throw ApiException.invalido("Falta el RUC, pe. La factura necesita el RUC del cliente; si no tiene, emite boleta.");
            }
        } else if (c.getTipo() == TipoComprobante.BOLETA && "0".equals(c.getClienteTipoDoc())
                && c.getTotal().compareTo(LIMITE_BOLETA_SIN_DOC) >= 0) {
            throw ApiException.invalido("Para boletas desde S/ 700 necesitas el DNI del cliente.");
        }
    }

    private Map<Long, Producto> cargarProductos(Long empresaId, List<ItemIn> items) {
        List<Long> ids = items.stream().map(ItemIn::productoId).filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) return Map.of();
        Map<Long, Producto> m = new HashMap<>();
        productos.findByEmpresaIdAndIdIn(empresaId, ids).forEach(p -> m.put(p.getId(), p));
        if (m.size() != ids.size()) throw ApiException.invalido("Uno de los productos ya no existe.");
        return m;
    }

    private ComprobanteDetalle linea(int n, ItemIn it, Producto p, BigDecimal igv) {
        ComprobanteDetalle d = new ComprobanteDetalle();
        d.setItem(n);
        d.setProducto(p);
        d.setCodigo(it.codigo() != null ? it.codigo() : p != null ? p.getCodigo() : null);
        d.setDescripcion(it.descripcion().trim());
        d.setUnidadMedida(it.unidadMedida() != null ? it.unidadMedida() : p != null ? p.getUnidadMedida() : "NIU");
        d.setCantidad(it.cantidad());
        d.setPrecioUnitario(it.precioUnitario());
        d.setTipoAfectacion(it.tipoAfectacion() != null ? it.tipoAfectacion() : p != null ? p.getTipoAfectacion() : Calculadora.GRAVADO);
        boolean icbper = it.icbper() != null ? it.icbper() : p != null && p.isAplicaIcbper();
        Calculadora.calcularLinea(d, igv, icbper);
        return d;
    }

    private ComprobanteDetalle copia(ComprobanteDetalle o) {
        ComprobanteDetalle d = new ComprobanteDetalle();
        d.setItem(o.getItem());
        d.setProducto(o.getProducto());
        d.setCodigo(o.getCodigo());
        d.setDescripcion(o.getDescripcion());
        d.setUnidadMedida(o.getUnidadMedida());
        d.setCantidad(o.getCantidad());
        d.setPrecioUnitario(o.getPrecioUnitario());
        d.setValorUnitario(o.getValorUnitario());
        d.setTipoAfectacion(o.getTipoAfectacion());
        d.setValorVenta(o.getValorVenta());
        d.setIgv(o.getIgv());
        d.setIcbper(o.getIcbper());
        d.setTotal(o.getTotal());
        return d;
    }

    /** signo -1 descuenta (venta), +1 repone (anulación o devolución). */
    private void moverStock(Comprobante c, int signo) {
        for (ComprobanteDetalle d : c.getDetalles()) {
            Producto p = d.getProducto();
            if (p == null || !p.isControlaStock()) continue;
            BigDecimal nuevo = p.getStock().add(d.getCantidad().multiply(BigDecimal.valueOf(signo)));
            if (signo < 0 && nuevo.signum() < 0) {
                throw ApiException.invalido("No alcanza el stock de «%s»: quedan %s.".formatted(p.getNombre(), p.getStock().stripTrailingZeros().toPlainString()));
            }
            p.setStock(nuevo);
        }
    }

    private void registrarPagosIniciales(Comprobante c, Usuario usuario, List<PagoIn> pagos) {
        boolean credito = c.getFormaPago() == FormaPago.CREDITO;
        List<PagoIn> lista = pagos == null ? List.of() : pagos;
        if (lista.isEmpty() && !credito) {
            lista = List.of(new PagoIn(Pago.Metodo.EFECTIVO, c.getTotal(), null));
        }
        BigDecimal suma = lista.stream().map(PagoIn::monto).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (!credito && Montos.r2(suma).compareTo(c.getTotal()) != 0) {
            throw ApiException.invalido("Los pagos (S/ %s) no cuadran con el total (S/ %s). Si queda saldo, emite al crédito."
                    .formatted(Montos.txt(suma), Montos.txt(c.getTotal())));
        }
        if (credito && suma.compareTo(c.getTotal()) >= 0) {
            throw ApiException.invalido("Si se paga todo hoy, emítela al contado.");
        }
        for (PagoIn in : lista) c.agregarPago(nuevoPago(c, usuario, in));
        actualizarEstadoPago(c);
    }

    private Pago nuevoPago(Comprobante c, Usuario usuario, PagoIn in) {
        Pago p = new Pago();
        p.setUsuario(usuario);
        p.setMetodo(in.metodo());
        p.setMonto(Montos.r2(in.monto()));
        p.setReferencia(limpiar(in.referencia()));
        p.setPagadoAt(Instant.now());
        p.setCaja(c.getCaja());
        return p;
    }

    private void actualizarEstadoPago(Comprobante c) {
        BigDecimal pagado = c.getPagos().stream().map(Pago::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
        c.setMontoPagado(Montos.r2(pagado));
        if (pagado.compareTo(c.getTotal()) >= 0) c.setEstadoPago(EstadoPago.PAGADO);
        else if (pagado.signum() > 0) c.setEstadoPago(EstadoPago.PARCIAL);
        else c.setEstadoPago(EstadoPago.POR_COBRAR);
    }

    private static String limpiar(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
