package com.facturacion.factura;

import com.facturacion.empresa.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface FacturaRepository extends JpaRepository<Factura, UUID> {
    List<Factura> findByEmpresaOrderByCreatedAtDesc(Empresa empresa);
}