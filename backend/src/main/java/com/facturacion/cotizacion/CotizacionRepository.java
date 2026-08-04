package com.facturacion.cotizacion;

import com.facturacion.empresa.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CotizacionRepository extends JpaRepository<Cotizacion, UUID> {
    List<Cotizacion> findByEmpresaOrderByCreatedAtDesc(Empresa empresa);
}