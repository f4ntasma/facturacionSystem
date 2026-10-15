import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Api, mensajeError } from '../../core/api.service';
import { FechaPipe, HoraPipe, SolesPipe, hoyIso } from '../../core/formato';
import { EstadoPago, EstadoSunat, Fila, NOMBRE_TIPO, Pagina } from '../../core/modelos';
import { ToastService } from '../../core/toast.service';
import { EstadoPagoComponent, EstadoSunatComponent } from '../../shared/estados.component';
import { IconoComponent } from '../../shared/icono.component';

@Component({
  selector: 'at-comprobantes',
  imports: [FormsModule, RouterLink, SolesPipe, FechaPipe, HoraPipe, IconoComponent, EstadoSunatComponent, EstadoPagoComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="pagina">
      <header class="cabecera">
        <div>
          <h1 class="titulo">Comprobantes</h1>
          <p class="suave chico">Boletas, facturas y notas, con su estado en SUNAT.</p>
        </div>
        <div class="fila-wrap">
          <button type="button" class="btn btn-borde btn-chico" (click)="exportar()"><at-icono nombre="descargar" [tam]="18" /> Registro de ventas</button>
          <a routerLink="/app/emitir" class="btn btn-tinta btn-chico"><at-icono nombre="sumar" [tam]="18" /> Emitir</a>
        </div>
      </header>

      <div class="filtros tarjeta">
        <div class="segmentado tipos" role="group" aria-label="Tipo">
          @for (t of tipos; track t.valor) {
            <button type="button" [attr.aria-pressed]="tipo() === t.valor" (click)="tipo.set(t.valor); buscar(0)">{{ t.texto }}</button>
          }
        </div>
        <div class="campos">
          <div class="campo crece">
            <label for="q" class="sr-only">Buscar</label>
            <input id="q" class="input" placeholder="Cliente, DNI/RUC o número (B001-12)" [(ngModel)]="q" (keyup.enter)="buscar(0)" />
          </div>
          <div class="campo">
            <label for="desde" class="sr-only">Desde</label>
            <input id="desde" class="input" type="date" [(ngModel)]="desde" (change)="buscar(0)" />
          </div>
          <div class="campo">
            <label for="hasta" class="sr-only">Hasta</label>
            <input id="hasta" class="input" type="date" [(ngModel)]="hasta" (change)="buscar(0)" />
          </div>
          <div class="campo">
            <label for="estado" class="sr-only">Estado SUNAT</label>
            <select id="estado" class="select" [(ngModel)]="estado" (change)="buscar(0)">
              <option value="">Todo estado SUNAT</option>
              <option value="ACEPTADO">Aceptados</option>
              <option value="OBSERVADO">Con observaciones</option>
              <option value="PENDIENTE">Pendientes</option>
              <option value="RECHAZADO">Rechazados</option>
              <option value="ANULADO">Anulados</option>
            </select>
          </div>
          <div class="campo">
            <label for="pago" class="sr-only">Cobro</label>
            <select id="pago" class="select" [(ngModel)]="estadoPago" (change)="buscar(0)">
              <option value="">Todo cobro</option>
              <option value="PAGADO">Pagados</option>
              <option value="PARCIAL">Pago parcial</option>
              <option value="POR_COBRAR">Por cobrar</option>
            </select>
          </div>
          <button type="button" class="btn btn-tinta" (click)="buscar(0)"><at-icono nombre="buscar" [tam]="18" /> Buscar</button>
        </div>
      </div>

      @if (pagina(); as p) {
        <div class="tabla-caja escritorio">
          <table class="tabla">
            <thead><tr><th>Número</th><th>Fecha</th><th>Cliente</th><th class="derecha">Total</th><th>SUNAT</th><th>Cobro</th></tr></thead>
            <tbody>
              @for (f of p.content; track f.id) {
                <tr class="clic" (click)="abrir(f)" tabindex="0" (keyup.enter)="abrir(f)">
                  <td><span class="mono" style="font-weight: 700">{{ f.numero }}</span><br /><span class="mini suave">{{ nombreTipo(f) }}</span></td>
                  <td class="nowrap">{{ f.fechaEmision | fecha }}<br /><span class="mini suave">{{ f.horaEmision | hora }}</span></td>
                  <td>{{ f.clienteNombre }}<br /><span class="mini suave mono">{{ f.clienteNumDoc !== '-' ? f.clienteNumDoc : '' }}</span></td>
                  <td class="derecha monto" [class.neg]="f.tipo === '07'">{{ f.tipo === '07' ? '−' : '' }}{{ f.total | soles: f.moneda }}</td>
                  <td><at-estado-sunat [estado]="f.estadoSunat" /></td>
                  <td>@if (f.tipo !== '07') { <at-estado-pago [estado]="f.estadoPago" /> }</td>
                </tr>
              }
            </tbody>
          </table>
        </div>
        <div class="lista movil">
          @for (f of p.content; track f.id) {
            <a class="lista-item" [routerLink]="['/app/comprobantes', f.id]">
              <div class="crece">
                <div class="mono chico" style="font-weight: 700">{{ f.numero }}</div>
                <div class="mini suave elipsis">{{ f.clienteNombre }} · {{ f.fechaEmision | fecha }}</div>
              </div>
              <div class="der">
                <span class="monto chico">{{ f.tipo === '07' ? '−' : '' }}{{ f.total | soles: f.moneda }}</span>
                @if (f.estadoSunat !== 'ACEPTADO' && f.estadoSunat !== 'OBSERVADO') { <at-estado-sunat [estado]="f.estadoSunat" /> }
                @else if (f.tipo !== '07') { <at-estado-pago [estado]="f.estadoPago" /> }
              </div>
            </a>
          }
        </div>
        @if (!p.content.length) {
          <div class="vacio tarjeta"><strong>No hay comprobantes con esos filtros</strong><span>Prueba con otras fechas o quita filtros.</span></div>
        }
        @if (p.totalPages > 1) {
          <nav class="entre paginacion" aria-label="Paginación">
            <button type="button" class="btn btn-borde btn-chico" [disabled]="p.number === 0" (click)="buscar(p.number - 1)"><at-icono nombre="izquierda" [tam]="18" /> Anterior</button>
            <span class="chico">Página {{ p.number + 1 }} de {{ p.totalPages }} · {{ p.totalElements }} comprobantes</span>
            <button type="button" class="btn btn-borde btn-chico" [disabled]="p.number + 1 >= p.totalPages" (click)="buscar(p.number + 1)">Siguiente <at-icono nombre="derecha" [tam]="18" /></button>
          </nav>
        }
      } @else {
        <div class="col">@for (i of [1, 2, 3, 4]; track i) { <div class="esqueleto" style="height: 56px"></div> }</div>
      }
    </div>
  `,
  styles: `
    .filtros { display: flex; flex-direction: column; gap: 12px; }
    .tipos { align-self: flex-start; max-width: 100%; overflow-x: auto; }
    .campos { display: flex; gap: 10px; flex-wrap: wrap; align-items: center; }
    .campos .campo:not(.crece) { width: 168px; }
    .crece { min-width: 220px; }
    .neg { color: var(--magenta); }
    .der { display: flex; flex-direction: column; align-items: flex-end; gap: 4px; }
    .elipsis { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 60vw; }
    .movil { display: none; }
    @media (max-width: 760px) {
      .escritorio { display: none; }
      .movil { display: block; }
      .campos .campo:not(.crece) { width: calc(50% - 5px); }
      .campos .btn { width: 100%; }
      .paginacion { flex-wrap: wrap; justify-content: center; }
    }
  `,
})
export class ComprobantesComponent {
  private api = inject(Api);
  private router = inject(Router);
  private toast = inject(ToastService);

  protected readonly tipos = [
    { valor: '', texto: 'Todos' },
    { valor: '03', texto: 'Boletas' },
    { valor: '01', texto: 'Facturas' },
    { valor: '07,08', texto: 'Notas' },
  ];
  protected tipo = signal('');
  protected q = '';
  protected desde = '';
  protected hasta = '';
  protected estado: EstadoSunat | '' = '';
  protected estadoPago: EstadoPago | '' = '';
  protected pagina = signal<Pagina<Fila> | null>(null);

  constructor() {
    const qp = inject(ActivatedRoute).snapshot.queryParamMap;
    this.estado = (qp.get('estado') as EstadoSunat) ?? '';
    this.estadoPago = (qp.get('estadoPago') as EstadoPago) ?? '';
    this.buscar(0);
  }

  protected async buscar(n: number) {
    try {
      this.pagina.set(await this.api.get<Pagina<Fila>>('/comprobantes', {
        tipo: this.tipo(), q: this.q.trim(), desde: this.desde, hasta: this.hasta, estado: this.estado, estadoPago: this.estadoPago, pagina: n, tamano: 20,
      }));
    } catch (e) {
      this.toast.error(mensajeError(e));
      this.pagina.set({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 20 });
    }
  }

  protected abrir(f: Fila) {
    this.router.navigate(['/app/comprobantes', f.id]);
  }

  protected nombreTipo(f: Fila) {
    return NOMBRE_TIPO[f.tipo];
  }

  protected async exportar() {
    const hasta = this.hasta || hoyIso();
    const desde = this.desde || hasta.substring(0, 8) + '01';
    try {
      await this.api.descargar('/comprobantes/exportar', { desde, hasta });
      this.toast.ok('Registro de ventas descargado.');
    } catch (e) {
      this.toast.error(mensajeError(e));
    }
  }
}
