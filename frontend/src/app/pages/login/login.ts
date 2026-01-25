import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, FormsModule],
    template: `
        <div
            class="min-h-screen w-full flex items-center justify-center bg-cover bg-center bg-no-repeat relative"
            style="background-image: url('https://fqjltiegiezfetthbags.supabase.co/storage/v1/object/public/block.images/blocks/signin/signin-glass.jpg')"
        >
            <!-- Overlay para mejor contraste -->
            <div class="absolute inset-0 backdrop-blur-sm bg-black/20"></div>
            
            <!-- Contenedor del formulario -->
            <div class="relative z-10 w-full max-w-md mx-6 p-8 backdrop-blur-2xl rounded-3xl bg-white/10 border border-white/20 shadow-2xl">
                <!-- Header con logo y título -->
                <div class="flex flex-col items-center text-center mb-8">
                    <img src="/icon.png" alt="Logo" class="h-16 w-20 mb-4 object-contain">
                    <h1 class="text-4xl font-bold text-white mb-2">Bienvenido</h1>
                    <p class="text-white/90 text-lg">Factullama C.O.R.P</p>
                </div>

                <!-- Formulario -->
                <form (ngSubmit)="onLogin()" class="space-y-6">
                    <!-- Mensaje de error -->
                    <div *ngIf="errorMessage" class="bg-red-500/20 border border-red-500/30 rounded-2xl p-4 text-center">
                        <p class="text-red-200 text-sm">{{ errorMessage }}</p>
                    </div>

                    <!-- Campo Usuario -->
                    <div class="relative">
                        <div class="flex items-center">
                            <div class="absolute left-4 z-10">
                                <i class="pi pi-user text-white/70 text-lg"></i>
                            </div>
                            <input
                                type="text"
                                [(ngModel)]="username"
                                name="username"
                                required
                                [disabled]="isLoading"
                                class="w-full pl-12 pr-4 py-4 bg-white/15 border border-white/20 rounded-2xl text-white placeholder:text-white/70 outline-none focus:border-white/40 focus:bg-white/20 transition-all duration-200 disabled:opacity-50"
                                placeholder="Usuario"
                            />
                        </div>
                    </div>

                    <!-- Campo Contraseña -->
                    <div class="relative">
                        <div class="flex items-center">
                            <div class="absolute left-4 z-10">
                                <i class="pi pi-lock text-white/70 text-lg"></i>
                            </div>
                            <input
                                type="password"
                                [(ngModel)]="password"
                                name="password"
                                required
                                [disabled]="isLoading"
                                class="w-full pl-12 pr-4 py-4 bg-white/15 border border-white/20 rounded-2xl text-white placeholder:text-white/70 outline-none focus:border-white/40 focus:bg-white/20 transition-all duration-200 disabled:opacity-50"
                                placeholder="Contraseña"
                            />
                        </div>
                    </div>

                    <!-- Botón de login -->
                    <button 
                        type="submit"
                        [disabled]="isLoading"
                        class="w-full py-4 mt-8 bg-white/25 hover:bg-white/35 border border-white/30 rounded-2xl text-white font-semibold text-lg transition-all duration-200 shadow-lg disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        <span *ngIf="!isLoading">Iniciar Sesión</span>
                        <span *ngIf="isLoading" class="flex items-center justify-center">
                            <i class="pi pi-spin pi-spinner mr-2"></i>
                            Iniciando sesión...
                        </span>
                    </button>
                </form>

                <!-- Link de contraseña olvidada -->
                <div class="text-center mt-6">
                    <a href="#" class="text-white/80 hover:text-white/100 text-sm transition-colors">
                        ¿Olvidaste tu contraseña?
                    </a>
                </div>
            </div>
        </div>
    `,
    styles: [`
        :host {
            display: block;
            width: 100%;
            height: 100vh;
        }
    `]
})
export class LoginComponent {
    username: string = '';
    password: string = '';
    isLoading: boolean = false;
    errorMessage: string = '';

    constructor(
        private router: Router,
        private authService: AuthService
    ) {}

    onLogin() {
        if (!this.username || !this.password) {
            this.errorMessage = 'Por favor ingresa usuario y contraseña';
            return;
        }

        console.log('Iniciando login con:', { username: this.username, password: this.password });
        this.isLoading = true;
        this.errorMessage = '';

        this.authService.login({
            username: this.username,
            password: this.password
        }).subscribe({
            next: (response) => {
                console.log('Login exitoso:', response);
                this.router.navigate(['/pisos/1']);
            },
            error: (error) => {
                console.error('Error en login:', error);
                this.isLoading = false;
                
                if (error.status === 401) {
                    this.errorMessage = 'Usuario o contraseña incorrectos';
                } else if (error.status === 0) {
                    this.errorMessage = 'No se puede conectar al servidor. Verifica que el backend esté funcionando.';
                } else {
                    this.errorMessage = 'Error al iniciar sesión. Intenta de nuevo.';
                }
            },
            complete: () => {
                console.log('Login request completed');
                this.isLoading = false;
            }
        });
    }
}

//Configure mal el tamaño del glass, chekas eso bro y lo adaptas.