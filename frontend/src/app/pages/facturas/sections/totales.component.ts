import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-factura-totales',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './totales.component.html'
})
export class FacturaTotalesComponent implements OnInit {
  totalesForm: FormGroup;

  mtoOperGravadas = 0;
  mtoOperInafectas = 0;
  mtoOperExoneradas = 0;
  mtoIGV = 0;
  mtoISC = 0;
  mtoOtrosTributos = 0;
  icbper = 0;
  valorVenta = 0;
  subTotal = 0;
  mtoImpVenta = 0;

  constructor(
    private fb: FormBuilder,
    private router: Router
  ) {
    this.totalesForm = this.fb.group({
      mtoOperGravadas: [0],
      mtoOperInafectas: [0],
      mtoOperExoneradas: [0],
      mtoOperExportacion: [0],
      mtoIGV: [0],
      mtoIGVGratuitas: [0],
      mtoISC: [0],
      mtoOtrosTributos: [0],
      icbper: [0],
      valorVenta: [0],
      subTotal: [0],
      mtoImpVenta: [0],
      totalImpuestos: [0]
    });
  }

  ngOnInit() {
    this.calcularTotales();
  }

  calcularTotales() {
    const itemsData = localStorage.getItem('factura_items');
    if (itemsData) {
      const items = JSON.parse(itemsData).items;
      
      let mtoOperGravadas = 0;
      let mtoIGV = 0;
      let valorVenta = 0;
      
      items.forEach((item: any) => {
        mtoOperGravadas += item.mtoValorVenta || 0;
        mtoIGV += item.igv || 0;
        valorVenta += item.mtoValorVenta || 0;
      });
      
      const mtoImpVenta = mtoOperGravadas + mtoIGV;
      
      this.totalesForm.patchValue({
        mtoOperGravadas: mtoOperGravadas,
        mtoIGV: mtoIGV,
        valorVenta: valorVenta,
        subTotal: mtoOperGravadas,
        mtoImpVenta: mtoImpVenta,
        totalImpuestos: mtoIGV
      });
      
      this.mtoOperGravadas = mtoOperGravadas;
      this.mtoIGV = mtoIGV;
      this.valorVenta = valorVenta;
      this.subTotal = mtoOperGravadas;
      this.mtoImpVenta = mtoImpVenta;
    }
  }

  guardarYContinuar() {
    localStorage.setItem('factura_totales', JSON.stringify(this.totalesForm.value));
    this.router.navigate(['/app/facturas/nuevo/adicional']);
  }

  volver() {
    this.router.navigate(['/app/facturas/nuevo/items']);
  }
}
