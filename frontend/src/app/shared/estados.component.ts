import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { EstadoPago, EstadoSunat } from '../core/modelos';

const SUNAT: Record<EstadoSunat, { texto: string; clase: string }> = {
  PENDIENTE: { texto: 'Enviando a SUNAT', clase: 'chip-amarillo' },
  ACEPTADO: { texto: 'Aceptado', clase: 'chip-tinta' },
  OBSERVADO: { texto: 'Aceptado c/ obs.', clase: 'chip-tinta' },
  RECHAZADO: { texto: 'Rechazado', clase: 'chip-magenta' },
  POR_ANULAR: { texto: 'Anulando…', clase: 'chip-gris' },
  ANULADO: { texto: 'Anulado', clase: 'chip-gris' },
};

const PAGO: Record<EstadoPago, { texto: string; clase: string }> = {
  PAGADO: { texto: 'Pagado', clase: 'chip-verde' },
  PARCIAL: { texto: 'Pago parcial', clase: 'chip-amarillo' },
  POR_COBRAR: { texto: 'Por cobrar', clase: 'chip-magenta' },
};

@Component({
  selector: 'at-estado-sunat',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<span class="chip" [class]="'chip ' + e().clase">{{ e().texto }}</span>`,
})
export class EstadoSunatComponent {
  readonly estado = input.required<EstadoSunat>();
  protected readonly e = computed(() => SUNAT[this.estado()]);
}

@Component({
  selector: 'at-estado-pago',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<span [class]="'chip ' + e().clase">{{ e().texto }}</span>`,
})
export class EstadoPagoComponent {
  readonly estado = input.required<EstadoPago>();
  protected readonly e = computed(() => PAGO[this.estado()]);
}
