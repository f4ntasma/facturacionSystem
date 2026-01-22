import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

interface Mesa {
  id: number;
  nombre: string;
  piso: number;
}

@Component({
  selector: 'app-mesas',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './mesas.component.html',
  styleUrl: './mesas.component.css'
})
export class MesasComponent {

  pisoActual = 1;

  mesas: Mesa[] = [
    { id: 1, nombre: 'Mesa 1', piso: 1 },
    { id: 2, nombre: 'Mesa 1', piso: 2 }
  ];

  pisos: number[] = [1, 2, 3];

  openDropdownId: number | null = null;

  mesaEditando: Mesa | null = null;

  formMesa = {
    nombre: '',
    piso: 1
  };

  constructor(private router: Router) {}

  /* ===============================
     FILTRADO POR PISO (CLAVE)
     =============================== */
  get mesasFiltradas(): Mesa[] {
    return this.mesas.filter(m => m.piso === this.pisoActual);
  }

  irAPiso(piso: number) {
    this.pisoActual = piso;
    this.openDropdownId = null;
  }

  abrirMesa(mesa: Mesa) {
    this.router.navigate(['/mesa', mesa.id]);
  }

  agregarMesa() {
    const nuevoId = this.mesas.length
      ? Math.max(...this.mesas.map(m => m.id)) + 1
      : 1;

    const cantidadEnPiso = this.mesas.filter(m => m.piso === this.pisoActual).length + 1;

    this.mesas.push({
      id: nuevoId,
      nombre: `Mesa ${cantidadEnPiso}`,
      piso: this.pisoActual
    });
  }

  eliminarMesa(id: number) {
    this.mesas = this.mesas.filter(m => m.id !== id);
    this.openDropdownId = null;
  }

  toggleDropdown(id: number) {
    this.openDropdownId = this.openDropdownId === id ? null : id;
  }

  abrirEditarMesa(mesa: Mesa) {
    this.mesaEditando = mesa;
    this.formMesa = {
      nombre: mesa.nombre,
      piso: mesa.piso
    };
  }

  /* =====================================================
     🔴 NUEVA LÓGICA: ELIMINAR + CREAR EN OTRO PISO
     ===================================================== */
  guardarCambiosMesa() {
    if (!this.mesaEditando) return;

    const mesaOriginal = this.mesaEditando;

    // 1️⃣ eliminar la mesa original
    this.mesas = this.mesas.filter(m => m.id !== mesaOriginal.id);

    // 2️⃣ crear nueva mesa en el piso seleccionado
    const nuevoId = this.mesas.length
      ? Math.max(...this.mesas.map(m => m.id)) + 1
      : 1;

    this.mesas.push({
      id: nuevoId,
      nombre: this.formMesa.nombre,
      piso: this.formMesa.piso
    });

    // 3️⃣ cambiar vista al nuevo piso
    this.pisoActual = this.formMesa.piso;

    // limpiar estado
    this.mesaEditando = null;
    this.openDropdownId = null;
  }
}
