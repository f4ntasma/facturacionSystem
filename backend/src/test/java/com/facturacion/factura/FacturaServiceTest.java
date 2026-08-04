package com.facturacion.factura;

import com.facturacion.common.MetodoPago;
import com.facturacion.common.TipoComprobante;
import com.facturacion.empresa.Empresa;
import com.facturacion.orden.OrdenRepository;
import com.facturacion.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Tests del calculo de IGV y totales segun Catalogo 07 SUNAT:
// 10-16 = Gravado (IGV 18%), 20 = Exonerado, 30-36 = Inafecto, 40 = Exportacion
class FacturaServiceTest {

    private FacturaRepository facturaRepository;
    private OrdenRepository ordenRepository;
    private FacturaService facturaService;
    private User user;

    @BeforeEach
    void setUp() {
        facturaRepository = mock(FacturaRepository.class);
        ordenRepository = mock(OrdenRepository.class);
        // save() devuelve la misma entidad que recibe
        when(facturaRepository.save(any(Factura.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        // Sin facturas previas: el primer correlativo debe ser 00000001
        when(facturaRepository.findMaxCorrelativo(any(), any()))
                .thenReturn(Optional.empty());
        facturaService = new FacturaService(facturaRepository, ordenRepository);

        Empresa empresa = new Empresa();
        user = new User();
        user.setEmpresa(empresa);
    }

    private FacturaRequest baseRequest(FacturaRequest.ItemRequest... items) {
        FacturaRequest r = new FacturaRequest();
        r.setSerie("F001");
        r.setTipoComprobante(TipoComprobante.FACTURA);
        r.setTipoDocIdentidad("6");
        r.setNroDocIdentidad("20123456789");
        r.setClienteNombre("EMPRESA DEMO S.A.C.");
        r.setMetodoPago(MetodoPago.CONTADO);
        r.setFechaEmision("2026-07-20");
        r.setItems(List.of(items));
        return r;
    }

    private FacturaRequest.ItemRequest item(int tipAfe, String precio, int cantidad) {
        FacturaRequest.ItemRequest i = new FacturaRequest.ItemRequest();
        i.setDescripcion("Producto de prueba");
        i.setCantidad(cantidad);
        i.setMtoValorUnitario(new BigDecimal(precio));
        i.setTipAfeIgv(tipAfe);
        return i;
    }

    @Test
    void itemGravado10_calculaIgv18PorCiento() {
        FacturaResponse res = facturaService.crear(user, baseRequest(item(10, "100.00", 2)));

        assertThat(res.getMtoOperGravadas()).isEqualByComparingTo("200.00");
        assertThat(res.getIgv()).isEqualByComparingTo("36.00");
        assertThat(res.getTotal()).isEqualByComparingTo("236.00");
    }

    @Test
    void itemGravado16_tambienCalculaIgv() {
        // Todo el rango 10-16 es gravado, no solo el 10
        FacturaResponse res = facturaService.crear(user, baseRequest(item(16, "50.00", 1)));

        assertThat(res.getMtoOperGravadas()).isEqualByComparingTo("50.00");
        assertThat(res.getIgv()).isEqualByComparingTo("9.00");
        assertThat(res.getTotal()).isEqualByComparingTo("59.00");
    }

    @Test
    void itemExonerado20_sinIgv() {
        FacturaResponse res = facturaService.crear(user, baseRequest(item(20, "100.00", 1)));

        assertThat(res.getMtoOperExoneradas()).isEqualByComparingTo("100.00");
        assertThat(res.getIgv()).isEqualByComparingTo("0.00");
        assertThat(res.getTotal()).isEqualByComparingTo("100.00");
    }

    @Test
    void itemInafecto30_sinIgv() {
        FacturaResponse res = facturaService.crear(user, baseRequest(item(30, "80.00", 1)));

        assertThat(res.getMtoOperInafectas()).isEqualByComparingTo("80.00");
        assertThat(res.getIgv()).isEqualByComparingTo("0.00");
        assertThat(res.getTotal()).isEqualByComparingTo("80.00");
    }

    @Test
    void itemExportacion40_sinIgv() {
        FacturaResponse res = facturaService.crear(user, baseRequest(item(40, "500.00", 1)));

        assertThat(res.getMtoOperInafectas()).isEqualByComparingTo("500.00");
        assertThat(res.getIgv()).isEqualByComparingTo("0.00");
        assertThat(res.getTotal()).isEqualByComparingTo("500.00");
    }

    @Test
    void mezclaDeAfectaciones_sumaCorrecta() {
        FacturaResponse res = facturaService.crear(user, baseRequest(
                item(10, "100.00", 1),   // gravado: 100 + 18 IGV
                item(20, "50.00", 1),    // exonerado: 50
                item(30, "25.00", 2)     // inafecto: 50
        ));

        assertThat(res.getMtoOperGravadas()).isEqualByComparingTo("100.00");
        assertThat(res.getMtoOperExoneradas()).isEqualByComparingTo("50.00");
        assertThat(res.getMtoOperInafectas()).isEqualByComparingTo("50.00");
        assertThat(res.getIgv()).isEqualByComparingTo("18.00");
        assertThat(res.getSubtotal()).isEqualByComparingTo("200.00");
        assertThat(res.getTotal()).isEqualByComparingTo("218.00");
    }

    @Test
    void primerCorrelativo_es00000001() {
        FacturaResponse res = facturaService.crear(user, baseRequest(item(10, "10.00", 1)));

        assertThat(res.getCorrelativo()).isEqualTo("00000001");
    }

    @Test
    void correlativoSiguiente_esUltimoMasUno() {
        when(facturaRepository.findMaxCorrelativo(any(), any()))
                .thenReturn(Optional.of("00000041"));

        FacturaResponse res = facturaService.crear(user, baseRequest(item(10, "10.00", 1)));

        assertThat(res.getCorrelativo()).isEqualTo("00000042");
    }

    @Test
    void clienteConRuc_seGuardaEnCampoRuc() {
        FacturaResponse res = facturaService.crear(user, baseRequest(item(10, "10.00", 1)));

        assertThat(res.getClienteRuc()).isEqualTo("20123456789");
        assertThat(res.getClienteDni()).isNull();
    }
}
