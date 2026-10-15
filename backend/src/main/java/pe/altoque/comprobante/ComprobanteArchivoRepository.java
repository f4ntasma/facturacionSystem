package pe.altoque.comprobante;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ComprobanteArchivoRepository extends JpaRepository<ComprobanteArchivo, Long> {

    Optional<ComprobanteArchivo> findByComprobanteIdAndTipo(Long comprobanteId, ComprobanteArchivo.Tipo tipo);
}
