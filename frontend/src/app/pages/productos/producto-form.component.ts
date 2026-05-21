import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { ProductoService, Producto } from '../../services/producto.service';

@Component({
  selector: 'app-producto-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ButtonModule,
    ToastModule
  ],
  providers: [MessageService],
  templateUrl: './producto-form.component.html'
})
export class ProductoFormComponent implements OnInit {
  productoForm: FormGroup;
  isEditing = false;
  productoId: string | null = null;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private productoService: ProductoService,
    private router: Router,
    private route: ActivatedRoute,
    private messageService: MessageService
  ) {
    this.productoForm = this.fb.group({
      nombre:      ['', [Validators.required]],
      sku:         [''],
      descripcion: [''],
      categoria:   [''],
      precio:      [0, [Validators.required, Validators.min(0.01)]],
      stock:       [0, [Validators.required, Validators.min(0)]]
    });
  }

  ngOnInit() {
    this.productoId = this.route.snapshot.params['id'];
    this.isEditing = !!this.productoId;
    if (this.isEditing) {
      this.loadProducto();
    }
  }

  loadProducto() {
    if (this.productoId) {
      this.productoService.getProducto(this.productoId).subscribe({
        next: (producto) => this.productoForm.patchValue(producto),
        error: () => this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo cargar el producto' })
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
        next: () => {
          this.messageService.add({ severity: 'success', summary: 'Éxito', detail: `Producto ${this.isEditing ? 'actualizado' : 'creado'} correctamente` });
          setTimeout(() => this.router.navigate(['/app/productos']), 1500);
        },
        error: () => {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: `No se pudo ${this.isEditing ? 'actualizar' : 'crear'} el producto` });
          this.loading = false;
        }
      });
    }
  }

  volver() {
    this.router.navigate(['/app/productos']);
  }
}