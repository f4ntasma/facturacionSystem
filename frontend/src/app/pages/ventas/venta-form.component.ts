import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { OrdenService, CheckoutRequest } from '../../services/orden.service';
import { CarritoService, CarritoItemRequest } from '../../services/carrito.service';
import { ProductoService, Producto } from '../../services/producto.service';
import { AuthService } from '../../services/auth.service';

export interface ProductoForm {
  productoId?: string;
  nombre: string;
  cantidad: number;
  precio: number;
}

@Component({
  standalone: true,
  selector: 'app-venta-form',
  templateUrl: './venta-form.component.html',
  imports: [CommonModule, FormsModule]
})
export class VentaFormComponent implements OnInit {
  productos: Producto[] = [];
  loading = false;
  buscandoDni = false;
  errorDni = '';

  ventaForm = {
    clienteNombre: '',
    clienteApellido: '',
    dni: '',
    productos: [{ productoId: '', nombre: '', cantidad: 1, precio: 0 }] as ProductoForm[],
    tipoComprobante: 'BOLETA' as 'BOLETA' | 'FACTURA',
    metodoPago: 'EFECTIVO' as 'EFECTIVO' | 'TARJETA' | 'YAPE' | 'PLIN'
  };

  constructor(
    private ordenService: OrdenService,
    private carritoService: CarritoService,
    private productoService: ProductoService,
    private router: Router,
    private http: HttpClient,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.cargarProductos();
  }

  cargarProductos() {
    this.productoService.getProductos().subscribe({
      next: (productos) => {
        this.productos = productos;
      },
      error: (error) => {
        console.error('Error cargando productos:', error);
      }
    });
  }

  buscarDni() {
    const dni = this.ventaForm.dni;
    if (!dni || dni.length !== 8) {
      this.errorDni = 'El DNI debe tener 8 dígitos';
      return;
    }

    this.buscandoDni = true;
    this.errorDni = '';

    const token = this.authService.getToken();
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });

    this.http.get<any>(`http://localhost:8081/api/v1/consulta/dni/${dni}`, { headers }).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.ventaForm.clienteNombre = res.data.nombres;
          this.ventaForm.clienteApellido = `${res.data.apellido_paterno} ${res.data.apellido_materno}`.trim();
        } else {
          this.errorDni = 'No se encontró el DNI';
        }
        this.buscandoDni = false;
      },
      error: () => {
        this.errorDni = 'Error al consultar el DNI';
        this.buscandoDni = false;
      }
    });
  }

  onProductoSeleccionado(productoForm: ProductoForm) {
    const encontrado = this.productos.find(p => p.id === productoForm.productoId);
    if (encontrado) {
      productoForm.nombre = encontrado.nombre;
      productoForm.precio = encontrado.precio;
    }
  }

  agregarProducto() {
    this.ventaForm.productos.push({ productoId: '', nombre: '', cantidad: 1, precio: 0 });
  }

  eliminarProducto(index: number) {
    if (this.ventaForm.productos.length > 1) {
      this.ventaForm.productos.splice(index, 1);
    }
  }

  calcularTotal(): number {
    return this.ventaForm.productos.reduce((total, p) => total + (p.cantidad * p.precio), 0);
  }

  async guardarVenta() {
    if (!this.ventaForm.clienteNombre || !this.ventaForm.dni) {
      alert('Por favor busca el DNI del cliente primero');
      return;
    }

    if (this.ventaForm.productos.some(p => !p.productoId || p.cantidad <= 0)) {
      alert('Por favor seleccione todos los productos');
      return;
    }

    this.loading = true;

    try {
      await this.carritoService.limpiarCarrito().toPromise();

      for (const producto of this.ventaForm.productos) {
        const carritoItem: CarritoItemRequest = {
          productoId: producto.productoId!,
          cantidad: producto.cantidad
        };
        await this.carritoService.agregarItem(carritoItem).toPromise();
      }

      const checkoutRequest: CheckoutRequest = {
        tipoComprobante: this.ventaForm.tipoComprobante,
        metodoPago: this.ventaForm.metodoPago,
        clienteNombre: this.ventaForm.clienteNombre,
        clienteApellido: this.ventaForm.clienteApellido,
        clienteDni: this.ventaForm.dni
      };

      this.ordenService.checkout(checkoutRequest).subscribe({
        next: () => {
          alert('Venta creada exitosamente');
          this.router.navigate(['/app/ventas']);
        },
        error: (error) => {
          console.error('Error creando venta:', error);
          alert('Error al crear la venta: ' + (error.error?.message || error.message || 'Error desconocido'));
          this.loading = false;
        }
      });
    } catch (error) {
      console.error('Error en el proceso:', error);
      alert('Error al procesar la venta');
      this.loading = false;
    }
  }

  cancelar() {
    this.router.navigate(['/app/ventas']);
  }
}