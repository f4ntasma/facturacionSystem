// src/app/layout/sidebar.component.ts
import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { NavbarService } from './navbar.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  templateUrl: './sidebar.component.html'
})
export class SidebarComponent {

  constructor(
    private router: Router,
    private navbarService: NavbarService
  ) {
    console.log('📦 Sidebar CONSTRUIDO');
  }

  // ---------------------
  // Métodos implementados
  // ---------------------
  irAVentas() {
    console.log('➡️ Sidebar → Ventas');
    this.navbarService.cambiarNavbar('main');
    this.router.navigate(['/ventas']);
  }

  irAProductos() {
    console.log('➡️ Sidebar → Productos');
    this.navbarService.cambiarNavbar('productos');
    this.router.navigate(['/productos']);
  }

  // ---------------------
  // Métodos vacíos (placeholders)
  // ---------------------
  irAClientes() {
      console.log('➡️ Sidebar → Clientes (pendiente)');
    }

  irACaja() {
    console.log('➡️ Sidebar → Caja (pendiente)');
    // Aquí iría la lógica de navegación a Caja cuando esté lista
  }

  irAOpciones() {
    console.log('➡️ Sidebar → Opciones (pendiente)');
    // Aquí iría la lógica de navegación a Opciones cuando esté lista
  }
}