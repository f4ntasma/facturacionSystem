import { Component, OnInit } from '@angular/core';

export interface Venta {
  id: number;
  clienteNombre: string;
  clienteApellido: string;
  dni: string;
  total: number;
  fecha: Date;
  menuAbierto?: boolean; // solo UI
}

@Component({
  standalone: true,
  selector: 'app-ventas',
  templateUrl: './ventas.component.html'
})
export class VentasComponent implements OnInit {

  // 🔹 Ventas simuladas (luego vendrán del backend)
  ventas: Venta[] = [];

  // 🔹 Ventas filtradas para mostrar
  ventasFiltradas: Venta[] = [];

  // 🔹 Filtros
  searchTerm: string = '';
  fechaDesde: string = '';
  fechaHasta: string = '';

  ngOnInit(): void {
    console.log('VentasComponent iniciado');

    // Datos falsos de ejemplo
    this.ventas = [
      {
        id: 3,
        clienteNombre: 'Carlos',
        clienteApellido: 'Ramirez',
        dni: '71234567',
        total: 85,
        fecha: new Date('2025-12-15T19:45')
      },
      {
        id: 2,
        clienteNombre: 'Maria',
        clienteApellido: 'Lopez',
        dni: '70123456',
        total: 150,
        fecha: new Date('2025-12-15T18:10')
      },
      {
        id: 1,
        clienteNombre: 'Juan',
        clienteApellido: 'Jose',
        dni: '70987654',
        total: 100,
        fecha: new Date('2025-12-14T20:30')
      }
    ];

    // Ordenar por fecha (más reciente primero)
    this.ordenarVentas();
    this.aplicarFiltros();
  }

  ordenarVentas() {
    this.ventas.sort(
      (a, b) => b.fecha.getTime() - a.fecha.getTime()
    );
  }

  aplicarFiltros() {
    console.log('Aplicando filtros');

    this.ventasFiltradas = this.ventas.filter(venta => {
      const texto = `${venta.clienteNombre} ${venta.clienteApellido} ${venta.dni}`.toLowerCase();
      const coincideBusqueda = texto.includes(this.searchTerm.toLowerCase());

      let coincideFecha = true;

      if (this.fechaDesde) {
        coincideFecha = venta.fecha >= new Date(this.fechaDesde);
      }

      if (this.fechaHasta) {
        const hasta = new Date(this.fechaHasta);
        hasta.setHours(23, 59, 59);
        coincideFecha = coincideFecha && venta.fecha <= hasta;
      }

      return coincideBusqueda && coincideFecha;
    });

    console.log('Ventas filtradas:', this.ventasFiltradas);
  }

  toggleMenu(venta: Venta) {
    venta.menuAbierto = !venta.menuAbierto;
  }

  verDetalle(venta: Venta) {
    console.log('Ver detalle de venta:', venta);
    venta.menuAbierto = false;
  }

  descargar(venta: Venta) {
    console.log('Descargar venta:', venta);
    venta.menuAbierto = false;
  }

  anular(venta: Venta) {
    console.log('Anular venta:', venta);
    venta.menuAbierto = false;
  }
}
