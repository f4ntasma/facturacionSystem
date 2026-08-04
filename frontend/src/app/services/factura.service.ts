import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../enviroment/enviroment';


export interface FacturaItem {
  id?: number;
  productoId: number;
  productoNombre?: string;
  cantidad: number;
  precio: number;
  subtotal?: number;
}

export interface Factura {
  id?: number;
  numero?: string;
  fecha?: string;
  clienteNombre: string;
  clienteRuc?: string;
  clienteDireccion?: string;
  items: FacturaItem[];
  subtotal?: number;
  impuesto?: number;
  total?: number;
  estado?: string;
  empresaId: number;
}

// El token de autorizacion lo agrega AuthInterceptor automaticamente
@Injectable({
  providedIn: 'root'
})
export class FacturaService {
  private apiUrl = `${environment.apiUrl}/facturas`;

  constructor(private http: HttpClient) {}

  getFacturas(): Observable<Factura[]> {
    return this.http.get<Factura[]>(this.apiUrl);
  }

  getFactura(id: number): Observable<Factura> {
    return this.http.get<Factura>(`${this.apiUrl}/${id}`);
  }

  createFactura(factura: Factura): Observable<Factura> {
    return this.http.post<Factura>(this.apiUrl, factura);
  }

  updateFactura(id: number, factura: Factura): Observable<Factura> {
    return this.http.put<Factura>(`${this.apiUrl}/${id}`, factura);
  }

  deleteFactura(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getFacturasByEmpresa(empresaId: number): Observable<Factura[]> {
    return this.http.get<Factura[]>(`${this.apiUrl}/empresa/${empresaId}`);
  }

  generarPDF(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/pdf`, { responseType: 'blob' });
  }
}
