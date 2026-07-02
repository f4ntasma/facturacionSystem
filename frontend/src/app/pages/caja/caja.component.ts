import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';

interface Movimiento {
  descripcion: string;
  tipo: 'Ingreso' | 'Egreso';
  monto: number;
  hora: string;
}

interface CuadreHistorial {
  fecha: string;
  horaEntrada: string;
  horaSalida: string;
  montoInicial: number;
  totalIngresos: number;
  totalEgresos: number;
  totalEsperado: number;
  totalContado: number;
  diferencia: number;
  diferenciaLabel: string;
  observaciones: string;
  movimientos: Movimiento[];
}

@Component({
  selector: 'app-caja',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './caja.component.html',
  styleUrls: ['./caja.component.css']
})
export class CajaComponent implements OnInit {

  private storageKey = 'factullama_caja';
  private historialKey = 'factullama_caja_historial';

  // Vista activa: 'lista' | 'caja'
  vista: 'lista' | 'caja' = 'lista';

  // Estado caja activa
  montoInicial: number = 0;
  horaEntrada: string = '';
  horaSalida: string = '';
  totalContado: number = 0;
  descripcionCuadre: string = '';
  mostrarFormMovimiento = false;
  mostrarConfirmCierre = false;

  nuevoMovimiento: { descripcion: string; tipo: 'Ingreso' | 'Egreso'; monto: number | null } = {
    descripcion: '',
    tipo: 'Ingreso',
    monto: null
  };

  movimientos: Movimiento[] = [];

  // Historial
  historial: CuadreHistorial[] = [];
  cuadreSeleccionado: CuadreHistorial | null = null;
  mostrarDetalle = false;

  // Filtros lista
  searchFecha: string = '';

  constructor(private authService: AuthService) {}

  ngOnInit() {
    const userId = this.authService.getUserId();
    this.storageKey = `factullama_caja_${userId}`;
    this.historialKey = `factullama_caja_historial_${userId}`;
    this.cargarEstado();
    this.cargarHistorial();
  }

  // ── Persistencia ─────────────────────────────────────

  guardarEstado() {
    localStorage.setItem(this.storageKey, JSON.stringify({
      montoInicial: this.montoInicial,
      horaEntrada: this.horaEntrada,
      horaSalida: this.horaSalida,
      totalContado: this.totalContado,
      descripcionCuadre: this.descripcionCuadre,
      movimientos: this.movimientos
    }));
  }

  cargarEstado() {
    const raw = localStorage.getItem(this.storageKey);
    if (!raw) return;
    try {
      const e = JSON.parse(raw);
      this.montoInicial      = e.montoInicial      ?? 0;
      this.horaEntrada       = e.horaEntrada       ?? '';
      this.horaSalida        = e.horaSalida        ?? '';
      this.totalContado      = e.totalContado      ?? 0;
      this.descripcionCuadre = e.descripcionCuadre ?? '';
      this.movimientos       = e.movimientos       ?? [];
    } catch { }
  }

  cargarHistorial() {
    const raw = localStorage.getItem(this.historialKey);
    try { this.historial = raw ? JSON.parse(raw) : []; }
    catch { this.historial = []; }
  }

  guardarEnHistorial(cuadre: CuadreHistorial) {
    this.historial.unshift(cuadre);
    localStorage.setItem(this.historialKey, JSON.stringify(this.historial));
  }

  // ── Totales ──────────────────────────────────────────

  get ventas(): number { return 0; }

  get totalIngresos(): number {
    return this.movimientos.filter(m => m.tipo === 'Ingreso').reduce((s, m) => s + m.monto, 0);
  }

  get totalEgresos(): number {
    return this.movimientos.filter(m => m.tipo === 'Egreso').reduce((s, m) => s + m.monto, 0);
  }

  get ingresosEgresos(): number {
    return this.totalIngresos - this.totalEgresos;
  }

  get total(): number {
    return this.montoInicial + this.ventas + this.ingresosEgresos;
  }

  get diferencia(): number {
    return this.totalContado - this.total;
  }

  get diferenciaClass(): string {
    return this.diferencia > 0 ? 'text-success' : this.diferencia < 0 ? 'text-error' : 'text-muted';
  }

  get diferenciaLabel(): string {
    return this.diferencia > 0 ? 'Sobrante' : this.diferencia < 0 ? 'Faltante' : 'Cuadrado';
  }

  getDiferenciaClass(dif: number): string {
    return dif > 0 ? 'text-success' : dif < 0 ? 'text-error' : 'text-muted';
  }

  getDiferenciaLabel(dif: number): string {
    return dif > 0 ? 'Sobrante' : dif < 0 ? 'Faltante' : 'Cuadrado';
  }

  // ── Filtro lista ─────────────────────────────────────

  get historialFiltrado(): CuadreHistorial[] {
    if (!this.searchFecha) return this.historial;
    return this.historial.filter(c => c.fecha.includes(this.searchFecha));
  }

  // ── Navegación ───────────────────────────────────────

  nuevoCuadre() {
    this.vista = 'caja';
  }

  volverLista() {
    this.vista = 'lista';
    this.mostrarFormMovimiento = false;
    this.mostrarConfirmCierre = false;
  }

  // ── Acciones caja ────────────────────────────────────

  onDatosChange() { this.guardarEstado(); }

  toggleFormMovimiento() {
    this.mostrarFormMovimiento = !this.mostrarFormMovimiento;
    if (!this.mostrarFormMovimiento) this.resetNuevoMovimiento();
  }

  agregarMovimiento() {
    if (!this.nuevoMovimiento.descripcion || !this.nuevoMovimiento.monto || this.nuevoMovimiento.monto <= 0) return;
    const ahora = new Date();
    this.movimientos.push({
      descripcion: this.nuevoMovimiento.descripcion,
      tipo: this.nuevoMovimiento.tipo,
      monto: this.nuevoMovimiento.monto,
      hora: ahora.toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit' })
    });
    this.guardarEstado();
    this.resetNuevoMovimiento();
    this.mostrarFormMovimiento = false;
  }

  resetNuevoMovimiento() {
    this.nuevoMovimiento = { descripcion: '', tipo: 'Ingreso', monto: null };
  }

  cuadrarCaja() {
    if (this.totalContado <= 0) return;
    const cuadre: CuadreHistorial = {
      fecha: new Date().toLocaleDateString('es-PE'),
      horaEntrada: this.horaEntrada,
      horaSalida: this.horaSalida,
      montoInicial: this.montoInicial,
      totalIngresos: this.totalIngresos,
      totalEgresos: this.totalEgresos,
      totalEsperado: this.total,
      totalContado: this.totalContado,
      diferencia: this.diferencia,
      diferenciaLabel: this.diferenciaLabel,
      observaciones: this.descripcionCuadre,
      movimientos: [...this.movimientos]
    };
    this.guardarEnHistorial(cuadre);
    localStorage.removeItem(this.storageKey);
    this.montoInicial = 0; this.horaEntrada = ''; this.horaSalida = '';
    this.movimientos = []; this.totalContado = 0; this.descripcionCuadre = '';
    this.vista = 'lista';
  }

  confirmarCierre() { this.mostrarConfirmCierre = true; }
  cancelarCierre()  { this.mostrarConfirmCierre = false; }

  cerrarCaja() {
    localStorage.removeItem(this.storageKey);
    this.montoInicial = 0; this.horaEntrada = ''; this.horaSalida = '';
    this.movimientos = []; this.totalContado = 0; this.descripcionCuadre = '';
    this.mostrarConfirmCierre = false;
    this.vista = 'lista';
  }

  // ── Detalle cuadre ───────────────────────────────────

  verDetalle(cuadre: CuadreHistorial) {
    this.cuadreSeleccionado = cuadre;
    this.mostrarDetalle = true;
  }

  cerrarDetalle() {
    this.mostrarDetalle = false;
    this.cuadreSeleccionado = null;
  }

  eliminarCuadre(index: number) {
    this.historial.splice(index, 1);
    localStorage.setItem(this.historialKey, JSON.stringify(this.historial));
  }
}
