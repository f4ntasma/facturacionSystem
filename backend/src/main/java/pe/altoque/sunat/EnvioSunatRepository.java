package pe.altoque.sunat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnvioSunatRepository extends JpaRepository<EnvioSunat, Long> {

    List<EnvioSunat> findByComprobanteIdOrderByIdDesc(Long comprobanteId);
}
