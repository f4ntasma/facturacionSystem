package pe.altoque.sunat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArchivoRepository extends JpaRepository<Archivo, Long> {

    Optional<Archivo> findByComprobanteIdAndTipo(Long comprobanteId, Archivo.Tipo tipo);

    Optional<Archivo> findByLoteIdAndTipo(Long loteId, Archivo.Tipo tipo);
}
