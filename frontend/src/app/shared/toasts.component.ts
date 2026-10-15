import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ToastService } from '../core/toast.service';
import { IconoComponent } from './icono.component';

@Component({
  selector: 'at-toasts',
  imports: [IconoComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="pila" aria-live="polite" role="status">
      @for (t of toasts.toasts(); track t.id) {
        <div class="toast" [class.error]="t.tipo === 'error'" [class.ok]="t.tipo === 'ok'">
          <at-icono [nombre]="t.tipo === 'error' ? 'alerta' : t.tipo === 'ok' ? 'check' : 'info'" [tam]="20" />
          <span>{{ t.texto }}</span>
          <button type="button" aria-label="Cerrar aviso" (click)="toasts.cerrar(t.id)"><at-icono nombre="cerrar" [tam]="16" /></button>
        </div>
      }
    </div>
  `,
  styles: `
    .pila { position: fixed; z-index: 1000; left: 50%; transform: translateX(-50%); bottom: 96px; display: flex; flex-direction: column;
      gap: 8px; width: min(440px, calc(100vw - 32px)); pointer-events: none; }
    @media (min-width: 900px) { .pila { bottom: 24px; } }
    .toast { pointer-events: auto; display: flex; align-items: center; gap: 10px; padding: 12px 12px 12px 16px; border-radius: 14px;
      background: var(--tinta); color: var(--blanco); font-weight: 700; box-shadow: var(--sombra-alta); animation: entra .2s ease-out; }
    .toast.ok at-icono { color: var(--amarillo); }
    .toast.error { background: var(--magenta); }
    .toast span { flex: 1; }
    .toast button { background: transparent; border: 0; color: inherit; cursor: pointer; padding: 6px; border-radius: 8px; display: flex; }
    @keyframes entra { from { transform: translateY(8px); opacity: 0; } to { transform: none; opacity: 1; } }
  `,
})
export class ToastsComponent {
  protected readonly toasts = inject(ToastService);
}
