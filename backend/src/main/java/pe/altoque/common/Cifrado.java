package pe.altoque.common;

import org.springframework.stereotype.Component;
import pe.altoque.config.AltoqueProperties;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Cifra secretos de cada empresa (clave SOL, clave del certificado) con AES-256-GCM. */
@Component
public class Cifrado {

    private static final int IV = 12;
    private final SecretKeySpec clave;
    private final SecureRandom random = new SecureRandom();

    public Cifrado(AltoqueProperties props) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(props.cryptoKey().getBytes(StandardCharsets.UTF_8));
            this.clave = new SecretKeySpec(hash, "AES");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public String cifrar(String plano) {
        if (plano == null) return null;
        try {
            byte[] iv = new byte[IV];
            random.nextBytes(iv);
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.ENCRYPT_MODE, clave, new GCMParameterSpec(128, iv));
            byte[] cifrado = c.doFinal(plano.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(ByteBuffer.allocate(IV + cifrado.length).put(iv).put(cifrado).array());
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo cifrar", e);
        }
    }

    public String descifrar(String texto) {
        if (texto == null) return null;
        try {
            byte[] todo = Base64.getDecoder().decode(texto);
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.DECRYPT_MODE, clave, new GCMParameterSpec(128, todo, 0, IV));
            return new String(c.doFinal(todo, IV, todo.length - IV), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo descifrar (¿cambió APP_CRYPTO_KEY?)", e);
        }
    }
}
