import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';

type Params = Record<string, string | number | boolean | null | undefined>;

/** Envoltorio de HttpClient con promesas y mensajes de error legibles. */
@Injectable({ providedIn: 'root' })
export class Api {
  private http = inject(HttpClient);
  readonly base = '/api';

  get<T>(ruta: string, params?: Params): Promise<T> {
    return firstValueFrom(this.http.get<T>(this.base + ruta, { params: this.params(params) }));
  }

  post<T>(ruta: string, cuerpo: unknown = {}): Promise<T> {
    return firstValueFrom(this.http.post<T>(this.base + ruta, cuerpo));
  }

  put<T>(ruta: string, cuerpo: unknown): Promise<T> {
    return firstValueFrom(this.http.put<T>(this.base + ruta, cuerpo));
  }

  patch<T>(ruta: string, params?: Params): Promise<T> {
    return firstValueFrom(this.http.patch<T>(this.base + ruta, {}, { params: this.params(params) }));
  }

  delete<T>(ruta: string): Promise<T> {
    return firstValueFrom(this.http.delete<T>(this.base + ruta));
  }

  subir<T>(ruta: string, datos: FormData): Promise<T> {
    return firstValueFrom(this.http.post<T>(this.base + ruta, datos));
  }

  /** Descarga un archivo protegido (XML, CDR, CSV) y lo guarda con su nombre. */
  async descargar(ruta: string, params?: Params): Promise<void> {
    const res = await firstValueFrom(
      this.http.get(this.base + ruta, { params: this.params(params), responseType: 'blob', observe: 'response' }),
    );
    const disp = res.headers.get('Content-Disposition') ?? '';
    const m = /filename\*=UTF-8''([^;]+)|filename="?([^";]+)"?/i.exec(disp);
    const nombre = m ? decodeURIComponent(m[1] ?? m[2]) : 'archivo';
    const url = URL.createObjectURL(res.body!);
    const a = document.createElement('a');
    a.href = url;
    a.download = nombre;
    a.click();
    setTimeout(() => URL.revokeObjectURL(url), 2000);
  }

  private params(p?: Params): HttpParams {
    let hp = new HttpParams();
    for (const [k, v] of Object.entries(p ?? {})) {
      if (v !== null && v !== undefined && v !== '') hp = hp.set(k, String(v));
    }
    return hp;
  }
}

/** Saca el mensaje del backend o uno amable por defecto. */
export function mensajeError(e: unknown): string {
  if (e instanceof HttpErrorResponse) {
    if (e.status === 0) return 'Sin conexión. Revisa tu internet e inténtalo otra vez.';
    const cuerpo = e.error;
    if (cuerpo && typeof cuerpo === 'object' && 'mensaje' in cuerpo) return String(cuerpo.mensaje);
    if (e.status >= 500) return 'Algo salió mal de nuestro lado. Inténtalo otra vez.';
    return 'No se pudo completar la operación.';
  }
  return 'Algo salió mal. Inténtalo otra vez.';
}
