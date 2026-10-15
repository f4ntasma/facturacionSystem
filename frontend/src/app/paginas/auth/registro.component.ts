import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Api, mensajeError } from '../../core/api.service';
import { Auth } from '../../core/auth.service';
import { LogoComponent } from '../../shared/logo.component';

interface Contribuyente {
  ruc: string;
  razonSocial: string;
  direccion?: string;
  estado?: string;
  condicion?: string;
  departamento?: string;
  provincia?: string;
  distrito?: string;
  ubigeo?: string;
}

function rucValido(ruc: string): boolean {
  if (!/^(10|15|16|17|20)\d{9}$/.test(ruc)) return false;
  const p = [5, 4, 3, 2, 7, 6, 5, 4, 3, 2];
  const s = p.reduce((a, w, i) => a + w * Number(ruc[i]), 0);
  let d = 11 - (s % 11);
  if (d === 10) d = 0;
  if (d === 11) d = 1;
  return d === Number(ruc[10]);
}

@Component({
  selector: 'at-registro',
  imports: [FormsModule, RouterLink, LogoComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  styleUrl: './auth-layout.css',
  template: `
    <main class="auth">
      <section class="lado-marca">
        <a routerLink="/" aria-label="Al Toque, inicio"><at-logo [tam]="40" /></a>
        <div class="col">
          <h1 class="display">Emite<br />al toque.</h1>
          <p>Registra tu negocio una sola vez. 15 días gratis con todo incluido, sin tarjeta.</p>
        </div>
        <span class="mono mini sec">Paso {{ paso() }} de 2</span>
      </section>

      <section class="lado-form">
        <form class="form-caja" (ngSubmit)="siguiente()" novalidate>
          <div class="pasos" aria-hidden="true"><span class="on"></span><span [class.on]="paso() === 2"></span></div>
          @if (paso() === 1) {
            <h2 class="display">Tu negocio</h2>
            <div class="campo">
              <label for="ruc">RUC</label>
              <div class="fila">
                <input id="ruc" name="ruc" class="input mono crece" inputmode="numeric" maxlength="11" autocomplete="off"
                  [(ngModel)]="ruc" (ngModelChange)="alCambiarRuc()" [attr.aria-invalid]="ruc.length === 11 && !rucOk()" />
                @if (buscando()) { <span class="chip chip-amarillo">Buscando…</span> }
              </div>
              @if (ruc.length === 11 && !rucOk()) { <span class="ayuda" style="color: var(--magenta)">Ese RUC no es válido, pe. Revisa los 11 dígitos.</span> }
              @if (contrib()?.estado && contrib()?.estado !== 'ACTIVO') { <span class="ayuda">SUNAT lo reporta como {{ contrib()?.estado }} / {{ contrib()?.condicion }}.</span> }
            </div>
            <div class="campo">
              <label for="razon">Razón social</label>
              <input id="razon" name="razon" class="input" [(ngModel)]="razonSocial" autocomplete="organization" />
            </div>
            <div class="campo">
              <label for="comercial">Nombre comercial <span class="suave">(opcional)</span></label>
              <input id="comercial" name="comercial" class="input" placeholder="Así te conocen en el barrio" [(ngModel)]="nombreComercial" />
            </div>
            <div class="campo">
              <label for="dir">Dirección fiscal</label>
              <input id="dir" name="dir" class="input" [(ngModel)]="direccion" autocomplete="street-address" />
            </div>
          } @else {
            <h2 class="display">Tus datos</h2>
            <div class="campo">
              <label for="nombre">Tu nombre</label>
              <input id="nombre" name="nombre" class="input" [(ngModel)]="nombre" autocomplete="name" />
            </div>
            <div class="campo">
              <label for="email">Correo</label>
              <input id="email" name="email" class="input" type="email" [(ngModel)]="email" autocomplete="email" />
            </div>
            <div class="campo">
              <label for="cel">Celular <span class="suave">(opcional)</span></label>
              <input id="cel" name="cel" class="input mono" type="tel" [(ngModel)]="telefono" autocomplete="tel" />
            </div>
            <div class="campo">
              <label for="clave">Contraseña</label>
              <input id="clave" name="clave" class="input" type="password" [(ngModel)]="password" autocomplete="new-password" />
              <span class="ayuda">Mínimo 8 caracteres.</span>
            </div>
            <label class="check"><input type="checkbox" name="acepto" [(ngModel)]="acepto" /> Acepto los <a routerLink="/terminos" target="_blank">términos</a> y la <a routerLink="/privacidad" target="_blank">política de privacidad</a>.</label>
          }
          @if (error()) { <div class="aviso aviso-magenta" role="alert">{{ error() }}</div> }
          <div class="fila">
            @if (paso() === 2) { <button type="button" class="btn btn-borde btn-grande" (click)="paso.set(1)">Atrás</button> }
            <button class="btn btn-tinta btn-grande crece" type="submit" [disabled]="cargando()">
              {{ paso() === 1 ? 'Seguir' : cargando() ? 'Creando tu cuenta…' : 'Crear mi cuenta' }}
            </button>
          </div>
          <p class="pie-form">¿Ya tienes cuenta? <a routerLink="/ingresar"><strong>Ingresa</strong></a></p>
        </form>
      </section>
    </main>
  `,
})
export class RegistroComponent {
  private api = inject(Api);
  private auth = inject(Auth);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  protected paso = signal<1 | 2>(1);
  protected cargando = signal(false);
  protected buscando = signal(false);
  protected error = signal<string | null>(null);
  protected contrib = signal<Contribuyente | null>(null);
  protected rucOk = signal(false);

  protected ruc = '';
  protected razonSocial = '';
  protected nombreComercial = '';
  protected direccion = '';
  protected nombre = '';
  protected email = '';
  protected telefono = this.route.snapshot.queryParamMap.get('cel') ?? '';
  protected password = '';
  protected acepto = false;
  private plan = this.route.snapshot.queryParamMap.get('plan') ?? undefined;

  protected async alCambiarRuc() {
    this.ruc = this.ruc.replace(/\D/g, '').slice(0, 11);
    this.rucOk.set(rucValido(this.ruc));
    if (!this.rucOk()) return;
    this.buscando.set(true);
    try {
      const c = await this.api.get<Contribuyente>(`/consulta/ruc/${this.ruc}`);
      this.contrib.set(c);
      if (c.razonSocial) this.razonSocial = c.razonSocial;
      if (c.direccion) this.direccion = c.direccion;
    } catch {
      // Sin autocompletado: el usuario escribe a mano.
    } finally {
      this.buscando.set(false);
    }
  }

  protected async siguiente() {
    this.error.set(null);
    if (this.paso() === 1) {
      if (!this.rucOk()) return this.error.set('Escribe un RUC válido de 11 dígitos.');
      if (!this.razonSocial.trim()) return this.error.set('Falta la razón social.');
      if (!this.direccion.trim()) return this.error.set('Falta la dirección fiscal.');
      this.paso.set(2);
      return;
    }
    if (!this.nombre.trim() || !this.email.trim()) return this.error.set('Completa tu nombre y correo.');
    if (this.password.length < 8) return this.error.set('La contraseña debe tener al menos 8 caracteres.');
    if (!this.acepto) return this.error.set('Acepta los términos para continuar.');
    this.cargando.set(true);
    try {
      const c = this.contrib();
      await this.auth.registrar({
        ruc: this.ruc,
        razonSocial: this.razonSocial,
        nombreComercial: this.nombreComercial,
        direccion: this.direccion,
        ubigeo: c?.ubigeo,
        departamento: c?.departamento,
        provincia: c?.provincia,
        distrito: c?.distrito,
        telefono: this.telefono,
        nombre: this.nombre,
        email: this.email,
        password: this.password,
        plan: this.plan,
      });
      this.router.navigate(['/app'], { queryParams: { bienvenida: 1 } });
    } catch (e) {
      this.error.set(mensajeError(e));
    } finally {
      this.cargando.set(false);
    }
  }
}
