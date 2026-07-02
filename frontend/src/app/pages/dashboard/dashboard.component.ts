import { Component } from '@angular/core';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  template: `
<div class="fade-in" style="padding: 2rem; text-align: center; margin-top: 6rem;">
  <h1 style="font-size: 80px; font-weight: 700; color: var(--primary-blue); margin-bottom: 5rem;">
    Te doy la bienvenida a <br/><span style="color: rgba(34, 96, 221, 0.87);">Factullama</span>
  </h1>
  <p style="font-size: 30px; color: #f8f8f8; background: #532aeaba; padding: 20px; border-radius: 15px; width: 50rem; margin: 0 auto;">
    Por favor, selecciona una sección para empezar
  </p>
</div>
  `
})
export class DashboardComponent {}
