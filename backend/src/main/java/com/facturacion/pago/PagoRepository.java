package com.facturacion.pago;

import com.facturacion.orden.Orden;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PagoRepository extends JpaRepository<Pago, UUID> {
    Optional<Pago> findByOrden(Orden orden);
}
