package pe.altoque.sunat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EnvioSunatRepository extends JpaRepository<EnvioSunat, Long> {

    List<EnvioSunat> findByComprobanteIdOrderByIdDesc(Long comprobanteId);

    /** Última respuesta de SUNAT de un comprobante (código y mensaje que ve el usuario). */
    Optional<EnvioSunat> findFirstByComprobanteIdOrderByIdDesc(Long comprobanteId);

    Optional<EnvioSunat> findFirstByLoteIdOrderByIdDesc(Long loteId);

    @Query("select count(e) from EnvioSunat e where e.comprobanteId = :comprobanteId and e.operacion = 'sendBill'")
    long intentos(Long comprobanteId);
}
