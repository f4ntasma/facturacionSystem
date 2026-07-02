import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { FacturaService, Factura } from '../../services/factura.service';
import { ProductoService, Producto } from '../../services/producto.service';
import { EmpresaService, Empresa } from '../../services/empresa.service';

@Component({
  selector: 'app-factura-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ToastModule],
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
      tipoDoc: ['01', [Validators.required]],
      serie: ['F001', [Validators.required]],
      correlativo: ['', [Validators.required]],
      fechaEmision: [new Date().toISOString().split('T')[0], [Validators.required]],
      fecVencimiento: [''],
      tipoMoneda: ['PEN', [Validators.required]],
      tipoOperacion: ['0101'],
      clienteNombre: ['', [Validators.required]],
      clienteRuc: [''],
      clienteDireccion: [''],
      clienteTipoDoc: ['6'],
      empresaId: [null, [Validators.required]],
      mtoOperGravadas: [0],
      mtoOperInafectas: [0],
      mtoOperExoneradas: [0],
      mtoIGV: [0],
      mtoImpVenta: [0],
      subTotal: [0],
      valorVenta: [0],
      items: this.fb.array([])
    });
  }

  get items() {
    return this.facturaForm.get('items') as FormArray;
  }

  get tipoDocActivo(): '01' | '03' {
    return this.facturaForm.get('tipoDoc')?.value || '01';
  }

  get numeroDocumento(): string {
    const serie = this.facturaForm.get('serie')?.value || (this.tipoDocActivo === '01' ? 'F001' : 'B001');
    const correlativo = String(this.facturaForm.get('correlativo')?.value || '');
    return `${serie}-${correlativo.padStart(8, '0')}`;
  }

  get empresaSeleccionada(): Empresa | undefined {
    const id = this.facturaForm.get('empresaId')?.value;
    return id ? this.empresas.find(e => String(e.id) === String(id)) : undefined;
  }

  get mtoOperGravadas(): number { return this.facturaForm.get('mtoOperGravadas')?.value || 0; }
  get mtoIGV(): number { return this.facturaForm.get('mtoIGV')?.value || 0; }
  get mtoImpVenta(): number { return this.facturaForm.get('mtoImpVenta')?.value || 0; }

  ngOnInit() {
    this.facturaId = this.route.snapshot.params['id'];
    this.isEditing = !!this.facturaId;
    this.loadEmpresas();
    this.loadProductos();
    if (this.isEditing) {
      this.loadFactura();
    } else {
      this.agregarItem();
    }
  }

  cambiarTipoDoc(tipo: '01' | '03') {
    const serie = tipo === '01' ? 'F001' : 'B001';
    const clienteTipoDoc = tipo === '01' ? '6' : '1';
    this.facturaForm.patchValue({ tipoDoc: tipo, serie, clienteTipoDoc });
  }

  loadEmpresas() {
    this.empresaService.getEmpresas().subscribe({
      next: (empresas) => {
        this.empresas = empresas;
        // Auto-select si solo hay una empresa
        if (empresas.length >= 1 && !this.isEditing) {
          this.facturaForm.patchValue({ empresaId: empresas[0].id });
        }
      },
      error: (e) => console.error('Error cargando empresas:', e)
    });
  }

  loadProductos() {
    this.productoService.getProductos().subscribe({
      next: (productos) => { this.productos = productos; },
      error: (e) => console.error('Error cargando productos:', e)
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
          factura.items.forEach(item => {
            this.items.push(this.crearItemGroup({
              descripcion: item.productoNombre || '',
              cantidad: item.cantidad,
              mtoValorUnitario: item.precio
            }));
          });
          this.calcularTotales();
        },
        error: () => {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo cargar el comprobante' });
        }
      });
    }
  }

  crearItemGroup(defaults: any = {}) {
    return this.fb.group({
      productoId: [defaults.productoId || ''],
      codProducto: [defaults.codProducto || ''],
      descripcion: [defaults.descripcion || '', [Validators.required]],
      unidadMedida: [defaults.unidadMedida || 'NIU'],
      cantidad: [defaults.cantidad || 1, [Validators.required, Validators.min(1)]],
      mtoValorUnitario: [defaults.mtoValorUnitario || 0, [Validators.required, Validators.min(0)]],
      mtoBaseIgv: [0],
      porcentajeIgv: [18],
      igv: [0],
      tipAfeIgv: [10],
      mtoValorVenta: [0],
      mtoPrecioUnitario: [0]
    });
  }

  agregarItem() {
    this.items.push(this.crearItemGroup());
  }

  eliminarItem(index: number) {
    this.items.removeAt(index);
    this.calcularTotales();
  }

  onProductoSeleccionado(index: number) {
    const item = this.items.at(index);
    const productoId = item.get('productoId')?.value;
    if (!productoId) return;
    const producto = this.productos.find(p => String(p.id) === String(productoId));
    if (producto) {
      item.patchValue({
        codProducto: String(producto.id || ''),
        descripcion: (producto as any).nombre || '',
        mtoValorUnitario: (producto as any).precio || 0
      });
      this.calcularItem(index);
    }
  }

  calcularItem(index: number) {
    const item = this.items.at(index);
    const cantidad = item.get('cantidad')?.value || 0;
    const precioUnitario = item.get('mtoValorUnitario')?.value || 0;
    const porcentajeIgv = 18;

    const valorVenta = cantidad * precioUnitario;
    const igv = valorVenta * (porcentajeIgv / 100);
    const precioUnitarioConIgv = precioUnitario * (1 + porcentajeIgv / 100);

    item.patchValue({
      mtoBaseIgv: valorVenta,
      igv: igv,
      mtoValorVenta: valorVenta,
      mtoPrecioUnitario: precioUnitarioConIgv
    }, { emitEvent: false });

    this.calcularTotales();
  }

  getSubtotalItem(index: number): number {
    const item = this.items.at(index);
    const cantidad = item.get('cantidad')?.value || 0;
    const precioConIgv = item.get('mtoPrecioUnitario')?.value || 0;
    return cantidad * precioConIgv;
  }

  calcularTotales() {
    let mtoOperGravadas = 0;
    let mtoIGV = 0;
    for (let i = 0; i < this.items.length; i++) {
      mtoOperGravadas += this.items.at(i).get('mtoValorVenta')?.value || 0;
      mtoIGV += this.items.at(i).get('igv')?.value || 0;
    }
    const mtoImpVenta = mtoOperGravadas + mtoIGV;
    this.facturaForm.patchValue({
      mtoOperGravadas,
      mtoIGV,
      mtoImpVenta,
      valorVenta: mtoOperGravadas,
      subTotal: mtoOperGravadas
    }, { emitEvent: false });
  }

  onSubmit() {
    if (this.facturaForm.valid && this.items.length > 0) {
      this.loading = true;
      const facturaData: Factura = { ...this.facturaForm.value };

      const operation = this.isEditing
        ? this.facturaService.updateFactura(this.facturaId!, facturaData)
        : this.facturaService.createFactura(facturaData);

      operation.subscribe({
        next: () => {
          const tipoLabel = this.tipoDocActivo === '01' ? 'Factura' : 'Boleta';
          this.messageService.add({
            severity: 'success',
            summary: 'Éxito',
            detail: `${tipoLabel} ${this.isEditing ? 'actualizada' : 'emitida'} correctamente`
          });
          setTimeout(() => this.router.navigate(['/app/facturas']), 1500);
        },
        error: () => {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo guardar el comprobante' });
          this.loading = false;
        }
      });
    }
  }

  volver() {
    this.router.navigate(['/app/facturas']);
  }
}
