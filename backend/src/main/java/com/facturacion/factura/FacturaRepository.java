package com.facturacion.factura;

import com.facturacion.empresa.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FacturaRepository extends JpaRepository<Factura, UUID> {
    List<Factura> findByEmpresaOrderByCreatedAtDesc(Empresa empresa);

    // Ultimo correlativo emitido para una serie de la empresa.
    // El correlativo se guarda con ceros a la izquierda (00000001), por lo que
    // MAX() como texto funciona correcto mientras el largo sea uniforme.
    @Query("SELECT MAX(f.correlativo) FROM Factura f WHERE f.empresa.id = :empresaId AND f.serie = :serie")
    Optional<String> findMaxCorrelativo(@Param("empresaId") UUID empresaId, @Param("serie") String serie);
}