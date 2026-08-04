import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../enviroment/enviroment';

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

// El token de autorizacion lo agrega AuthInterceptor automaticamente
@Injectable({
  providedIn: 'root'
})
export class CarritoService {
  private apiUrl = `${environment.apiUrl}/carrito`;

  constructor(private http: HttpClient) {}

  getCarrito(): Observable<Carrito> {
    return this.http.get<Carrito>(this.apiUrl);
  }

  agregarItem(request: CarritoItemRequest): Observable<Carrito> {
    return this.http.post<Carrito>(`${this.apiUrl}/items`, request);
  }

  actualizarItem(itemId: number, cantidad: number): Observable<Carrito> {
    return this.http.put<Carrito>(`${this.apiUrl}/items/${itemId}`, { cantidad });
  }

  eliminarItem(itemId: number): Observable<Carrito> {
    return this.http.delete<Carrito>(`${this.apiUrl}/items/${itemId}`);
  }

  limpiarCarrito(): Observable<void> {
    return this.http.delete<void>(this.apiUrl);
  }
}
