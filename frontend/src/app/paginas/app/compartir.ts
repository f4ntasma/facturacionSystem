import { aSoles } from '../../core/formato';
import { Comprobante } from '../../core/modelos';

/** Abre WhatsApp con el enlace público del comprobante. */
export function compartirWhatsapp(c: Comprobante, telefono?: string | null) {
  const negocio = c.emisor.nombreComercial || c.emisor.razonSocial;
  const enlace = `${location.origin}/c/${c.tokenPublico}`;
  const texto = `¡Hola! Aquí tienes tu ${c.tipoNombre.toLowerCase()} ${c.numero} de ${negocio} por ${aSoles(c.total, c.moneda)}: ${enlace}`;
  const tel = (telefono || c.clienteTelefono || '').replace(/\D/g, '');
  const numero = tel.length === 9 ? '51' + tel : tel;
  window.open(`https://wa.me/${numero}?text=${encodeURIComponent(texto)}`, '_blank', 'noopener');
}

export async function copiarEnlace(c: Comprobante): Promise<boolean> {
  try {
    await navigator.clipboard.writeText(`${location.origin}/c/${c.tokenPublico}`);
    return true;
  } catch {
    return false;
  }
}
