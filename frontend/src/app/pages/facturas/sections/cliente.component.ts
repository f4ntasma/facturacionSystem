import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { EmpresaService, Empresa } from '../../../services/empresa.service';

@Component({
  selector: 'app-factura-cliente',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './cliente.component.html'
})
export class FacturaClienteComponent implements OnInit {
  clienteForm: FormGroup;
  empresas: Empresa[] = [];

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private empresaService: EmpresaService
  ) {
    this.clienteForm = this.fb.group({
      // Datos del cliente
      tipoDoc: ['6', [Validators.required]], // 1 = DNI, 6 = RUC
      numDoc: ['', [Validators.required]],
      rznSocial: ['', [Validators.required]],
      
      // Dirección del cliente
      direccion: [''],
      provincia: [''],
      departamento: [''],
      distrito: [''],
      ubigueo: [''],
      
      // Datos de la empresa emisora
      empresaId: [null, [Validators.required]]
    });
  }

  ngOnInit() {
    this.loadEmpresas();
    this.cargarDatosGuardados();
  }

  loadEmpresas() {
    this.empresaService.getEmpresas().subscribe({
      next: (empresas) => {
        this.empresas = empresas;
      },
      error: (error) => console.error('Error cargando empresas:', error)
    });
  }

  cargarDatosGuardados() {
    const datosGuardados = localStorage.getItem('factura_documento');
    if (datosGuardados) {
      // Cargar datos previos si existen
    }
  }

  guardarYContinuar() {
    if (this.clienteForm.valid) {
      localStorage.setItem('factura_cliente', JSON.stringify(this.clienteForm.value));
      this.router.navigate(['/app/facturas/nuevo/items']);
    }
  }

  volver() {
    this.router.navigate(['/app/facturas/nuevo/documento']);
  }
}
