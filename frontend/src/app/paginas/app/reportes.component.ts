import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Api, mensajeError } from '../../core/api.service';
import { CantidadPipe, FechaPipe, SolesPipe, hoyIso, sumarDias } from '../../core/formato';
import { Lote, NOMBRE_METODO, NOMBRE_TIPO, Resumen } from '../../core/modelos';
import { ToastService } from '../../core/toast.service';
import { IconoComponent } from '../../shared/icono.component';

type Rango = 'hoy' | '7' | 'mes' | 'pasado' | 'libre';

@Component({
  selector: 'at-reportes',
  imports: [FormsModule, SolesPipe, FechaPipe, CantidadPipe, IconoComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="pagina">
      <header class="cabecera">
        <div>
          <h1 class="titulo">Reportes</h1>
          <p class="suave chico">Cuánto vendiste, cómo te pagaron y qué se vende más. Ventas netas: boletas + facturas − notas de crédito.</p>
        </div>
        <button type="button" class="btn btn-borde btn-chico" (click)="exportar()"><at-icono nombre="descargar" [tam]="18" /> Exportar CSV</button>
      </header>

      <div class="filtros">
        <div class="segmentado" role="group" aria-label="Periodo">
          @for (r of rangos; track r.v) {
            <button type="button" [attr.aria-pressed]="rango() === r.v" (click)="elegir(r.v)">{{ r.t }}</button>
          }
        </div>
        <div class="fila">
          <label class="sr-only" for="d">Desde</label>
          <input id="d" class="input" type="date" [(ngModel)]="desde" (change)="rango.set('libre'); cargar()" />
          <span>a</span>
          <label class="sr-only" for="h">Hasta</label>
          <input id="h" class="input" type="date" [(ngModel)]="hasta" (change)="rango.set('libre'); cargar()" />
        </div>
      </div>

      @if (r(); as r) {
        <div class="grilla-4">
          <div class="tarjeta-amarilla kpi"><span class="chico"><strong>Ventas netas</strong></span><span class="mono k">{{ r.ventasNetas | soles }}</span></div>
          <div class="tarjeta kpi"><span class="chico suave">Comprobantes</span><span class="mono k">{{ r.comprobantes }}</span></div>
          <div class="tarjeta kpi"><span class="chico suave">Ticket promedio</span><span class="mono k">{{ r.ticketPromedio | soles }}</span></div>
          <div class="tarjeta kpi"><span class="chico suave">Por cobrar (total)</span><span class="mono k" style="color: var(--magenta)">{{ r.porCobrar | soles }}</span></div>
        </div>

        <section class="tarjeta col">
          <h2 class="subtitulo">Ventas netas por día</h2>
          @if (r.dias.length > 1) {
            <div class="grafico" role="img" [attr.aria-label]="'Ventas netas por día del ' + (r.desde | fecha) + ' al ' + (r.hasta | fecha)">
              <div class="eje mono mini" aria-hidden="true"><span>{{ maxDia() | soles }}</span><span>S/ 0</span></div>
              <div class="barras">
                @for (d of r.dias; track d.fecha) {
                  <div class="b-col" tabindex="0" [attr.aria-label]="(d.fecha | fecha) + ': ' + (d.total | soles) + ', ' + d.comprobantes + ' comprobantes'">
                    <div class="b" [style.height.%]="pct(d.total, maxDia())"></div>
                    <span class="tip mono">{{ d.fecha | fecha }}<br /><strong>{{ d.total | soles }}</strong><br />{{ d.comprobantes }} comp.</span>
                  </div>
                }
              </div>
            </div>
            <div class="entre mono mini suave"><span>{{ r.desde | fecha }}</span><span>{{ r.hasta | fecha }}</span></div>
          } @else {
            <p class="mono" style="font-size: 28px; font-weight: 700">{{ r.ventasNetas | soles }}</p>
          }
          <details>
            <summary class="chico">Ver como tabla</summary>
            <table class="tabla"><thead><tr><th>Fecha</th><th class="derecha">Comprobantes</th><th class="derecha">Ventas netas</th></tr></thead>
              <tbody>@for (d of r.dias; track d.fecha) { <tr><td>{{ d.fecha | fecha }}</td><td class="derecha">{{ d.comprobantes }}</td><td class="derecha mono">{{ d.total | soles }}</td></tr> }</tbody>
            </table>
          </details>
        </section>

        <div class="grilla-2">
          <section class="tarjeta col">
            <h2 class="subtitulo">Cómo te pagaron</h2>
            @for (m of r.porMetodo; track m.metodo) {
              <div class="hbar">
                <div class="entre chico"><span><strong>{{ metodo(m.metodo) }}</strong> · {{ m.cantidad }}</span><span class="mono">{{ m.total | soles }}</span></div>
                <div class="pista"><div class="relleno" [style.width.%]="pct(m.total, maxMetodo())"></div></div>
              </div>
            } @empty { <p class="suave chico">Sin cobros en este periodo.</p> }
          </section>
          <section class="tarjeta col">
            <h2 class="subtitulo">Por tipo de comprobante</h2>
            <table class="tabla">
              <thead><tr><th>Tipo</th><th class="derecha">Cantidad</th><th class="derecha">Total</th></tr></thead>
              <tbody>
                @for (t of r.porTipo; track t.tipo) { <tr><td>{{ tipo(t.tipo) }}</td><td class="derecha">{{ t.cantidad }}</td><td class="derecha mono">{{ t.tipo === '07' ? '−' : '' }}{{ t.total | soles }}</td></tr> }
                @empty { <tr><td colspan="3" class="suave">Sin comprobantes.</td></tr> }
              </tbody>
            </table>
          </section>
        </div>

        <section class="tarjeta col">
          <h2 class="subtitulo">Lo más vendido</h2>
          <table class="tabla">
            <thead><tr><th>#</th><th>Producto</th><th class="derecha">Cantidad</th><th class="derecha">Vendido</th></tr></thead>
            <tbody>
              @for (t of r.topProductos; track t.descripcion; let i = $index) {
                <tr><td class="mono">{{ i + 1 }}</td><td>{{ t.descripcion }}</td><td class="derecha mono">{{ t.cantidad | cant }}</td><td class="derecha mono">{{ t.total | soles }}</td></tr>
              } @empty { <tr><td colspan="4" class="suave">Aún no hay ventas en este periodo.</td></tr> }
            </tbody>
          </table>
        </section>
      } @else {
        <div class="esqueleto" style="height: 360px"></div>
      }

      <section class="tarjeta col">
        <div class="entre"><h2 class="subtitulo">Anulaciones enviadas a SUNAT</h2><span class="mini suave">Resúmenes diarios (boletas) y comunicaciones de baja (facturas)</span></div>
        <div class="tabla-caja" style="box-shadow: none">
          <table class="tabla">
            <thead><tr><th>Identificador</th><th>Documentos</th><th>Estado</th><th>Respuesta</th><th></th></tr></thead>
            <tbody>
              @for (l of lotes(); track l.clase + l.id) {
                <tr>
                  <td class="mono nowrap">{{ l.identificador }}<br /><span class="mini suave">{{ l.clase === 'RESUMEN' ? 'Resumen diario' : 'Baja' }}</span></td>
                  <td class="mono chico">{{ l.documentos.join(', ') }}</td>
                  <td><span class="chip" [class.chip-tinta]="l.estado === 'ACEPTADO'" [class.chip-amarillo]="l.estado === 'ENVIADO' || l.estado === 'GENERADO'" [class.chip-magenta]="l.estado === 'RECHAZADO'">{{ l.estado }}</span></td>
                  <td class="chico">{{ l.sunatMensaje }}</td>
                  <td>@if (l.estado === 'ENVIADO' || l.estado === 'GENERADO') { <button type="button" class="btn btn-chico btn-borde" (click)="consultar(l)">Consultar</button> }</td>
                </tr>
              } @empty { <tr><td colspan="5" class="suave chico">No has anulado comprobantes. ¡Bien ahí!</td></tr> }
            </tbody>
          </table>
        </div>
      </section>
    </div>
  `,
  styles: `
    .filtros { display: flex; flex-wrap: wrap; gap: 10px; align-items: center; justify-content: space-between; }
    .filtros .input { width: 160px; }
    .kpi { display: flex; flex-direction: column; gap: 4px; }
    .k { font-size: 22px; font-weight: 700; }
    .grafico { display: flex; gap: 8px; height: 220px; }
    .eje { display: flex; flex-direction: column; justify-content: space-between; color: var(--gris); padding-bottom: 2px; text-align: right; min-width: 64px; }
    .barras { flex: 1; display: flex; align-items: flex-end; gap: 2px; border-bottom: 1px solid var(--linea-fuerte);
      background-image: linear-gradient(var(--linea) 1px, transparent 1px); background-size: 100% 25%; }
    .b-col { flex: 1; height: 100%; display: flex; align-items: flex-end; justify-content: center; position: relative; outline-offset: 0; }
    .b { width: 100%; max-width: 28px; background: var(--tinta); border-radius: 4px 4px 0 0; min-height: 2px; }
    .b-col:hover .b, .b-col:focus .b { background: var(--magenta); }
    .tip { display: none; position: absolute; bottom: calc(100% + 6px); left: 50%; transform: translateX(-50%); background: var(--tinta); color: var(--blanco);
      padding: 6px 10px; border-radius: 8px; font-size: 11px; white-space: nowrap; z-index: 2; pointer-events: none; text-align: center; }
    .b-col:hover .tip, .b-col:focus .tip { display: block; }
    .hbar { display: flex; flex-direction: column; gap: 4px; }
    .pista { height: 10px; background: var(--papel); border-radius: 4px; overflow: hidden; }
    .relleno { height: 100%; background: var(--tinta); border-radius: 0 4px 4px 0; }
    details summary { cursor: pointer; font-weight: 700; }
    @media (max-width: 760px) { .filtros .input { width: 140px; } .eje { display: none; } }
  `,
})
export class ReportesComponent {
  private api = inject(Api);
  private toast = inject(ToastService);
  protected readonly rangos: { v: Rango; t: string }[] = [
    { v: 'hoy', t: 'Hoy' }, { v: '7', t: '7 días' }, { v: 'mes', t: 'Este mes' }, { v: 'pasado', t: 'Mes pasado' },
  ];
  protected rango = signal<Rango>('7');
  protected desde = sumarDias(hoyIso(), -6);
  protected hasta = hoyIso();
  protected r = signal<Resumen | null>(null);
  protected lotes = signal<Lote[]>([]);
  protected maxDia = computed(() => Math.max(1, ...(this.r()?.dias.map((d) => d.total) ?? [1])));
  protected maxMetodo = computed(() => Math.max(1, ...(this.r()?.porMetodo.map((d) => d.total) ?? [1])));

  constructor() {
    this.cargar();
    this.cargarLotes();
  }

  protected elegir(v: Rango) {
    this.rango.set(v);
    const hoy = hoyIso();
    if (v === 'hoy') [this.desde, this.hasta] = [hoy, hoy];
    if (v === '7') [this.desde, this.hasta] = [sumarDias(hoy, -6), hoy];
    if (v === 'mes') [this.desde, this.hasta] = [hoy.substring(0, 8) + '01', hoy];
    if (v === 'pasado') {
      const ini = new Date(hoy.substring(0, 8) + '01T12:00:00');
      ini.setMonth(ini.getMonth() - 1);
      const desde = ini.toISOString().substring(0, 10);
      this.desde = desde;
      this.hasta = sumarDias(hoy.substring(0, 8) + '01', -1);
    }
    this.cargar();
  }

  protected async cargar() {
    this.r.set(null);
    try {
      this.r.set(await this.api.get<Resumen>('/reportes/resumen', { desde: this.desde, hasta: this.hasta }));
    } catch (e) {
      this.toast.error(mensajeError(e));
    }
  }

  protected async cargarLotes() {
    this.lotes.set(await this.api.get<Lote[]>('/lotes').catch(() => []));
  }

  protected async consultar(l: Lote) {
    try {
      await this.api.post(`/lotes/${l.clase}/${l.id}/consultar`);
      await this.cargarLotes();
      this.toast.info('Consultamos el ticket en SUNAT.');
    } catch (e) {
      this.toast.error(mensajeError(e));
    }
  }

  protected exportar() {
    this.api.descargar('/comprobantes/exportar', { desde: this.desde, hasta: this.hasta }).catch((e) => this.toast.error(mensajeError(e)));
  }

  protected pct(v: number, max: number) {
    return Math.max(0.5, (Math.max(0, v) / max) * 100);
  }

  protected metodo(m: keyof typeof NOMBRE_METODO) {
    return NOMBRE_METODO[m];
  }

  protected tipo(t: keyof typeof NOMBRE_TIPO) {
    return NOMBRE_TIPO[t];
  }
}
