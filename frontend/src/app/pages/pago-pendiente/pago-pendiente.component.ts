import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-pago-pendiente',
  standalone: true,
  template: `
    <div style="min-height: 100vh; display: flex; align-items: center; justify-content: center;
                flex-direction: column; background: #f8f9ff; padding: 2rem; text-align: center;">
      <div style="background: white; border-radius: 1rem; padding: 3rem 2rem; max-width: 460px;
                  box-shadow: 0 4px 24px rgba(0,0,0,0.08);">
        <div style="width: 64px; height: 64px; background: #fefce8; border-radius: 50%;
                    display: flex; align-items: center; justify-content: center; margin: 0 auto 1.5rem;">
          <span style="font-size: 28px; color: #d97706;">&#9201;</span>
        </div>
        <h1 style="font-family: Nunito, sans-serif; font-weight: 900; font-size: 26px; color: #d97706; margin-bottom: 0.75rem;">
          Pago en proceso
        </h1>
        <p style="color: #6b7280; font-size: 14px; line-height: 1.6; margin-bottom: 2rem;">
          Tu pago esta siendo procesado por MercadoPago. Esto puede tardar algunos minutos. Una vez confirmado, tu cuenta sera creada y recibiras acceso al sistema. Si el problema persiste, contactanos.
        </p>
        <div style="display: flex; flex-direction: column; gap: 0.75rem;">
          <button (click)="irAlInicio()"
                  style="padding: 12px 24px; background: linear-gradient(135deg, #1e3a8a, #7c3aed);
                         color: white; border: none; border-radius: 8px; font-family: Nunito, sans-serif;
                         font-weight: 800; font-size: 14px; cursor: pointer;">
            Ir al inicio
          </button>
        </div>
        <p style="margin-top: 1.5rem; font-size: 12px; color: #9ca3af;">
          Soporte: soporte&#64;factullama.site
        </p>
      </div>
    </div>
  `
})
export class PagoPendienteComponent {
  constructor(private router: Router) {}

  irAlInicio() {
    this.router.navigate(['/']);
  }
}
