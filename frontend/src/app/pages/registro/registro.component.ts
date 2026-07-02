import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-registro',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div style="min-height: 100vh; background: linear-gradient(135deg, #1e3a8a, #7c3aed);
                display: flex; align-items: center; justify-content: center; padding: 2rem;">
      
      <div style="background: white; border-radius: 1.5rem; padding: 2.5rem;
                  width: 100%; max-width: 480px; box-shadow: 0 25px 60px rgba(0,0,0,0.3);">
        
        <div style="text-align: center; margin-bottom: 2rem;">
          <img src="/svg/iconFactulow.png" style="height: 60px; margin-bottom: 1rem;">
          <h1 style="font-family: Nunito, sans-serif; font-weight: 900; font-size: 28px; color: #1e3a8a;">
            Crear Cuenta
          </h1>
          <p style="color: #6b7280; font-size: 14px;">
            Plan elegido: <strong style="color: #7c3aed;">{{ planNombre }}</strong> — 
            <strong style="color: #059669;">S/ {{ planPrecio }}/mes</strong>
          </p>
        </div>

        <div *ngIf="error" style="background: #fef2f2; border: 1px solid #fca5a5;
             border-radius: 0.5rem; padding: 0.75rem; margin-bottom: 1rem;
             color: #dc2626; font-size: 14px;">
          {{ error }}
        </div>

        <div style="display: flex; flex-direction: column; gap: 1rem;">
          <div>
            <label style="font-size: 13px; font-weight: 600; color: #374151;">
              Nombre completo *
            </label>
            <input type="text" [(ngModel)]="form.nombre"
                   placeholder="Tu nombre"
                   style="width: 100%; margin-top: 4px; padding: 12px 16px;
                          border: 1.5px solid #e5e7eb; border-radius: 8px;
                          font-size: 14px; outline: none; box-sizing: border-box;">
          </div>

          <div>
            <label style="font-size: 13px; font-weight: 600; color: #374151;">
              Correo electrónico *
            </label>
            <input type="email" [(ngModel)]="form.email"
                   placeholder="tu@email.com"
                   style="width: 100%; margin-top: 4px; padding: 12px 16px;
                          border: 1.5px solid #e5e7eb; border-radius: 8px;
                          font-size: 14px; outline: none; box-sizing: border-box;">
          </div>

          <div>
            <label style="font-size: 13px; font-weight: 600; color: #374151;">
              Contraseña * <span style="font-weight: 400; color: #9ca3af;">(mínimo 6 caracteres)</span>
            </label>
            <input type="password" [(ngModel)]="form.password"
                   placeholder="••••••••"
                   style="width: 100%; margin-top: 4px; padding: 12px 16px;
                          border: 1.5px solid #e5e7eb; border-radius: 8px;
                          font-size: 14px; outline: none; box-sizing: border-box;">
          </div>

          <div>
            <label style="font-size: 13px; font-weight: 600; color: #374151;">
              Confirmar contraseña *
            </label>
            <input type="password" [(ngModel)]="form.confirmPassword"
                   placeholder="••••••••"
                   style="width: 100%; margin-top: 4px; padding: 12px 16px;
                          border: 1.5px solid #e5e7eb; border-radius: 8px;
                          font-size: 14px; outline: none; box-sizing: border-box;">
          </div>
        </div>

        <button (click)="registrar()" [disabled]="loading"
                style="width: 100%; margin-top: 1.5rem; padding: 14px;
                       background: linear-gradient(135deg, #1e3a8a, #7c3aed);
                       color: white; border: none; border-radius: 8px;
                       font-family: Nunito, sans-serif; font-weight: 800;
                       font-size: 15px; cursor: pointer; letter-spacing: 1px;">
          {{ loading ? 'Creando cuenta...' : 'CREAR CUENTA Y PAGAR' }}
        </button>

        <p style="text-align: center; margin-top: 1rem; font-size: 13px; color: #6b7280;">
          ¿Ya tienes cuenta? 
          <a href="/login" style="color: #7c3aed; font-weight: 600;">Inicia sesión</a>
        </p>
      </div>
    </div>
  `
})
export class RegistroComponent {
  planNombre = 'Básico';
  planPrecio = '29.99';
  loading = false;
  error = '';

  form = {
    nombre: '',
    email: '',
    password: '',
    confirmPassword: ''
  };

  constructor(
    private http: HttpClient,
    private router: Router,
    private route: ActivatedRoute
  ) {
    // Recibir plan desde query params
    this.route.queryParams.subscribe(params => {
      if (params['plan']) {
        this.planNombre = params['plan'];
        this.planPrecio = this.getPrecio(params['plan']);
      }
    });
  }

  getPrecio(plan: string): string {
    switch (plan) {
      case 'Pro': return '49.99';
      case 'Enterprise': return '79.99';
      default: return '29.99';
    }
  }

  registrar() {
    if (!this.form.nombre || !this.form.email || !this.form.password) {
      this.error = 'Por favor completa todos los campos';
      return;
    }
    if (this.form.password.length < 6) {
      this.error = 'La contraseña debe tener al menos 6 caracteres';
      return;
    }
    if (this.form.password !== this.form.confirmPassword) {
      this.error = 'Las contraseñas no coinciden';
      return;
  }

    this.loading = true;
    this.error = '';

    localStorage.setItem('registro_pendiente', JSON.stringify({
    nombre: this.form.nombre,
    email: this.form.email,
    password: this.form.password
  }));

  this.router.navigate(['/pago'], {
    queryParams: {
      plan: this.planNombre,
      precio: this.planPrecio
    }
  });
}
}