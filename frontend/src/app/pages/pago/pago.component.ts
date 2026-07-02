import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../enviroment/enviroment';

@Component({
  selector: 'app-pago',
  standalone: true,
  imports: [CommonModule],
  template: `
<div style="min-height: 100vh; background: #f3f4f6; display: flex; align-items: center; justify-content: center; padding: 2rem;">

  <div style="background: white; border-radius: 1rem; box-shadow: 0 4px 6px -1px rgba(0,0,0,.1); padding: 2.5rem; width: 100%; max-width: 480px;">

    <div style="text-align: center; margin-bottom: 2rem;">
      <h1 style="font-size: 28px; font-weight: 700; color: #1e3a8a; margin-bottom: 0.5rem;">Factullama</h1>
      <p style="font-size: 14px; color: #6b7280;">Completa tu suscripcion</p>
    </div>

    <div style="background: #dbeafe; border: 1px solid #2563eb; border-radius: 0.75rem; padding: 1.5rem; margin-bottom: 2rem;">
      <div style="font-size: 12px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; color: #2563eb; margin-bottom: 0.5rem;">Plan seleccionado</div>
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <div style="font-size: 22px; font-weight: 700; color: #1e3a8a;">{{ planNombre }}</div>
        <div style="font-size: 28px; font-weight: 700; color: #059669;">
          S/ {{ planPrecio }}<span style="font-size: 13px; font-weight: 400; color: #6b7280;">/mes</span>
        </div>
      </div>
    </div>

    <div style="margin-bottom: 2rem;">
      <div style="font-size: 13px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; color: #6b7280; margin-bottom: 0.75rem;">Metodo de pago</div>
      <div style="display: flex; align-items: center; gap: 0.75rem; padding: 1rem; border: 2px solid #1e3a8a; border-radius: 0.5rem; background: rgba(30,58,138,0.04);">
        <div style="width: 40px; height: 25px; background: linear-gradient(135deg, #009ee3, #0070c0); border-radius: 4px; display: flex; align-items: center; justify-content: center;">
          <span style="color: white; font-size: 9px; font-weight: 700;">MP</span>
        </div>
        <span style="font-size: 14px; font-weight: 500; color: #1f2937;">MercadoPago</span>
        <span style="margin-left: auto; font-size: 12px; color: #6b7280;">Tarjeta, Yape, Plin y mas</span>
      </div>
    </div>

    <div *ngIf="error" style="background: #fee2e2; border: 1px solid #dc2626; border-radius: 0.5rem; padding: 0.875rem 1rem; margin-bottom: 1.5rem; font-size: 14px; color: #dc2626;">
      {{ error }}
    </div>

    <button (click)="pagar()" [disabled]="loading"
      style="width: 100%; padding: 1rem; background: linear-gradient(135deg, #1e3a8a, #3b82f6); color: white; border: none; border-radius: 0.75rem; font-size: 16px; font-weight: 700; cursor: pointer; letter-spacing: 0.5px;"
      [style.opacity]="loading ? '0.6' : '1'">
      {{ loading ? 'Redirigiendo a MercadoPago...' : 'Pagar S/ ' + planPrecio }}
    </button>

    <p style="text-align: center; font-size: 12px; color: #6b7280; margin-top: 1.25rem;">
      Seras redirigido a MercadoPago para completar el pago de forma segura.
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
    // Validar que exista un registro pendiente antes de proceder
    const registroPendiente = localStorage.getItem('registro_pendiente');
    if (!registroPendiente) {
      this.error = 'No se encontró información de registro. Por favor regístrate primero.';
      return;
    }

    this.loading = true;
    this.error = '';

    this.http.post<string>(`${environment.apiUrl}/pagos/crear-preferencia`, {
      planNombre: `Factullama ${this.planNombre}`,
      precio: parseFloat(this.planPrecio)
    }, { responseType: 'text' as 'json' }).subscribe({
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