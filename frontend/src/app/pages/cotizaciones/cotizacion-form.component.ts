import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';

interface ItemForm {
  nombre: string;
  cantidad: number;
  precio: number;
}

@Component({
  selector: 'app-cotizacion-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="fade-in">
      <div class="d-flex justify-between items-center mb-4 w-100">
        <div>
          <h1 class="font-bold" style="font-size: 28px; color: var(--primary-blue);">Nueva Cotización</h1>
          <p class="text-muted" style="font-size: 14px;">Crear un presupuesto para un cliente</p>
        </div>
        <button class="btn btn-outline" (click)="cancelar()">Cancelar</button>
      </div>

      <div class="card mb-4">
        <div class="card-header"><h4>Datos del Cliente</h4></div>
        <div class="card-body">
          <div class="form-group">
            <label class="form-label">Nombre del Cliente *</label>
            <input type="text" class="form-control"
                   [(ngModel)]="cliente"
                   placeholder="Nombre del cliente">
          </div>
        </div>
      </div>

      <div class="card mb-4">
        <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
          <h4>Productos</h4>
          <button class="btn btn-sm btn-primary" (click)="agregarItem()">+ Agregar</button>
        </div>
        <div class="card-body">
          <div *ngFor="let item of items; let i = index"
               style="margin-bottom: 1rem; padding: 1rem; background: #f5f5f5; border-radius: 0.5rem;">
            <div class="grid grid-3">
              <div class="form-group">
                <label class="form-label">Producto</label>
                <input type="text" class="form-control"
                       [(ngModel)]="item.nombre"
                       placeholder="Nombre del producto">
              </div>
              <div class="form-group">
                <label class="form-label">Cantidad</label>
                <input type="number" class="form-control"
                       [(ngModel)]="item.cantidad" min="1">
              </div>
              <div class="form-group">
                <label class="form-label">Precio Unit.</label>
                <input type="number" class="form-control"
                       [(ngModel)]="item.precio" min="0" step="0.01">
              </div>
            </div>
            <button class="btn btn-sm btn-danger"
                    *ngIf="items.length > 1"
                    (click)="eliminarItem(i)"
                    style="margin-top: 0.5rem;">
              Eliminar
            </button>
          </div>
        </div>
      </div>

      <div class="card mb-4">
        <div class="card-body">
          <div class="d-flex justify-between items-center"
               style="background: var(--neutral-light); padding: 20px; border-radius: var(--radius);">
            <span style="font-size: 14px; color: rgb(55, 54, 54);">Total de la cotización:</span>
            <div class="font-bold" style="font-size: 24px; color: var(--success);">
              S/ {{ calcularTotal() | number:'1.2-2' }}
            </div>
          </div>
        </div>
      </div>

      <div class="d-flex justify-end gap-2">
        <button class="btn btn-outline" (click)="cancelar()">Cancelar</button>
        <button class="btn btn-primary" (click)="guardar()" [disabled]="loading">
          {{ loading ? 'Guardando...' : 'Guardar Cotización' }}
        </button>
      </div>
    </div>
  `
})
export class CotizacionFormComponent {
  cliente = '';
  items: ItemForm[] = [{ nombre: '', cantidad: 1, precio: 0 }];
  loading = false;

  constructor(
    private router: Router,
    private http: HttpClient,
    private authService: AuthService
  ) {}

  agregarItem() {
    this.items.push({ nombre: '', cantidad: 1, precio: 0 });
  }

  eliminarItem(index: number) {
    this.items.splice(index, 1);
  }

  calcularTotal(): number {
    return this.items.reduce((total, i) => total + (i.cantidad * i.precio), 0);
  }

  guardar() {
    if (!this.cliente) {
      alert('Por favor ingresa el nombre del cliente');
      return;
    }
    if (this.items.some(i => !i.nombre || i.precio <= 0)) {
      alert('Por favor completa todos los productos');
      return;
    }

    this.loading = true;
    const token = this.authService.getToken();
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });

    const body = {
      cliente: this.cliente,
      items: this.items.map(i => ({
        nombre: i.nombre,
        cantidad: i.cantidad,
        precio: i.precio
      }))
    };

    this.http.post('http://localhost:8081/api/v1/cotizaciones', body, { headers }).subscribe({
      next: () => {
        this.router.navigate(['/app/cotizaciones']);
      },
      error: (err) => {
        console.error('Error:', err);
        alert('Error al guardar la cotización');
        this.loading = false;
      }
    });
  }

  cancelar() {
    this.router.navigate(['/app/cotizaciones']);
  }
}