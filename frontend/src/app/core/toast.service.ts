import { Injectable, signal } from '@angular/core';

export interface Toast {
  id: number;
  tipo: 'ok' | 'error' | 'info';
  texto: string;
}

/** Mensajes cortos con la voz de la marca: «¡Listo, caserito!». */
@Injectable({ providedIn: 'root' })
export class ToastService {
  readonly toasts = signal<Toast[]>([]);
  private siguiente = 1;

  ok(texto: string) {
    this.mostrar('ok', texto);
  }

  error(texto: string) {
    this.mostrar('error', texto, 6000);
  }

  info(texto: string) {
    this.mostrar('info', texto);
  }

  cerrar(id: number) {
    this.toasts.update((l) => l.filter((t) => t.id !== id));
  }

  private mostrar(tipo: Toast['tipo'], texto: string, ms = 3500) {
    const id = this.siguiente++;
    this.toasts.update((l) => [...l.slice(-2), { id, tipo, texto }]);
    setTimeout(() => this.cerrar(id), ms);
  }
}
