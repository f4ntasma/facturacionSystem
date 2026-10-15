import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { Api } from './api.service';
import { Perfil, TokenResponse } from './modelos';

const CLAVE = 'altoque.sesion';

interface Guardado {
  token: string;
  expira: string;
}

function leer(): Guardado | null {
  try {
    const s = localStorage.getItem(CLAVE);
    if (!s) return null;
    const g = JSON.parse(s) as Guardado;
    return new Date(g.expira).getTime() > Date.now() ? g : null;
  } catch {
    return null;
  }
}

@Injectable({ providedIn: 'root' })
export class Auth {
  private api = inject(Api);
  private router = inject(Router);

  private sesion = signal<Guardado | null>(leer());
  readonly perfil = signal<Perfil | null>(null);

  readonly token = computed(() => this.sesion()?.token ?? null);
  readonly logueado = computed(() => this.sesion() !== null);
  readonly empresa = computed(() => this.perfil()?.empresa ?? null);
  readonly esAdmin = computed(() => ['PROPIETARIO', 'ADMINISTRADOR'].includes(this.perfil()?.rol ?? ''));
  readonly esDueno = computed(() => this.perfil()?.rol === 'PROPIETARIO');

  async ingresar(email: string, password: string) {
    this.guardar(await this.api.post<TokenResponse>('/auth/login', { email, password }));
  }

  async registrar(datos: Record<string, unknown>) {
    this.guardar(await this.api.post<TokenResponse>('/auth/registro', datos));
  }

  /** Recarga el perfil (plan, uso del mes, ambiente SUNAT). */
  async refrescar(): Promise<Perfil | null> {
    if (!this.logueado()) return null;
    try {
      const p = await this.api.get<Perfil>('/auth/yo');
      this.perfil.set(p);
      return p;
    } catch {
      return null;
    }
  }

  salir(irA = '/ingresar') {
    try {
      localStorage.removeItem(CLAVE);
    } catch {
      /* almacenamiento bloqueado */
    }
    this.sesion.set(null);
    this.perfil.set(null);
    this.router.navigateByUrl(irA);
  }

  private guardar(r: TokenResponse) {
    const g = { token: r.token, expira: r.expira };
    try {
      localStorage.setItem(CLAVE, JSON.stringify(g));
    } catch {
      /* modo privado: la sesión dura lo que la pestaña */
    }
    this.sesion.set(g);
    this.perfil.set(r.perfil);
  }
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(Auth);
  const token = auth.token();
  const conToken = token && req.url.startsWith('/api') ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;
  return next(conToken).pipe(
    catchError((e: unknown) => {
      if (e instanceof HttpErrorResponse && e.status === 401 && token && !req.url.includes('/auth/')) {
        auth.salir('/ingresar?vencida=1');
      }
      return throwError(() => e);
    }),
  );
};

export const soloLogueados: CanActivateFn = async (_r, state) => {
  const auth = inject(Auth);
  const router = inject(Router);
  if (!auth.logueado()) return router.createUrlTree(['/ingresar'], { queryParams: { volver: state.url } });
  if (!auth.perfil()) {
    const p = await auth.refrescar();
    if (!p) return router.createUrlTree(['/ingresar']);
  }
  return true;
};

export const soloInvitados: CanActivateFn = () => {
  const auth = inject(Auth);
  return auth.logueado() ? inject(Router).createUrlTree(['/app']) : true;
};
