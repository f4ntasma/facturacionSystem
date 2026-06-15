import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';

@Component({
  selector: 'app-pago',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div style="min-height: 100vh; background: linear-gradient(135deg, #1e3a8a, #7c3aed);
                display: flex; align-items: center; justify-content: center; padding: 2rem;">
      
      <div style="background: white; border-radius: 1.5rem; padding: 2.5rem;
                  width: 100%; max-width: 480px; box-shadow: 0 25px 60px rgba(0,0,0,0.3);
                  text-align: center;">
        
        <img src="/svg/iconFactulow.png" style="height: 60px; margin-bottom: 1rem;">
        
        <h1 style="font-family: Nunito, sans-serif; font-weight: 900; font-size: 28px;
                   color: #1e3a8a; margin-bottom: 0.5rem;">
          Confirmar Pago
        </h1>

        <div style="background: #f8f9ff; border-radius: 1rem; padding: 1.5rem; margin: 1.5rem 0;">
          <div style="font-size: 13px; color: #6b7280;">Plan seleccionado</div>
          <div style="font-family: Nunito, sans-serif; font-weight: 900;
                      font-size: 24px; color: #1e3a8a; margin: 4px 0;">
            Factullama {{ planNombre }}
          </div>
          <div style="font-family: Nunito, sans-serif; font-weight: 900;
                      font-size: 36px; color: #059669;">
            S/ {{ planPrecio }}
            <span style="font-size: 14px; color: #6b7280; font-weight: 400;">/mes</span>
          </div>
        </div>

        <p style="color: #6b7280; font-size: 14px; margin-bottom: 1.5rem;">
          Serás redirigido a Mercado Pago para completar el pago de forma segura.
        </p>

        <button (click)="pagar()" [disabled]="loading"
                style="width: 100%; padding: 16px;
                       background: #009ee3; color: white;
                       border: none; border-radius: 8px;
                       font-family: Nunito, sans-serif; font-weight: 800;
                       font-size: 16px; cursor: pointer; letter-spacing: 1px;">
          {{ loading ? 'Procesando...' : '💳 PAGAR CON MERCADO PAGO' }}
        </button>

        <div *ngIf="error" style="margin-top: 1rem; color: #dc2626; font-size: 14px;">
          {{ error }}
        </div>

        <p style="margin-top: 1.5rem; font-size: 12px; color: #9ca3af;">
          🔒 Pago 100% seguro. Puedes cancelar cuando quieras.
        </p>
      </div>
    </div>
  `
})
export class PagoComponent implements OnInit {
  planNombre = 'Básico';
  planPrecio = '29.99';
  loading = false;
  error = '';

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      if (params['plan']) this.planNombre = params['plan'];
      if (params['precio']) this.planPrecio = params['precio'];
    });
  }

  pagar() {
    this.loading = true;
    this.error = '';

    const token = localStorage.getItem('auth_token');
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });

    this.http.post<string>('http://localhost:8081/api/v1/pagos/crear-preferencia', {
      planNombre: `Factullama ${this.planNombre}`,
      precio: this.planPrecio
    }, { headers, responseType: 'text' as 'json' }).subscribe({
      next: (url) => {
        window.location.href = url;
      },
      error: (err) => {
        this.error = 'Error al procesar el pago. Intenta nuevamente.';
        this.loading = false;
      }
    });
  }
}