package com.facturacion.cliente;

import com.facturacion.empresa.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
    List<Cliente> findByEmpresa(Empresa empresa);
}