import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Api, mensajeError } from '../../core/api.service';
import { CatalogosService } from '../../core/catalogos.service';
import { Cliente, Pagina } from '../../core/modelos';
import { ToastService } from '../../core/toast.service';
import { IconoComponent } from '../../shared/icono.component';
import { ModalComponent } from '../../shared/modal.component';

type Form = Omit<Cliente, 'id' | 'createdAt'> & { id?: number };
const VACIO: Form = { tipoDocumento: '1', numeroDocumento: '', nombre: '', direccion: '', email: '', telefono: '' };

@Component({
  selector: 'at-clientes',
  imports: [FormsModule, RouterLink, IconoComponent, ModalComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="pagina">
      <header class="cabecera">
        <div>
          <h1 class="titulo">Tus caseritos</h1>
          <p class="suave chico">Clientes frecuentes con su DNI o RUC listo pa' la próxima venta.</p>
        </div>
        <button type="button" class="btn btn-tinta" (click)="nuevo()"><at-icono nombre="sumar" /> Nuevo cliente</button>
      </header>

      <div class="fila-wrap">
        <div class="buscador crece">
          <at-icono nombre="buscar" />
          <label class="sr-only" for="q">Buscar cliente</label>
          <input id="q" class="input" placeholder="Nombre, DNI o RUC" [(ngModel)]="q" (ngModelChange)="buscarLuego()" autocomplete="off" />
        </div>
        <div class="segmentado" role="group" aria-label="Tipo">
          <button type="button" [attr.aria-pressed]="tipo() === ''" (click)="tipo.set(''); buscar(0)">Todos</button>
          <button type="button" [attr.aria-pressed]="tipo() === '1'" (click)="tipo.set('1'); buscar(0)">Con DNI</button>
          <button type="button" [attr.aria-pressed]="tipo() === '6'" (click)="tipo.set('6'); buscar(0)">Con RUC</button>
        </div>
      </div>

      @if (pagina(); as p) {
        <div class="lista">
          @for (c of p.content; track c.id) {
            <div class="lista-item">
              <span class="avatar" aria-hidden="true">{{ inicial(c.nombre) }}</span>
              <div class="crece">
                <strong>{{ c.nombre }}</strong>
                <div class="mini suave"><span class="mono">{{ cat.nombreDoc(c.tipoDocumento) }} {{ c.numeroDocumento }}</span>{{ c.telefono ? ' · ' + c.telefono : '' }}</div>
              </div>
              <a class="btn btn-chico btn-borde" routerLink="/app/emitir" [queryParams]="{ tipo: c.tipoDocumento === '6' ? '01' : '03' }">Venderle</a>
              <button type="button" class="btn btn-icono btn-fantasma" (click)="editar(c)" [attr.aria-label]="'Editar ' + c.nombre"><at-icono nombre="editar" [tam]="18" /></button>
            </div>
          } @empty {
            <div class="vacio"><strong>Aún no tienes caseritos guardados</strong><span>Se guardan solos cuando emites con DNI o RUC, o agrégalos aquí.</span></div>
          }
        </div>
        @if (p.totalPages > 1) {
          <nav class="entre" aria-label="Paginación">
            <button type="button" class="btn btn-borde btn-chico" [disabled]="p.number === 0" (click)="buscar(p.number - 1)">Anterior</button>
            <span class="chico">{{ p.number + 1 }} / {{ p.totalPages }}</span>
            <button type="button" class="btn btn-borde btn-chico" [disabled]="p.number + 1 >= p.totalPages" (click)="buscar(p.number + 1)">Siguiente</button>
          </nav>
        }
      } @else {
        <div class="esqueleto" style="height: 240px"></div>
      }
    </div>

    <at-modal [abierto]="abierto()" [titulo]="form.id ? 'Editar cliente' : 'Nuevo cliente'" (cerrar)="abierto.set(false)">
      <form class="col" (ngSubmit)="guardar()">
        <div class="doc">
          <div class="campo" style="width: 140px">
            <label for="f-tipo">Documento</label>
            <select id="f-tipo" name="tipo" class="select" [(ngModel)]="form.tipoDocumento">
              <option value="1">DNI</option><option value="6">RUC</option><option value="4">C. extranjería</option><option value="7">Pasaporte</option>
            </select>
          </div>
          <div class="campo crece">
            <label for="f-num">Número</label>
            <input id="f-num" name="num" class="input mono" [(ngModel)]="form.numeroDocumento" (ngModelChange)="autocompletar()" autocomplete="off" />
          </div>
        </div>
        <div class="campo">
          <label for="f-nombre">{{ form.tipoDocumento === '6' ? 'Razón social' : 'Nombre completo' }}</label>
          <input id="f-nombre" name="nombre" class="input" [(ngModel)]="form.nombre" />
        </div>
        <div class="campo">
          <label for="f-dir">Dirección</label>
          <input id="f-dir" name="dir" class="input" [(ngModel)]="form.direccion" />
        </div>
        <div class="grilla-2" style="grid-template-columns: 1fr 1fr">
          <div class="campo">
            <label for="f-tel">Celular</label>
            <input id="f-tel" name="tel" class="input mono" type="tel" [(ngModel)]="form.telefono" />
          </div>
          <div class="campo">
            <label for="f-email">Correo</label>
            <input id="f-email" name="email" class="input" type="email" [(ngModel)]="form.email" />
          </div>
        </div>
        <div class="fila">
          @if (form.id) { <button type="button" class="btn btn-fantasma" style="color: var(--magenta)" (click)="eliminar()"><at-icono nombre="borrar" [tam]="18" /> Eliminar</button> }
          <button type="submit" class="btn btn-tinta crece" [disabled]="guardando()">{{ guardando() ? 'Guardando…' : 'Guardar' }}</button>
        </div>
      </form>
    </at-modal>
  `,
  styles: `
    .buscador { position: relative; display: flex; align-items: center; min-width: 240px; }
    .buscador at-icono { position: absolute; left: 14px; color: var(--gris); }
    .buscador .input { padding-left: 44px; }
    .avatar { width: 40px; height: 40px; border-radius: 12px; background: var(--amarillo); display: flex; align-items: center; justify-content: center;
      font-weight: 900; font-stretch: 80%; flex-shrink: 0; }
    .doc { display: flex; gap: 10px; }
  `,
})
export class ClientesComponent {
  private api = inject(Api);
  private toast = inject(ToastService);
  protected cat = inject(CatalogosService);
  private cdr = inject(ChangeDetectorRef);
  protected q = '';
  protected tipo = signal('');
  protected pagina = signal<Pagina<Cliente> | null>(null);
  protected abierto = signal(false);
  protected guardando = signal(false);
  protected form: Form = { ...VACIO };
  private temporizador?: ReturnType<typeof setTimeout>;

  constructor() {
    this.buscar(0);
  }

  protected buscarLuego() {
    clearTimeout(this.temporizador);
    this.temporizador = setTimeout(() => this.buscar(0), 300);
  }

  protected async buscar(n: number) {
    this.pagina.set(await this.api.get<Pagina<Cliente>>('/clientes', { q: this.q, tipo: this.tipo(), pagina: n, tamano: 25 }).catch(() => null)
      ?? { content: [], totalElements: 0, totalPages: 0, number: 0, size: 25 });
  }

  protected inicial(n: string) {
    return n.trim().charAt(0).toUpperCase();
  }

  protected nuevo() {
    this.form = { ...VACIO };
    this.abierto.set(true);
  }

  protected editar(c: Cliente) {
    this.form = { ...c, direccion: c.direccion ?? '', email: c.email ?? '', telefono: c.telefono ?? '' };
    this.abierto.set(true);
  }

  protected async autocompletar() {
    const n = this.form.numeroDocumento.trim();
    const ruta = this.form.tipoDocumento === '1' && /^\d{8}$/.test(n) ? `/consulta/dni/${n}`
      : this.form.tipoDocumento === '6' && /^\d{11}$/.test(n) ? `/consulta/ruc/${n}` : null;
    if (!ruta || this.form.nombre) return;
    const r = await this.api.get<{ nombre?: string; razonSocial?: string; direccion?: string }>(ruta).catch(() => null);
    if (r) {
      this.form = { ...this.form, nombre: r.nombre ?? r.razonSocial ?? '', direccion: this.form.direccion || r.direccion || '' };
      this.cdr.markForCheck();
    }
  }

  protected async guardar() {
    this.guardando.set(true);
    try {
      if (this.form.id) await this.api.put(`/clientes/${this.form.id}`, this.form);
      else await this.api.post('/clientes', this.form);
      this.toast.ok('¡Caserito guardado!');
      this.abierto.set(false);
      this.buscar(this.pagina()?.number ?? 0);
    } catch (e) {
      this.toast.error(mensajeError(e));
    } finally {
      this.guardando.set(false);
    }
  }

  protected async eliminar() {
    if (!this.form.id || !confirm(`¿Eliminar a ${this.form.nombre} de tus caseritos?`)) return;
    try {
      await this.api.delete(`/clientes/${this.form.id}`);
      this.toast.ok('Cliente eliminado.');
      this.abierto.set(false);
      this.buscar(0);
    } catch (e) {
      this.toast.error(mensajeError(e));
    }
  }
}
