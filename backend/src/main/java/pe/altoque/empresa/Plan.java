package pe.altoque.empresa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "plan")
@Getter
@Setter
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigo;
    private String nombre;
    private String descripcion;
    private BigDecimal precioMensual;
    /** null = ilimitado */
    private Integer limiteComprobantes;
    private int maxUsuarios;
    private int maxEstablecimientos;
    private boolean notasCredito;
    private boolean reportesAvanzados;
    private boolean soportePrioritario;
    private boolean destacado;
    private int orden;
    private boolean activo;
}
