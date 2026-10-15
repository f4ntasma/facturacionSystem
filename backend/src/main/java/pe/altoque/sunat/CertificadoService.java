package pe.altoque.sunat;

import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.altoque.common.ApiException;
import pe.altoque.common.Cifrado;
import pe.altoque.common.Tokens;
import pe.altoque.empresa.CertificadoDigital;
import pe.altoque.empresa.CertificadoRepository;
import pe.altoque.empresa.Empresa;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Certificado con el que se firma cada XML. En SIMULADO y BETA, si la empresa no subió uno,
 * se genera un autofirmado (SUNAT beta lo acepta). En PRODUCCION es obligatorio subir el .pfx.
 */
@Service
public class CertificadoService {

    public record Llave(PrivateKey clave, X509Certificate certificado) {}

    private final CertificadoRepository repo;
    private final Cifrado cifrado;
    private final Map<Long, CacheItem> cache = new ConcurrentHashMap<>();

    private record CacheItem(Long certificadoId, Llave llave) {}

    public CertificadoService(CertificadoRepository repo, Cifrado cifrado) {
        this.repo = repo;
        this.cifrado = cifrado;
    }

    @Transactional
    public Llave llavePara(Empresa empresa) {
        CertificadoDigital cert = repo.findFirstByEmpresaIdAndActivoTrueOrderByIdDesc(empresa.getId()).orElse(null);
        if (cert == null) {
            if (empresa.getAmbienteSunat() == Empresa.Ambiente.PRODUCCION) {
                throw ApiException.invalido("Para emitir en producción sube tu certificado digital (.pfx) en Ajustes › SUNAT.");
            }
            cert = generarAutofirmado(empresa);
        } else if (cert.isAutogenerado() && empresa.getAmbienteSunat() == Empresa.Ambiente.PRODUCCION) {
            throw ApiException.invalido("El certificado de prueba no sirve en producción. Sube tu certificado digital (.pfx).");
        }
        CacheItem item = cache.get(empresa.getId());
        if (item != null && item.certificadoId().equals(cert.getId())) return item.llave();
        Llave llave = abrir(cert.getArchivo(), cifrado.descifrar(cert.getClaveEnc()));
        cache.put(empresa.getId(), new CacheItem(cert.getId(), llave));
        return llave;
    }

    @Transactional
    public CertificadoDigital subir(Empresa empresa, byte[] pfx, String clave) {
        Llave llave;
        try {
            llave = abrir(pfx, clave);
        } catch (Exception e) {
            throw ApiException.invalido("No pudimos abrir el certificado. Revisa que sea .pfx/.p12 y que la clave sea la correcta.");
        }
        try {
            llave.certificado().checkValidity();
        } catch (Exception e) {
            throw ApiException.invalido("Ese certificado está vencido o aún no es válido.");
        }
        repo.desactivarTodos(empresa.getId());
        cache.remove(empresa.getId());
        return guardar(empresa, pfx, clave, llave.certificado(), false);
    }

    @Transactional
    public CertificadoDigital generarAutofirmado(Empresa empresa) {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair par = gen.generateKeyPair();
            var nombre = new X500NameBuilder(BCStyle.INSTANCE)
                    .addRDN(BCStyle.CN, empresa.getRazonSocial())
                    .addRDN(BCStyle.O, empresa.getRuc())
                    .addRDN(BCStyle.OU, "Al Toque · certificado de pruebas")
                    .addRDN(BCStyle.C, "PE")
                    .build();
            Instant desde = Instant.now().minus(1, ChronoUnit.DAYS);
            Instant hasta = desde.plus(730, ChronoUnit.DAYS);
            var builder = new JcaX509v3CertificateBuilder(nombre, BigInteger.valueOf(System.currentTimeMillis()),
                    Date.from(desde), Date.from(hasta), nombre, par.getPublic());
            var firmante = new JcaContentSignerBuilder("SHA256withRSA").build(par.getPrivate());
            X509Certificate x509 = new JcaX509CertificateConverter().getCertificate(builder.build(firmante));

            String clave = Tokens.nuevo(24);
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(null, null);
            ks.setKeyEntry("altoque", par.getPrivate(), clave.toCharArray(), new java.security.cert.Certificate[]{x509});
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ks.store(out, clave.toCharArray());
            return guardar(empresa, out.toByteArray(), clave, x509, true);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el certificado de pruebas", e);
        }
    }

    private CertificadoDigital guardar(Empresa empresa, byte[] pfx, String clave, X509Certificate x509, boolean autogenerado) {
        CertificadoDigital c = new CertificadoDigital();
        c.setEmpresa(empresa);
        c.setArchivo(pfx);
        c.setClaveEnc(cifrado.cifrar(clave));
        c.setSujeto(x509.getSubjectX500Principal().getName());
        c.setEmisor(x509.getIssuerX500Principal().getName());
        c.setNumeroSerie(x509.getSerialNumber().toString(16));
        c.setValidoDesde(x509.getNotBefore().toInstant());
        c.setValidoHasta(x509.getNotAfter().toInstant());
        c.setAutogenerado(autogenerado);
        c.setActivo(true);
        return repo.save(c);
    }

    static Llave abrir(byte[] pfx, String clave) {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(new ByteArrayInputStream(pfx), clave.toCharArray());
            for (String alias : Collections.list(ks.aliases())) {
                if (ks.isKeyEntry(alias)) {
                    PrivateKey pk = (PrivateKey) ks.getKey(alias, clave.toCharArray());
                    X509Certificate x509 = (X509Certificate) ks.getCertificate(alias);
                    return new Llave(pk, x509);
                }
            }
            throw new IllegalStateException("El .pfx no trae clave privada");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
