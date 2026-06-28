import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-error',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div style="min-height: 100vh; display: flex; align-items: center; justify-content: center;
                flex-direction: column; background: #f8f9ff; padding: 2rem; text-align: center;">
      
      <img src="/siteUnderConstuct.gif" 
           alt="En construcción" 
           style="max-width: 400px; width: 100%; margin-bottom: 2rem;">

      <h1 style="font-family: Nunito, sans-serif; font-weight: 900; font-size: 32px;
                 color: #1e3a8a; margin-bottom: 0.5rem;">
        {{ titulo }}
      </h1>
      <p style="color: #6b7280; font-size: 16px; margin-bottom: 2rem; max-width: 480px;">
        {{ mensaje }}
      </p>

      <button (click)="volver()"
              style="padding: 12px 32px; background: linear-gradient(135deg, #1e3a8a, #7c3aed);
                     color: white; border: none; border-radius: 8px;
                     font-family: Nunito, sans-serif; font-weight: 800;
                     font-size: 14px; cursor: pointer; letter-spacing: 1px;">
        VOLVER AL INICIO
      </button>
    </div>
  `
})
export class ErrorComponent {
  @Input() codigo: string = '404';

  get titulo(): string {
    switch (this.codigo) {
      case '404': return '¡Página no encontrada!';
      case 'construccion': return 'Estamos trabajando en esto';
      default: return 'Algo salió mal';
    }
  }

  get mensaje(): string {
    switch (this.codigo) {
      case '404': return 'La página que buscas no existe o fue movida.';
      case 'construccion': return 'Esta sección está en construcción. Vuelve pronto.';
      default: return 'Ocurrió un error inesperado. Intenta nuevamente.';
    }
  }

  constructor(private router: Router) {}

  volver() {
    this.router.navigate(['/']);
  }
}