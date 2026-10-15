package pe.altoque.sunat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

final class Zip {

    private Zip() {}

    static byte[] comprimir(String nombre, byte[] contenido) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry(nombre));
            zos.write(contenido);
            zos.closeEntry();
            zos.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Devuelve el primer .xml del zip (el CDR viene como R-xxxx.xml). */
    static byte[] primerXml(byte[] zip) {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zip))) {
            ZipEntry e;
            while ((e = zis.getNextEntry()) != null) {
                if (!e.isDirectory() && e.getName().toLowerCase().endsWith(".xml")) {
                    return zis.readAllBytes();
                }
            }
            return null;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
