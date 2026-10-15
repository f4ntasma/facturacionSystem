package pe.altoque.common;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogosYLetrasTest {

    @Test
    void montoEnLetras() {
        assertThat(NumeroALetras.convertir(new BigDecimal("12.50"), "PEN")).isEqualTo("DOCE CON 50/100 SOLES");
        assertThat(NumeroALetras.convertir(new BigDecimal("0.40"), "PEN")).isEqualTo("CERO CON 40/100 SOLES");
        assertThat(NumeroALetras.convertir(new BigDecimal("100"), "PEN")).isEqualTo("CIEN CON 00/100 SOLES");
        assertThat(NumeroALetras.convertir(new BigDecimal("118.00"), "PEN")).isEqualTo("CIENTO DIECIOCHO CON 00/100 SOLES");
        assertThat(NumeroALetras.convertir(new BigDecimal("1248"), "PEN")).isEqualTo("MIL DOSCIENTOS CUARENTA Y OCHO CON 00/100 SOLES");
        assertThat(NumeroALetras.convertir(new BigDecimal("21000"), "PEN")).isEqualTo("VEINTIUN MIL CON 00/100 SOLES");
        assertThat(NumeroALetras.convertir(new BigDecimal("31500.05"), "PEN")).isEqualTo("TREINTA Y UN MIL QUINIENTOS CON 05/100 SOLES");
        assertThat(NumeroALetras.convertir(new BigDecimal("1000000"), "USD")).isEqualTo("UN MILLON CON 00/100 DÓLARES AMERICANOS");
        assertThat(NumeroALetras.convertir(new BigDecimal("2501001.99"), "PEN"))
                .isEqualTo("DOS MILLONES QUINIENTOS UN MIL UNO CON 99/100 SOLES");
    }

    @Test
    void validaRuc() {
        assertThat(Catalogos.rucValido("20000000001")).isTrue();
        assertThat(Catalogos.rucValido("20601234565")).isTrue();
        assertThat(Catalogos.rucValido("20601234566")).isFalse();
        assertThat(Catalogos.rucValido("30601234565")).isFalse();
        assertThat(Catalogos.rucValido("2060123456")).isFalse();
    }

    @Test
    void validaDocumentos() {
        assertThat(Catalogos.documentoValido("1", "45678912")).isTrue();
        assertThat(Catalogos.documentoValido("1", "4567891")).isFalse();
        assertThat(Catalogos.documentoValido("4", "CE001234")).isTrue();
        assertThat(Catalogos.documentoValido("9", "123")).isFalse();
    }
}
