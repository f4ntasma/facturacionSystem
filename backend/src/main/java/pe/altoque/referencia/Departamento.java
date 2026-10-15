package pe.altoque.referencia;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "departamento")
@Getter
public class Departamento {

    @Id
    private String codigo;
    private String nombre;
}
