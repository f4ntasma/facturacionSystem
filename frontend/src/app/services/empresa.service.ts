import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../enviroment/enviroment';

export interface Empresa {
  id?: number;
  nombre: string;
  ruc: string;
  direccion: string;
  telefono?: string;
  email?: string;
}

// El token de autorizacion lo agrega AuthInterceptor automaticamente
@Injectable({
  providedIn: 'root'
})
export class EmpresaService {
  private apiUrl = `${environment.apiUrl}/empresas`;

  constructor(private http: HttpClient) {}

  getEmpresas(): Observable<Empresa[]> {
    return this.http.get<Empresa[]>(this.apiUrl);
  }

  getEmpresa(id: number): Observable<Empresa> {
    return this.http.get<Empresa>(`${this.apiUrl}/${id}`);
  }

  createEmpresa(empresa: Empresa): Observable<Empresa> {
    return this.http.post<Empresa>(this.apiUrl, empresa);
  }

  updateEmpresa(id: number, empresa: Empresa): Observable<Empresa> {
    return this.http.put<Empresa>(`${this.apiUrl}/${id}`, empresa);
  }

  deleteEmpresa(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
