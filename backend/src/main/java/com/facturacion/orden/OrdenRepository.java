package com.facturacion.orden;

import com.facturacion.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OrdenRepository extends JpaRepository<Orden, UUID> {

    // FIX N+1: JOIN FETCH carga items y pago en una sola query en vez de N queries adicionales
    @Query("SELECT DISTINCT o FROM Orden o " +
            "LEFT JOIN FETCH o.items i " +
            "LEFT JOIN FETCH i.producto " +
            "LEFT JOIN FETCH o.pago " +
            "WHERE o.user = :user " +
            "ORDER BY o.createdAt DESC")
    List<Orden> findByUser(@Param("user") User user);
}
