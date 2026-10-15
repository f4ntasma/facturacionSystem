import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Api } from '../../core/api.service';
import { Auth } from '../../core/auth.service';
import { HoraPipe, SolesPipe, aFecha } from '../../core/formato';
import { Fila, Pagina, Resumen } from '../../core/modelos';
import { EstadoPagoComponent, EstadoSunatComponent } from '../../shared/estados.component';
import { IconoComponent } from '../../shared/icono.component';
import { LogoComponent } from '../../shared/logo.component';

@Component({
  selector: 'at-inicio',
  imports: [RouterLink, SolesPipe, HoraPipe, LogoComponent, IconoComponent, EstadoPagoComponent, EstadoSunatComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="pagina">
      <header class="entre movil">
        <at-logo [tam]="34" />
        <a class="btn btn-icono campana" routerLink="/app/comprobantes" [queryParams]="{ estado: 'PENDIENTE' }"
          [attr.aria-label]="pendientes() ? pendientes() + ' comprobantes pendientes de SUNAT' : 'Comprobantes'">
          <at-icono nombre="campana" />
          @if (pendientes()) { <span class="punto"></span> }
        </a>
      </header>

      @if (bienvenida) {
        <div class="aviso aviso-verde">
          <at-icono nombre="check" />
          <div><strong>¡Listo, caserito! Tu negocio ya está en Al Toque.</strong><br />
            Empiezas en modo pruebas con SUNAT beta: emite tu primera boleta y mírala aceptada. Cuando quieras, pasa a producción en Ajustes.</div>
        </div>
      }

      <div class="grid">
        <section class="col">
          <div>
            <h1 class="titulo">¡Habla, caserito!</h1>
            <p class="chico">{{ auth.empresa()?.nombreComercial || auth.empresa()?.razonSocial }}</p>
          </div>

          <div class="tarjeta-amarilla ventas">
            <span class="chico" style="font-weight: 700">Ventas de hoy</span>
            @if (resumen(); as r) {
              <span class="mono grande">{{ r.ventasHoy | soles }}</span>
              <span class="chico">{{ r.comprobantesHoy }} {{ r.comprobantesHoy === 1 ? 'comprobante emitido' : 'comprobantes emitidos' }}</span>
            } @else {
              <span class="esqueleto" style="height: 40px; width: 60%"></span>
            }
          </div>

          <div class="col botones">
            <a routerLink="/app/emitir" [queryParams]="{ tipo: '03' }" class="btn btn-tinta btn-grande">
              <at-icono nombre="sumar" [grosor]="2.5" /> Emitir boleta al toque
            </a>
            <a routerLink="/app/emitir" [queryParams]="{ tipo: '01' }" class="btn btn-borde" style="min-height: 52px; font-size: 17px">Emitir factura</a>
          </div>

          <div class="col" style="gap: 8px">
            <div class="entre">
              <h2 class="subtitulo">Últimos comprobantes</h2>
              <a routerLink="/app/comprobantes" class="chico"><strong>Ver todos</strong></a>
            </div>
            <div class="lista">
              @for (f of ultimos(); track f.id) {
                <a class="lista-item" [routerLink]="['/app/comprobantes', f.id]">
                  <div class="crece">
                    <div class="mono chico" style="font-weight: 700">{{ f.numero }}</div>
                    <div class="mini suave elipsis">{{ f.clienteNombre === 'CLIENTES VARIOS' ? 'Cliente varios' : f.clienteNombre }} · {{ f.horaEmision | hora }}</div>
                  </div>
                  <div class="der">
                    <span class="monto chico">{{ f.total | soles }}</span>
                    @if (f.estadoSunat === 'ANULADO' || f.estadoSunat === 'RECHAZADO' || f.estadoSunat === 'PENDIENTE') {
                      <at-estado-sunat [estado]="f.estadoSunat" />
                    } @else if (f.tipo === '07') {
                      <span class="chip chip-gris">Nota crédito</span>
                    } @else {
                      <at-estado-pago [estado]="f.estadoPago" />
                    }
                  </div>
                </a>
              } @empty {
                @if (cargado()) {
                  <div class="vacio">
                    <strong>Aún no emites nada hoy</strong>
                    <span>Tu primera boleta te está esperando, causa.</span>
                  </div>
                } @else {
                  @for (i of [1, 2, 3]; track i) { <div class="lista-item"><span class="esqueleto" style="width: 100%; height: 36px"></span></div> }
                }
              }
            </div>
          </div>
        </section>

        <section class="col lateral">
          @if (resumen(); as r) {
            <div class="tarjeta col">
              <div class="entre"><h2 class="subtitulo">Esta semana</h2><span class="monto">{{ r.ventasNetas | soles }}</span></div>
              <div class="barras" role="img" [attr.aria-label]="'Ventas de los últimos 7 días, total ' + (r.ventasNetas | soles)">
                @for (d of r.dias; track d.fecha) {
                  <div class="barra-col" [title]="fecha(d.fecha) + ': ' + (d.total | soles)">
                    <div class="barra" [style.height.%]="altura(d.total)" [class.hoy]="$last"></div>
                    <span class="mono mini">{{ diaCorto(d.fecha) }}</span>
                  </div>
                }
              </div>
            </div>
            <div class="grilla-2 kpis">
              <a class="tarjeta kpi" routerLink="/app/comprobantes" [queryParams]="{ estadoPago: 'POR_COBRAR' }">
                <span class="chico suave">Por cobrar</span>
                <span class="monto" style="font-size: 20px; color: var(--magenta)">{{ r.porCobrar | soles }}</span>
              </a>
              <a class="tarjeta kpi" routerLink="/app/comprobantes" [queryParams]="{ estado: 'PENDIENTE' }">
                <span class="chico suave">Pendientes SUNAT</span>
                <span class="monto" style="font-size: 20px">{{ r.pendientesSunat }}</span>
              </a>
            </div>
          }
          @if (auth.empresa(); as e) {
            <div class="tarjeta col" style="gap: 8px">
              <div class="entre"><span class="chico"><strong>Plan {{ e.plan.nombre }}</strong></span>
                <a routerLink="/app/ajustes" [queryParams]="{ tab: 'plan' }" class="mini">Ver planes</a></div>
              @if (e.plan.limiteComprobantes) {
                <div class="uso" role="progressbar" [attr.aria-valuenow]="e.usadosMes" aria-valuemin="0" [attr.aria-valuemax]="e.plan.limiteComprobantes">
                  <div [style.width.%]="usoPct()"></div>
                </div>
                <span class="mini suave">{{ e.usadosMes }} de {{ e.plan.limiteComprobantes }} comprobantes este mes</span>
              } @else {
                <span class="mini suave">{{ e.usadosMes }} comprobantes este mes · ilimitados</span>
              }
            </div>
          }
        </section>
      </div>
    </div>
  `,
  styles: `
    .grid { display: grid; grid-template-columns: minmax(0, 1fr); gap: 20px; }
    @media (min-width: 900px) { .grid { grid-template-columns: minmax(0, 1.2fr) minmax(0, 1fr); align-items: start; } .movil { display: none; } }
    .campana { position: relative; background: var(--blanco); }
    .punto { position: absolute; top: 9px; right: 10px; width: 9px; height: 9px; border-radius: 50%; background: var(--magenta); border: 2px solid var(--blanco); }
    .ventas { display: flex; flex-direction: column; gap: 6px; }
    .grande { font-size: 34px; font-weight: 700; }
    .botones { gap: 10px; }
    .der { display: flex; flex-direction: column; align-items: flex-end; gap: 4px; }
    .elipsis { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .barras { display: flex; align-items: flex-end; gap: 8px; height: 140px; }
    .barra-col { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 6px; height: 100%; justify-content: flex-end; }
    .barra { width: 100%; max-width: 34px; background: var(--tinta); border-radius: 6px 6px 2px 2px; min-height: 3px; }
    .barra.hoy { background: var(--magenta); }
    .kpi { display: flex; flex-direction: column; gap: 4px; text-decoration: none; }
    .kpis { grid-template-columns: repeat(2, minmax(0, 1fr)) !important; }
    .uso { height: 8px; background: var(--papel); border-radius: 99px; overflow: hidden; }
    .uso div { height: 100%; background: var(--amarillo); border-right: 2px solid var(--tinta); }
  `,
})
export class InicioComponent {
  private api = inject(Api);
  protected auth = inject(Auth);
  protected bienvenida = inject(ActivatedRoute).snapshot.queryParamMap.has('bienvenida');

  protected resumen = signal<Resumen | null>(null);
  protected ultimos = signal<Fila[]>([]);
  protected cargado = signal(false);
  protected pendientes = computed(() => this.resumen()?.pendientesSunat ?? 0);
  protected usoPct = computed(() => {
    const e = this.auth.empresa();
    return e?.plan.limiteComprobantes ? Math.min(100, (e.usadosMes / e.plan.limiteComprobantes) * 100) : 0;
  });
  private maximo = computed(() => Math.max(1, ...(this.resumen()?.dias.map((d) => d.total) ?? [1])));

  constructor() {
    this.api.get<Resumen>('/reportes/resumen').then((r) => this.resumen.set(r)).catch(() => {});
    this.api.get<Pagina<Fila>>('/comprobantes', { tamano: 6 })
      .then((p) => this.ultimos.set(p.content))
      .catch(() => {})
      .finally(() => this.cargado.set(true));
    this.auth.refrescar();
  }

  protected altura(v: number) {
    return Math.max(2, (Math.max(0, v) / this.maximo()) * 100);
  }

  protected diaCorto(iso: string) {
    return ['Do', 'Lu', 'Ma', 'Mi', 'Ju', 'Vi', 'Sá'][new Date(iso + 'T12:00:00').getDay()];
  }

  protected fecha(iso: string) {
    return aFecha(iso);
  }
}
