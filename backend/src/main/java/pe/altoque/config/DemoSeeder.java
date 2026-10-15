package pe.altoque.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.altoque.catalogo.*;
import pe.altoque.empresa.Empresa;
import pe.altoque.empresa.EmpresaRepository;
import pe.altoque.empresa.EmpresaService;
import pe.altoque.usuario.Miembro;
import pe.altoque.usuario.UsuarioRepository;

import java.math.BigDecimal;

/**
 * Negocio de demostración (RUC de pruebas 20000000001, SUNAT beta).
 * Acceso: demo@altoque.pe / altoque123. Se desactiva con ALTOQUE_DEMO=false.
 */
@Component
public class DemoSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoSeeder.class);
    public static final String EMAIL = "demo@altoque.pe";

    private final AltoqueProperties props;
    private final UsuarioRepository usuarios;
    private final EmpresaRepository empresas;
    private final EmpresaService empresaService;
    private final ProductoRepository productos;
    private final CategoriaRepository categorias;
    private final ClienteRepository clientes;

    public DemoSeeder(AltoqueProperties props, UsuarioRepository usuarios, EmpresaRepository empresas, EmpresaService empresaService,
                      ProductoRepository productos, CategoriaRepository categorias, ClienteRepository clientes) {
        this.props = props;
        this.usuarios = usuarios;
        this.empresas = empresas;
        this.empresaService = empresaService;
        this.productos = productos;
        this.categorias = categorias;
        this.clientes = clientes;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!props.demo() || usuarios.existsByEmailIgnoreCase(EMAIL) || empresas.existsByRuc("20000000001")) return;
        Miembro m = empresaService.registrar(
                new EmpresaService.DatosEmpresa("20000000001", "BODEGA DON PEPE S.A.C.", "Bodega Don Pepe",
                        "AV. LOS INCAS 123", "150132", null, null, null, "987654321", "NEGOCIANTE"),
                new EmpresaService.DatosPersona("Pepe Quispe", EMAIL, "altoque123"));
        Empresa e = m.getEmpresa();
        e.setEstado(Empresa.Estado.ACTIVA);
        e.setPruebaHasta(null);
        e.setMensajeTicket("¡Gracias, caserito! Vuelve al toque.");

        Categoria bebidas = categoria(e, "Bebidas");
        Categoria abarrotes = categoria(e, "Abarrotes");
        Categoria panaderia = categoria(e, "Panadería");
        Categoria lacteos = categoria(e, "Lácteos y huevos");
        producto(e, bebidas, "P0001", "Gaseosa 1 L", "NIU", "5.50", "10", false, true);
        producto(e, bebidas, "P0002", "Agua sin gas 625 ml", "NIU", "1.50", "10", false, true);
        producto(e, panaderia, "P0003", "Pan francés", "NIU", "0.40", "10", false, true);
        producto(e, lacteos, "P0004", "Leche evaporada 400 g", "NIU", "3.00", "10", false, true);
        producto(e, abarrotes, "P0005", "Arroz extra 1 kg", "KGM", "4.20", "20", false, false);
        producto(e, lacteos, "P0006", "Huevos rosados 1 kg", "KGM", "8.50", "20", false, false);
        producto(e, abarrotes, "P0007", "Aceite vegetal 1 L", "NIU", "9.90", "10", false, false);
        producto(e, abarrotes, "P0008", "Azúcar rubia 1 kg", "KGM", "3.80", "10", false, false);
        producto(e, null, "P0009", "Bolsa plástica", "NIU", "0.10", "10", true, true);

        cliente(e, "6", "20601234565", "DISTRIBUIDORA EL CASERITO S.A.C.", "JR. HUALLAGA 456, CERCADO DE LIMA");
        cliente(e, "1", "45678912", "María Huamán Rojas", null);
        log.info("Negocio demo listo: {} / altoque123", EMAIL);
    }

    private Categoria categoria(Empresa e, String nombre) {
        Categoria c = new Categoria();
        c.setEmpresa(e);
        c.setNombre(nombre);
        return categorias.save(c);
    }

    private void producto(Empresa e, Categoria cat, String codigo, String nombre, String unidad, String precio, String afectacion,
                          boolean icbper, boolean favorito) {
        Producto p = new Producto();
        p.setEmpresa(e);
        p.setCategoria(cat);
        p.setCodigo(codigo);
        p.setNombre(nombre);
        p.setUnidadMedida(unidad);
        p.setPrecio(new BigDecimal(precio));
        p.setTipoAfectacion(afectacion);
        p.setAplicaIcbper(icbper);
        p.setFavorito(favorito);
        productos.save(p);
    }

    private void cliente(Empresa e, String tipo, String numero, String nombre, String direccion) {
        Cliente c = new Cliente();
        c.setEmpresa(e);
        c.setTipoDocumento(tipo);
        c.setNumeroDocumento(numero);
        c.setNombre(nombre);
        c.setDireccion(direccion);
        clientes.save(c);
    }
}
