import { Pipe, PipeTransform } from '@angular/core';

const soles = new Intl.NumberFormat('es-PE', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
const cantidad = new Intl.NumberFormat('es-PE', { maximumFractionDigits: 3 });

/** S/ 1,248.00 */
export function aSoles(v: number | null | undefined, moneda = 'PEN'): string {
  const n = Number(v ?? 0);
  return `${moneda === 'USD' ? 'US$' : 'S/'} ${soles.format(n)}`;
}

export function aCantidad(v: number | null | undefined): string {
  return cantidad.format(Number(v ?? 0));
}

/** 2026-10-15 → 15/10/2026 */
export function aFecha(iso: string | null | undefined): string {
  if (!iso) return '';
  const [a, m, d] = iso.substring(0, 10).split('-');
  return `${d}/${m}/${a}`;
}

/** 17:42:10 → 5:42 p. m. */
export function aHora(hora: string | null | undefined): string {
  if (!hora) return '';
  const [h, m] = hora.split(':').map(Number);
  const sufijo = h >= 12 ? 'p. m.' : 'a. m.';
  const h12 = h % 12 === 0 ? 12 : h % 12;
  return `${h12}:${String(m).padStart(2, '0')} ${sufijo}`;
}

export function hoyIso(): string {
  const d = new Date();
  const local = new Date(d.getTime() - d.getTimezoneOffset() * 60000);
  return local.toISOString().substring(0, 10);
}

export function sumarDias(iso: string, dias: number): string {
  const d = new Date(iso + 'T12:00:00');
  d.setDate(d.getDate() + dias);
  return d.toISOString().substring(0, 10);
}

@Pipe({ name: 'soles' })
export class SolesPipe implements PipeTransform {
  transform(v: number | null | undefined, moneda = 'PEN'): string {
    return aSoles(v, moneda);
  }
}

@Pipe({ name: 'cant' })
export class CantidadPipe implements PipeTransform {
  transform(v: number | null | undefined): string {
    return aCantidad(v);
  }
}

@Pipe({ name: 'fecha' })
export class FechaPipe implements PipeTransform {
  transform(v: string | null | undefined): string {
    return aFecha(v);
  }
}

@Pipe({ name: 'hora' })
export class HoraPipe implements PipeTransform {
  transform(v: string | null | undefined): string {
    return aHora(v);
  }
}
