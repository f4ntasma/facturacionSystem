import { ChangeDetectionStrategy, Component, inject, input, linkedSignal, model, signal } from '@angular/core';
import { Api } from '../core/api.service';
import { Ubigeo } from '../core/modelos';

/** Buscador de distrito (ubigeo INEI): escribe «miraf» y elige «MIRAFLORES, LIMA, LIMA». */
@Component({
  selector: 'at-ubigeo',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="ubigeo">
      <input [id]="idInput()" class="input" autocomplete="off" role="combobox" [attr.aria-expanded]="opciones().length > 0"
        [attr.aria-controls]="idInput() + '-lista'" [disabled]="deshabilitado()" [value]="texto()"
        placeholder="Escribe tu distrito" (input)="buscar($any($event.target).value)" (blur)="cerrarLuego()" />
      @if (seleccion(); as s) { <span class="mono codigo">{{ s.ubigeo }}</span> }
      @if (opciones().length) {
        <ul class="lista" role="listbox" [id]="idInput() + '-lista'">
          @for (o of opciones(); track o.ubigeo) {
            <li role="option" [attr.aria-selected]="o.ubigeo === seleccion()?.ubigeo" (mousedown)="elegir(o)">
              <strong>{{ o.distrito }}</strong><span>{{ o.provincia }}, {{ o.departamento }}</span>
            </li>
          }
        </ul>
      }
    </div>
  `,
  styles: `
    .ubigeo { position: relative; }
    .codigo { position: absolute; right: 12px; top: 50%; transform: translateY(-50%); font-size: 12px; color: var(--gris); pointer-events: none; }
    .lista { position: absolute; z-index: 20; left: 0; right: 0; top: calc(100% + 4px); margin: 0; padding: 4px; list-style: none;
      background: var(--blanco); border: 2px solid var(--tinta); border-radius: var(--radio-sm); box-shadow: var(--sombra-alta);
      max-height: 260px; overflow-y: auto; }
    li { display: flex; flex-direction: column; gap: 2px; padding: 8px 10px; border-radius: 8px; cursor: pointer; }
    li span { font-size: 12px; color: var(--gris); }
    li:hover, li[aria-selected='true'] { background: var(--amarillo-suave); }
  `,
})
export class UbigeoComponent {
  private api = inject(Api);
  readonly idInput = input('ubigeo');
  readonly deshabilitado = input(false);
  /** El distrito elegido (two-way). */
  readonly seleccion = model<Ubigeo | null>(null);
  protected opciones = signal<Ubigeo[]>([]);
  /** Lo que se ve en la caja: la etiqueta del distrito elegido, o lo que se está escribiendo. */
  protected texto = linkedSignal(() => this.seleccion()?.etiqueta ?? '');
  private turno = 0;

  protected async buscar(q: string) {
    this.texto.set(q);
    const t = q.trim();
    if (t.length < 2) {
      this.opciones.set([]);
      return;
    }
    const turno = ++this.turno;
    const r = await this.api.get<Ubigeo[]>('/public/ubigeos', { q: t }).catch(() => []);
    if (turno === this.turno) this.opciones.set(r);
  }

  protected elegir(o: Ubigeo) {
    this.seleccion.set(o);
    this.texto.set(o.etiqueta);
    this.opciones.set([]);
  }

  protected cerrarLuego() {
    setTimeout(() => {
      this.opciones.set([]);
      const s = this.seleccion();
      this.texto.set(s?.etiqueta ?? '');
    }, 150);
  }
}
