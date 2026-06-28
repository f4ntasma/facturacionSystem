import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';
import { environment } from '../../enviroment/enviroment';

export interface OrdenItem {
  id?: string;
  productoNombre?: string;
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
  createdAt?: string;
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
    return this.http.get<Orden[]>(`${this.apiUrl}/ordenes`, { headers: this.getHeaders() });
  }

  getOrden(id: string): Observable<Orden> {
    return this.http.get<Orden>(`${this.apiUrl}/ordenes/${id}`, { headers: this.getHeaders() });
  }

  checkout(request: CheckoutRequest): Observable<Orden> {
    return this.http.post<Orden>(`${this.apiUrl}/checkout`, request, { headers: this.getHeaders() });
  }

  confirmarPago(id: string, referencia?: string): Observable<Orden> {
    let params: any = {};
    if (referencia) {
      params = { referencia };
    }
    return this.http.post<Orden>(`${this.apiUrl}/ordenes/${id}/confirmar`, null, { 
      headers: this.getHeaders(),
      params 
    });
  }
}
