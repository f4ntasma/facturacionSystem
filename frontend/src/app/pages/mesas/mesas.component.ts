import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';

export interface Mesa {
  id: string;
  nombre: string;
  piso: number;
  estado: 'LIBRE' | 'OCUPADA' | 'RESERVADA';
}

@Component({
  selector: 'app-mesas',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './mesas.component.html',
  styleUrl: './mesas.component.css'
})
export class MesasComponent implements OnInit {

  pisoActual = 1;
  mesas: Mesa[] = [];
  pisos: number[] = [1, 2, 3];
  openDropdownId: string | null = null;
  mesaEditando: Mesa | null = null;
  mostrarModalEditar = false;
  formMesa = { nombre: '', piso: 1 };

  private apiUrl = 'http://localhost:8081/api/v1/mesas';

  constructor(
    private router: Router,
    private http: HttpClient,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.cargarMesas();
  }

  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({ 'Authorization': `Bearer ${token}` });
  }

  cargarMesas() {
    this.http.get<Mesa[]>(this.apiUrl, { headers: this.getHeaders() }).subscribe({
      next: (mesas) => {
        this.mesas = mesas;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error cargando mesas:', err)
    });
  }

  get mesasFiltradas(): Mesa[] {
    return this.mesas.filter(m => m.piso === this.pisoActual);
  }

  irAPiso(piso: number) {
    this.pisoActual = piso;
    this.openDropdownId = null;
  }

  abrirMesa(mesa: Mesa) {
    this.router.navigate(['/app/mesa', mesa.id]);
  }

  agregarMesa() {
    const cantidadEnPiso = this.mesas.filter(m => m.piso === this.pisoActual).length + 1;
    const body = { nombre: `Mesa ${cantidadEnPiso}`, piso: this.pisoActual };
    this.http.post<Mesa>(this.apiUrl, body, { headers: this.getHeaders() }).subscribe({
      next: () => this.cargarMesas(),
      error: (err) => console.error('Error creando mesa:', err)
    });
  }

  eliminarMesa(id: string) {
    if (!confirm('¿Eliminar esta mesa?')) return;
    this.http.delete(`${this.apiUrl}/${id}`, { headers: this.getHeaders() }).subscribe({
      next: () => {
        this.openDropdownId = null;
        this.cargarMesas();
      },
      error: (err) => console.error('Error eliminando mesa:', err)
    });
  }

  toggleDropdown(id: string) {
    this.openDropdownId = this.openDropdownId === id ? null : id;
  }

  abrirEditarMesa(mesa: Mesa) {
    this.mesaEditando = mesa;
    this.formMesa = { nombre: mesa.nombre, piso: mesa.piso };
    this.mostrarModalEditar = true;
    this.openDropdownId = null;
  }

  cerrarModal() {
    this.mostrarModalEditar = false;
    this.mesaEditando = null;
    this.openDropdownId = null;
  }

  guardarCambiosMesa() {
    if (!this.mesaEditando) return;
    const body = { nombre: this.formMesa.nombre, piso: this.formMesa.piso };
    this.http.put<Mesa>(`${this.apiUrl}/${this.mesaEditando.id}`, body, { headers: this.getHeaders() }).subscribe({
      next: () => {
        this.pisoActual = this.formMesa.piso;
        this.cerrarModal();
        this.cargarMesas();
      },
      error: (err) => console.error('Error actualizando mesa:', err)
    });
  }

  cambiarEstado(id: string, estado: string) {
    this.http.patch(`${this.apiUrl}/${id}/estado?estado=${estado}`, {}, { headers: this.getHeaders() }).subscribe({
      next: () => {
        this.openDropdownId = null;
        this.cargarMesas();
      },
      error: (err) => console.error('Error cambiando estado:', err)
    });
  }

  getEstadoColor(estado: string): string {
    switch (estado) {
      case 'OCUPADA': return '#ffcccc';
      case 'RESERVADA': return '#fff3cd';
      default: return '#eeeeee';
    }
  }
}