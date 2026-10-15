package pe.altoque.catalogo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.altoque.empresa.Empresa;

@Entity
@Table(name = "categoria")
@Getter
@Setter
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    private String nombre;
}
