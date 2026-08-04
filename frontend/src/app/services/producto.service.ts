import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../enviroment/enviroment';

export interface Producto {
  id?: string;
  nombre: string;
  descripcion?: string;
  precio: number;
  stock: number;
  categoria?: string;
  empresaId: string;
}

// El token de autorizacion lo agrega AuthInterceptor automaticamente
@Injectable({
  providedIn: 'root'
})
export class ProductoService {
  private apiUrl = `${environment.apiUrl}/productos`;

  constructor(private http: HttpClient) {}

  getProductos(): Observable<Producto[]> {
    return this.http.get<Producto[]>(this.apiUrl);
  }

  getProducto(id: string): Observable<Producto> {
    return this.http.get<Producto>(`${this.apiUrl}/${id}`);
  }

  createProducto(producto: Producto): Observable<Producto> {
    return this.http.post<Producto>(this.apiUrl, producto);
  }

  updateProducto(id: string, producto: Producto): Observable<Producto> {
    return this.http.put<Producto>(`${this.apiUrl}/${id}`, producto);
  }

  deleteProducto(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getProductosByEmpresa(empresaId: string): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/empresa/${empresaId}`);
  }
}
