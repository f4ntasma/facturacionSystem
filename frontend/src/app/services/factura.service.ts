import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

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

// Se modificó el http://localhost:8080/api por la http://localhost:8080/api
@Injectable({
  providedIn: 'root'
})
export class FacturaService {
  private apiUrl = 'http://localhost:8081/api/facturas';

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

  getFacturas(): Observable<Factura[]> {
    return this.http.get<Factura[]>(this.apiUrl, { headers: this.getHeaders() });
  }

  getFactura(id: number): Observable<Factura> {
    return this.http.get<Factura>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }

  createFactura(factura: Factura): Observable<Factura> {
    return this.http.post<Factura>(this.apiUrl, factura, { headers: this.getHeaders() });
  }

  updateFactura(id: number, factura: Factura): Observable<Factura> {
    return this.http.put<Factura>(`${this.apiUrl}/${id}`, factura, { headers: this.getHeaders() });
  }

  deleteFactura(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }

  getFacturasByEmpresa(empresaId: number): Observable<Factura[]> {
    return this.http.get<Factura[]>(`${this.apiUrl}/empresa/${empresaId}`, { headers: this.getHeaders() });
  }

  generarPDF(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/pdf`, { 
      headers: this.getHeaders(),
      responseType: 'blob'
    });
  }
}