import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../enviroment/enviroment';

export interface OrdenItemProducto {
  id: string;
  nombre: string;
  sku?: string;
  precio: number;
  stock?: number;
}

export interface OrdenItem {
  id?: string;
  producto?: OrdenItemProducto;  // FIX: backend devuelve objeto, no productoNombre
  cantidad: number;
  precioUnitario: number;
  subtotal?: number;
}

export interface Orden {
  id: string;
  estado: string;
  tipoComprobante: string;
  subtotal: number;
  impuesto: number;
  total: number;
  items: OrdenItem[];
  comprobanteUrl?: string;
  createdAt?: string;  // ISO-8601 string desde el backend
  pago?: PagoInfo;
  clienteNombre?: string;
  clienteApellido?: string;
  clienteDni?: string;
  facturaId?: string;  // comprobante electronico vinculado
}

export interface PagoInfo {
  id?: string;
  metodo: string;
  monto: number;
  estado: string;
  referencia?: string;
}

export interface CheckoutRequest {
  tipoComprobante: 'BOLETA' | 'FACTURA';
  metodoPago: 'EFECTIVO' | 'TARJETA' | 'YAPE' | 'PLIN';
  clienteNombre?: string;
  clienteApellido?: string;
  clienteDni?: string;
  clienteRuc?: string;  // obligatorio cuando tipoComprobante = FACTURA
}

// El token de autorizacion lo agrega AuthInterceptor automaticamente
@Injectable({
  providedIn: 'root'
})
export class OrdenService {
  private apiUrl = `${environment.apiUrl}/ordenes`;

  constructor(private http: HttpClient) {}

  getOrdenes(): Observable<Orden[]> {
    return this.http.get<Orden[]>(this.apiUrl);
  }

  getOrden(id: string): Observable<Orden> {
    return this.http.get<Orden>(`${this.apiUrl}/${id}`);
  }

  checkout(request: CheckoutRequest): Observable<Orden> {
    // /checkout esta en /api/v1, no bajo /ordenes
    return this.http.post<Orden>(`${environment.apiUrl}/checkout`, request);
  }

  confirmarPago(id: string, referencia?: string): Observable<Orden> {
    let params: any = {};
    if (referencia) {
      params = { referencia };
    }
    return this.http.post<Orden>(`${this.apiUrl}/${id}/confirmar`, null, { params });
  }

  eliminar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
