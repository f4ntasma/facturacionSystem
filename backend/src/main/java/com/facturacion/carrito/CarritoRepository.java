package com.facturacion.carrito;

import com.facturacion.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CarritoRepository extends JpaRepository<Carrito, UUID> {
    Optional<Carrito> findByUser(User user);
}
