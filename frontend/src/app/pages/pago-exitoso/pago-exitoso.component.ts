import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../enviroment/enviroment';

@Component({
  selector: 'app-pago-exitoso',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div style="min-height: 100vh; display: flex; align-items: center; justify-content: center;
                flex-direction: column; background: #f8f9ff; padding: 2rem; text-align: center;">

      <div *ngIf="loading">
        <div style="width: 48px; height: 48px; border: 4px solid #e5e7eb;
                    border-top-color: #1e3a8a; border-radius: 50%;
                    animation: spin 1s linear infinite; margin: 0 auto 1.5rem;"></div>
        <h2 style="font-family: Nunito, sans-serif; font-weight: 800; color: #1e3a8a;">
          Confirmando tu pago y creando tu cuenta...
        </h2>
      </div>

      <div *ngIf="!loading && !error">
        <h1 style="font-family: Nunito, sans-serif; font-weight: 900; font-size: 32px; color: #059669;">
          ¡Pago exitoso!
        </h1>
        <p style="color: #6b7280; margin-top: 0.5rem;">
          Tu cuenta ha sido creada. Redirigiendo...
        </p>
      </div>

      <div *ngIf="error">
        <h1 style="font-family: Nunito, sans-serif; font-weight: 900; font-size: 28px; color: #dc2626;">
          Hubo un problema
        </h1>
        <p style="color: #6b7280; margin-top: 0.5rem; max-width: 400px;">
          {{ error }}
        </p>
        <button (click)="reintentar()"
                style="margin-top: 1.5rem; padding: 12px 32px;
                       background: linear-gradient(135deg, #1e3a8a, #7c3aed);
                       color: white; border: none; border-radius: 8px;
                       font-family: Nunito, sans-serif; font-weight: 800;
                       cursor: pointer;">
          Reintentar
        </button>
      </div>
    </div>

    <style>
      @keyframes spin { to { transform: rotate(360deg); } }
    </style>
  `
})
export class PagoExitosoComponent implements OnInit {
  loading = true;
  error = '';

  constructor(
    private http: HttpClient,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    const datosGuardados = sessionStorage.getItem('registro_pendiente');

    if (!datosGuardados) {
      this.error = 'No se encontraron datos de registro. Por favor, regístrate de nuevo.';
      this.loading = false;
      return;
    }

    const datos = JSON.parse(datosGuardados);

    this.http.post<any>(`${environment.authUrl}/register`, datos)
      .subscribe({
        next: (res) => {
          // Guardar token y limpiar datos temporales
          localStorage.setItem('auth_token', res.token);
          sessionStorage.removeItem('registro_pendiente');
          this.loading = false;

          // Pequeña pausa para que el usuario vea el mensaje de éxito
          setTimeout(() => {
            this.router.navigate(['/app']);
          }, 1500);
        },
        error: (err) => {
          this.loading = false;
          this.error = err.error?.message || 'No se pudo crear tu cuenta. Si el pago se realizó, contáctanos.';
        }
      });
  }

  reintentar() {
    this.loading = true;
    this.error = '';
    this.ngOnInit();
  }
}