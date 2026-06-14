import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { OrdenService, Orden } from '../../services/orden.service';

export interface ProductoVenta {
  nombre: string;
  cantidad: number;
  precio: number;
}

export interface Venta {
  id: string;
  clienteNombre: string;
  clienteApellido: string;
  dni: string;
  total: number;
  fecha: Date;
  productos: ProductoVenta[];
  menuAbierto?: boolean;
  estado?: string;
}

@Component({
  standalone: true,
  selector: 'app-ventas',
  templateUrl: './ventas.component.html',
  imports: [CommonModule, FormsModule, RouterModule]
})
export class VentasComponent implements OnInit {

  ventas: Venta[] = [];
  ventasFiltradas: Venta[] = [];
  loading = false;
  error = false;

  searchTerm = '';
  fechaDesde = '';
  fechaHasta = '';

  mostrarDetalle = false;
  ventaSeleccionada!: Venta;

  constructor(
    private ordenService: OrdenService,
    private cdr: ChangeDetectorRef  // ← agregado
  ) {}

  ngOnInit(): void {
    this.cargarVentas();
  }

  cargarVentas() {
    this.loading = true;
    this.error = false;
    this.ordenService.getOrdenes().subscribe({
      next: (ordenes) => {
        this.ventas = ordenes.map(orden => this.convertirOrdenAVenta(orden));
        this.ordenarVentas();
        this.aplicarFiltros();
        this.loading = false;
        this.cdr.detectChanges(); // ← agregado
      },
      error: (error) => {
        console.error('Error cargando ventas:', error);
        this.error = true;
        this.loading = false;
        this.cdr.detectChanges(); // ← agregado
      }
    });
  }

  convertirOrdenAVenta(orden: Orden): Venta {
    return {
      id: orden.id,
      clienteNombre: 'Cliente',
      clienteApellido: '',
      dni: '',
      total: orden.total,
      fecha: orden.createdAt ? new Date(orden.createdAt) : new Date(),
      productos: orden.items.map(item => ({
        nombre: item.productoNombre || 'Producto',
        cantidad: item.cantidad,
        precio: item.precioUnitario
      })),
      estado: orden.estado
    };
  }

  ordenarVentas() {
    this.ventas.sort((a, b) => b.fecha.getTime() - a.fecha.getTime());
  }

  aplicarFiltros() {
    this.ventasFiltradas = this.ventas.filter(v => {
      const texto = `${v.clienteNombre} ${v.clienteApellido} ${v.dni}`.toLowerCase();
      const coincideTexto = texto.includes(this.searchTerm.toLowerCase());

      let coincideFecha = true;

      if (this.fechaDesde) {
        coincideFecha = v.fecha >= new Date(this.fechaDesde);
      }

      if (this.fechaHasta) {
        const hasta = new Date(this.fechaHasta);
        hasta.setHours(23, 59, 59);
        coincideFecha = coincideFecha && v.fecha <= hasta;
      }

      return coincideTexto && coincideFecha;
    });
  }

  toggleMenu(venta: Venta) {
    this.ventasFiltradas.forEach(v => v.menuAbierto = false);
    venta.menuAbierto = !venta.menuAbierto;
  }

  verDetalle(venta: Venta) {
    this.ventasFiltradas.forEach(v => v.menuAbierto = false);
    this.ventaSeleccionada = venta;
    this.mostrarDetalle = true;
  }

  cerrarDetalle() {
    this.mostrarDetalle = false;
  }

  descargar(venta: Venta) {
    let contenido = `VENTA #${venta.id}\n\n`;
    contenido += `Cliente: ${venta.clienteNombre} ${venta.clienteApellido}\n`;
    contenido += `DNI: ${venta.dni}\n\nPRODUCTOS:\n`;
    venta.productos.forEach(p => {
      contenido += `- ${p.nombre} x${p.cantidad} = S/${p.cantidad * p.precio}\n`;
    });
    contenido += `\nTOTAL: S/${venta.total}`;

    const blob = new Blob([contenido], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `venta_${venta.id}.txt`;
    a.click();
    URL.revokeObjectURL(url);
  }

  anular(venta: Venta) {
    if (!confirm(`¿Anular venta #${venta.id}?`)) return;
    this.ventas = this.ventas.filter(v => v.id !== venta.id);
    this.aplicarFiltros();
  }
}