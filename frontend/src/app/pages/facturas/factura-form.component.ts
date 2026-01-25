import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { FacturaService, Factura, FacturaItem } from '../../services/factura.service';
import { ProductoService, Producto } from '../../services/producto.service';
import { EmpresaService, Empresa } from '../../services/empresa.service';

@Component({
  selector: 'app-factura-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    CardModule,
    InputTextModule,
    InputNumberModule,
    ButtonModule,
    TableModule,
    ToastModule
  ],
  providers: [MessageService],
  template: `
    <p-card>
      <ng-template pTemplate="header">
        <div class="flex align-items-center justify-content-between p-3 pb-0">
          <h3 class="m-0">{{ isEditing ? 'Editar Factura' : 'Nueva Factura' }}</h3>
          <button pButton type="button" icon="pi pi-arrow-left" label="Volver" 
                  class="p-button-outlined" (click)="volver()"></button>
        </div>
      </ng-template>

      <form [formGroup]="facturaForm" (ngSubmit)="onSubmit()">
        <!-- Información del Cliente -->
        <div class="grid mb-4">
          <div class="col-12">
            <h4>Información del Cliente</h4>
          </div>
          <div class="col-12 md:col-4">
            <label for="clienteNombre" class="block text-900 font-medium mb-2">Nombre del Cliente *</label>
            <input pInputText id="clienteNombre" formControlName="clienteNombre" 
                   class="w-full" placeholder="Nombre del cliente">
          </div>
          <div class="col-12 md:col-4">
            <label for="clienteRuc" class="block text-900 font-medium mb-2">RUC</label>
            <input pInputText id="clienteRuc" formControlName="clienteRuc" 
                   class="w-full" placeholder="RUC del cliente">
          </div>
          <div class="col-12 md:col-4">
            <label for="empresaId" class="block text-900 font-medium mb-2">Empresa ID *</label>
            <p-inputNumber id="empresaId" formControlName="empresaId" 
                           class="w-full" placeholder="ID de la empresa">
            </p-inputNumber>
          </div>
          <div class="col-12">
            <label for="clienteDireccion" class="block text-900 font-medium mb-2">Dirección</label>
            <input pInputText id="clienteDireccion" formControlName="clienteDireccion" 
                   class="w-full" placeholder="Dirección del cliente">
          </div>
        </div>

        <!-- Items de la Factura -->
        <div class="mb-4">
          <div class="flex align-items-center justify-content-between mb-3">
            <h4>Items de la Factura</h4>
            <button pButton type="button" icon="pi pi-plus" label="Agregar Item" 
                    class="p-button-sm" (click)="agregarItem()"></button>
          </div>

          <p-table [value]="items.controls" responsiveLayout="scroll">
            <ng-template pTemplate="header">
              <tr>
                <th>Producto</th>
                <th>Cantidad</th>
                <th>Precio</th>
                <th>Subtotal</th>
                <th>Acciones</th>
              </tr>
            </ng-template>
            <ng-template pTemplate="body" let-item let-i="rowIndex">
              <tr [formGroup]="item">
                <td>
                  <p-inputNumber formControlName="productoId" 
                                 class="w-full" placeholder="ID del producto">
                  </p-inputNumber>
                </td>
                <td>
                  <p-inputNumber formControlName="cantidad" 
                                 class="w-full" [min]="1"
                                 (onInput)="calcularSubtotal(i)">
                  </p-inputNumber>
                </td>
                <td>
                  <p-inputNumber formControlName="precio" 
                                 mode="currency" currency="USD" locale="en-US"
                                 class="w-full" [readonly]="true">
                  </p-inputNumber>
                </td>
                <td>
                  <span class="font-medium">\${{ getSubtotal(i) | number:'1.2-2' }}</span>
                </td>
                <td>
                  <button pButton type="button" icon="pi pi-trash" 
                          class="p-button-rounded p-button-text p-button-danger p-button-sm"
                          (click)="eliminarItem(i)"></button>
                </td>
              </tr>
            </ng-template>
            <ng-template pTemplate="emptymessage">
              <tr>
                <td colspan="5" class="text-center p-3">
                  <span>No hay items agregados</span>
                </td>
              </tr>
            </ng-template>
          </p-table>
        </div>

        <!-- Totales -->
        <div class="grid mb-4">
          <div class="col-12 md:col-8"></div>
          <div class="col-12 md:col-4">
            <div class="border-1 border-300 border-round p-3">
              <div class="flex justify-content-between mb-2">
                <span>Subtotal:</span>
                <span>\${{ subtotal | number:'1.2-2' }}</span>
              </div>
              <div class="flex justify-content-between mb-2">
                <span>Impuesto (12%):</span>
                <span>\${{ impuesto | number:'1.2-2' }}</span>
              </div>
              <div class="flex justify-content-between font-bold text-lg">
                <span>Total:</span>
                <span>\${{ total | number:'1.2-2' }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Botones -->
        <div class="flex gap-2 justify-content-end">
          <button pButton type="button" label="Cancelar" 
                  class="p-button-outlined" (click)="volver()"></button>
          <button pButton type="submit" [label]="isEditing ? 'Actualizar' : 'Crear'" 
                  [loading]="loading" [disabled]="facturaForm.invalid || items.length === 0"
                  class="p-button-success"></button>
        </div>
      </form>
    </p-card>

    <p-toast></p-toast>
  `
})
export class FacturaFormComponent implements OnInit {
  facturaForm: FormGroup;
  isEditing = false;
  facturaId: number | null = null;
  loading = false;
  empresas: Empresa[] = [];
  productos: Producto[] = [];

  subtotal = 0;
  impuesto = 0;
  total = 0;

  constructor(
    private fb: FormBuilder,
    private facturaService: FacturaService,
    private productoService: ProductoService,
    private empresaService: EmpresaService,
    private router: Router,
    private route: ActivatedRoute,
    private messageService: MessageService
  ) {
    this.facturaForm = this.fb.group({
      clienteNombre: ['', [Validators.required]],
      clienteRuc: [''],
      clienteDireccion: [''],
      empresaId: [null, [Validators.required]],
      items: this.fb.array([])
    });
  }

  get items() {
    return this.facturaForm.get('items') as FormArray;
  }

  ngOnInit() {
    this.facturaId = this.route.snapshot.params['id'];
    this.isEditing = !!this.facturaId;

    this.loadEmpresas();
    this.loadProductos();

    if (this.isEditing) {
      this.loadFactura();
    } else {
      this.agregarItem(); // Agregar un item por defecto
    }
  }

  loadEmpresas() {
    this.empresaService.getEmpresas().subscribe({
      next: (empresas) => {
        this.empresas = empresas;
      },
      error: (error) => console.error('Error cargando empresas:', error)
    });
  }

  loadProductos() {
    this.productoService.getProductos().subscribe({
      next: (productos) => {
        this.productos = productos;
      },
      error: (error) => console.error('Error cargando productos:', error)
    });
  }

  loadFactura() {
    if (this.facturaId) {
      this.facturaService.getFactura(this.facturaId).subscribe({
        next: (factura) => {
          this.facturaForm.patchValue({
            clienteNombre: factura.clienteNombre,
            clienteRuc: factura.clienteRuc,
            clienteDireccion: factura.clienteDireccion,
            empresaId: factura.empresaId
          });

          // Cargar items
          factura.items.forEach(item => {
            this.items.push(this.fb.group({
              productoId: [item.productoId, [Validators.required]],
              cantidad: [item.cantidad, [Validators.required, Validators.min(1)]],
              precio: [item.precio, [Validators.required]]
            }));
          });

          this.calcularTotales();
        },
        error: (error) => {
          console.error('Error cargando factura:', error);
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'No se pudo cargar la factura'
          });
        }
      });
    }
  }

  agregarItem() {
    const itemGroup = this.fb.group({
      productoId: [null, [Validators.required]],
      cantidad: [1, [Validators.required, Validators.min(1)]],
      precio: [0, [Validators.required]]
    });

    this.items.push(itemGroup);
  }

  eliminarItem(index: number) {
    this.items.removeAt(index);
    this.calcularTotales();
  }

  onProductoChange(index: number, event: any) {
    const productoId = event.value;
    const producto = this.productos.find(p => p.id === productoId);
    
    if (producto) {
      const itemGroup = this.items.at(index);
      itemGroup.patchValue({
        precio: producto.precio
      });
      this.calcularSubtotal(index);
    }
  }

  calcularSubtotal(index: number) {
    setTimeout(() => {
      this.calcularTotales();
    });
  }

  getSubtotal(index: number): number {
    const item = this.items.at(index);
    const cantidad = item.get('cantidad')?.value || 0;
    const precio = item.get('precio')?.value || 0;
    return cantidad * precio;
  }

  calcularTotales() {
    this.subtotal = 0;
    
    for (let i = 0; i < this.items.length; i++) {
      this.subtotal += this.getSubtotal(i);
    }
    
    this.impuesto = this.subtotal * 0.12; // 12% de impuesto
    this.total = this.subtotal + this.impuesto;
  }

  onSubmit() {
    if (this.facturaForm.valid && this.items.length > 0) {
      this.loading = true;
      
      const facturaData: Factura = {
        ...this.facturaForm.value,
        subtotal: this.subtotal,
        impuesto: this.impuesto,
        total: this.total
      };

      const operation = this.isEditing 
        ? this.facturaService.updateFactura(this.facturaId!, facturaData)
        : this.facturaService.createFactura(facturaData);

      operation.subscribe({
        next: (factura) => {
          this.messageService.add({
            severity: 'success',
            summary: 'Éxito',
            detail: `Factura ${this.isEditing ? 'actualizada' : 'creada'} correctamente`
          });
          setTimeout(() => {
            this.router.navigate(['/facturas']);
          }, 1500);
        },
        error: (error) => {
          console.error('Error guardando factura:', error);
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: `No se pudo ${this.isEditing ? 'actualizar' : 'crear'} la factura`
          });
          this.loading = false;
        }
      });
    }
  }

  volver() {
    this.router.navigate(['/facturas']);
  }
}