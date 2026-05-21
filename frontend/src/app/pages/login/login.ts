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
            style="background-image: url('/svg/backgroundFactu.png')"
        >
            <!-- Overlay -->
            <div class="absolute inset-0 backdrop-blur-sm bg-black/20"></div>

            <!-- Contenedor glass -->
            <div
                class="relative z-10 w-full max-w-md mx-6 p-8 backdrop-blur-2xl
                       rounded-3xl bg-white/10 border border-white/20 shadow-2xl"
            >
                <div class="flex flex-col items-center text-center mb-8">
                    <img src="/icon.png" class="h-16 w-20 mb-4 object-contain">
                    <h1 class="text-4xl font-bold text-white mb-2">Bienvenido</h1>
                    <p class="text-white/90 text-lg"> A Factullama </p>
                </div>

                <form (ngSubmit)="onLogin()" class="space-y-6">

                    <!-- Mensaje de error -->
                    <div *ngIf="errorMessage"
                        class="bg-red-500/20 border border-red-500/30 rounded-2xl p-4 text-center">
                        <p class="text-red-200 text-sm">{{ errorMessage }}</p>
                    </div>

                    <!-- Usuario -->
                    <input
                        type="text"
                        [(ngModel)]="email"
                        name="email"
                        required
                        [disabled]="isLoading"
                        class="w-full px-4 py-4 bg-white/15 border border-white/20 rounded-2xl text-white"
                        placeholder="Usuario"
                    />

                    <!-- Contraseña -->
                    <input
                        type="password"
                        [(ngModel)]="password"
                        name="password"
                        required
                        [disabled]="isLoading"
                        class="w-full px-4 py-4 bg-white/15 border border-white/20 rounded-2xl text-white"
                        placeholder="Contraseña"
                    />

                    <!-- Botón -->
                    <button
                        type="submit"
                        [disabled]="isLoading"
                        class="w-full py-4 bg-white/25 hover:bg-white/35 rounded-2xl text-white font-semibold"
                    >
                        <span *ngIf="!isLoading">Iniciar Sesión</span>
                        <span *ngIf="isLoading">Cargando...</span>
                    </button>

                </form>
            </div>
        </div>
    `,
    styles: [`
        :host {
            display: block;
            height: 100vh;
        }
    `]
})
export class LoginComponent {

    email: string = '';
    password: string = '';
    isLoading: boolean = false;
    errorMessage: string = '';

    constructor(
        private router: Router,
        private authService: AuthService
    ) {}

    onLogin() {
        if (!this.email || !this.password) {
            this.errorMessage = 'Por favor ingresa email y contraseña';
            return;
        }

        this.isLoading = true;
        this.errorMessage = '';

        this.authService.login({
            email: this.email,
            password: this.password
        }).subscribe({
            next: () => {
                this.isLoading = false;
                this.router.navigate(['/app/ventas']);
            },
            error: () => {
                this.isLoading = false;
                this.errorMessage = 'Credenciales incorrectas';
            }
        });
    }
}