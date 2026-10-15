// Tipos que devuelve la API de Al Toque.

export type TipoComprobante = '01' | '03' | '07' | '08';
export type EstadoSunat = 'PENDIENTE' | 'ACEPTADO' | 'OBSERVADO' | 'RECHAZADO' | 'POR_ANULAR' | 'ANULADO';
export type EstadoPago = 'PAGADO' | 'PARCIAL' | 'POR_COBRAR';
export type MetodoPago = 'EFECTIVO' | 'YAPE' | 'PLIN' | 'TARJETA' | 'TRANSFERENCIA' | 'OTRO';
export type Rol = 'PROPIETARIO' | 'ADMINISTRADOR' | 'VENDEDOR';
export type Ambiente = 'SIMULADO' | 'BETA' | 'PRODUCCION';

export interface Pagina<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface PlanVista {
  codigo: string;
  nombre: string;
  limiteComprobantes: number | null;
  maxUsuarios: number;
  maxEstablecimientos: number;
  notasCredito: boolean;
  reportesAvanzados: boolean;
}

export interface EmpresaVista {
  id: number;
  ruc: string;
  razonSocial: string;
  nombreComercial?: string;
  direccionFiscal: string;
  ambienteSunat: Ambiente;
  estado: 'PRUEBA' | 'ACTIVA' | 'SUSPENDIDA';
  pruebaHasta?: string;
  certificadoPropio: boolean;
  solConfigurado: boolean;
  plan: PlanVista;
  usadosMes: number;
}

export interface Perfil {
  id: number;
  nombre: string;
  email: string;
  rol: Rol;
  establecimientoId?: number;
  establecimiento?: string;
  empresa: EmpresaVista;
}

export interface TokenResponse {
  token: string;
  expira: string;
  perfil: Perfil;
}

export interface Fila {
  id: number;
  tipo: TipoComprobante;
  serie: string;
  correlativo: number;
  numero: string;
  fechaEmision: string;
  horaEmision: string;
  clienteNombre: string;
  clienteNumDoc: string;
  total: number;
  moneda: string;
  estadoSunat: EstadoSunat;
  estadoPago: EstadoPago;
  tokenPublico: string;
}

export interface Emisor {
  ruc: string;
  razonSocial: string;
  nombreComercial?: string;
  direccion: string;
  distrito: string;
  provincia: string;
  departamento: string;
  telefono?: string;
  email?: string;
  mensajeTicket: string;
  establecimiento: string;
  ambiente: Ambiente;
}

export interface Detalle {
  item: number;
  codigo?: string;
  descripcion: string;
  unidadMedida: string;
  cantidad: number;
  precioUnitario: number;
  valorUnitario: number;
  tipoAfectacion: string;
  valorVenta: number;
  igv: number;
  icbper: number;
  total: number;
  productoId?: number;
}

export interface PagoVista {
  id: number;
  metodo: MetodoPago;
  monto: number;
  referencia?: string;
  pagadoAt: string;
}

export interface Ref {
  id: number;
  tipo: TipoComprobante;
  numero: string;
  token: string;
}

export interface Comprobante {
  id: number;
  tipo: TipoComprobante;
  tipoNombre: string;
  serie: string;
  correlativo: number;
  numero: string;
  fechaEmision: string;
  horaEmision: string;
  fechaVencimiento?: string;
  moneda: string;
  formaPago: 'CONTADO' | 'CREDITO';
  clienteTipoDoc: string;
  clienteNumDoc: string;
  clienteNombre: string;
  clienteDireccion?: string;
  clienteEmail?: string;
  clienteTelefono?: string;
  igvPorcentaje: number;
  totalGravado: number;
  totalExonerado: number;
  totalInafecto: number;
  totalIgv: number;
  totalIcbper: number;
  total: number;
  totalLetras: string;
  observaciones?: string;
  estadoSunat: EstadoSunat;
  sunatCodigo?: string;
  sunatMensaje?: string;
  hash?: string;
  estadoPago: EstadoPago;
  montoPagado: number;
  saldo: number;
  anulacionMotivo?: string;
  referencia?: Ref;
  motivoCodigo?: string;
  motivoDescripcion?: string;
  notas: Ref[];
  tokenPublico: string;
  qr: string;
  vendedor: string;
  emisor: Emisor;
  detalles: Detalle[];
  cuotas: { numero: number; monto: number; fechaVencimiento: string }[];
  pagos: PagoVista[];
  createdAt: string;
}

export interface Producto {
  id: number;
  codigo: string;
  nombre: string;
  descripcion?: string;
  unidadMedida: string;
  precio: number;
  tipoAfectacion: string;
  aplicaIcbper: boolean;
  controlaStock: boolean;
  stock: number;
  favorito: boolean;
  activo: boolean;
  categoriaId?: number;
  categoria?: string;
}

export interface Cliente {
  id: number;
  tipoDocumento: string;
  numeroDocumento: string;
  nombre: string;
  direccion?: string;
  email?: string;
  telefono?: string;
  createdAt?: string;
}

export interface Catalogos {
  documentos: Record<string, string>;
  motivosNotaCredito: Record<string, string>;
  motivosNotaDebito: Record<string, string>;
  unidades: Record<string, string>;
  afectaciones: Record<string, string>;
}

export interface Resumen {
  desde: string;
  hasta: string;
  ventasNetas: number;
  comprobantes: number;
  ticketPromedio: number;
  ventasHoy: number;
  comprobantesHoy: number;
  porCobrar: number;
  pendientesSunat: number;
  dias: { fecha: string; comprobantes: number; total: number }[];
  porTipo: { tipo: TipoComprobante; cantidad: number; total: number }[];
  porMetodo: { metodo: MetodoPago; cantidad: number; total: number }[];
  topProductos: { descripcion: string; cantidad: number; total: number }[];
}

export interface CajaVista {
  id: number;
  estado: 'ABIERTA' | 'CERRADA';
  usuario: string;
  establecimiento: string;
  montoApertura: number;
  efectivoCobrado: number;
  montoEsperado: number;
  montoDeclarado?: number;
  diferencia?: number;
  totalCobrado: number;
  porMetodo: { metodo: MetodoPago; cantidad: number; total: number }[];
  notas?: string;
  abiertaAt: string;
  cerradaAt?: string;
}

export interface PlanPublico {
  codigo: string;
  nombre: string;
  descripcion: string;
  precioMensual: number;
  precioAnual: number;
  limiteComprobantes: number | null;
  maxUsuarios: number;
  maxEstablecimientos: number;
  notasCredito: boolean;
  reportesAvanzados: boolean;
  soportePrioritario: boolean;
  destacado: boolean;
}

export interface Lote {
  id: number;
  clase: 'RESUMEN' | 'BAJA';
  identificador: string;
  fechaReferencia: string;
  fechaGeneracion: string;
  estado: 'GENERADO' | 'ENVIADO' | 'ACEPTADO' | 'RECHAZADO';
  ticket?: string;
  sunatCodigo?: string;
  sunatMensaje?: string;
  documentos: string[];
  createdAt: string;
}

export const NOMBRE_TIPO: Record<TipoComprobante, string> = {
  '01': 'Factura',
  '03': 'Boleta',
  '07': 'Nota de crédito',
  '08': 'Nota de débito',
};

export const NOMBRE_METODO: Record<MetodoPago, string> = {
  EFECTIVO: 'Efectivo',
  YAPE: 'Yape',
  PLIN: 'Plin',
  TARJETA: 'Tarjeta',
  TRANSFERENCIA: 'Transferencia',
  OTRO: 'Otro',
};
