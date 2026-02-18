import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface ProductoVenta {
  nombre: string;
  cantidad: number;
  precio: number;
}

export interface Venta {
  id: number;
  clienteNombre: string;
  clienteApellido: string;
  dni: string;
  total: number;
  fecha: Date;
  productos: ProductoVenta[];
  menuAbierto?: boolean;
}

@Component({
  standalone: true,
  selector: 'app-ventas',
  templateUrl: './ventas.component.html',
  imports: [CommonModule, FormsModule]
})
export class VentasComponent implements OnInit {

  ventas: Venta[] = [];
  ventasFiltradas: Venta[] = [];

  searchTerm = '';
  fechaDesde = '';
  fechaHasta = '';

  // 🔹 MODAL
  mostrarDetalle = false;
  ventaSeleccionada!: Venta;

  ngOnInit(): void {
    this.ventas = [
      {
        id: 3,
        clienteNombre: 'Carlos',
        clienteApellido: 'Ramirez',
        dni: '71234567',
        total: 85,
        fecha: new Date('2025-12-15T19:45'),
        productos: [
          { nombre: 'Hamburguesa', cantidad: 2, precio: 20 },
          { nombre: 'Gaseosa', cantidad: 1, precio: 5 }
        ]
      },
      {
        id: 2,
        clienteNombre: 'Maria',
        clienteApellido: 'Lopez',
        dni: '70123456',
        total: 150,
        fecha: new Date('2025-12-15T18:10'),
        productos: [
          { nombre: 'Parrilla', cantidad: 1, precio: 120 },
          { nombre: 'Cerveza', cantidad: 2, precio: 15 }
        ]
      },
      {
        id: 1,
        clienteNombre: 'Juan',
        clienteApellido: 'Jose',
        dni: '70987654',
        total: 100,
        fecha: new Date('2025-12-14T20:30'),
        productos: [
          { nombre: 'Pizza', cantidad: 1, precio: 60 },
          { nombre: 'Jugo', cantidad: 2, precio: 20 }
        ]
      }
    ];

    this.ordenarVentas();
    this.aplicarFiltros();
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

  // ✅ CORREGIDO
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
