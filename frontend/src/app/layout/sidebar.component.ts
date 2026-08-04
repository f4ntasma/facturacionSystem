// src/app/layout/sidebar.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { NavbarService } from './navbar.service';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './sidebar.component.html'
})
export class SidebarComponent implements OnInit {
  isTrial = false;
  trialDaysLeft = 0;

  constructor(
    private router: Router,
    private navbarService: NavbarService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.isTrial = this.authService.isTrial();
    this.trialDaysLeft = this.authService.getTrialDaysLeft();
  }

  irAPago() {
    this.router.navigate(['/registro'], { queryParams: { plan: 'Basico', precio: '29.99' } });
  }

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
