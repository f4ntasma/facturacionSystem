import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  templateUrl: './sidebar.component.html'
})
export class SidebarComponent {

  constructor(private router: Router) {}

  irAVentas() {
    console.log('➡️ Sidebar: Ventas');
    this.router.navigate(['/ventas']);
  }

  irAProductos() {
    console.log('➡️ Sidebar: Productos');
    this.router.navigate(['/productos']);
  }

  irAClientes() {
    console.log('➡️ Sidebar: Clientes');
    this.router.navigate(['/clientes']);
  }

  irACaja() {
    console.log('➡️ Sidebar: Caja');
    this.router.navigate(['/caja']);
  }

  irAOpciones() {
    console.log('➡️ Sidebar: Opciones');
    this.router.navigate(['/opciones']);
  }
}
