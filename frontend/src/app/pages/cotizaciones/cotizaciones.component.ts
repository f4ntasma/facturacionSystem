import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface ProductoCotizacion {
  nombre: string;
  cantidad: number;
  precio: number;
}

export interface Cotizacion {
  id: number;
  cliente: string;
  total: number;
  fecha: Date;
  productos: ProductoCotizacion[];
  menuAbierto?: boolean;
}

@Component({
  standalone: true,
  selector: 'app-cotizaciones',
  templateUrl: './cotizaciones.component.html',
  imports: [CommonModule, FormsModule]
})
export class CotizacionesComponent implements OnInit {

  cotizaciones: Cotizacion[] = [];
  cotizacionesFiltradas: Cotizacion[] = [];

  searchTerm = '';
  fechaDesde = '';
  fechaHasta = '';

  // MODAL
  mostrarDetalle = false;
  cotizacionSeleccionada!: Cotizacion;

  ngOnInit(): void {
    this.cotizaciones = [
      {
        id: 1,
        cliente: 'Juan Jose',
        total: 100,
        fecha: new Date('2025-12-14'),
        productos: [
          { nombre: 'Pizza', cantidad: 1, precio: 60 },
          { nombre: 'Jugo', cantidad: 2, precio: 20 }
        ]
      }
    ];

    this.ordenar();
    this.aplicarFiltros();
  }

  ordenar() {
    this.cotizaciones.sort((a, b) => b.fecha.getTime() - a.fecha.getTime());
  }

  aplicarFiltros() {
    this.cotizacionesFiltradas = this.cotizaciones.filter(c => {
      const coincideTexto =
        c.cliente.toLowerCase().includes(this.searchTerm.toLowerCase());

      let coincideFecha = true;

      if (this.fechaDesde) {
        coincideFecha = c.fecha >= new Date(this.fechaDesde);
      }

      if (this.fechaHasta) {
        const hasta = new Date(this.fechaHasta);
        hasta.setHours(23, 59, 59);
        coincideFecha = coincideFecha && c.fecha <= hasta;
      }

      return coincideTexto && coincideFecha;
    });
  }

  toggleMenu(c: Cotizacion) {
    this.cotizacionesFiltradas.forEach(x => x.menuAbierto = false);
    c.menuAbierto = !c.menuAbierto;
  }

  verDetalle(c: Cotizacion) {
    this.cotizacionesFiltradas.forEach(x => x.menuAbierto = false);
    this.cotizacionSeleccionada = c;
    this.mostrarDetalle = true;
  }

  cerrarDetalle() {
    this.mostrarDetalle = false;
  }

  descargar(c: Cotizacion) {
    let contenido = `COTIZACIÓN #${c.id}\n\n`;
    contenido += `Cliente: ${c.cliente}\n\nPRODUCTOS:\n`;

    c.productos.forEach(p => {
      contenido += `- ${p.nombre} x${p.cantidad} = S/${p.cantidad * p.precio}\n`;
    });

    contenido += `\nTOTAL: S/${c.total}`;

    const blob = new Blob([contenido], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);

    const a = document.createElement('a');
    a.href = url;
    a.download = `cotizacion_${c.id}.txt`;
    a.click();

    URL.revokeObjectURL(url);
  }

  anular(c: Cotizacion) {
    if (!confirm(`¿Anular cotización #${c.id}?`)) return;
    this.cotizaciones = this.cotizaciones.filter(x => x.id !== c.id);
    this.aplicarFiltros();
  }

  nuevaCotizacion() {
    alert('Aquí luego puedes abrir el formulario de nueva cotización');
  }
}
