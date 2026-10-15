package pe.altoque.catalogo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pe.altoque.common.ApiException;
import pe.altoque.common.Catalogos;
import pe.altoque.empresa.EmpresaRepository;
import pe.altoque.seguridad.Sesion;

import java.time.Instant;

/** Caseritos: clientes frecuentes con su documento listo para la próxima venta. */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    public record ClienteVista(Long id, String tipoDocumento, String numeroDocumento, String nombre, String direccion,
                               String email, String telefono, Instant createdAt) {
        static ClienteVista de(Cliente c) {
            return new ClienteVista(c.getId(), c.getTipoDocumento(), c.getNumeroDocumento(), c.getNombre(), c.getDireccion(),
                    c.getEmail(), c.getTelefono(), c.getCreatedAt());
        }
    }

    public record ClienteRequest(@NotBlank @Pattern(regexp = "[1467A]", message = "Tipo de documento inválido") String tipoDocumento,
                                 @NotBlank(message = "Escribe el número de documento") @Size(max = 15) String numeroDocumento,
                                 @NotBlank(message = "Escribe el nombre o razón social") @Size(max = 200) String nombre,
                                 @Size(max = 250) String direccion,
                                 @Email(message = "Correo inválido") @Size(max = 150) String email,
                                 @Size(max = 20) String telefono) {}

    private final ClienteRepository clientes;
    private final EmpresaRepository empresas;

    public ClienteController(ClienteRepository clientes, EmpresaRepository empresas) {
        this.clientes = clientes;
        this.empresas = empresas;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public Page<ClienteVista> buscar(@RequestParam(required = false) String q, @RequestParam(required = false) String tipo,
                                     @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamano) {
        String texto = q == null || q.isBlank() ? null : q.trim();
        String t = tipo == null || tipo.isBlank() ? null : tipo;
        return clientes.buscar(Sesion.actual().empresaId(), texto, t,
                PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamano, 1), 100), Sort.by("nombre"))).map(ClienteVista::de);
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ClienteVista ver(@PathVariable Long id) {
        return ClienteVista.de(buscar(id));
    }

    /** Busca por documento exacto (al escribir el DNI/RUC en la venta). */
    @GetMapping("/documento/{tipo}/{numero}")
    @Transactional(readOnly = true)
    public ClienteVista porDocumento(@PathVariable String tipo, @PathVariable String numero) {
        return clientes.findByEmpresaIdAndTipoDocumentoAndNumeroDocumento(Sesion.actual().empresaId(), tipo, numero)
                .map(ClienteVista::de).orElseThrow(() -> ApiException.noEncontrado("Cliente"));
    }

    @PostMapping
    @Transactional
    public ClienteVista crear(@Valid @RequestBody ClienteRequest r) {
        Sesion s = Sesion.actual();
        validar(r);
        if (clientes.findByEmpresaIdAndTipoDocumentoAndNumeroDocumento(s.empresaId(), r.tipoDocumento(), r.numeroDocumento().trim()).isPresent()) {
            throw ApiException.conflicto("Ese caserito ya está registrado.");
        }
        Cliente c = new Cliente();
        c.setEmpresa(empresas.getReferenceById(s.empresaId()));
        aplicar(c, r);
        return ClienteVista.de(clientes.save(c));
    }

    @PutMapping("/{id}")
    @Transactional
    public ClienteVista actualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest r) {
        validar(r);
        Cliente c = buscar(id);
        clientes.findByEmpresaIdAndTipoDocumentoAndNumeroDocumento(Sesion.actual().empresaId(), r.tipoDocumento(), r.numeroDocumento().trim())
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(otro -> {
                    throw ApiException.conflicto("Ya tienes otro cliente con ese documento.");
                });
        aplicar(c, r);
        return ClienteVista.de(c);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void eliminar(@PathVariable Long id) {
        // Los comprobantes guardan copia de los datos del cliente; borrar solo lo quita de la lista de caseritos.
        Cliente c = buscar(id);
        try {
            clientes.delete(c);
            clientes.flush();
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw ApiException.conflicto("Este cliente tiene comprobantes; no se puede borrar, pero puedes editarlo.");
        }
    }

    private Cliente buscar(Long id) {
        return clientes.findByIdAndEmpresaId(id, Sesion.actual().empresaId()).orElseThrow(() -> ApiException.noEncontrado("Cliente"));
    }

    private static void validar(ClienteRequest r) {
        if (!Catalogos.documentoValido(r.tipoDocumento(), r.numeroDocumento().trim())) {
            throw ApiException.invalido("6".equals(r.tipoDocumento()) ? "El RUC no es válido, pe." : "1".equals(r.tipoDocumento())
                    ? "El DNI debe tener 8 dígitos." : "Revisa el número de documento.");
        }
    }

    private static void aplicar(Cliente c, ClienteRequest r) {
        c.setTipoDocumento(r.tipoDocumento());
        c.setNumeroDocumento(r.numeroDocumento().trim());
        c.setNombre(r.nombre().trim());
        c.setDireccion(r.direccion() == null || r.direccion().isBlank() ? null : r.direccion().trim());
        c.setEmail(r.email() == null || r.email().isBlank() ? null : r.email().trim());
        c.setTelefono(r.telefono() == null || r.telefono().isBlank() ? null : r.telefono().trim());
    }
}
