package com.facturacion.cliente;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
    // Usar empresa_id evita pasar entidades detached al query (fuente de errores en Hibernate)
    List<Cliente> findByEmpresaId(UUID empresaId);
}