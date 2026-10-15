package pe.altoque.referencia;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

/** Ubigeo INEI de 6 dígitos: el que SUNAT pide en la dirección del emisor. */
@Entity
@Immutable
@Table(name = "distrito")
@Getter
public class Distrito {

    @Id
    private String ubigeo;
    private String nombre;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "provincia_codigo")
    private Provincia provincia;

    public String departamentoNombre() {
        return provincia.getDepartamento().getNombre();
    }

    /** «SAN ISIDRO, LIMA, LIMA» */
    public String etiqueta() {
        return nombre + ", " + provincia.getNombre() + ", " + departamentoNombre();
    }
}
