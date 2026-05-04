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
      // Datos del documento
      tipoDoc: ['01', [Validators.required]], // 01 = Factura, 03 = Boleta
      serie: ['F001', [Validators.required]],
      correlativo: ['', [Validators.required]],
      fechaEmision: [new Date().toISOString().split('T')[0], [Validators.required]],
      fecVencimiento: [''],
      tipoMoneda: ['PEN', [Validators.required]],
      tipoOperacion: ['0101', [Validators.required]], // Venta interna
      
      // Datos del cliente
      clienteNombre: ['', [Validators.required]],
      clienteRuc: [''],
      clienteDireccion: [''],
      clienteTipoDoc: ['1'], // 1 = DNI, 6 = RUC
      
      // Datos de la empresa
      empresaId: [null, [Validators.required]],
      
      // Montos
      mtoOperGravadas: [0],
      mtoOperInafectas: [0],
      mtoOperExoneradas: [0],
      mtoIGV: [0],
      mtoImpVenta: [0],
      subTotal: [0],
      valorVenta: [0],
      
      // Items
      items: this.fb.array([])
    });
  }

  get items() {
    return this.facturaForm.get('items') as FormArray;
  }

  get mtoOperGravadas() {
    return this.facturaForm.get('mtoOperGravadas')?.value || 0;
  }

  get mtoIGV() {
    return this.facturaForm.get('mtoIGV')?.value || 0;
  }

  get valorVenta() {
    return this.facturaForm.get('valorVenta')?.value || 0;
  }

  get mtoImpVenta() {
    return this.facturaForm.get('mtoImpVenta')?.value || 0;
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
      codProducto: ['', [Validators.required]],
      descripcion: ['', [Validators.required]],
      unidadMedida: ['NIU', [Validators.required]],
      cantidad: [1, [Validators.required, Validators.min(1)]],
      mtoValorUnitario: [0, [Validators.required, Validators.min(0)]],
      mtoBaseIgv: [0],
      porcentajeIgv: [18],
      igv: [0],
      tipAfeIgv: [10], // Gravado - Operación Onerosa
      mtoValorVenta: [0],
      mtoPrecioUnitario: [0]
    });

    this.items.push(itemGroup);
  }

  eliminarItem(index: number) {
    this.items.removeAt(index);
    this.calcularTotales();
  }

  calcularItem(index: number) {
    const item = this.items.at(index);
    const cantidad = item.get('cantidad')?.value || 0;
    const precioUnitario = item.get('mtoValorUnitario')?.value || 0;
    const porcentajeIgv = item.get('porcentajeIgv')?.value || 18;
    
    // Calcular valor venta (base IGV)
    const valorVenta = cantidad * precioUnitario;
    
    // Calcular IGV
    const igv = valorVenta * (porcentajeIgv / 100);
    
    // Calcular precio unitario (con IGV)
    const precioUnitarioConIgv = precioUnitario * (1 + porcentajeIgv / 100);
    
    // Calcular subtotal (precio con IGV)
    const subtotal = cantidad * precioUnitarioConIgv;
    
    item.patchValue({
      mtoBaseIgv: valorVenta,
      igv: igv,
      mtoValorVenta: valorVenta,
      mtoPrecioUnitario: precioUnitarioConIgv
    });
    
    this.calcularTotales();
  }

  getValorVenta(index: number): number {
    const item = this.items.at(index);
    return item.get('mtoValorVenta')?.value || 0;
  }

  getIGV(index: number): number {
    const item = this.items.at(index);
    return item.get('igv')?.value || 0;
  }

  getSubtotalItem(index: number): number {
    const item = this.items.at(index);
    const cantidad = item.get('cantidad')?.value || 0;
    const precioUnitario = item.get('mtoPrecioUnitario')?.value || 0;
    return cantidad * precioUnitario;
  }

  calcularTotales() {
    let mtoOperGravadas = 0;
    let mtoIGV = 0;
    let mtoImpVenta = 0;
    let valorVenta = 0;
    
    for (let i = 0; i < this.items.length; i++) {
      mtoOperGravadas += this.getValorVenta(i);
      mtoIGV += this.getIGV(i);
      valorVenta += this.getValorVenta(i);
    }
    
    mtoImpVenta = mtoOperGravadas + mtoIGV;
    
    this.facturaForm.patchValue({
      mtoOperGravadas: mtoOperGravadas,
      mtoIGV: mtoIGV,
      mtoImpVenta: mtoImpVenta,
      valorVenta: valorVenta,
      subTotal: mtoOperGravadas
    });
    
    this.subtotal = mtoOperGravadas;
    this.impuesto = mtoIGV;
    this.total = mtoImpVenta;
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
            this.router.navigate(['/app/facturas']);
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
    this.router.navigate(['/app/facturas']);
  }
}