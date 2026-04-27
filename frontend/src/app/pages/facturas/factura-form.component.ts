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
  templateUrl: './factura-form.component.html'
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