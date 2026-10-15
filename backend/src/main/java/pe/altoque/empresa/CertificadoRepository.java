package pe.altoque.empresa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CertificadoRepository extends JpaRepository<CertificadoDigital, Long> {

    Optional<CertificadoDigital> findFirstByEmpresaIdAndActivoTrueOrderByIdDesc(Long empresaId);

    @Modifying
    @Query("update CertificadoDigital c set c.activo = false where c.empresa.id = :empresaId")
    void desactivarTodos(Long empresaId);
}
