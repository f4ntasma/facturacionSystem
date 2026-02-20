import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd } from '@angular/router';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule], // 🔥 ESTO ES LO QUE FALTABA
  templateUrl: './navbar.component.html'
})
export class NavbarComponent implements OnInit {

  modo: 'ventas' | 'productos' = 'ventas';

  constructor(private router: Router) {}

  ngOnInit() {
    this.detectarModo(this.router.url);

    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        this.detectarModo(event.urlAfterRedirects);
      }
    });
  }

  private detectarModo(url: string) {
    this.modo = url.startsWith('/productos') ? 'productos' : 'ventas';
  }

  // Ventas
  irAMesas() { this.router.navigate(['/pisos/1']); }
  irAVentas() { this.router.navigate(['/ventas']); }
  irACotizaciones() { this.router.navigate(['/cotizaciones']); }

  // Productos
  irAProductos() { this.router.navigate(['/productos']); }
  irACombos() { this.router.navigate(['/combos']); }
  irACategorias() { this.router.navigate(['/categorias']); }
}