import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';

import { SidebarComponent } from './layout/sidebar.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    SidebarComponent,
    CommonModule
  ],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  showLayout = true;

  constructor(private router: Router) {}

  ngOnInit() {
    // Verificar la ruta inicial
    this.checkRoute(this.router.url);
    
    // Escuchar cambios de ruta para mostrar/ocultar el layout
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: NavigationEnd) => {
        this.checkRoute(event.url);
      });
  }

  private checkRoute(url: string) {
    // Ocultar layout en la página de login, landing page y cualquier ruta que no sea /app/*
    const isLandingOrLogin = url === '/' || url.includes('/login');
    const isAppRoute = url.startsWith('/app');
    this.showLayout = isAppRoute && !isLandingOrLogin;
    console.log('Current URL:', url, 'Show Layout:', this.showLayout);
  }
}
