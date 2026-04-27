import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { EmpresaService, Empresa } from '../../services/empresa.service';

@Component({
  selector: 'app-empresa-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    CardModule,
    InputTextModule,
    ButtonModule,
    ToastModule
  ],
  providers: [MessageService],
  templateUrl: './empresa-form.component.html'
})
export class EmpresaFormComponent implements OnInit {
  empresaForm: FormGroup;
  isEditing = false;
  empresaId: number | null = null;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private empresaService: EmpresaService,
    private router: Router,
    private route: ActivatedRoute,
    private messageService: MessageService
  ) {
    this.empresaForm = this.fb.group({
      nombre: ['', [Validators.required]],
      ruc: ['', [Validators.required]],
      direccion: ['', [Validators.required]],
      telefono: [''],
      email: ['', [Validators.email]]
    });
  }

  ngOnInit() {
    this.empresaId = this.route.snapshot.params['id'];
    this.isEditing = !!this.empresaId;

    if (this.isEditing) {
      this.loadEmpresa();
    }
  }

  loadEmpresa() {
    if (this.empresaId) {
      this.empresaService.getEmpresa(this.empresaId).subscribe({
        next: (empresa) => {
          this.empresaForm.patchValue(empresa);
        },
        error: (error) => {
          console.error('Error cargando empresa:', error);
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'No se pudo cargar la empresa'
          });
        }
      });
    }
  }

  onSubmit() {
    if (this.empresaForm.valid) {
      this.loading = true;
      const empresaData: Empresa = this.empresaForm.value;

      const operation = this.isEditing 
        ? this.empresaService.updateEmpresa(this.empresaId!, empresaData)
        : this.empresaService.createEmpresa(empresaData);

      operation.subscribe({
        next: (empresa) => {
          this.messageService.add({
            severity: 'success',
            summary: 'Éxito',
            detail: `Empresa ${this.isEditing ? 'actualizada' : 'creada'} correctamente`
          });
          setTimeout(() => {
            this.router.navigate(['/empresas']);
          }, 1500);
        },
        error: (error) => {
          console.error('Error guardando empresa:', error);
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: `No se pudo ${this.isEditing ? 'actualizar' : 'crear'} la empresa`
          });
          this.loading = false;
        }
      });
    }
  }

  volver() {
    this.router.navigate(['/empresas']);
  }
}