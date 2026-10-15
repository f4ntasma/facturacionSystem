import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Api, mensajeError } from '../../core/api.service';
import { Auth } from '../../core/auth.service';
import { FechaPipe, SolesPipe } from '../../core/formato';
import { Ambiente, NOMBRE_TIPO, PlanPublico, Rol, TipoComprobante } from '../../core/modelos';
import { ToastService } from '../../core/toast.service';
import { IconoComponent } from '../../shared/icono.component';
import { ModalComponent } from '../../shared/modal.component';

type Tab = 'negocio' | 'sunat' | 'series' | 'equipo' | 'plan' | 'cuenta';

interface Empresa {
  ruc: string; razonSocial: string; nombreComercial?: string; direccionFiscal: string; ubigeo: string; departamento: string;
  provincia: string; distrito: string; urbanizacion?: string; telefono?: string; email?: string; mensajeTicket?: string;
  ambienteSunat: Ambiente; solUsuario?: string; solConfigurado: boolean;
}
interface Certificado { sujeto: string; emisor: string; validoDesde: string; validoHasta: string; autogenerado: boolean }
interface Serie { id: number; tipo: TipoComprobante; serie: string; correlativoActual: number; activo: boolean; establecimientoId: number; establecimiento: string }
interface Local { id: number; codigoSunat: string; nombre: string; direccion?: string; ubigeo?: string; activo: boolean }
interface Usuario { id: number; nombre: string; email: string; rol: Rol; activo: boolean; establecimientoId?: number; establecimiento?: string; ultimoAcceso?: string }
interface Cuenta { estado: string; pruebaHasta?: string; plan: PlanPublico; historial: { id: number; plan: string; periodoInicio: string; periodoFin: string; monto: number; estado: string }[]; pagosEnLinea: boolean }

@Component({
  selector: 'at-ajustes',
  imports: [FormsModule, IconoComponent, ModalComponent, FechaPipe, SolesPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './ajustes.component.html',
  styles: `
    .tabs { display: flex; gap: 6px; overflow-x: auto; padding-bottom: 4px; scrollbar-width: none; }
    .tabs button { border: 0; background: var(--blanco); border-radius: 12px; padding: 10px 16px; font-weight: 800; cursor: pointer; white-space: nowrap; min-height: 44px; color: var(--gris); }
    .tabs button[aria-selected="true"] { background: var(--tinta); color: var(--amarillo); }
    .ambientes { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 10px; }
    .amb { border: 2px solid var(--linea-fuerte); background: var(--blanco); border-radius: 14px; padding: 14px; text-align: left; cursor: pointer; display: flex; flex-direction: column; gap: 4px; }
    .amb[aria-pressed="true"] { border-color: var(--tinta); background: var(--amarillo); }
    .planes { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; }
    .plan { background: var(--blanco); border-radius: 18px; padding: 18px; display: flex; flex-direction: column; gap: 8px; border: 2px solid transparent; }
    .plan.actual { border-color: var(--tinta); }
    .plan.destacado { background: var(--amarillo); }
    @media (max-width: 760px) { .ambientes, .planes { grid-template-columns: minmax(0, 1fr); } }
  `,
})
export class AjustesComponent {
  private api = inject(Api);
  private toast = inject(ToastService);
  private router = inject(Router);
  protected auth = inject(Auth);

  protected tab = signal<Tab>((inject(ActivatedRoute).snapshot.queryParamMap.get('tab') as Tab) || 'negocio');
  protected readonly tabs: { v: Tab; t: string; admin?: boolean }[] = [
    { v: 'negocio', t: 'Mi negocio' }, { v: 'sunat', t: 'SUNAT' }, { v: 'series', t: 'Series y locales' },
    { v: 'equipo', t: 'Equipo', admin: true }, { v: 'plan', t: 'Plan' }, { v: 'cuenta', t: 'Mi cuenta' },
  ];
  protected readonly nombreTipo = NOMBRE_TIPO;
  protected ocupado = signal(false);

  protected empresa = signal<Empresa | null>(null);
  protected certificado = signal<Certificado | null>(null);
  protected series = signal<Serie[]>([]);
  protected locales = signal<Local[]>([]);
  protected usuarios = signal<Usuario[]>([]);
  protected cuenta = signal<Cuenta | null>(null);
  protected planes = signal<PlanPublico[]>([]);

  // formularios
  protected ambiente = signal<Ambiente>('BETA');
  protected solUsuario = '';
  protected solClave = '';
  protected certArchivo: File | null = null;
  protected certClave = '';
  protected serieForm = { tipo: '03' as TipoComprobante, serie: '', establecimientoId: 0, correlativoInicial: 0 };
  protected serieAbierta = signal(false);
  protected localForm: Partial<Local> & { codigoSunat: string; nombre: string } = { codigoSunat: '', nombre: '' };
  protected localAbierto = signal(false);
  protected usuarioForm: Partial<Usuario> & { password?: string } = {};
  protected usuarioAbierto = signal(false);
  protected clave = { actual: '', nueva: '' };
  protected seriesPorTipo = computed(() => {
    const g = new Map<TipoComprobante, Serie[]>();
    for (const s of this.series()) g.set(s.tipo, [...(g.get(s.tipo) ?? []), s]);
    return [...g.entries()];
  });

  constructor() {
    this.cargar();
  }

  protected irA(t: Tab) {
    this.tab.set(t);
    this.router.navigate([], { queryParams: { tab: t }, replaceUrl: true });
  }

  private async cargar() {
    const [e, c, s, l, cu, p] = await Promise.all([
      this.api.get<Empresa>('/empresa').catch(() => null),
      this.api.get<Certificado | null>('/empresa/certificado').catch(() => null),
      this.api.get<Serie[]>('/series').catch(() => []),
      this.api.get<Local[]>('/establecimientos').catch(() => []),
      this.api.get<Cuenta>('/suscripcion').catch(() => null),
      this.api.get<PlanPublico[]>('/planes').catch(() => []),
    ]);
    this.empresa.set(e);
    if (e) {
      this.ambiente.set(e.ambienteSunat);
      this.solUsuario = e.solUsuario ?? '';
    }
    this.certificado.set(c);
    this.series.set(s);
    this.locales.set(l);
    this.cuenta.set(cu);
    this.planes.set(p);
    if (this.auth.esAdmin()) this.api.get<Usuario[]>('/usuarios').then((u) => this.usuarios.set(u)).catch(() => {});
  }

  private async correr<T>(fn: () => Promise<T>, ok: string): Promise<T | null> {
    this.ocupado.set(true);
    try {
      const r = await fn();
      this.toast.ok(ok);
      return r;
    } catch (e) {
      this.toast.error(mensajeError(e));
      return null;
    } finally {
      this.ocupado.set(false);
    }
  }

  // negocio
  protected async guardarNegocio() {
    const e = this.empresa()!;
    const r = await this.correr(() => this.api.put<Empresa>('/empresa', e), '¡Datos del negocio guardados!');
    if (r) {
      this.empresa.set(r);
      this.auth.refrescar();
    }
  }

  // sunat
  protected async guardarSunat() {
    const r = await this.correr(() => this.api.put<Empresa>('/empresa/sunat', { ambiente: this.ambiente(), solUsuario: this.solUsuario, solClave: this.solClave }),
      this.ambiente() === 'PRODUCCION' ? '¡Ya emites en producción, causa!' : 'Configuración SUNAT guardada.');
    if (r) {
      this.empresa.set(r);
      this.solClave = '';
      this.auth.refrescar();
    } else if (this.empresa()) {
      this.ambiente.set(this.empresa()!.ambienteSunat);
    }
  }

  protected elegirArchivo(ev: Event) {
    this.certArchivo = (ev.target as HTMLInputElement).files?.[0] ?? null;
  }

  protected async subirCertificado() {
    if (!this.certArchivo || !this.certClave) return this.toast.error('Elige tu archivo .pfx y escribe su clave.');
    const fd = new FormData();
    fd.append('archivo', this.certArchivo);
    fd.append('clave', this.certClave);
    const r = await this.correr(() => this.api.subir<Certificado>('/empresa/certificado', fd), '¡Certificado cargado!');
    if (r) {
      this.certificado.set(r);
      this.certClave = '';
      this.auth.refrescar();
    }
  }

  // series y locales
  protected nuevaSerie() {
    this.serieForm = { tipo: '03', serie: '', establecimientoId: this.locales()[0]?.id ?? 0, correlativoInicial: 0 };
    this.serieAbierta.set(true);
  }

  protected async guardarSerie() {
    const r = await this.correr(() => this.api.post<Serie>('/series', { ...this.serieForm, serie: this.serieForm.serie.toUpperCase() }), 'Serie creada.');
    if (r) {
      this.series.update((l) => [...l, r]);
      this.serieAbierta.set(false);
    }
  }

  protected async alternarSerie(s: Serie) {
    const r = await this.correr(() => this.api.patch<Serie>(`/series/${s.id}`, { activo: !s.activo }), s.activo ? 'Serie desactivada.' : 'Serie activada.');
    if (r) this.series.update((l) => l.map((x) => (x.id === r.id ? r : x)));
  }

  protected editarLocal(l?: Local) {
    this.localForm = l ? { ...l } : { codigoSunat: String(this.locales().length).padStart(4, '0'), nombre: '', direccion: '', ubigeo: '', activo: true };
    this.localAbierto.set(true);
  }

  protected async guardarLocal() {
    const f = { ...this.localForm, ubigeo: this.localForm.ubigeo || null };
    const r = await this.correr(() => (f.id ? this.api.put<Local>(`/establecimientos/${f.id}`, f) : this.api.post<Local>('/establecimientos', f)), 'Local guardado.');
    if (r) {
      this.locales.update((l) => (f.id ? l.map((x) => (x.id === r.id ? r : x)) : [...l, r]));
      this.localAbierto.set(false);
    }
  }

  // equipo
  protected editarUsuario(u?: Usuario) {
    this.usuarioForm = u ? { ...u, password: '' } : { nombre: '', email: '', rol: 'VENDEDOR', activo: true, establecimientoId: this.locales()[0]?.id, password: '' };
    this.usuarioAbierto.set(true);
  }

  protected async guardarUsuario() {
    const f = { ...this.usuarioForm, password: this.usuarioForm.password || null };
    const r = await this.correr(() => (f.id ? this.api.put<Usuario>(`/usuarios/${f.id}`, f) : this.api.post<Usuario>('/usuarios', f)), 'Usuario guardado.');
    if (r) {
      this.usuarios.update((l) => (f.id ? l.map((x) => (x.id === r.id ? r : x)) : [...l, r]));
      this.usuarioAbierto.set(false);
    }
  }

  // plan
  protected async cambiarPlan(p: PlanPublico, periodo: 'MENSUAL' | 'ANUAL') {
    const r = await this.correr(() => this.api.post<{ mensaje: string; urlPago?: string }>('/suscripcion/cambiar', { plan: p.codigo, periodo }), 'Procesando tu plan…');
    if (r?.urlPago) window.location.href = r.urlPago;
    else if (r) {
      this.toast.ok(r.mensaje);
      this.cuenta.set(await this.api.get<Cuenta>('/suscripcion'));
      this.auth.refrescar();
    }
  }

  // cuenta
  protected async cambiarClave() {
    const r = await this.correr(() => this.api.post('/auth/clave', this.clave).then(() => true), 'Contraseña actualizada.');
    if (r) this.clave = { actual: '', nueva: '' };
  }
}
