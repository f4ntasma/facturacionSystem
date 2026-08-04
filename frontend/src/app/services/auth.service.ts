import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { environment } from '../../enviroment/enviroment';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: {
    id: number;
    username: string;
    email?: string;
    isTrial?: boolean;
    trialExpiresAt?: number;
  };
}

export interface RegisterTrialRequest {
  nombre: string;
  email: string;
  password: string;
}

// Se modificó el http://localhost:8080/api por la http://localhost:8080/api
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}`; 
  private authUrl = `${environment.authUrl}`;
  private tokenKey = 'auth_token';
  private currentUserSubject = new BehaviorSubject<any>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {
    // Verificar si hay un token guardado al inicializar
    const token = this.getToken();
    if (token) {
      // Aquí podrías validar el token con el backend
      this.currentUserSubject.next({ token });
    }
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.authUrl}/login`, credentials)
      .pipe(
        tap(response => {
          localStorage.setItem(this.tokenKey, response.token);
          this.currentUserSubject.next(response.user);
        })
      );
  }

  registerTrial(data: RegisterTrialRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.authUrl}/trial`, data).pipe(
      tap(response => {
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
    if (!token) return false;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const now = Math.floor(Date.now() / 1000);
      if (payload.exp && payload.exp < now) {
        this.logout();
        return false;
      }
      return true;
    } catch {
      this.logout();
      return false;
    }
  }

  // Decodifica el JWT y devuelve el campo 'sub' (email/username del usuario)
  getUserId(): string {
    const token = this.getToken();
    if (!token) return 'anonimo';
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.sub || payload.email || payload.username || 'anonimo';
    } catch {
      return 'anonimo';
    }
  }

  isTrial(): boolean {
    const token = this.getToken();
    if (!token) return false;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.isTrial === true;
    } catch {
      return false;
    }
  }

  getTrialDaysLeft(): number {
    const token = this.getToken();
    if (!token) return 0;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      if (!payload.trialExpiresAt) return 0;
      const msLeft = payload.trialExpiresAt - Date.now();
      return Math.max(0, Math.ceil(msLeft / (1000 * 60 * 60 * 24)));
    } catch {
      return 0;
    }
  }

  handleTrialExpired(): void {
    this.logout();
    this.router.navigate(['/registro'], { queryParams: { plan: 'Basico', precio: '29.99' } });
  }

  // Método para agregar el token a las peticiones HTTP
  getAuthHeaders() {
    const token = this.getToken();
    return token ? { 'Authorization': `Bearer ${token}` } : {};
  }
}