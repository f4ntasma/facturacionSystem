import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Api, mensajeError } from '../../core/api.service';
import { SolesPipe } from '../../core/formato';
import { CajaVista, NOMBRE_METODO } from '../../core/modelos';
import { ToastService } from '../../core/toast.service';
import { IconoComponent } from '../../shared/icono.component';

@Component({
  selector: 'at-caja',
  imports: [FormsModule, SolesPipe, IconoComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="pagina" style="max-width: 900px">
      <header>
        <h1 class="titulo">Caja del día</h1>
        <p class="suave chico">Abre con tu sencillo, vende, y al cerrar te decimos si cuadra.</p>
      </header>

      @if (cargando()) {
        <div class="esqueleto" style="height: 220px"></div>
      } @else if (actual(); as c) {
        <div class="tarjeta-amarilla col">
          <div class="entre"><strong>Caja abierta</strong><span class="mini mono">{{ c.abiertaAt.substring(11, 16) }} · {{ c.usuario }}</span></div>
          <span class="mono" style="font-size: 34px; font-weight: 700">{{ c.montoEsperado | soles }}</span>
          <span class="chico">Efectivo que debería haber: {{ c.montoApertura | soles }} de apertura + {{ c.efectivoCobrado | soles }} cobrado.</span>
        </div>
        <div class="tarjeta col">
          <h2 class="subtitulo">Cobrado en este turno · {{ c.totalCobrado | soles }}</h2>
          @for (m of c.porMetodo; track m.metodo) {
            <div class="entre"><span>{{ nombre[m.metodo] }} <span class="suave">({{ m.cantidad }})</span></span><span class="mono">{{ m.total | soles }}</span></div>
          } @empty { <p class="suave chico">Aún no hay cobros en este turno.</p> }
        </div>
        <form class="tarjeta col" (ngSubmit)="cerrar()">
          <h2 class="subtitulo">Cerrar caja</h2>
          <div class="campo">
            <label for="contado">¿Cuánto efectivo contaste?</label>
            <input id="contado" name="contado" class="input mono" type="number" step="0.10" min="0" [(ngModel)]="declarado" />
          </div>
          <div class="campo">
            <label for="notas">Notas <span class="suave">(opcional)</span></label>
            <input id="notas" name="notas" class="input" maxlength="250" [(ngModel)]="notas" />
          </div>
          <button type="submit" class="btn btn-tinta" [disabled]="ocupado()">Cerrar y cuadrar</button>
        </form>
      } @else {
        @if (cierre(); as k) {
          <div class="aviso" [class.aviso-verde]="k.diferencia === 0" [class.aviso-magenta]="(k.diferencia ?? 0) < 0" [class.aviso-amarillo]="(k.diferencia ?? 0) > 0">
            <at-icono [nombre]="k.diferencia === 0 ? 'check' : 'alerta'" />
            <div>
              @if (k.diferencia === 0) { <strong>¡Cuadró al céntimo, caserito!</strong> }
              @else if ((k.diferencia ?? 0) < 0) { <strong>Faltan {{ -(k.diferencia ?? 0) | soles }}.</strong> }
              @else { <strong>Sobran {{ k.diferencia | soles }}.</strong> }
              Esperado {{ k.montoEsperado | soles }} · contado {{ k.montoDeclarado | soles }}.
            </div>
          </div>
        }
        <form class="tarjeta col" (ngSubmit)="abrir()">
          <h2 class="subtitulo">Abrir caja</h2>
          <div class="campo">
            <label for="apertura">Sencillo con el que empiezas</label>
            <input id="apertura" name="apertura" class="input mono" type="number" step="0.10" min="0" [(ngModel)]="apertura" />
          </div>
          <button type="submit" class="btn btn-tinta" [disabled]="ocupado()"><at-icono nombre="dinero" /> Abrir caja</button>
        </form>
      }

      @if (historial().length) {
        <section class="col">
          <h2 class="subtitulo">Cierres anteriores</h2>
          <div class="tabla-caja">
            <table class="tabla">
              <thead><tr><th>Apertura</th><th>Usuario</th><th class="derecha">Esperado</th><th class="derecha">Contado</th><th class="derecha">Diferencia</th></tr></thead>
              <tbody>
                @for (h of historial(); track h.id) {
                  @if (h.estado === 'CERRADA') {
                    <tr>
                      <td class="mono chico">{{ h.abiertaAt.substring(0, 16).replace('T', ' ') }}</td><td>{{ h.usuario }}</td>
                      <td class="derecha mono">{{ h.montoEsperado | soles }}</td><td class="derecha mono">{{ h.montoDeclarado | soles }}</td>
                      <td class="derecha mono" [style.color]="(h.diferencia ?? 0) < 0 ? 'var(--magenta)' : (h.diferencia ?? 0) > 0 ? '#8a6a00' : 'var(--verde)'">{{ h.diferencia | soles }}</td>
                    </tr>
                  }
                }
              </tbody>
            </table>
          </div>
        </section>
      }
    </div>
  `,
})
export class CajaComponent {
  private api = inject(Api);
  private toast = inject(ToastService);
  protected readonly nombre = NOMBRE_METODO;
  protected actual = signal<CajaVista | null>(null);
  protected cierre = signal<CajaVista | null>(null);
  protected historial = signal<CajaVista[]>([]);
  protected cargando = signal(true);
  protected ocupado = signal(false);
  protected apertura = 0;
  protected declarado: number | null = null;
  protected notas = '';

  constructor() {
    this.cargar();
  }

  private async cargar() {
    try {
      const [a, h] = await Promise.all([this.api.get<CajaVista | null>('/caja/actual'), this.api.get<CajaVista[]>('/caja/historial')]);
      this.actual.set(a);
      this.historial.set(h);
    } catch (e) {
      this.toast.error(mensajeError(e));
    } finally {
      this.cargando.set(false);
    }
  }

  protected async abrir() {
    this.ocupado.set(true);
    try {
      this.actual.set(await this.api.post<CajaVista>('/caja/abrir', { montoApertura: this.apertura || 0 }));
      this.cierre.set(null);
      this.toast.ok('Caja abierta. ¡A vender!');
    } catch (e) {
      this.toast.error(mensajeError(e));
    } finally {
      this.ocupado.set(false);
    }
  }

  protected async cerrar() {
    if (this.declarado === null || this.declarado < 0) return this.toast.error('Escribe cuánto efectivo contaste.');
    this.ocupado.set(true);
    try {
      this.cierre.set(await this.api.post<CajaVista>('/caja/cerrar', { montoDeclarado: this.declarado, notas: this.notas }));
      this.actual.set(null);
      this.declarado = null;
      this.notas = '';
      this.historial.set(await this.api.get<CajaVista[]>('/caja/historial'));
    } catch (e) {
      this.toast.error(mensajeError(e));
    } finally {
      this.ocupado.set(false);
    }
  }
}
