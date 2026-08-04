import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-pago-fallido',
  standalone: true,
  template: `
    <div style="min-height: 100vh; display: flex; align-items: center; justify-content: center;
                flex-direction: column; background: #f8f9ff; padding: 2rem; text-align: center;">
      <div style="background: white; border-radius: 1rem; padding: 3rem 2rem; max-width: 460px;
                  box-shadow: 0 4px 24px rgba(0,0,0,0.08);">
        <div style="width: 64px; height: 64px; background: #fef2f2; border-radius: 50%;
                    display: flex; align-items: center; justify-content: center; margin: 0 auto 1.5rem;">
          <span style="font-size: 28px; color: #dc2626;">&#10005;</span>
        </div>
        <h1 style="font-family: Nunito, sans-serif; font-weight: 900; font-size: 26px; color: #dc2626; margin-bottom: 0.75rem;">
          El pago no fue procesado
        </h1>
        <p style="color: #6b7280; font-size: 14px; line-height: 1.6; margin-bottom: 2rem;">
          Tu pago fue rechazado o cancelado. No se realizó ningún cargo. Puedes intentarlo nuevamente con otro método de pago o contactarnos si el problema persiste.
        </p>
        <div style="display: flex; flex-direction: column; gap: 0.75rem;">
          <button (click)="reintentar()"
                  style="padding: 12px 24px; background: linear-gradient(135deg, #1e3a8a, #7c3aed);
                         color: white; border: none; border-radius: 8px; font-family: Nunito, sans-serif;
                         font-weight: 800; font-size: 14px; cursor: pointer;">
            Intentar de nuevo
          </button>
          <button (click)="irAlInicio()"
                  style="padding: 12px 24px; background: transparent; color: #6b7280;
                         border: 1.5px solid #e5e7eb; border-radius: 8px; font-size: 14px; cursor: pointer;">
            Volver al inicio
          </button>
        </div>
        <p style="margin-top: 1.5rem; font-size: 12px; color: #9ca3af;">
          Soporte: soporte&#64;factullama.site
        </p>
      </div>
    </div>
  `
})
export class PagoFallidoComponent {
  constructor(private router: Router) {}

  reintentar() {
    const datos = localStorage.getItem('registro_pendiente');
    if (datos) {
      const parsed = JSON.parse(datos);
      this.router.navigate(['/registro'], {
        queryParams: { plan: parsed.plan || 'Basico', precio: parsed.precio || '29.99' }
      });
    } else {
      this.router.navigate(['/']);
    }
  }

  irAlInicio() {
    this.router.navigate(['/']);
  }
}
