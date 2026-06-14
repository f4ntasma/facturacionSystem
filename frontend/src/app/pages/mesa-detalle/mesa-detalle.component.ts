import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';

interface Plato {
  id: string;
  nombre: string;
  precio: number;
}

interface PedidoItem {
  plato: Plato;
  cantidad: number;
  comentario: string;
}

@Component({
  selector: 'app-mesa-detalle',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './mesa-detalle.component.html',
  styleUrl: './mesa-detalle.component.css'
})
export class MesaDetalleComponent implements OnInit {

  mesaId!: string;
  mesaNombre = '';

  platos: Plato[] = [];
  pedido: PedidoItem[] = [];
  loading = false;

  // MODAL agregar producto
  mostrarModal = false;
  platoSeleccionado!: Plato;
  cantidad = 1;
  comentario = '';

  // MODAL cobrar
  mostrarModalCobro = false;
  tipoComprobante: 'BOLETA' | 'FACTURA' = 'BOLETA';
  metodoPago: 'EFECTIVO' | 'TARJETA' | 'YAPE' | 'PLIN' = 'EFECTIVO';

  private apiUrl = 'http://localhost:8081/api/v1';

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private http: HttpClient,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.mesaId = this.route.snapshot.paramMap.get('id')!;
    this.cargarProductos();
    this.cambiarEstadoMesa('OCUPADA');
  }

  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({ 'Authorization': `Bearer ${token}` });
  }

  cargarProductos() {
    this.http.get<any[]>(`${this.apiUrl}/productos`, { headers: this.getHeaders() }).subscribe({
      next: (productos) => {
        this.platos = productos.map(p => ({
          id: p.id,
          nombre: p.nombre,
          precio: p.precio
        }));
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error cargando productos:', err)
    });
  }

  cambiarEstadoMesa(estado: string) {
    this.http.patch(
      `${this.apiUrl}/mesas/${this.mesaId}/estado?estado=${estado}`,
      {},
      { headers: this.getHeaders() }
    ).subscribe({
      error: (err) => console.error('Error cambiando estado mesa:', err)
    });
  }

  volver() {
    this.router.navigate(['/app/pisos/1']);
  }

  abrirModal(plato: Plato) {
    this.platoSeleccionado = plato;
    this.cantidad = 1;
    this.comentario = '';
    this.mostrarModal = true;
  }

  agregarPlato() {
    const existente = this.pedido.find(p => p.plato.id === this.platoSeleccionado.id);
    if (existente) {
      existente.cantidad += this.cantidad;
    } else {
      this.pedido.push({
        plato: this.platoSeleccionado,
        cantidad: this.cantidad,
        comentario: this.comentario
      });
    }
    this.mostrarModal = false;
  }

  quitarPlato(index: number) {
    this.pedido.splice(index, 1);
  }

  total(): number {
    return this.pedido.reduce((sum, p) => sum + p.plato.precio * p.cantidad, 0);
  }

  abrirModalCobro() {
    if (this.pedido.length === 0) {
      alert('Agrega productos al pedido antes de cobrar');
      return;
    }
    this.mostrarModalCobro = true;
  }

  async cobrar() {
    this.loading = true;

    try {
      // Paso 1: limpiar carrito
      await this.http.delete(`${this.apiUrl}/carrito`, { headers: this.getHeaders() }).toPromise();

      // Paso 2: agregar items al carrito
      for (const item of this.pedido) {
        await this.http.post(`${this.apiUrl}/carrito/items`, {
          productoId: item.plato.id,
          cantidad: item.cantidad
        }, { headers: this.getHeaders() }).toPromise();
      }

      // Paso 3: checkout
      await this.http.post(`${this.apiUrl}/checkout`, {
        tipoComprobante: this.tipoComprobante,
        metodoPago: this.metodoPago
      }, { headers: this.getHeaders() }).toPromise();

      // Paso 4: liberar mesa
      await this.cambiarEstadoMesaAsync('LIBRE');

      alert('¡Cobro exitoso!');
      this.router.navigate(['/app/pisos/1']);

    } catch (error) {
      console.error('Error al cobrar:', error);
      alert('Error al procesar el cobro');
      this.loading = false;
    }
  }

  cambiarEstadoMesaAsync(estado: string): Promise<any> {
    return this.http.patch(
      `${this.apiUrl}/mesas/${this.mesaId}/estado?estado=${estado}`,
      {},
      { headers: this.getHeaders() }
    ).toPromise();
  }
}