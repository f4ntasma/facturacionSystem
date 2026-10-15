package pe.altoque.referencia;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "provincia")
@Getter
public class Provincia {

    @Id
    private String codigo;
    private String nombre;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "departamento_codigo")
    private Departamento departamento;
}
