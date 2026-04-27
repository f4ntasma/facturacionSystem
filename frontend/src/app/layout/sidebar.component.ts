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
    this.router.navigate(['/app/ventas']);
  }

  irAProductos() {
    console.log('➡️ Sidebar → Productos');
    this.navbarService.cambiarNavbar('productos');
    this.router.navigate(['/app/productos']);
  }

  irACotizaciones() {
    console.log('➡️ Sidebar → Cotizaciones');
    this.navbarService.cambiarNavbar('main');
    this.router.navigate(['/app/cotizaciones']);
  }

  irAMesas() {
    console.log('➡️ Sidebar → Mesas');
    this.navbarService.cambiarNavbar('main');
    this.router.navigate(['/app/pisos/1']);
  }

  irAFacturas() {
    console.log('➡️ Sidebar → Facturas');
    this.navbarService.cambiarNavbar('main');
    this.router.navigate(['/app/facturas']);
  }

  irAEmpresas() {
    console.log('➡️ Sidebar → Empresas');
    this.navbarService.cambiarNavbar('main');
    this.router.navigate(['/app/empresas']);
  }

  // ---------------------
  // Métodos de navegación
  // ---------------------
  irAClientes() {
    console.log('➡️ Sidebar → Clientes');
    this.navbarService.cambiarNavbar('main');
    this.router.navigate(['/app/clientes']);
  }

  irACaja() {
    console.log('➡️ Sidebar → Caja');
    this.navbarService.cambiarNavbar('main');
    this.router.navigate(['/app/caja']);
  }

  irAOpciones() {
    console.log('➡️ Sidebar → Opciones');
    this.navbarService.cambiarNavbar('main');
    this.router.navigate(['/app/opciones']);
  }
}