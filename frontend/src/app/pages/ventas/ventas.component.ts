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
    private cdr: ChangeDetectorRef
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
        this.cdr.detectChanges(); 
      },
      error: (error) => {
        console.error('Error cargando ventas:', error);
        this.error = true;
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  convertirOrdenAVenta(orden: Orden): Venta {
    // createdAt viene como string ISO-8601 desde el backend (ej: "2025-01-15T10:30:00Z")
    // new Date(isoString) lo parsea correctamente con la fecha exacta de cada orden
    let fecha: Date;
    if (orden.createdAt) {
      fecha = new Date(orden.createdAt);
    } else {
      // Si no tiene fecha (órdenes antiguas), se usa fecha inválida para mostrar "—"
      fecha = new Date(NaN);
    }

    return {
      id: orden.id,
      clienteNombre:   orden.clienteNombre   || '—',
      clienteApellido: orden.clienteApellido  || '',
      dni:             orden.clienteDni       || '—',
      total: orden.total,
      fecha,
      productos: orden.items.map(item => ({
        nombre: item.producto?.nombre || 'Producto',
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

    // FIX: usaba URL hardcodeada localhost — ahora usa OrdenService correcto
    this.ordenService.eliminar(venta.id).subscribe({
      next: () => this.cargarVentas(),
      error: (err) => console.error('Error anulando:', err)
    });
  }
}