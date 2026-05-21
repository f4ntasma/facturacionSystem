import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';

export interface CotizacionItem {
  nombre: string;
  cantidad: number;
  precio: number;
}

export interface Cotizacion {
  id: string;
  cliente: string;
  total: number;
  estado: string;
  createdAt: string;
  items: CotizacionItem[];
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
  mostrarDetalle = false;
  cotizacionSeleccionada!: Cotizacion;

  constructor(
    private router: Router,
    private http: HttpClient,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.cargarCotizaciones();
  }

  cargarCotizaciones() {
    const token = this.authService.getToken();
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });

    this.http.get<Cotizacion[]>('http://localhost:8081/api/v1/cotizaciones', { headers }).subscribe({
      next: (data) => {
        this.cotizaciones = data;
        this.cotizacionesFiltradas = data;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error cargando cotizaciones:', err)
    });
  }

  aplicarFiltros() {
    this.cotizacionesFiltradas = this.cotizaciones.filter(c => {
      const coincideTexto = c.cliente.toLowerCase().includes(this.searchTerm.toLowerCase());
      let coincideFecha = true;
      if (this.fechaDesde) coincideFecha = new Date(c.createdAt) >= new Date(this.fechaDesde);
      if (this.fechaHasta) {
        const hasta = new Date(this.fechaHasta);
        hasta.setHours(23, 59, 59);
        coincideFecha = coincideFecha && new Date(c.createdAt) <= hasta;
      }
      return coincideTexto && coincideFecha;
    });
  }

  verDetalle(c: Cotizacion) {
    this.cotizacionSeleccionada = c;
    this.mostrarDetalle = true;
  }

  cerrarDetalle() {
    this.mostrarDetalle = false;
  }

  descargar(c: Cotizacion) {
    let contenido = `COTIZACIÓN #${c.id}\nCliente: ${c.cliente}\n\nPRODUCTOS:\n`;
    c.items.forEach(i => {
      contenido += `- ${i.nombre} x${i.cantidad} = S/${i.cantidad * i.precio}\n`;
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
    if (!confirm(`¿Anular cotización?`)) return;
    const token = this.authService.getToken();
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
    this.http.delete(`http://localhost:8081/api/v1/cotizaciones/${c.id}`, { headers }).subscribe({
      next: () => this.cargarCotizaciones(),
      error: (err) => console.error('Error anulando:', err)
    });
  }

  nuevaCotizacion() {
    this.router.navigate(['/app/cotizaciones/nueva']);
  }
}