package pe.altoque.sunat;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.altoque.common.ApiException;
import pe.altoque.config.AltoqueProperties;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Guarda XML y CDR en disco, ordenados por RUC y mes (RUC/AAAA/MM/archivo). La base solo guarda la ruta
 * y el SHA-256, que se verifica al leer. SUNAT exige conservarlos: el volumen debe respaldarse.
 */
@Service
public class AlmacenArchivos {

    private final Path raiz;
    private final ArchivoRepository archivos;

    public AlmacenArchivos(AltoqueProperties props, ArchivoRepository archivos) {
        this.raiz = Path.of(props.archivosDir()).toAbsolutePath().normalize();
        this.archivos = archivos;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Archivo guardarDeComprobante(Long empresaId, String ruc, Long comprobanteId, Archivo.Tipo tipo, String nombre, byte[] contenido) {
        Archivo a = archivos.findByComprobanteIdAndTipo(comprobanteId, tipo).orElseGet(Archivo::new);
        a.setComprobanteId(comprobanteId);
        return guardar(a, empresaId, ruc, tipo, nombre, contenido);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Archivo guardarDeLote(Long empresaId, String ruc, Long loteId, Archivo.Tipo tipo, String nombre, byte[] contenido) {
        Archivo a = archivos.findByLoteIdAndTipo(loteId, tipo).orElseGet(Archivo::new);
        a.setLoteId(loteId);
        return guardar(a, empresaId, ruc, tipo, nombre, contenido);
    }

    public Optional<byte[]> deComprobante(Long comprobanteId, Archivo.Tipo tipo) {
        return archivos.findByComprobanteIdAndTipo(comprobanteId, tipo).map(this::leer);
    }

    public Optional<Archivo> metaDeComprobante(Long comprobanteId, Archivo.Tipo tipo) {
        return archivos.findByComprobanteIdAndTipo(comprobanteId, tipo);
    }

    public Optional<byte[]> deLote(Long loteId, Archivo.Tipo tipo) {
        return archivos.findByLoteIdAndTipo(loteId, tipo).map(this::leer);
    }

    public byte[] leer(Archivo a) {
        byte[] contenido;
        try {
            contenido = Files.readAllBytes(resolver(a.getRuta()));
        } catch (IOException e) {
            throw ApiException.noEncontrado("Archivo " + a.getNombre());
        }
        if (!sha256(contenido).equals(a.getSha256())) {
            throw ApiException.conflicto("El archivo " + a.getNombre() + " no coincide con su huella. Restáuralo del respaldo.");
        }
        return contenido;
    }

    private Archivo guardar(Archivo a, Long empresaId, String ruc, Archivo.Tipo tipo, String nombre, byte[] contenido) {
        LocalDate hoy = LocalDate.now();
        String ruta = "%s/%d/%02d/%s".formatted(ruc, hoy.getYear(), hoy.getMonthValue(), nombre);
        try {
            Path destino = resolver(ruta);
            Files.createDirectories(destino.getParent());
            Path temporal = Files.createTempFile(destino.getParent(), ".tmp-", ".part");
            Files.write(temporal, contenido);
            Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar " + nombre, e);
        }
        a.setEmpresaId(empresaId);
        a.setTipo(tipo);
        a.setNombre(nombre);
        a.setRuta(ruta);
        a.setBytes(contenido.length);
        a.setSha256(sha256(contenido));
        return archivos.save(a);
    }

    private Path resolver(String ruta) {
        Path p = raiz.resolve(ruta).normalize();
        if (!p.startsWith(raiz)) throw new IllegalArgumentException("Ruta fuera del almacén: " + ruta);
        return p;
    }

    static String sha256(byte[] datos) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(datos));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
