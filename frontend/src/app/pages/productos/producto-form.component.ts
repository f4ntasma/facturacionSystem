import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { ProductoService, Producto } from '../../services/producto.service';
import { EmpresaService, Empresa } from '../../services/empresa.service';

@Component({
  selector: 'app-producto-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    CardModule,
    InputTextModule,
    InputNumberModule,
    ButtonModule,
    ToastModule
  ],
  providers: [MessageService],
  template: `
    <p-card>
      <ng-template pTemplate="header">
        <div class="flex align-items-center justify-content-between p-3 pb-0">
          <h3 class="m-0">{{ isEditing ? 'Editar Producto' : 'Nuevo Producto' }}</h3>
          <button pButton type="button" icon="pi pi-arrow-left" label="Volver" 
                  class="p-button-outlined" (click)="volver()"></button>
        </div>
      </ng-template>

      <form [formGroup]="productoForm" (ngSubmit)="onSubmit()" class="grid">
        <div class="col-12 md:col-6">
          <label for="nombre" class="block text-900 font-medium mb-2">Nombre *</label>
          <input pInputText id="nombre" formControlName="nombre" 
                 class="w-full" placeholder="Nombre del producto"
                 [class.ng-invalid]="productoForm.get('nombre')?.invalid && productoForm.get('nombre')?.touched">
          <small class="p-error block" 
                 *ngIf="productoForm.get('nombre')?.invalid && productoForm.get('nombre')?.touched">
            El nombre es requerido
          </small>
        </div>

        <div class="col-12 md:col-6">
          <label for="categoria" class="block text-900 font-medium mb-2">Categoría</label>
          <input pInputText id="categoria" formControlName="categoria" 
                 class="w-full" placeholder="Categoría del producto">
        </div>

        <div class="col-12">
          <label for="descripcion" class="block text-900 font-medium mb-2">Descripción</label>
          <input pInputText id="descripcion" formControlName="descripcion" 
                 class="w-full" placeholder="Descripción del producto">
        </div>

        <div class="col-12 md:col-4">
          <label for="precio" class="block text-900 font-medium mb-2">Precio *</label>
          <p-inputNumber id="precio" formControlName="precio" 
                         mode="currency" currency="USD" locale="en-US"
                         class="w-full" placeholder="0.00"
                         [class.ng-invalid]="productoForm.get('precio')?.invalid && productoForm.get('precio')?.touched">
          </p-inputNumber>
          <small class="p-error block" 
                 *ngIf="productoForm.get('precio')?.invalid && productoForm.get('precio')?.touched">
            El precio es requerido y debe ser mayor a 0
          </small>
        </div>

        <div class="col-12 md:col-4">
          <label for="stock" class="block text-900 font-medium mb-2">Stock *</label>
          <p-inputNumber id="stock" formControlName="stock" 
                         class="w-full" placeholder="0"
                         [class.ng-invalid]="productoForm.get('stock')?.invalid && productoForm.get('stock')?.touched">
          </p-inputNumber>
          <small class="p-error block" 
                 *ngIf="productoForm.get('stock')?.invalid && productoForm.get('stock')?.touched">
            El stock es requerido
          </small>
        </div>

        <div class="col-12 md:col-4">
          <label for="empresaId" class="block text-900 font-medium mb-2">Empresa ID *</label>
          <p-inputNumber id="empresaId" formControlName="empresaId" 
                         class="w-full" placeholder="ID de la empresa"
                         [class.ng-invalid]="productoForm.get('empresaId')?.invalid && productoForm.get('empresaId')?.touched">
          </p-inputNumber>
          <small class="p-error block" 
                 *ngIf="productoForm.get('empresaId')?.invalid && productoForm.get('empresaId')?.touched">
            Debe ingresar el ID de la empresa
          </small>
        </div>

        <div class="col-12">
          <div class="flex gap-2 justify-content-end">
            <button pButton type="button" label="Cancelar" 
                    class="p-button-outlined" (click)="volver()"></button>
            <button pButton type="submit" [label]="isEditing ? 'Actualizar' : 'Crear'" 
                    [loading]="loading" [disabled]="productoForm.invalid"
                    class="p-button-success"></button>
          </div>
        </div>
      </form>
    </p-card>

    <p-toast></p-toast>
  `
})
export class ProductoFormComponent implements OnInit {
  productoForm: FormGroup;
  isEditing = false;
  productoId: number | null = null;
  loading = false;
  empresas: Empresa[] = [];

  constructor(
    private fb: FormBuilder,
    private productoService: ProductoService,
    private empresaService: EmpresaService,
    private router: Router,
    private route: ActivatedRoute,
    private messageService: MessageService
  ) {
    this.productoForm = this.fb.group({
      nombre: ['', [Validators.required]],
      descripcion: [''],
      precio: [0, [Validators.required, Validators.min(0.01)]],
      stock: [0, [Validators.required, Validators.min(0)]],
      categoria: [''],
      empresaId: [null, [Validators.required]]
    });
  }

  ngOnInit() {
    this.productoId = this.route.snapshot.params['id'];
    this.isEditing = !!this.productoId;

    this.loadEmpresas();

    if (this.isEditing) {
      this.loadProducto();
    }
  }

  loadEmpresas() {
    this.empresaService.getEmpresas().subscribe({
      next: (empresas) => {
        this.empresas = empresas;
      },
      error: (error) => {
        console.error('Error cargando empresas:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudieron cargar las empresas'
        });
      }
    });
  }

  loadProducto() {
    if (this.productoId) {
      this.productoService.getProducto(this.productoId).subscribe({
        next: (producto) => {
          this.productoForm.patchValue(producto);
        },
        error: (error) => {
          console.error('Error cargando producto:', error);
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'No se pudo cargar el producto'
          });
        }
      });
    }
  }

  onSubmit() {
    if (this.productoForm.valid) {
      this.loading = true;
      const productoData: Producto = this.productoForm.value;

      const operation = this.isEditing 
        ? this.productoService.updateProducto(this.productoId!, productoData)
        : this.productoService.createProducto(productoData);

      operation.subscribe({
        next: (producto) => {
          this.messageService.add({
            severity: 'success',
            summary: 'Éxito',
            detail: `Producto ${this.isEditing ? 'actualizado' : 'creado'} correctamente`
          });
          setTimeout(() => {
            this.router.navigate(['/productos']);
          }, 1500);
        },
        error: (error) => {
          console.error('Error guardando producto:', error);
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: `No se pudo ${this.isEditing ? 'actualizar' : 'crear'} el producto`
          });
          this.loading = false;
        }
      });
    }
  }

  volver() {
    this.router.navigate(['/productos']);
  }
}