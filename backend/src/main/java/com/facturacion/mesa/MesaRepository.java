package com.facturacion.mesa;

import com.facturacion.empresa.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MesaRepository extends JpaRepository<Mesa, UUID> {
    List<Mesa> findByEmpresa(Empresa empresa);
}