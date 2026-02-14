import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-navbar',
  standalone: true,
  templateUrl: './navbar.component.html'
})
export class NavbarComponent {

  constructor(private router: Router) {}

  irAMesas() {
    console.log('➡️ Navbar: ir a Mesas');
    this.router.navigate(['/mesas']);
  }

  irAVentas() {
    console.log('➡️ Navbar: ir a Ventas');
    this.router.navigate(['/ventas']);
  }

  irACotizaciones() {
    console.log('➡️ Navbar: ir a Cotizaciones');
    this.router.navigate(['/cotizaciones']);
  }
}
