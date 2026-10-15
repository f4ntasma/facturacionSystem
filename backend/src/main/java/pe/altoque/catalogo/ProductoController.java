package pe.altoque.catalogo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pe.altoque.common.ApiException;
import pe.altoque.common.Catalogos;
import pe.altoque.empresa.EmpresaRepository;
import pe.altoque.seguridad.Sesion;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api")
public class ProductoController {

    public record ProductoVista(Long id, String codigo, String nombre, String descripcion, String unidadMedida, BigDecimal precio,
                                String tipoAfectacion, boolean aplicaIcbper, boolean controlaStock, BigDecimal stock,
                                boolean favorito, boolean activo, Long categoriaId, String categoria) {
        static ProductoVista de(Producto p) {
            return new ProductoVista(p.getId(), p.getCodigo(), p.getNombre(), p.getDescripcion(), p.getUnidadMedida(), p.getPrecio(),
                    p.getTipoAfectacion(), p.isAplicaIcbper(), p.isControlaStock(), p.getStock(), p.isFavorito(), p.isActivo(),
                    p.getCategoria() != null ? p.getCategoria().getId() : null,
                    p.getCategoria() != null ? p.getCategoria().getNombre() : null);
        }
    }

    public record ProductoRequest(@Size(max = 30) String codigo,
                                  @NotBlank(message = "Ponle nombre al producto") @Size(max = 200) String nombre,
                                  @Size(max = 500) String descripcion,
                                  @NotBlank String unidadMedida,
                                  @NotNull(message = "Ponle precio") @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero") BigDecimal precio,
                                  @NotBlank @Pattern(regexp = "10|20|30") String tipoAfectacion,
                                  boolean aplicaIcbper,
                                  boolean controlaStock,
                                  BigDecimal stock,
                                  boolean favorito,
                                  Boolean activo,
                                  @Size(max = 80) String categoria) {}

    public record CategoriaVista(Long id, String nombre) {}

    public record IngresoRequest(@NotNull @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor a cero") BigDecimal cantidad,
                                 @Size(max = 200) String nota) {}

    public record MovimientoVista(Long id, MovimientoInventario.Tipo tipo, BigDecimal cantidad, BigDecimal stockResultante,
                                  Long comprobanteId, String nota, java.time.Instant fecha) {}

    private final ProductoRepository productos;
    private final CategoriaRepository categorias;
    private final EmpresaRepository empresas;
    private final InventarioService inventario;
    private final MovimientoInventarioRepository movimientos;
    private final Catalogos catalogos;

    public ProductoController(ProductoRepository productos, CategoriaRepository categorias, EmpresaRepository empresas,
                              InventarioService inventario, MovimientoInventarioRepository movimientos, Catalogos catalogos) {
        this.productos = productos;
        this.categorias = categorias;
        this.empresas = empresas;
        this.inventario = inventario;
        this.movimientos = movimientos;
        this.catalogos = catalogos;
    }

    @GetMapping("/productos")
    @Transactional(readOnly = true)
    public Page<ProductoVista> buscar(@RequestParam(required = false) String q, @RequestParam(required = false) Long categoriaId,
                                      @RequestParam(defaultValue = "false") boolean soloActivos,
                                      @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "30") int tamano) {
        String texto = q == null || q.isBlank() ? null : q.trim();
        var orden = Sort.by(Sort.Order.desc("favorito"), Sort.Order.asc("nombre"));
        return productos.buscar(Sesion.actual().empresaId(), texto, categoriaId, soloActivos,
                PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamano, 1), 200), orden)).map(ProductoVista::de);
    }

    @GetMapping("/productos/{id}")
    @Transactional(readOnly = true)
    public ProductoVista ver(@PathVariable Long id) {
        return ProductoVista.de(buscar(id));
    }

    @PostMapping("/productos")
    @PreAuthorize("hasAnyRole('PROPIETARIO','ADMINISTRADOR')")
    @Transactional
    public ProductoVista crear(@Valid @RequestBody ProductoRequest r) {
        Sesion s = Sesion.actual();
        Producto p = new Producto();
        p.setEmpresa(empresas.getReferenceById(s.empresaId()));
        String codigo = r.codigo() == null || r.codigo().isBlank() ? siguienteCodigo(s.empresaId()) : r.codigo().trim().toUpperCase(Locale.ROOT);
        if (productos.existsByEmpresaIdAndCodigo(s.empresaId(), codigo)) throw ApiException.conflicto("Ya tienes un producto con el código " + codigo + ".");
        p.setCodigo(codigo);
        aplicar(p, r, s.empresaId());
        productos.save(p);
        if (p.isControlaStock() && r.stock() != null && r.stock().signum() > 0) {
            inventario.ingresar(p, r.stock(), s.usuarioId(), "Stock inicial");
        }
        return ProductoVista.de(p);
    }

    @PutMapping("/productos/{id}")
    @PreAuthorize("hasAnyRole('PROPIETARIO','ADMINISTRADOR')")
    @Transactional
    public ProductoVista actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest r) {
        Sesion s = Sesion.actual();
        Producto p = buscar(id);
        if (r.codigo() != null && !r.codigo().isBlank()) {
            String codigo = r.codigo().trim().toUpperCase(Locale.ROOT);
            productos.findByEmpresaIdAndCodigo(s.empresaId(), codigo).filter(o -> !o.getId().equals(id)).ifPresent(o -> {
                throw ApiException.conflicto("Ya tienes otro producto con el código " + codigo + ".");
            });
            p.setCodigo(codigo);
        }
        aplicar(p, r, s.empresaId());
        if (p.isControlaStock() && r.stock() != null) inventario.ajustar(p, r.stock(), s.usuarioId(), "Ajuste desde el catálogo");
        return ProductoVista.de(p);
    }

    /** Ingreso de mercadería (compra o reposición). */
    @PostMapping("/productos/{id}/ingresos")
    @PreAuthorize("hasAnyRole('PROPIETARIO','ADMINISTRADOR')")
    @Transactional
    public ProductoVista ingresar(@PathVariable Long id, @Valid @RequestBody IngresoRequest r) {
        Producto p = buscar(id);
        if (!p.isControlaStock()) throw ApiException.invalido("Activa «controlar stock» en este producto primero.");
        inventario.ingresar(p, r.cantidad(), Sesion.actual().usuarioId(), r.nota());
        return ProductoVista.de(p);
    }

    /** Kardex del producto: últimos movimientos. */
    @GetMapping("/productos/{id}/kardex")
    @Transactional(readOnly = true)
    public List<MovimientoVista> kardex(@PathVariable Long id) {
        Producto p = buscar(id);
        return movimientos.findByEmpresaIdAndProductoIdOrderByIdDesc(p.getEmpresa().getId(), p.getId(), PageRequest.of(0, 100)).stream()
                .map(m -> new MovimientoVista(m.getId(), m.getTipo(), m.getCantidad(), m.getStockResultante(), m.getComprobanteId(),
                        m.getNota(), m.getCreatedAt()))
                .toList();
    }

    @PatchMapping("/productos/{id}/favorito")
    @Transactional
    public ProductoVista favorito(@PathVariable Long id, @RequestParam boolean valor) {
        Producto p = buscar(id);
        p.setFavorito(valor);
        return ProductoVista.de(p);
    }

    @GetMapping("/categorias")
    @Transactional(readOnly = true)
    public List<CategoriaVista> categorias() {
        return categorias.findByEmpresaIdOrderByNombre(Sesion.actual().empresaId()).stream()
                .map(c -> new CategoriaVista(c.getId(), c.getNombre())).toList();
    }

    private void aplicar(Producto p, ProductoRequest r, Long empresaId) {
        if (!catalogos.unidades().containsKey(r.unidadMedida())) throw ApiException.invalido("Unidad de medida no válida.");
        p.setNombre(r.nombre().trim());
        p.setDescripcion(r.descripcion() == null || r.descripcion().isBlank() ? null : r.descripcion().trim());
        p.setUnidadMedida(r.unidadMedida());
        p.setPrecio(r.precio());
        p.setTipoAfectacion(r.tipoAfectacion());
        p.setAplicaIcbper(r.aplicaIcbper());
        p.setControlaStock(r.controlaStock());
        p.setFavorito(r.favorito());
        if (r.activo() != null) p.setActivo(r.activo());
        if (r.categoria() == null || r.categoria().isBlank()) {
            p.setCategoria(null);
        } else {
            String nombre = r.categoria().trim();
            Categoria cat = categorias.findByEmpresaIdAndNombreIgnoreCase(empresaId, nombre).orElseGet(() -> {
                Categoria nueva = new Categoria();
                nueva.setEmpresa(empresas.getReferenceById(empresaId));
                nueva.setNombre(nombre);
                return categorias.save(nueva);
            });
            p.setCategoria(cat);
        }
    }

    private String siguienteCodigo(Long empresaId) {
        long n = productos.contar(empresaId) + 1;
        String codigo;
        do {
            codigo = "P%04d".formatted(n++);
        } while (productos.existsByEmpresaIdAndCodigo(empresaId, codigo));
        return codigo;
    }

    private Producto buscar(Long id) {
        return productos.findByIdAndEmpresaId(id, Sesion.actual().empresaId()).orElseThrow(() -> ApiException.noEncontrado("Producto"));
    }
}
