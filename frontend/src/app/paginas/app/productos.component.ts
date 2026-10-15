import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Api, mensajeError } from '../../core/api.service';
import { Auth } from '../../core/auth.service';
import { CatalogosService } from '../../core/catalogos.service';
import { CantidadPipe, FechaPipe, SolesPipe } from '../../core/formato';
import { Movimiento, Pagina, Producto } from '../../core/modelos';
import { ToastService } from '../../core/toast.service';
import { IconoComponent } from '../../shared/icono.component';
import { ModalComponent } from '../../shared/modal.component';

interface Form {
  id?: number;
  codigo: string;
  nombre: string;
  descripcion: string;
  unidadMedida: string;
  precio: number | null;
  tipoAfectacion: string;
  aplicaIcbper: boolean;
  controlaStock: boolean;
  stock: number;
  favorito: boolean;
  activo: boolean;
  categoria: string;
}

const VACIO: Form = { codigo: '', nombre: '', descripcion: '', unidadMedida: 'NIU', precio: null, tipoAfectacion: '10', aplicaIcbper: false,
  controlaStock: false, stock: 0, favorito: false, activo: true, categoria: '' };

@Component({
  selector: 'at-productos',
  imports: [FormsModule, SolesPipe, CantidadPipe, FechaPipe, IconoComponent, ModalComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="pagina">
      <header class="cabecera">
        <div>
          <h1 class="titulo">Productos</h1>
          <p class="suave chico">Tu gaseosa, tu pan, tus servicios: con precio y todo, listos pa' vender.</p>
        </div>
        @if (auth.esAdmin()) { <button type="button" class="btn btn-tinta" (click)="nuevo()"><at-icono nombre="sumar" /> Nuevo producto</button> }
      </header>

      <div class="fila-wrap">
        <div class="buscador crece">
          <at-icono nombre="buscar" />
          <label class="sr-only" for="q">Buscar producto</label>
          <input id="q" class="input" placeholder="Nombre o código" [ngModel]="q()" (ngModelChange)="q.set($event)" autocomplete="off" />
        </div>
        <label class="check chico"><input type="checkbox" [ngModel]="inactivos()" (ngModelChange)="inactivos.set($event)" /> Ver desactivados</label>
      </div>

      @if (cargando()) {
        <div class="esqueleto" style="height: 300px"></div>
      } @else {
        <div class="tabla-caja">
          <table class="tabla">
            <thead><tr><th></th><th>Producto</th><th>Categoría</th><th class="derecha">Precio</th><th class="derecha">Stock</th><th></th></tr></thead>
            <tbody>
              @for (p of filtrados(); track p.id) {
                <tr [style.opacity]="p.activo ? 1 : .5">
                  <td style="width: 44px">
                    <button type="button" class="btn btn-icono btn-fantasma estrella" [class.on]="p.favorito" (click)="favorito(p)"
                      [attr.aria-label]="p.favorito ? 'Quitar de favoritos' : 'Marcar como favorito'" [attr.aria-pressed]="p.favorito">
                      <at-icono nombre="estrella" [tam]="18" />
                    </button>
                  </td>
                  <td><strong>{{ p.nombre }}</strong><br /><span class="mini suave mono">{{ p.codigo }} · {{ unidad(p.unidadMedida) }}{{ p.tipoAfectacion === '20' ? ' · exonerado' : p.tipoAfectacion === '30' ? ' · inafecto' : '' }}{{ p.aplicaIcbper ? ' · ICBPER' : '' }}</span></td>
                  <td>{{ p.categoria || '—' }}</td>
                  <td class="derecha monto">{{ p.precio | soles }}</td>
                  <td class="derecha mono" [style.color]="p.controlaStock && p.stock <= 5 ? 'var(--magenta)' : ''">
                    @if (p.controlaStock) {
                      <button type="button" class="enlace mono" (click)="verKardex(p)" [attr.aria-label]="'Kardex de ' + p.nombre">{{ p.stock | cant }}</button>
                    } @else { — }
                  </td>
                  <td class="derecha">@if (auth.esAdmin()) { <button type="button" class="btn btn-icono btn-fantasma" (click)="editar(p)" [attr.aria-label]="'Editar ' + p.nombre"><at-icono nombre="editar" [tam]="18" /></button> }</td>
                </tr>
              } @empty {
                <tr><td colspan="6"><div class="vacio"><strong>Sin productos todavía</strong><span>Carga lo que más vendes y márcalo con estrella para tenerlo a la mano.</span></div></td></tr>
              }
            </tbody>
          </table>
        </div>
      }
    </div>

    <at-modal [abierto]="!!kardexDe()" [titulo]="'Kardex · ' + (kardexDe()?.nombre ?? '')" (cerrar)="kardexDe.set(null)">
      @if (kardexDe(); as p) {
        <div class="col" style="gap: 14px">
          <div class="entre"><span>Stock actual</span><strong class="mono" style="font-size: 22px">{{ p.stock | cant }}</strong></div>
          @if (auth.esAdmin()) {
            <form class="fila-wrap" (ngSubmit)="ingresar(p)">
              <input name="ing-cant" class="input mono" style="max-width: 110px" type="number" min="0.001" step="any" placeholder="Cantidad" [(ngModel)]="ingreso.cantidad" />
              <input name="ing-nota" class="input" style="flex: 1; min-width: 140px" placeholder="Nota (ej.: compra al mayorista)" [(ngModel)]="ingreso.nota" />
              <button type="submit" class="btn btn-tinta btn-chico" [disabled]="guardando()">+ Ingreso</button>
            </form>
          }
          <div class="tabla-caja">
            <table class="tabla">
              <thead><tr><th>Fecha</th><th>Movimiento</th><th class="derecha">Cant.</th><th class="derecha">Saldo</th></tr></thead>
              <tbody>
                @for (m of kardex(); track m.id) {
                  <tr>
                    <td class="mini">{{ m.fecha | fecha }}</td>
                    <td>{{ nombreMovimiento[m.tipo] }}@if (m.nota) { <br /><span class="mini suave">{{ m.nota }}</span> }</td>
                    <td class="derecha mono" [style.color]="m.cantidad < 0 ? 'var(--magenta)' : 'var(--verde)'">{{ m.cantidad > 0 ? '+' : '' }}{{ m.cantidad | cant }}</td>
                    <td class="derecha mono">{{ m.stockResultante | cant }}</td>
                  </tr>
                } @empty {
                  <tr><td colspan="4" class="suave">Sin movimientos todavía.</td></tr>
                }
              </tbody>
            </table>
          </div>
        </div>
      }
    </at-modal>

    <at-modal [abierto]="abierto()" [titulo]="form.id ? 'Editar producto' : 'Nuevo producto'" (cerrar)="abierto.set(false)">
      <form class="col" (ngSubmit)="guardar()">
        <div class="campo">
          <label for="p-nombre">Nombre</label>
          <input id="p-nombre" name="nombre" class="input" [(ngModel)]="form.nombre" placeholder="Ej.: Gaseosa 1 L" />
        </div>
        <div class="grilla-2" style="grid-template-columns: 1fr 1fr">
          <div class="campo">
            <label for="p-precio">Precio de venta (con IGV)</label>
            <input id="p-precio" name="precio" class="input mono" type="number" step="0.01" min="0.01" [(ngModel)]="form.precio" />
          </div>
          <div class="campo">
            <label for="p-codigo">Código <span class="suave">(opcional)</span></label>
            <input id="p-codigo" name="codigo" class="input mono" [(ngModel)]="form.codigo" placeholder="Automático" />
          </div>
        </div>
        <div class="grilla-2" style="grid-template-columns: 1fr 1fr">
          <div class="campo">
            <label for="p-und">Unidad</label>
            <select id="p-und" name="und" class="select" [(ngModel)]="form.unidadMedida">
              @for (u of cat.lista('unidades'); track u.codigo) { <option [value]="u.codigo">{{ u.nombre }}</option> }
            </select>
          </div>
          <div class="campo">
            <label for="p-afe">IGV</label>
            <select id="p-afe" name="afe" class="select" [(ngModel)]="form.tipoAfectacion">
              @for (a of cat.lista('afectaciones'); track a.codigo) { <option [value]="a.codigo">{{ a.nombre }}</option> }
            </select>
          </div>
        </div>
        <div class="campo">
          <label for="p-cat">Categoría <span class="suave">(opcional)</span></label>
          <input id="p-cat" name="cat" class="input" list="categorias" [(ngModel)]="form.categoria" placeholder="Ej.: Bebidas" />
          <datalist id="categorias">@for (c of categorias(); track c) { <option [value]="c"></option> }</datalist>
        </div>
        <label class="check"><input type="checkbox" name="stock-on" [(ngModel)]="form.controlaStock" /> Controlar stock</label>
        @if (form.controlaStock) {
          <div class="campo">
            <label for="p-stock">Stock actual</label>
            <input id="p-stock" name="stock" class="input mono" type="number" step="any" [(ngModel)]="form.stock" />
          </div>
        }
        <label class="check"><input type="checkbox" name="icbper" [(ngModel)]="form.aplicaIcbper" /> Es bolsa plástica (cobra ICBPER S/ 0.50)</label>
        <label class="check"><input type="checkbox" name="fav" [(ngModel)]="form.favorito" /> Favorito (aparece primero al vender)</label>
        @if (form.id) { <label class="check"><input type="checkbox" name="activo" [(ngModel)]="form.activo" /> Activo</label> }
        <button type="submit" class="btn btn-tinta btn-bloque" [disabled]="guardando()">{{ guardando() ? 'Guardando…' : 'Guardar producto' }}</button>
      </form>
    </at-modal>
  `,
  styles: `
    .buscador { position: relative; display: flex; align-items: center; min-width: 240px; }
    .buscador at-icono { position: absolute; left: 14px; color: var(--gris); }
    .buscador .input { padding-left: 44px; }
    .estrella { color: var(--linea-fuerte); }
    .estrella.on { color: #E0A800; }
  `,
})
export class ProductosComponent {
  private api = inject(Api);
  private toast = inject(ToastService);
  protected auth = inject(Auth);
  protected cat = inject(CatalogosService);

  protected productos = signal<Producto[]>([]);
  protected cargando = signal(true);
  protected q = signal('');
  protected inactivos = signal(false);
  protected abierto = signal(false);
  protected guardando = signal(false);
  protected form: Form = { ...VACIO };
  protected kardexDe = signal<Producto | null>(null);
  protected kardex = signal<Movimiento[]>([]);
  protected ingreso = { cantidad: null as number | null, nota: '' };
  protected readonly nombreMovimiento: Record<Movimiento['tipo'], string> = {
    VENTA: 'Venta', DEVOLUCION: 'Devolución (nota de crédito)', ANULACION: 'Anulación o rechazo', AJUSTE: 'Ajuste de conteo', INGRESO: 'Ingreso',
  };
  protected categorias = computed(() => [...new Set(this.productos().map((p) => p.categoria).filter((c): c is string => !!c))]);
  protected filtrados = computed(() => {
    const q = this.q().trim().toLowerCase();
    return this.productos().filter((p) => (this.inactivos() || p.activo) && (!q || p.nombre.toLowerCase().includes(q) || p.codigo.toLowerCase().startsWith(q)));
  });

  constructor() {
    this.cargar();
  }

  protected async cargar() {
    try {
      this.productos.set((await this.api.get<Pagina<Producto>>('/productos', { tamano: 200 })).content);
    } catch (e) {
      this.toast.error(mensajeError(e));
    } finally {
      this.cargando.set(false);
    }
  }

  protected unidad(c: string) {
    return this.cat.datos().unidades[c] ?? c;
  }

  protected nuevo() {
    this.form = { ...VACIO };
    this.abierto.set(true);
  }

  protected editar(p: Producto) {
    this.form = { ...VACIO, ...p, descripcion: p.descripcion ?? '', categoria: p.categoria ?? '' };
    this.abierto.set(true);
  }

  protected async verKardex(p: Producto) {
    this.kardexDe.set(p);
    this.kardex.set([]);
    this.kardex.set(await this.api.get<Movimiento[]>(`/productos/${p.id}/kardex`).catch(() => []));
  }

  protected async ingresar(p: Producto) {
    if (!this.ingreso.cantidad || this.ingreso.cantidad <= 0) {
      this.toast.error('Pon cuánto ingresa.');
      return;
    }
    this.guardando.set(true);
    try {
      const r = await this.api.post<Producto>(`/productos/${p.id}/ingresos`, { cantidad: this.ingreso.cantidad, nota: this.ingreso.nota || null });
      this.productos.update((l) => l.map((x) => (x.id === r.id ? r : x)));
      this.ingreso = { cantidad: null, nota: '' };
      this.toast.ok('Ingreso registrado.');
      await this.verKardex(r);
    } catch (e) {
      this.toast.error(mensajeError(e));
    } finally {
      this.guardando.set(false);
    }
  }

  protected async favorito(p: Producto) {
    try {
      const r = await this.api.patch<Producto>(`/productos/${p.id}/favorito`, { valor: !p.favorito });
      this.productos.update((l) => l.map((x) => (x.id === r.id ? r : x)));
    } catch (e) {
      this.toast.error(mensajeError(e));
    }
  }

  protected async guardar() {
    this.guardando.set(true);
    try {
      const cuerpo = { ...this.form, codigo: this.form.codigo || null };
      const r = this.form.id ? await this.api.put<Producto>(`/productos/${this.form.id}`, cuerpo) : await this.api.post<Producto>('/productos', cuerpo);
      this.productos.update((l) => (this.form.id ? l.map((x) => (x.id === r.id ? r : x)) : [r, ...l]));
      this.toast.ok('¡Producto listo pa\' vender!');
      this.abierto.set(false);
    } catch (e) {
      this.toast.error(mensajeError(e));
    } finally {
      this.guardando.set(false);
    }
  }
}
