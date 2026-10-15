package pe.altoque.sunat;

import pe.altoque.empresa.Empresa;

/** Envío asíncrono por ticket (resumen diario o comunicación de baja). */
public interface LoteSunat {

    enum EstadoLote { GENERADO, ENVIADO, ACEPTADO, RECHAZADO }

    Long getId();

    Empresa getEmpresa();

    String getIdentificador();

    EstadoLote getEstado();

    void setEstado(EstadoLote estado);

    String getTicket();

    void setTicket(String ticket);

    void setSunatCodigo(String codigo);

    void setSunatMensaje(String mensaje);

    byte[] getXml();

    void setCdr(byte[] cdr);
}
