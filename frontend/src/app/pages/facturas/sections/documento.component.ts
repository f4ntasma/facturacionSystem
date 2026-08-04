import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-factura-documento',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './documento.component.html'
})
export class FacturaDocumentoComponent implements OnInit {
  documentoForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private router: Router
  ) {
    this.documentoForm = this.fb.group({
      tipoDoc: ['01', [Validators.required]], // 01 = Factura, 03 = Boleta
      serie: ['F001', [Validators.required]],
      correlativo: ['', [Validators.required]],
      fechaEmision: [new Date().toISOString().split('T')[0], [Validators.required]],
      fecVencimiento: [''],
      tipoMoneda: ['PEN', [Validators.required]],
      tipoOperacion: ['0101', [Validators.required]], // Venta interna
      ublVersion: ['2.1']
    });
  }

  ngOnInit() {}

  guardarYContinuar() {
    if (this.documentoForm.valid) {
      // Guardar datos en localStorage o servicio compartido
      localStorage.setItem('factura_documento', JSON.stringify(this.documentoForm.value));
      this.router.navigate(['/app/facturas/nuevo/cliente']);
    }
  }

  volver() {
    this.router.navigate(['/app/facturas/nuevo']);
  }
}
