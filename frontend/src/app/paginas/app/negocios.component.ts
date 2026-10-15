import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Api, mensajeError } from '../../core/api.service';
import { Auth } from '../../core/auth.service';
import { Negocio } from '../../core/modelos';
import { ToastService } from '../../core/toast.service';
import { IconoComponent } from '../../shared/icono.component';

interface Contribuyente {
  ruc: string;
  razonSocial: string;
  direccion?: string;
  departamento?: string;
  provincia?: string;
  distrito?: string;
  ubigeo?: string;
}

/** Negocios de la persona: cambiar entre ellos o sumar otro (ideal para contadores). */
@Component({
  selector: 'at-negocios',
  imports: [FormsModule, IconoComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="pagina">
      <div class="col" style="gap: 4px">
        <h1 class="titulo">Mis negocios</h1>
        <p class="suave">Con una sola cuenta llevas varios RUC. Cada negocio tiene sus series, su caja y su equipo.</p>
      </div>

      <div class="lista">
        @for (n of negocios(); track n.empresaId) {
          <button type="button" class="lista-item" [class.actual]="n.actual" [disabled]="n.actual || ocupado()" (click)="entrar(n)">
            <span class="col" style="gap: 2px; text-align: left">
              <strong>{{ n.nombre }}</strong>
              <span class="mono mini suave">RUC {{ n.ruc }} · {{ rol(n.rol) }}</span>
            </span>
            @if (n.actual) { <span class="chip chip-tinta">Aquí estás</span> } @else { <at-icono nombre="derecha" [tam]="18" /> }
          </button>
        } @empty {
          <div class="esqueleto" style="height: 72px"></div>
        }
      </div>

      <form class="tarjeta col" (ngSubmit)="agregar()">
        <h2 class="subtitulo">Agregar otro negocio</h2>
        <div class="grilla-2">
          <div class="campo">
            <label for="nruc">RUC</label>
            <input id="nruc" name="nruc" class="input mono" inputmode="numeric" maxlength="11" [(ngModel)]="ruc" (ngModelChange)="alEscribirRuc()" />
            @if (buscando()) { <span class="mini suave">Buscando en SUNAT…</span> }
          </div>
          <div class="campo">
            <label for="nrs">Razón social</label>
            <input id="nrs" name="nrs" class="input" [(ngModel)]="razonSocial" />
          </div>
        </div>
        <div class="campo">
          <label for="ndir">Dirección fiscal</label>
          <input id="ndir" name="ndir" class="input" [(ngModel)]="direccion" />
        </div>
        <span class="mini suave">Arranca con 15 días de prueba en SUNAT beta, como un negocio nuevo.</span>
        <button type="submit" class="btn btn-tinta" [disabled]="ocupado()">Agregar y entrar</button>
      </form>
    </div>
  `,
  styles: `
    .lista-item { width: 100%; border: 2px solid transparent; background: var(--blanco); cursor: pointer; font: inherit; color: inherit; }
    .lista-item.actual { border-color: var(--tinta); cursor: default; }
  `,
})
export class NegociosComponent {
  private api = inject(Api);
  private auth = inject(Auth);
  private router = inject(Router);
  private toast = inject(ToastService);
  protected negocios = signal<Negocio[]>([]);
  protected ocupado = signal(false);
  protected buscando = signal(false);
  protected ruc = '';
  protected razonSocial = '';
  protected direccion = '';
  private contrib: Contribuyente | null = null;

  constructor() {
    this.api.get<Negocio[]>('/auth/negocios').then((n) => this.negocios.set(n)).catch(() => {});
  }

  protected rol(r: string) {
    return r === 'PROPIETARIO' ? 'Dueño' : r === 'ADMINISTRADOR' ? 'Administrador' : 'Vendedor';
  }

  protected async entrar(n: Negocio) {
    this.ocupado.set(true);
    try {
      await this.auth.entrar(n.empresaId);
      this.toast.ok(`Ahora estás en ${n.nombre}.`);
      this.router.navigateByUrl('/app');
    } catch (e) {
      this.toast.error(mensajeError(e));
    } finally {
      this.ocupado.set(false);
    }
  }

  protected async alEscribirRuc() {
    const r = this.ruc.replace(/\D/g, '');
    if (r.length !== 11) return;
    this.buscando.set(true);
    try {
      this.contrib = await this.api.get<Contribuyente>(`/consulta/ruc/${r}`);
      this.razonSocial = this.contrib.razonSocial ?? this.razonSocial;
      this.direccion = this.contrib.direccion ?? this.direccion;
    } catch {
      this.contrib = null;
    } finally {
      this.buscando.set(false);
    }
  }

  protected async agregar() {
    const ruc = this.ruc.replace(/\D/g, '');
    if (ruc.length !== 11 || !this.razonSocial.trim()) {
      this.toast.error('Pon el RUC y la razón social, pe.');
      return;
    }
    this.ocupado.set(true);
    try {
      const c = this.contrib;
      await this.auth.agregarNegocio({
        ruc, razonSocial: this.razonSocial.trim(), direccion: this.direccion.trim() || null,
        ubigeo: c?.ubigeo, distrito: c?.distrito, provincia: c?.provincia, departamento: c?.departamento,
      });
      this.toast.ok('¡Listo! Ya estás en tu nuevo negocio.');
      this.router.navigateByUrl('/app');
    } catch (e) {
      this.toast.error(mensajeError(e));
    } finally {
      this.ocupado.set(false);
    }
  }
}
