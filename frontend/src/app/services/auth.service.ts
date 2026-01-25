import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: {
    id: number;
    username: string;
    email?: string;
  };
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api'; // Volver a Spring Boot
  private tokenKey = 'auth_token';
  private currentUserSubject = new BehaviorSubject<any>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    // Verificar si hay un token guardado al inicializar
    const token = this.getToken();
    if (token) {
      // Aquí podrías validar el token con el backend
      this.currentUserSubject.next({ token });
    }
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    console.log('AuthService: Enviando petición de login a:', `${this.apiUrl}/auth/login`);
    console.log('AuthService: Credenciales:', credentials);
    
    return this.http.post<LoginResponse>(`${this.apiUrl}/auth/login`, credentials)
      .pipe(
        tap(response => {
          console.log('AuthService: Respuesta recibida:', response);
          // Guardar token en localStorage
          localStorage.setItem(this.tokenKey, response.token);
          this.currentUserSubject.next(response.user);
        })
      );
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  isLoggedIn(): boolean {
    const token = this.getToken();
    return token !== null;
  }

  // Método para agregar el token a las peticiones HTTP
  getAuthHeaders() {
    const token = this.getToken();
    return token ? { 'Authorization': `Bearer ${token}` } : {};
  }
}