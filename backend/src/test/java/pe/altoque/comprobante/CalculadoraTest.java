package pe.altoque.comprobante;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraTest {

    private static final BigDecimal IGV = new BigDecimal("18.00");

    private static ComprobanteDetalle linea(String cantidad, String precio, String afectacion, boolean icbper) {
        ComprobanteDetalle d = new ComprobanteDetalle();
        d.setCantidad(new BigDecimal(cantidad));
        d.setPrecioUnitario(new BigDecimal(precio));
        d.setTipoAfectacion(afectacion);
        Calculadora.calcularLinea(d, IGV, icbper);
        return d;
    }

    @Test
    void boletaDeLaBodegaCuadraAlCentimo() {
        // La boleta del diseño: gaseosa 5.50 + 10 panes a 0.40 + leche 3.00 = 12.50
        Comprobante c = new Comprobante();
        c.agregarDetalle(linea("1", "5.50", "10", false));
        c.agregarDetalle(linea("10", "0.40", "10", false));
        c.agregarDetalle(linea("1", "3.00", "10", false));
        Calculadora.totalizar(c);
        assertThat(c.getTotal()).isEqualByComparingTo("12.50");
        assertThat(c.getTotalGravado()).isEqualByComparingTo("10.59");
        assertThat(c.getTotalIgv()).isEqualByComparingTo("1.91");
        assertThat(c.getTotalGravado().add(c.getTotalIgv())).isEqualByComparingTo(c.getTotal());
    }

    @Test
    void lineaGravada() {
        ComprobanteDetalle d = linea("2", "59.00", "10", false);
        assertThat(d.getValorVenta()).isEqualByComparingTo("100.00");
        assertThat(d.getIgv()).isEqualByComparingTo("18.00");
        assertThat(d.getTotal()).isEqualByComparingTo("118.00");
        assertThat(d.getValorUnitario()).isEqualByComparingTo("50.0000000000");
    }

    @Test
    void exoneradoNoLlevaIgv() {
        ComprobanteDetalle d = linea("1.5", "4.20", "20", false);
        assertThat(d.getValorVenta()).isEqualByComparingTo("6.30");
        assertThat(d.getIgv()).isEqualByComparingTo("0");
        assertThat(d.getTotal()).isEqualByComparingTo("6.30");
    }

    @Test
    void bolsaPlasticaSumaIcbper() {
        ComprobanteDetalle d = linea("3", "0.10", "10", true);
        assertThat(d.getIcbper()).isEqualByComparingTo("1.50");
        assertThat(d.getTotal()).isEqualByComparingTo("1.80");
        Comprobante c = new Comprobante();
        c.agregarDetalle(d);
        Calculadora.totalizar(c);
        assertThat(c.getTotalIcbper()).isEqualByComparingTo("1.50");
        assertThat(c.getTotal()).isEqualByComparingTo("1.80");
    }

    @Test
    void mezclaGravadoExonerado() {
        Comprobante c = new Comprobante();
        c.agregarDetalle(linea("1", "9.90", "10", false));
        c.agregarDetalle(linea("2", "4.20", "20", false));
        c.agregarDetalle(linea("1", "5.00", "30", false));
        Calculadora.totalizar(c);
        assertThat(c.getTotalExonerado()).isEqualByComparingTo("8.40");
        assertThat(c.getTotalInafecto()).isEqualByComparingTo("5.00");
        assertThat(c.getTotal()).isEqualByComparingTo("23.30");
    }
}
