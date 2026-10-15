package pe.altoque.comprobante;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Map;

/** QR en SVG: nítido en ticketera térmica y en A4, sin depender de AWT. */
public final class QrSvg {

    private QrSvg() {}

    public static String generar(String texto) {
        try {
            BitMatrix m = new QRCodeWriter().encode(texto, BarcodeFormat.QR_CODE, 0, 0,
                    Map.of(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M, EncodeHintType.MARGIN, 1, EncodeHintType.CHARACTER_SET, "UTF-8"));
            int w = m.getWidth(), h = m.getHeight();
            StringBuilder path = new StringBuilder();
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    if (m.get(x, y)) path.append('M').append(x).append(' ').append(y).append("h1v1h-1z");
                }
            }
            return "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 %d %d\" shape-rendering=\"crispEdges\">".formatted(w, h)
                    + "<rect width=\"100%\" height=\"100%\" fill=\"#fff\"/><path fill=\"#14141F\" d=\"" + path + "\"/></svg>";
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el QR", e);
        }
    }
}
