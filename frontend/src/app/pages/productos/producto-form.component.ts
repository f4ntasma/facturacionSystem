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
  templateUrl: './producto-form.component.html'
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