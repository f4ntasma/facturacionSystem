import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  FormArray,
  Validators,
  AbstractControl
} from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router, ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { environment } from '../../../enviroment/enviroment';

interface FacturaNuevoRequest {
  tipoComprobante: string;
  serie: string;
  correlativo: string;
  tipoOperacion: string;
  moneda: string;
  fechaEmision: string;
  fechaVencimiento?: string;
  tipoDocIdentidad: string;
  nroDocIdentidad: string;
  clienteNombre: string;
  clienteDireccion?: string;
  metodoPago: string;
  observacion?: string;
  leyenda?: string;
  nroOrdenCompra?: string;
  guiaRemision?: string;
  items: {
    codProducto?: string;
    descripcion: string;
    unidad: string;
    cantidad: number;
    mtoValorUnitario: number;
    tipAfeIgv: number;
  }[];
}

@Component({
  selector: 'app-factura-wizard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './factura-wizard.component.html',
  styleUrls: ['./factura-wizard.component.css']
})
export class FacturaWizardComponent implements OnInit, OnDestroy {

  currentStep = 0;
  loading = false;
  submitError = '';

  rucLoading = false;
  rucError = '';
  rucData: { estado?: string; condicion?: string } | null = null;
  docAutocompletado = false;

  // Venta (orden) de origen cuando se llega desde "Emitir comprobante"
  ordenId: string | null = null;

  steps = [
    { label: 'Documento' },
    { label: 'Cliente' },
    { label: 'Items' },
    { label: 'Pago' },
    { label: 'Revision' }
  ];

  docForm: FormGroup;
  clienteForm: FormGroup;
  itemsForm: FormGroup;
  pagoForm: FormGroup;

  totalGravadas = 0;
  totalExoneradas = 0;
  totalInafectas = 0;
  totalIGV = 0;
  totalGeneral = 0;

  private subs = new Subscription();

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private router: Router,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef
  ) {
    const today = new Date().toISOString().split('T')[0];

    this.docForm = this.fb.group({
      tipoComprobante: ['FACTURA', Validators.required],
      serie: ['F001', Validators.required],
      correlativo: [1, [Validators.required, Validators.min(1)]],
      fechaEmision: [today, Validators.required],
      fechaVencimiento: [''],
      moneda: ['PEN', Validators.required],
      tipoOperacion: ['0101', Validators.required]
    });

    this.clienteForm = this.fb.group({
      tipoDocIdentidad: ['6', Validators.required],
      nroDocIdentidad: ['', Validators.required],
      clienteNombre: ['', Validators.required],
      clienteDireccion: ['']
    });

    this.itemsForm = this.fb.group({
      items: this.fb.array([])
    });

    this.pagoForm = this.fb.group({
      metodoPago: ['CONTADO', Validators.required],
      observacion: [''],
      leyenda: [''],
      nroOrdenCompra: [''],
      guiaRemision: ['']
    });
  }

  ngOnInit(): void {
    this.addItem();

    // Si viene de una venta (Emitir comprobante), prellenar todo
    const ordenId = this.route.snapshot.queryParamMap.get('ordenId');
    if (ordenId) {
      this.cargarDesdeOrden(ordenId);
    }

    // Actualizar serie cuando cambia tipo comprobante
    this.subs.add(
      this.docForm.get('tipoComprobante')!.valueChanges.subscribe(tipo => {
        const serie = this.docForm.get('serie')!.value as string;
        if (tipo === 'FACTURA' && !serie.startsWith('F')) {
          this.docForm.patchValue({ serie: 'F001' });
        } else if (tipo === 'BOLETA' && !serie.startsWith('B')) {
          this.docForm.patchValue({ serie: 'B001' });
        }
      })
    );

    // Limpiar campos al cambiar tipo de documento
    this.subs.add(
      this.clienteForm.get('tipoDocIdentidad')!.valueChanges.subscribe(() => {
        this.clienteForm.patchValue({ nroDocIdentidad: '', clienteNombre: '', clienteDireccion: '' });
        this.rucData = null;
        this.rucError = '';
        this.docAutocompletado = false;
      })
    );

    // Autocompletar RUC/DNI al ingresar el numero completo
    this.subs.add(
      this.clienteForm.get('nroDocIdentidad')!.valueChanges.pipe(
        debounceTime(700),
        distinctUntilChanged()
      ).subscribe(val => {
        const tipo = this.clienteForm.get('tipoDocIdentidad')?.value;
        // setTimeout evita NG0100: ExpressionChangedAfterItHasBeenCheckedError
        if (tipo === '6' && val?.length === 11) {
          setTimeout(() => this.consultarRuc(), 0);
        } else if (tipo === '1' && val?.length === 8) {
          setTimeout(() => this.consultarDni(), 0);
        } else {
          this.rucData = null;
          this.rucError = '';
          this.docAutocompletado = false;
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  // --- Items ---
  get items(): FormArray {
    return this.itemsForm.get('items') as FormArray;
  }

  addItem(): void {
    const g = this.fb.group({
      codProducto: [''],
      descripcion: ['', Validators.required],
      unidad: ['NIU', Validators.required],
      cantidad: [1, [Validators.required, Validators.min(0.001)]],
      mtoValorUnitario: [0, [Validators.required, Validators.min(0)]],
      tipAfeIgv: [10, Validators.required]
    });
    this.subs.add(g.valueChanges.subscribe(() => this.calcTotals()));
    this.items.push(g);
    this.calcTotals();
  }

  removeItem(i: number): void {
    this.items.removeAt(i);
    this.calcTotals();
  }

  calcTotals(): void {
    let grav = 0, exon = 0, inaf = 0;
    this.items.controls.forEach((c: AbstractControl) => {
      const v = c.value;
      const sub = (Number(v.cantidad) || 0) * (Number(v.mtoValorUnitario) || 0);
      const afe = Number(v.tipAfeIgv);
      // Catalogo 07: 10-16 = Gravado, 20 = Exonerado, resto = Inafecto/Exportacion
      if (afe >= 10 && afe <= 16) grav += sub;
      else if (afe === 20) exon += sub;
      else inaf += sub;
    });
    this.totalGravadas = grav;
    this.totalExoneradas = exon;
    this.totalInafectas = inaf;
    this.totalIGV = grav * 0.18;
    this.totalGeneral = grav + exon + inaf + this.totalIGV;

    if (this.totalGeneral > 0) {
      this.pagoForm.patchValue({ leyenda: this.numToWords(this.totalGeneral) }, { emitEvent: false });
    }
  }

  // --- Navegacion ---
  goToStep(n: number): void {
    if (n < this.currentStep) this.currentStep = n;
  }

  nextStep(): void {
    if (this.currentStep < this.steps.length - 1) this.currentStep++;
  }

  prevStep(): void {
    if (this.currentStep > 0) this.currentStep--;
  }

  // --- Consulta RUC / DNI (ApiPeru) ---
  consultarRuc(): void {
    const nro = this.clienteForm.get('nroDocIdentidad')?.value;
    if (!nro || nro.length !== 11) return;

    this.rucLoading = true;
    this.rucError = '';
    this.rucData = null;

    this.http.get<any>(`${environment.apiUrl}/consulta/ruc/${nro}`).subscribe({
      next: (res) => {
        this.rucLoading = false;
        if (res?.success && res?.data) {
          const d = res.data;
          // ApiPeru puede devolver nombre_o_razon_social o razon_social segun version
          const nombre = d.nombre_o_razon_social || d.razon_social || '';
          this.rucData = { estado: d.estado, condicion: d.condicion };
          this.clienteForm.patchValue({
            clienteNombre: nombre,
            clienteDireccion: d.direccion || ''
          });
          this.docAutocompletado = true;
          if (d.condicion !== 'HABIDO') {
            this.rucError = `Condicion: ${d.condicion || 'DESCONOCIDA'}. Verifique si puede emitir comprobante a este contribuyente.`;
          }
        } else {
          this.rucError = res?.message || 'RUC no encontrado en SUNAT.';
        }
      },
      error: () => {
        this.rucLoading = false;
        this.rucError = 'No se pudo consultar el RUC. Ingrese los datos manualmente.';
      }
    });
  }

  consultarDni(): void {
    const nro = this.clienteForm.get('nroDocIdentidad')?.value;
    if (!nro || nro.length !== 8) return;

    this.rucLoading = true;
    this.rucError = '';
    this.rucData = null;

    this.http.get<any>(`${environment.apiUrl}/consulta/dni/${nro}`).subscribe({
      next: (res) => {
        this.rucLoading = false;
        if (res?.success && res?.data) {
          const d = res.data;
          this.clienteForm.patchValue({
            clienteNombre: d.nombre_completo || ''
          });
          this.docAutocompletado = true;
        } else {
          this.rucError = res?.message || 'DNI no encontrado.';
        }
      },
      error: () => {
        this.rucLoading = false;
        this.rucError = 'No se pudo consultar el DNI. Ingrese los datos manualmente.';
      }
    });
  }

  limpiarConsulta(): void {
    this.docAutocompletado = false;
    this.rucData = null;
    this.rucError = '';
    this.clienteForm.patchValue({ clienteNombre: '', clienteDireccion: '' });
  }

  get serieErrorMsg(): string | null {
    const tipo = this.docForm.get('tipoComprobante')?.value;
    const serie = (this.docForm.get('serie')?.value as string) || '';
    if (!serie) return 'La serie es requerida';
    if (serie.length !== 4) return 'La serie debe tener exactamente 4 caracteres';
    if (tipo === 'FACTURA' && !serie.toUpperCase().startsWith('F')) {
      return 'La serie de Factura debe iniciar con F (ej: F001)';
    }
    if (tipo === 'BOLETA' && !serie.toUpperCase().startsWith('B')) {
      return 'La serie de Boleta debe iniciar con B (ej: B001)';
    }
    return null;
  }

  // --- Helpers de revision ---
  get serieCorrelativo(): string {
    const s = this.docForm.get('serie')?.value || 'F001';
    const c = String(this.docForm.get('correlativo')?.value || 1).padStart(8, '0');
    return `${s}-${c}`;
  }

  get tipoComprobanteLabel(): string {
    return this.docForm.get('tipoComprobante')?.value === 'BOLETA' ? 'Boleta de Venta' : 'Factura';
  }

  get tipoDocLabel(): string {
    const map: Record<string, string> = {
      '6': 'RUC (6)', '1': 'DNI (1)', '4': 'Carnet Extran. (4)',
      '7': 'Pasaporte (7)', '0': 'No domiciliado (0)'
    };
    return map[this.clienteForm.get('tipoDocIdentidad')?.value] || '';
  }

  get esBoleta(): boolean {
    return this.docForm.get('tipoComprobante')?.value === 'BOLETA';
  }

  get advertenciaRuc(): boolean {
    return !this.esBoleta && this.clienteForm.get('tipoDocIdentidad')?.value !== '6';
  }

  // --- Prellenado desde una venta (orden) ---
  cargarDesdeOrden(id: string): void {
    this.http.get<any>(`${environment.apiUrl}/ordenes/${id}`).subscribe({
      next: (orden) => {
        if (orden.facturaId) {
          this.submitError = 'Esta venta ya tiene un comprobante emitido.';
          return;
        }
        this.ordenId = id;

        // Tipo de comprobante (la serie se ajusta sola por la suscripcion)
        const tipo = orden.tipoComprobante === 'FACTURA' ? 'FACTURA' : 'BOLETA';
        this.docForm.patchValue({ tipoComprobante: tipo });

        // Cliente: las ventas del POS registran DNI
        // emitEvent false evita que la consulta automatica pise estos datos
        if (orden.clienteDni) {
          this.clienteForm.patchValue({ tipoDocIdentidad: '1' }, { emitEvent: false });
        }
        this.clienteForm.patchValue({
          nroDocIdentidad: orden.clienteDni || '',
          clienteNombre: `${orden.clienteNombre || ''} ${orden.clienteApellido || ''}`.trim()
        }, { emitEvent: false });

        // Items de la venta (precioUnitario es valor base sin IGV, igual que el wizard)
        this.items.clear();
        (orden.items || []).forEach((it: any) => {
          this.addItem();
          this.items.at(this.items.length - 1).patchValue({
            descripcion: it.producto?.nombre || 'Producto',
            cantidad: it.cantidad,
            mtoValorUnitario: it.precioUnitario,
            tipAfeIgv: 10
          });
        });
        if (this.items.length === 0) this.addItem();

        this.calcTotals();
        this.cdr.detectChanges();
      },
      error: () => {
        this.submitError = 'No se pudo cargar la venta. Completa los datos manualmente.';
      }
    });
  }

  // --- Emision: registra el comprobante en el backend y descarga el PDF ---
  // Nota: aun no envia a SUNAT (pendiente); solo registro interno + preview
  emitir(): void {
    this.submitError = '';
    this.loading = true;

    const dv = this.docForm.value;
    const cv = this.clienteForm.value;
    const pv = this.pagoForm.value;

    const request: FacturaNuevoRequest & { ordenId?: string } = {
      tipoComprobante: dv.tipoComprobante,
      serie: dv.serie,
      correlativo: '',  // lo asigna el backend (ultimo + 1 por serie)
      tipoOperacion: dv.tipoOperacion,
      moneda: dv.moneda,
      fechaEmision: dv.fechaEmision,
      fechaVencimiento: dv.fechaVencimiento || undefined,
      tipoDocIdentidad: cv.tipoDocIdentidad,
      nroDocIdentidad: cv.nroDocIdentidad,
      clienteNombre: cv.clienteNombre,
      clienteDireccion: cv.clienteDireccion || undefined,
      metodoPago: pv.metodoPago,
      observacion: pv.observacion || undefined,
      leyenda: pv.leyenda || undefined,
      nroOrdenCompra: pv.nroOrdenCompra || undefined,
      guiaRemision: pv.guiaRemision || undefined,
      ordenId: this.ordenId || undefined,
      items: this.items.value.map((it: any) => ({
        codProducto: it.codProducto || undefined,
        descripcion: it.descripcion,
        unidad: it.unidad,
        cantidad: Number(it.cantidad),
        mtoValorUnitario: Number(it.mtoValorUnitario),
        tipAfeIgv: Number(it.tipAfeIgv)
      }))
    };

    this.http.post<any>(`${environment.apiUrl}/facturas`, request).subscribe({
      next: (res) => {
        this.loading = false;
        // Mostrar el correlativo real asignado por el backend en el PDF
        if (res?.correlativo) {
          this.docForm.patchValue({ correlativo: Number(res.correlativo) }, { emitEvent: false });
        }
        this.generarPdf();
        this.router.navigate([this.ordenId ? '/app/ventas' : '/app/facturas']);
      },
      error: (err) => {
        this.loading = false;
        this.submitError = err?.error?.message ||
          'Error al emitir. Si el problema persiste, cierra sesion y vuelve a entrar.';
      }
    });
  }

  // --- PDF del comprobante (preview; aun sin validez SUNAT) ---
  generarPdf(): void {
    const dv = this.docForm.value;
    const cv = this.clienteForm.value;
    const pv = this.pagoForm.value;
    const items = this.items.value;

    const tipoLabel = dv.tipoComprobante === 'BOLETA'
      ? 'BOLETA DE VENTA ELECTRONICA'
      : 'FACTURA ELECTRONICA';
    const serieCorr = dv.serie + '-' + String(dv.correlativo).padStart(8, '0');

    const monedaMap: Record<string, string> = {
      PEN: 'S/', USD: 'USD', EUR: 'EUR', GBP: 'GBP', JPY: 'JPY', CAD: 'CAD', AUD: 'AUD', CHF: 'CHF'
    };
    const simbolo = monedaMap[dv.moneda] || dv.moneda;

    const tipoDocMap: Record<string, string> = {
      '6': 'RUC', '1': 'DNI', '4': 'Carnet Ext.', '7': 'Pasaporte', '0': 'No Domiciliado'
    };
    const tipoDocLabel2 = tipoDocMap[cv.tipoDocIdentidad] || cv.tipoDocIdentidad;

    const tipAfeLabel: Record<number, string> = {
      10: 'Gravado - Oper. Onerosa', 11: 'Gravado - Retiro',
      12: 'Gravado - IVAP', 13: 'IVAP - Retiro',
      14: 'Gravado - Ret. 4%', 15: 'Gravado - NRUS', 16: 'Gravado - Ley 31556',
      20: 'Exonerado', 30: 'Inafecto',
      31: 'Inafecto - Retiro', 32: 'Inafecto - ATMP', 33: 'Inafecto - Ley 31380',
      34: 'Inafecto - Ley 31556', 35: 'Inafecto - Ret. Ley 31556', 36: 'Inafecto - Art. 19',
      40: 'Exportacion'
    };

    const metodoPagoLbl: Record<string, string> = {
      CONTADO: 'Contado', CREDITO: 'Credito', TARJETA: 'Tarjeta', YAPE: 'Yape', PLIN: 'Plin'
    };

    const tipoOpLbl: Record<string, string> = {
      '0101': 'Venta interna (0101)',
      '0112': 'Venta interna - sustenta gastos (0112)',
      '0200': 'Exportacion bienes (0200)',
      '0201': 'Exportacion servicios (0201)',
      '0301': 'No domiciliados (0301)',
      '0401': 'Venta itinerante (0401)',
      '1001': 'Op. sujetas IGV (1001)',
      '2001': 'Op. sujetas IVAP (2001)',
      '0110': 'Baja integrada Fact./Bol. (0110)'
    };

    const itemsHtml = items.map((it: any, i: number) => {
      const sub = ((Number(it.cantidad) || 0) * (Number(it.mtoValorUnitario) || 0)).toFixed(2);
      const afe = Number(it.tipAfeIgv);
      const igvIt = (afe >= 10 && afe <= 16) ? (Number(sub) * 0.18).toFixed(2) : '0.00';
      const totalIt = (Number(sub) + Number(igvIt)).toFixed(2);
      return '<tr>' +
        '<td style="text-align:center">' + (i + 1) + '</td>' +
        '<td>' + (it.codProducto || '-') + '</td>' +
        '<td>' + (it.descripcion || '') + '</td>' +
        '<td style="text-align:center">' + (it.unidad || 'NIU') + '</td>' +
        '<td style="text-align:right">' + Number(it.cantidad).toFixed(2) + '</td>' +
        '<td style="text-align:right">' + Number(it.mtoValorUnitario).toFixed(2) + '</td>' +
        '<td>' + (tipAfeLabel[afe] || String(afe)) + '</td>' +
        '<td style="text-align:right">' + sub + '</td>' +
        '<td style="text-align:right">' + igvIt + '</td>' +
        '<td style="text-align:right;font-weight:600">' + totalIt + '</td>' +
        '</tr>';
    }).join('');

    const tGrav = this.totalGravadas.toFixed(2);
    const tExon = this.totalExoneradas.toFixed(2);
    const tInaf = this.totalInafectas.toFixed(2);
    const tIGV = this.totalIGV.toFixed(2);
    const tGen = this.totalGeneral.toFixed(2);
    const leyenda = pv.leyenda || this.numToWords(this.totalGeneral);
    const fechaHoy = new Date().toLocaleDateString('es-PE', { year: 'numeric', month: 'long', day: 'numeric' });

    const exonRow = Number(tExon) > 0
      ? '<div class="t-row"><span>Op. Exoneradas:</span><span>' + simbolo + ' ' + tExon + '</span></div>' : '';
    const inafRow = Number(tInaf) > 0
      ? '<div class="t-row"><span>Op. Inafectas:</span><span>' + simbolo + ' ' + tInaf + '</span></div>' : '';
    const fVctoRow = dv.fechaVencimiento
      ? '<div class="i-line"><span class="i-lbl">Vencimiento:</span><span class="i-val">' + dv.fechaVencimiento + '</span></div>' : '';
    const dirRow = cv.clienteDireccion
      ? '<div class="i-line"><span class="i-lbl">Direccion:</span><span class="i-val">' + cv.clienteDireccion + '</span></div>' : '';
    const ordenRow = pv.nroOrdenCompra
      ? '<div class="f-item"><span class="f-lbl">Orden de compra:</span><span>' + pv.nroOrdenCompra + '</span></div>' : '';
    const guiaRow = pv.guiaRemision
      ? '<div class="f-item"><span class="f-lbl">Guia de remision:</span><span>' + pv.guiaRemision + '</span></div>' : '';
    const obsRow = pv.observacion
      ? '<div class="obs-box"><div class="obs-lbl">Observaciones:</div><div>' + pv.observacion + '</div></div>' : '';

    const html = '<!DOCTYPE html>\n<html lang="es">\n<head>\n' +
      '<meta charset="UTF-8">\n' +
      '<title>' + tipoLabel + ' ' + serieCorr + '</title>\n' +
      '<style>\n' +
      '* { margin:0; padding:0; box-sizing:border-box; }\n' +
      'body { font-family:Arial,sans-serif; font-size:11px; color:#1a1a1a; background:#f1f5f9; }\n' +
      '.page { max-width:840px; margin:20px auto; padding:28px; background:white; box-shadow:0 2px 12px rgba(0,0,0,.12); }\n' +
      '.sim { background:#fef9c3; border:1.5px solid #f59e0b; color:#92400e; padding:5px 12px; text-align:center;\n' +
      '  font-weight:bold; font-size:10px; margin-bottom:14px; border-radius:3px; letter-spacing:.4px; }\n' +
      '.header { display:flex; justify-content:space-between; align-items:flex-start;\n' +
      '  padding-bottom:12px; border-bottom:2px solid #1e3a8a; margin-bottom:14px; }\n' +
      '.co-name { font-size:15px; font-weight:bold; color:#1e3a8a; margin-bottom:3px; }\n' +
      '.co-ruc { font-size:11px; color:#475569; }\n' +
      '.co-addr { font-size:10px; color:#64748b; margin-top:2px; max-width:340px; }\n' +
      '.doc-box { border:2px solid #1e3a8a; padding:12px 20px; text-align:center; min-width:200px; border-radius:2px; }\n' +
      '.doc-tipo { font-size:12px; font-weight:bold; color:#1e3a8a; }\n' +
      '.doc-num { font-size:15px; font-weight:bold; color:#0f172a; margin-top:4px; letter-spacing:.5px; }\n' +
      '.doc-mon { font-size:10px; color:#475569; margin-top:5px; }\n' +
      '.info-row { display:flex; gap:12px; margin-bottom:12px; }\n' +
      '.info-box { flex:1; border:1px solid #cbd5e1; border-radius:3px; padding:10px; }\n' +
      '.info-title { font-weight:bold; font-size:10px; color:#1e3a8a; text-transform:uppercase;\n' +
      '  border-bottom:1px solid #e2e8f0; padding-bottom:4px; margin-bottom:7px; }\n' +
      '.i-line { display:flex; margin-bottom:3px; font-size:10.5px; }\n' +
      '.i-lbl { color:#64748b; min-width:90px; }\n' +
      '.i-val { font-weight:600; color:#0f172a; }\n' +
      'table { width:100%; border-collapse:collapse; margin-bottom:12px; font-size:10px; }\n' +
      'th { background:#1e3a8a; color:white; padding:6px 7px; text-align:left; font-size:9.5px; }\n' +
      'td { padding:5px 7px; border-bottom:1px solid #e2e8f0; }\n' +
      'tr:nth-child(even) td { background:#f8faff; }\n' +
      '.tw { display:flex; justify-content:flex-end; margin-bottom:12px; }\n' +
      '.ti { min-width:240px; }\n' +
      '.t-row { display:flex; justify-content:space-between; padding:3px 0; border-bottom:1px solid #e2e8f0; font-size:11px; }\n' +
      '.t-grand { font-weight:bold; font-size:14px; color:#1e3a8a; border-top:2px solid #1e3a8a; border-bottom:none; padding-top:6px; }\n' +
      '.ley { background:#f0f5ff; border:1px solid #bfdbfe; border-radius:3px; padding:9px 12px; margin-bottom:12px; }\n' +
      '.ley-lbl { font-weight:bold; color:#1e3a8a; font-size:10px; text-transform:uppercase; margin-bottom:2px; }\n' +
      '.ley-txt { font-style:italic; font-size:11px; }\n' +
      '.footer { display:flex; flex-wrap:wrap; gap:18px; font-size:10px; color:#475569;\n' +
      '  border-top:1px solid #e2e8f0; padding-top:10px; margin-bottom:10px; }\n' +
      '.f-item { display:flex; gap:4px; }\n' +
      '.f-lbl { font-weight:bold; color:#1e3a8a; }\n' +
      '.obs-box { border:1px solid #e2e8f0; border-radius:3px; padding:8px 12px; margin-bottom:10px; font-size:10.5px; }\n' +
      '.obs-lbl { font-weight:bold; color:#64748b; font-size:10px; margin-bottom:2px; }\n' +
      '.pdate { text-align:right; font-size:9px; color:#94a3b8; margin-top:8px; }\n' +
      '@media print { body{background:white;} .page{box-shadow:none;margin:0;padding:15px;} }\n' +
      '</style>\n</head>\n<body>\n' +
      '<div class="page">\n' +
      '<div class="sim">SIMULACION — Este documento NO tiene validez tributaria ante SUNAT</div>\n' +
      '<div class="header">\n' +
      '  <div>\n' +
      '    <div class="co-name">TU EMPRESA S.A.C.</div>\n' +
      '    <div class="co-ruc">RUC: 20000000000</div>\n' +
      '    <div class="co-addr">Completa los datos de tu empresa en el perfil</div>\n' +
      '  </div>\n' +
      '  <div class="doc-box">\n' +
      '    <div class="doc-tipo">' + tipoLabel + '</div>\n' +
      '    <div class="doc-num">' + serieCorr + '</div>\n' +
      '    <div class="doc-mon">Moneda: ' + dv.moneda + '</div>\n' +
      '  </div>\n' +
      '</div>\n' +
      '<div class="info-row">\n' +
      '  <div class="info-box">\n' +
      '    <div class="info-title">Datos de emision</div>\n' +
      '    <div class="i-line"><span class="i-lbl">Fecha emision:</span><span class="i-val">' + dv.fechaEmision + '</span></div>\n' +
      fVctoRow +
      '    <div class="i-line"><span class="i-lbl">Tipo operacion:</span><span class="i-val">' + (tipoOpLbl[dv.tipoOperacion] || dv.tipoOperacion) + '</span></div>\n' +
      '  </div>\n' +
      '  <div class="info-box">\n' +
      '    <div class="info-title">Cliente / Receptor</div>\n' +
      '    <div class="i-line"><span class="i-lbl">' + tipoDocLabel2 + ':</span><span class="i-val">' + cv.nroDocIdentidad + '</span></div>\n' +
      '    <div class="i-line"><span class="i-lbl">Nombre:</span><span class="i-val">' + cv.clienteNombre + '</span></div>\n' +
      dirRow +
      '  </div>\n' +
      '</div>\n' +
      '<table>\n' +
      '  <thead><tr>\n' +
      '    <th>#</th><th>Cod.</th><th>Descripcion</th><th>Und.</th>\n' +
      '    <th style="text-align:right">Cant.</th>\n' +
      '    <th style="text-align:right">P. Unit.</th>\n' +
      '    <th>Afect. IGV</th>\n' +
      '    <th style="text-align:right">V. Venta</th>\n' +
      '    <th style="text-align:right">IGV</th>\n' +
      '    <th style="text-align:right">Total</th>\n' +
      '  </tr></thead>\n' +
      '  <tbody>' + itemsHtml + '</tbody>\n' +
      '</table>\n' +
      '<div class="tw"><div class="ti">\n' +
      '  <div class="t-row"><span>Op. Gravadas:</span><span>' + simbolo + ' ' + tGrav + '</span></div>\n' +
      exonRow +
      inafRow +
      '  <div class="t-row"><span>IGV (18%):</span><span>' + simbolo + ' ' + tIGV + '</span></div>\n' +
      '  <div class="t-row t-grand"><span>TOTAL:</span><span>' + simbolo + ' ' + tGen + '</span></div>\n' +
      '</div></div>\n' +
      '<div class="ley"><div class="ley-lbl">Son (Catalogo 52 SUNAT):</div><div class="ley-txt">' + leyenda + '</div></div>\n' +
      '<div class="footer">\n' +
      '  <div class="f-item"><span class="f-lbl">Forma de pago:</span><span>' + (metodoPagoLbl[pv.metodoPago] || pv.metodoPago) + '</span></div>\n' +
      ordenRow +
      guiaRow +
      '</div>\n' +
      obsRow +
      '<div class="pdate">Generado el ' + fechaHoy + ' &mdash; Simulacion de comprobante electronico Factullama</div>\n' +
      '</div>\n</body>\n</html>';

    // Usar iframe oculto para evitar bloqueo de popups del navegador
    const existente = document.getElementById('factura-pdf-frame');
    if (existente) existente.remove();

    const iframe = document.createElement('iframe');
    iframe.id = 'factura-pdf-frame';
    iframe.style.cssText = 'position:fixed;top:0;left:0;width:0;height:0;border:none;visibility:hidden;';
    document.body.appendChild(iframe);

    const doc = iframe.contentDocument || (iframe.contentWindow as any)?.document;
    if (!doc) {
      this.submitError = 'No se pudo generar el PDF. Intenta desde otro navegador.';
      return;
    }

    doc.open();
    doc.write(html);
    doc.close();

    setTimeout(() => {
      try {
        iframe.contentWindow?.focus();
        iframe.contentWindow?.print();
      } catch (_e) {
        this.submitError = 'Error al abrir el dialogo de impresion.';
      }
      setTimeout(() => iframe.remove(), 2000);
    }, 600);
  }

  volver(): void {
    this.router.navigate(['/app/facturas']);
  }

  // --- Leyenda (Catalogo 52 SUNAT) ---
  numToWords(n: number): string {
    const entero = Math.floor(n);
    const centavos = Math.round((n - entero) * 100);

    const u = ['', 'UNO', 'DOS', 'TRES', 'CUATRO', 'CINCO', 'SEIS', 'SIETE', 'OCHO', 'NUEVE',
      'DIEZ', 'ONCE', 'DOCE', 'TRECE', 'CATORCE', 'QUINCE', 'DIECISEIS', 'DIECISIETE', 'DIECIOCHO', 'DIECINUEVE'];
    const dec = ['', '', 'VEINTE', 'TREINTA', 'CUARENTA', 'CINCUENTA', 'SESENTA', 'SETENTA', 'OCHENTA', 'NOVENTA'];

    const d2 = (x: number): string => {
      if (x < 20) return u[x];
      if (x === 20) return 'VEINTE';
      if (x < 30) return 'VEINTI' + u[x % 10];
      return dec[Math.floor(x / 10)] + (x % 10 ? ' Y ' + u[x % 10] : '');
    };

    const d3 = (x: number): string => {
      if (x === 100) return 'CIEN';
      if (x < 100) return d2(x);
      const c = ['', 'CIENTO', 'DOSCIENTOS', 'TRESCIENTOS', 'CUATROCIENTOS', 'QUINIENTOS',
        'SEISCIENTOS', 'SETECIENTOS', 'OCHOCIENTOS', 'NOVECIENTOS'];
      return c[Math.floor(x / 100)] + (x % 100 ? ' ' + d2(x % 100) : '');
    };

    const d6 = (x: number): string => {
      if (x === 0) return 'CERO';
      if (x < 1000) return d3(x);
      const m = Math.floor(x / 1000);
      const r = x % 1000;
      return (m === 1 ? 'MIL' : d3(m) + ' MIL') + (r ? ' ' + d3(r) : '');
    };

    const palabras = d6(entero);
    return 'SON ' + palabras + ' Y ' + String(centavos).padStart(2, '0') + '/100 SOLES';
  }

  fmt(n: number): string {
    return n.toFixed(2);
  }
}
