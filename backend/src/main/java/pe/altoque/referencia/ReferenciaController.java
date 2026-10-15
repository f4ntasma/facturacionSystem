package pe.altoque.referencia;

import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Datos de referencia públicos (sin información del negocio). */
@RestController
@RequestMapping("/api/public")
public class ReferenciaController {

    public record UbigeoVista(String ubigeo, String distrito, String provincia, String departamento, String etiqueta) {
        static UbigeoVista de(Distrito d) {
            return new UbigeoVista(d.getUbigeo(), d.getNombre(), d.getProvincia().getNombre(), d.departamentoNombre(), d.etiqueta());
        }
    }

    private final DistritoRepository distritos;

    public ReferenciaController(DistritoRepository distritos) {
        this.distritos = distritos;
    }

    @GetMapping("/ubigeos")
    @Transactional(readOnly = true)
    public List<UbigeoVista> ubigeos(@RequestParam String q) {
        String t = q == null ? "" : q.trim();
        if (t.length() < 2) return List.of();
        return distritos.buscar(t, PageRequest.of(0, 15)).stream().map(UbigeoVista::de).toList();
    }
}
