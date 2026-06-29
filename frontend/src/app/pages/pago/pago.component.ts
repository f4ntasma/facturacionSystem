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
    <!-- ... tu template igual ... -->
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
    const registroPendiente = sessionStorage.getItem('registro_pendiente');
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