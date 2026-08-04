import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../enviroment/enviroment';

export interface Cliente {
  id?: string;
  nombre: string;
  apellido: string;
  dni: string;
  direccion?: string;
  telefono?: string;
  email?: string;
}

// El token de autorizacion lo agrega AuthInterceptor automaticamente
@Injectable({
  providedIn: 'root'
})
export class ClienteService {
  private apiUrl = `${environment.apiUrl}/clientes`;

  constructor(private http: HttpClient) {}

  getClientes(): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(this.apiUrl);
  }

  getCliente(id: string): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.apiUrl}/${id}`);
  }

  createCliente(cliente: Cliente): Observable<Cliente> {
    return this.http.post<Cliente>(this.apiUrl, cliente);
  }

  updateCliente(id: string, cliente: Cliente): Observable<Cliente> {
    return this.http.put<Cliente>(`${this.apiUrl}/${id}`, cliente);
  }

  deleteCliente(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
