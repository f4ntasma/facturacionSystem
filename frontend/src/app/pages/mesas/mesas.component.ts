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

  irAPiso(piso: number) {
    this.pisoActual = piso;
    this.openDropdownId = null;
  }

  abrirMesa(mesa: Mesa) {
    this.router.navigate(['/mesa', mesa.id]);
  }

  agregarMesa() {
    const mesasDelPiso = this.mesas.filter(m => m.piso === this.pisoActual);
    const numero = mesasDelPiso.length + 1;

    this.mesas.push({
      id: Date.now(),
      nombre: `Mesa ${numero}`,
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
    this.formMesa = { ...mesa };
  }

  guardarCambiosMesa() {
    if (!this.mesaEditando) return;

    this.mesaEditando.nombre = this.formMesa.nombre;
    this.mesaEditando.piso = this.formMesa.piso;

    // ✅ CAMBIAR AUTOMÁTICAMENTE AL PISO DESTINO
    this.pisoActual = this.formMesa.piso;

    this.mesaEditando = null;
    this.openDropdownId = null;
  }

  get mesasFiltradas() {
    return this.mesas.filter(m => m.piso === this.pisoActual);
  }
}
