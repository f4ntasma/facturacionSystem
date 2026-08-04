import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-trial',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
<div class="trial-page">
  <div class="trial-card">
    <div class="trial-header">
      <img src="assets/logo.png" alt="Factullama" class="trial-logo" onerror="this.style.display='none'">
      <h1>Prueba gratis por 3 dias</h1>
      <p>Sin tarjeta de credito. Acceso completo durante 3 dias.</p>
    </div>

    <form (ngSubmit)="onSubmit()" #form="ngForm" class="trial-form">
      <div class="form-group">
        <label for="nombre">Nombre completo</label>
        <input
          id="nombre"
          type="text"
          [(ngModel)]="nombre"
          name="nombre"
          placeholder="Tu nombre"
          required
          [class.input-error]="submitted && !nombre"
        />
      </div>

      <div class="form-group">
        <label for="email">Correo electronico</label>
        <input
          id="email"
          type="email"
          [(ngModel)]="email"
          name="email"
          placeholder="tu@email.com"
          required
          [class.input-error]="submitted && !email"
        />
      </div>

      <div class="form-group">
        <label for="password">Contrasena</label>
        <input
          id="password"
          type="password"
          [(ngModel)]="password"
          name="password"
          placeholder="Minimo 6 caracteres"
          required
          minlength="6"
          [class.input-error]="submitted && !password"
        />
      </div>

      <div class="terminos-check">
        <input type="checkbox" [(ngModel)]="aceptaTerminos" name="aceptaTerminos" id="terminos-trial">
        <label for="terminos-trial">
          He leido y acepto los
          <a href="/terminos" target="_blank">Terminos y Condiciones</a>
          y la
          <a href="/privacidad" target="_blank">Politica de Privacidad</a>.
        </label>
      </div>

      <div *ngIf="errorMsg" class="error-msg">{{ errorMsg }}</div>

      <button type="submit" class="btn-trial" [disabled]="loading || !aceptaTerminos">
        {{ loading ? 'Creando cuenta...' : 'Comenzar prueba gratuita' }}
      </button>
    </form>

    <p class="trial-footer">
      Ya tienes cuenta? <a href="/login">Inicia sesion</a>
    </p>
  </div>
</div>
  `,
  styles: [`
    .trial-page {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #0052cc 0%, #003d99 100%);
      padding: 2rem;
    }
    .trial-card {
      background: #fff;
      border-radius: 16px;
      padding: 2.5rem 2rem;
      width: 100%;
      max-width: 420px;
      box-shadow: 0 20px 60px rgba(0,0,0,0.2);
    }
    .trial-header {
      text-align: center;
      margin-bottom: 2rem;
    }
    .trial-logo {
      height: 48px;
      margin-bottom: 1rem;
    }
    .trial-header h1 {
      font-size: 22px;
      font-weight: 700;
      color: #0052cc;
      margin: 0 0 0.5rem;
    }
    .trial-header p {
      font-size: 14px;
      color: #6b7280;
      margin: 0;
    }
    .trial-form {
      display: flex;
      flex-direction: column;
      gap: 1.25rem;
    }
    .form-group {
      display: flex;
      flex-direction: column;
      gap: 0.4rem;
    }
    .form-group label {
      font-size: 13px;
      font-weight: 600;
      color: #374151;
    }
    .form-group input {
      padding: 0.7rem 1rem;
      border: 1.5px solid #d1d5db;
      border-radius: 8px;
      font-size: 14px;
      outline: none;
      transition: border-color 0.2s;
    }
    .form-group input:focus {
      border-color: #0052cc;
    }
    .form-group input.input-error {
      border-color: #ef4444;
    }
    .terminos-check {
      display: flex;
      align-items: flex-start;
      gap: 0.5rem;
      font-size: 12.5px;
      color: #4b5563;
      line-height: 1.5;
    }
    .terminos-check input[type="checkbox"] {
      margin-top: 2px;
      cursor: pointer;
      accent-color: #0052cc;
      flex-shrink: 0;
    }
    .terminos-check a {
      color: #0052cc;
      font-weight: 600;
      text-decoration: none;
    }
    .terminos-check a:hover { text-decoration: underline; }
    .error-msg {
      background: #fef2f2;
      border: 1px solid #fecaca;
      color: #dc2626;
      padding: 0.6rem 0.9rem;
      border-radius: 8px;
      font-size: 13px;
    }
    .btn-trial {
      background: #0052cc;
      color: #fff;
      border: none;
      border-radius: 8px;
      padding: 0.85rem;
      font-size: 15px;
      font-weight: 600;
      cursor: pointer;
      transition: background 0.2s;
      margin-top: 0.25rem;
    }
    .btn-trial:hover:not(:disabled) {
      background: #003d99;
    }
    .btn-trial:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
    .trial-footer {
      text-align: center;
      margin-top: 1.5rem;
      font-size: 13px;
      color: #6b7280;
    }
    .trial-footer a {
      color: #0052cc;
      font-weight: 600;
      text-decoration: none;
    }
    .trial-footer a:hover {
      text-decoration: underline;
    }
  `]
})
export class TrialComponent {
  nombre = '';
  email = '';
  password = '';
  loading = false;
  submitted = false;
  errorMsg = '';
  aceptaTerminos = false;

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit() {
    this.submitted = true;
    this.errorMsg = '';

    if (!this.aceptaTerminos) {
      this.errorMsg = 'Debes aceptar los Terminos y Condiciones para continuar.';
      return;
    }

    if (!this.nombre || !this.email || !this.password || this.password.length < 6) {
      this.errorMsg = 'Completa todos los campos correctamente.';
      return;
    }

    this.loading = true;
    this.authService.registerTrial({ nombre: this.nombre, email: this.email, password: this.password })
      .subscribe({
        next: () => {
          this.router.navigate(['/app']);
        },
        error: (err) => {
          this.loading = false;
          this.errorMsg = err?.error?.message || 'No se pudo crear la cuenta. Intenta con otro correo.';
        }
      });
  }
}
