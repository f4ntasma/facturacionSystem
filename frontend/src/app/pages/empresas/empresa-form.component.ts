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
  template: `
    <p-card>
      <ng-template pTemplate="header">
        <div class="flex align-items-center justify-content-between p-3 pb-0">
          <h3 class="m-0">{{ isEditing ? 'Editar Empresa' : 'Nueva Empresa' }}</h3>
          <button pButton type="button" icon="pi pi-arrow-left" label="Volver" 
                  class="p-button-outlined" (click)="volver()"></button>
        </div>
      </ng-template>

      <form [formGroup]="empresaForm" (ngSubmit)="onSubmit()" class="grid">
        <div class="col-12 md:col-6">
          <label for="nombre" class="block text-900 font-medium mb-2">Nombre *</label>
          <input pInputText id="nombre" formControlName="nombre" 
                 class="w-full" placeholder="Nombre de la empresa"
                 [class.ng-invalid]="empresaForm.get('nombre')?.invalid && empresaForm.get('nombre')?.touched">
          <small class="p-error block" 
                 *ngIf="empresaForm.get('nombre')?.invalid && empresaForm.get('nombre')?.touched">
            El nombre es requerido
          </small>
        </div>

        <div class="col-12 md:col-6">
          <label for="ruc" class="block text-900 font-medium mb-2">RUC *</label>
          <input pInputText id="ruc" formControlName="ruc" 
                 class="w-full" placeholder="RUC de la empresa"
                 [class.ng-invalid]="empresaForm.get('ruc')?.invalid && empresaForm.get('ruc')?.touched">
          <small class="p-error block" 
                 *ngIf="empresaForm.get('ruc')?.invalid && empresaForm.get('ruc')?.touched">
            El RUC es requerido
          </small>
        </div>

        <div class="col-12">
          <label for="direccion" class="block text-900 font-medium mb-2">Dirección *</label>
          <input pInputText id="direccion" formControlName="direccion" 
                 class="w-full" placeholder="Dirección de la empresa"
                 [class.ng-invalid]="empresaForm.get('direccion')?.invalid && empresaForm.get('direccion')?.touched">
          <small class="p-error block" 
                 *ngIf="empresaForm.get('direccion')?.invalid && empresaForm.get('direccion')?.touched">
            La dirección es requerida
          </small>
        </div>

        <div class="col-12 md:col-6">
          <label for="telefono" class="block text-900 font-medium mb-2">Teléfono</label>
          <input pInputText id="telefono" formControlName="telefono" 
                 class="w-full" placeholder="Teléfono de contacto">
        </div>

        <div class="col-12 md:col-6">
          <label for="email" class="block text-900 font-medium mb-2">Email</label>
          <input pInputText id="email" formControlName="email" type="email"
                 class="w-full" placeholder="Email de contacto"
                 [class.ng-invalid]="empresaForm.get('email')?.invalid && empresaForm.get('email')?.touched">
          <small class="p-error block" 
                 *ngIf="empresaForm.get('email')?.invalid && empresaForm.get('email')?.touched">
            Ingrese un email válido
          </small>
        </div>

        <div class="col-12">
          <div class="flex gap-2 justify-content-end">
            <button pButton type="button" label="Cancelar" 
                    class="p-button-outlined" (click)="volver()"></button>
            <button pButton type="submit" [label]="isEditing ? 'Actualizar' : 'Crear'" 
                    [loading]="loading" [disabled]="empresaForm.invalid"
                    class="p-button-success"></button>
          </div>
        </div>
      </form>
    </p-card>

    <p-toast></p-toast>
  `
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