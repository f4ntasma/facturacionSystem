import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-pago-exitoso',
  standalone: true,
  template: `
    <div style="min-height: 100vh; background: linear-gradient(135deg, #059669, #1e3a8a);
                display: flex; align-items: center; justify-content: center; padding: 2rem;">
      <div style="background: white; border-radius: 1.5rem; padding: 3rem;
                  width: 100%; max-width: 480px; text-align: center;
                  box-shadow: 0 25px 60px rgba(0,0,0,0.3);">
        
        <div style="font-size: 80px; margin-bottom: 1rem;">🎉</div>
        
        <h1 style="font-family: Nunito, sans-serif; font-weight: 900;
                   font-size: 32px; color: #059669; margin-bottom: 0.5rem;">
          ¡Pago Exitoso!
        </h1>
        <p style="color: #6b7280; margin-bottom: 2rem;">
          Tu cuenta ha sido activada. Ya puedes usar Factullama.
        </p>

        <button (click)="irAlSistema()"
                style="width: 100%; padding: 14px;
                       background: linear-gradient(135deg, #1e3a8a, #7c3aed);
                       color: white; border: none; border-radius: 8px;
                       font-family: Nunito, sans-serif; font-weight: 800;
                       font-size: 15px; cursor: pointer;">
          IR AL SISTEMA →
        </button>
      </div>
    </div>
  `
})
export class PagoExitosoComponent {
  constructor(private router: Router) {}
  irAlSistema() { this.router.navigate(['/app/ventas']); }
}