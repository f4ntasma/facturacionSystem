package com.facturacion.producto;

import com.facturacion.empresa.Empresa;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductoRepository extends JpaRepository<Producto, UUID> {
    List<Producto> findByEmpresa(Empresa empresa, Pageable pageable);
    List<Producto> findByEmpresaAndNombreContainingIgnoreCase(Empresa empresa, String nombre, Pageable pageable);
    List<Producto> findByActivoTrueAndNombreContainingIgnoreCase(String nombre, Pageable pageable);
    boolean existsBySkuAndEmpresa(String sku, Empresa empresa);
}
