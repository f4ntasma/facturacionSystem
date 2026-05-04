import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-factura-adicional',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './adicional.component.html'
})
export class FacturaAdicionalComponent implements OnInit {
  adicionalForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private router: Router
  ) {
    this.adicionalForm = this.fb.group({
      // Leyendas
      legends: this.fb.array([]),
      
      // Guías de remisión
      guias: this.fb.array([]),
      
      // Documentos relacionados
      relDocs: this.fb.array([]),
      
      // Observación
      observacion: [''],
      
      // Compra
      compra: ['']
    });
  }

  ngOnInit() {
    this.cargarDatosGuardados();
    this.agregarLeyenda(); // Agregar leyenda por defecto
  }

  get legends() {
    return this.adicionalForm.get('legends') as FormArray;
  }

  get guias() {
    return this.adicionalForm.get('guias') as FormArray;
  }

  get relDocs() {
    return this.adicionalForm.get('relDocs') as FormArray;
  }

  cargarDatosGuardados() {
    const datosGuardados = localStorage.getItem('factura_totales');
    if (datosGuardados) {
      // Cargar datos previos si existen
    }
  }

  agregarLeyenda() {
    const legendGroup = this.fb.group({
      code: ['1000', [Validators.required]],
      value: ['', [Validators.required]]
    });
    this.legends.push(legendGroup);
  }

  eliminarLeyenda(index: number) {
    this.legends.removeAt(index);
  }

  agregarGuia() {
    const guiaGroup = this.fb.group({
      tipoDoc: ['09', [Validators.required]],
      nroDoc: ['', [Validators.required]]
    });
    this.guias.push(guiaGroup);
  }

  eliminarGuia(index: number) {
    this.guias.removeAt(index);
  }

  agregarRelDoc() {
    const relDocGroup = this.fb.group({
      tipoDoc: ['', [Validators.required]],
      nroDoc: ['', [Validators.required]]
    });
    this.relDocs.push(relDocGroup);
  }

  eliminarRelDoc(index: number) {
    this.relDocs.removeAt(index);
  }

  enviarFactura() {
    if (this.adicionalForm.valid) {
      localStorage.setItem('factura_adicional', JSON.stringify(this.adicionalForm.value));
      
      // Combinar todos los datos
      const facturaCompleta = {
        ...JSON.parse(localStorage.getItem('factura_documento') || '{}'),
        client: JSON.parse(localStorage.getItem('factura_cliente') || '{}'),
        details: JSON.parse(localStorage.getItem('factura_items') || '{}').items,
        ...JSON.parse(localStorage.getItem('factura_totales') || '{}'),
        ...JSON.parse(localStorage.getItem('factura_adicional') || '{}')
      };
      
      console.log('Factura completa:', facturaCompleta);
      
      // Aquí se enviaría al backend
      alert('Factura preparada para enviar a SUNAT. (Implementar llamada al backend)');
      
      // Limpiar localStorage
      localStorage.removeItem('factura_documento');
      localStorage.removeItem('factura_cliente');
      localStorage.removeItem('factura_items');
      localStorage.removeItem('factura_totales');
      localStorage.removeItem('factura_adicional');
      
      this.router.navigate(['/app/facturas']);
    }
  }

  volver() {
    this.router.navigate(['/app/facturas/nuevo/totales']);
  }
}
