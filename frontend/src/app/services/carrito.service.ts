import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

export interface CarritoItem {
  id?: number;
  productoId: string;
  productoNombre?: string;
  cantidad: number;
  precioUnitario: number;
  subtotal?: number;
}

export interface Carrito {
  id?: number;
  items: CarritoItem[];
  subtotal?: number;
  total?: number;
}

export interface CarritoItemRequest {
  productoId: string;
  cantidad: number;
}

@Injectable({
  providedIn: 'root'
})
export class CarritoService {
  private apiUrl = 'http://localhost:8081/api/v1/carrito';

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

  getCarrito(): Observable<Carrito> {
    return this.http.get<Carrito>(this.apiUrl, { headers: this.getHeaders() });
  }

  agregarItem(request: CarritoItemRequest): Observable<Carrito> {
    return this.http.post<Carrito>(`${this.apiUrl}/items`, request, { headers: this.getHeaders() });
  }

  actualizarItem(itemId: number, cantidad: number): Observable<Carrito> {
    return this.http.put<Carrito>(`${this.apiUrl}/items/${itemId}`, { cantidad }, { headers: this.getHeaders() });
  }

  eliminarItem(itemId: number): Observable<Carrito> {
    return this.http.delete<Carrito>(`${this.apiUrl}/items/${itemId}`, { headers: this.getHeaders() });
  }

  limpiarCarrito(): Observable<void> {
    return this.http.delete<void>(this.apiUrl, { headers: this.getHeaders() });
  }
}
