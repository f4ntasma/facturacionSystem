import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-factura-items',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './items.component.html'
})
export class FacturaItemsComponent implements OnInit {
  itemsForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private router: Router
  ) {
    this.itemsForm = this.fb.group({
      items: this.fb.array([])
    });
  }

  ngOnInit() {
    this.cargarDatosGuardados();
    if (this.items.length === 0) {
      this.agregarItem();
    }
  }

  get items() {
    return this.itemsForm.get('items') as FormArray;
  }

  cargarDatosGuardados() {
    const datosGuardados = localStorage.getItem('factura_cliente');
    if (datosGuardados) {
      // Cargar datos previos si existen
    }
  }

  agregarItem() {
    const itemGroup = this.fb.group({
      codProducto: ['', [Validators.required]],
      unidad: ['NIU', [Validators.required]],
      descripcion: ['', [Validators.required]],
      cantidad: [1, [Validators.required, Validators.min(1)]],
      mtoValorUnitario: [0, [Validators.required, Validators.min(0)]],
      mtoBaseIgv: [0],
      porcentajeIgv: [18],
      igv: [0],
      tipAfeIgv: [10], // Gravado - Operación Onerosa
      totalImpuestos: [0],
      mtoValorVenta: [0],
      mtoPrecioUnitario: [0]
    });

    this.items.push(itemGroup);
  }

  eliminarItem(index: number) {
    this.items.removeAt(index);
  }

  calcularItem(index: number) {
    const item = this.items.at(index);
    const cantidad = item.get('cantidad')?.value || 0;
    const precioUnitario = item.get('mtoValorUnitario')?.value || 0;
    const porcentajeIgv = item.get('porcentajeIgv')?.value || 18;
    
    const valorVenta = cantidad * precioUnitario;
    const igv = valorVenta * (porcentajeIgv / 100);
    const precioUnitarioConIgv = precioUnitario * (1 + porcentajeIgv / 100);
    
    item.patchValue({
      mtoBaseIgv: valorVenta,
      igv: igv,
      totalImpuestos: igv,
      mtoValorVenta: valorVenta,
      mtoPrecioUnitario: precioUnitarioConIgv
    });
  }

  getValorVenta(index: number): number {
    const item = this.items.at(index);
    return item.get('mtoValorVenta')?.value || 0;
  }

  getIGV(index: number): number {
    const item = this.items.at(index);
    return item.get('igv')?.value || 0;
  }

  getSubtotalItem(index: number): number {
    const item = this.items.at(index);
    const cantidad = item.get('cantidad')?.value || 0;
    const precioUnitario = item.get('mtoPrecioUnitario')?.value || 0;
    return cantidad * precioUnitario;
  }

  guardarYContinuar() {
    if (this.itemsForm.valid && this.items.length > 0) {
      localStorage.setItem('factura_items', JSON.stringify(this.itemsForm.value));
      this.router.navigate(['/app/facturas/nuevo/totales']);
    }
  }

  volver() {
    this.router.navigate(['/app/facturas/nuevo/cliente']);
  }
}
