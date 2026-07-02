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
  ) {}

  irAVentas() {
    this.navbarService.cambiarNavbar('main');
    this.router.navigate(['/app/ventas']);
  }

  irAProductos() {
    this.navbarService.cambiarNavbar('productos');
    this.router.navigate(['/app/productos']);
  }

  irACotizaciones() {
    this.navbarService.cambiarNavbar('main');
    this.router.navigate(['/app/cotizaciones']);
  }

  irAMesas() {
    this.navbarService.cambiarNavbar('main');
    this.router.navigate(['/app/pisos/1']);
  }

  irAFacturas() {
    this.navbarService.cambiarNavbar('main');
    this.router.navigate(['/app/facturas']);
  }

  irAEmpresas() {
    this.navbarService.cambiarNavbar('main');
    this.router.navigate(['/app/empresas']);
  }

  irAClientes() {
    this.navbarService.cambiarNavbar('main');
    this.router.navigate(['/app/clientes']);
  }

  irACaja() {
    this.navbarService.cambiarNavbar('main');
    this.router.navigate(['/app/caja']);
  }

  irAOpciones() {
    this.navbarService.cambiarNavbar('main');
    this.router.navigate(['/app/opciones']);
  }
}
