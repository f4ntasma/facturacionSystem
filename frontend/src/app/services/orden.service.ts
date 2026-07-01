import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';
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
  createdAt?: string;  // viene del backend solo si OrdenResponse lo expone
  pago?: PagoInfo;
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
}

@Injectable({
  providedIn: 'root'
})
export class OrdenService {
  private apiUrl = `${environment.apiUrl}/ordenes`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    });
  }

  getOrdenes(): Observable<Orden[]> {
    // FIX: apiUrl ya incluye /ordenes — no repetir
    return this.http.get<Orden[]>(this.apiUrl, { headers: this.getHeaders() });
  }

  getOrden(id: string): Observable<Orden> {
    return this.http.get<Orden>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }

  checkout(request: CheckoutRequest): Observable<Orden> {
    // FIX: /checkout está en /api/v1, no bajo /ordenes
    return this.http.post<Orden>(`${environment.apiUrl}/checkout`, request, { headers: this.getHeaders() });
  }

  confirmarPago(id: string, referencia?: string): Observable<Orden> {
    let params: any = {};
    if (referencia) {
      params = { referencia };
    }
    return this.http.post<Orden>(`${this.apiUrl}/${id}/confirmar`, null, {
      headers: this.getHeaders(),
      params
    });
  }

  eliminar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }
}
