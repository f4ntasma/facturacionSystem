import { Component } from '@angular/core';

@Component({
  selector: 'app-caja',
  templateUrl: './caja.component.html'
})
export class CajaComponent {

  inicioCaja: string = '16/12/25 21:17';

  ventas: number = 5000;
  ingresosEgresos: number = 240;
  total: number = 5240;

  totalContado: number = 0;
  descripcionCuadre: string = '';

  movimientos = [
    {
      descripcion: 'Inicio caja',
      tipo: 'Ingreso',
      monto: 250
    },
    {
      descripcion: 'Compra verduras',
      tipo: 'Egreso',
      monto: 10
    }
  ];

  agregarMovimiento() {
    console.log('Agregar movimiento');
  }

  cerrarCaja() {
    console.log('Caja cerrada');
  }

  cuadrarCaja() {
    console.log('Total contado:', this.totalContado);
    console.log('Descripción:', this.descripcionCuadre);
  }
}