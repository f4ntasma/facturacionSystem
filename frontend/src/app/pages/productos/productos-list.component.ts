import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { ProductoService, Producto } from '../../services/producto.service';
import { NavbarService } from '../../layout/navbar.service';

@Component({
  selector: 'app-productos-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './productos-list.component.html',
  styleUrls: ['./productos-list.component.css']
})
export class ProductosListComponent implements OnInit, OnDestroy {

  productos: Producto[] = [];
  loading = false;
  searchTerm = '';

  constructor(
    private productoService: ProductoService,
    private navbarService: NavbarService
  ) {}

  ngOnInit(): void {
    console.log('🟢 ProductosListComponent INIT → navbar = productos');
    this.navbarService.cambiarNavbar('productos');
    this.loadProductos();
  }

  ngOnDestroy(): void {
    console.log('🔴 ProductosListComponent DESTROY → navbar = main');
    this.navbarService.cambiarNavbar('main');
  }

  loadProductos(): void {
    this.loading = true;
    console.log('📦 Cargando productos...');

    this.productoService.getProductos().subscribe({
      next: (productos) => {
        console.log('✅ Productos cargados:', productos.length);
        this.productos = productos;
        this.loading = false;
      },
      error: (error) => {
        console.error('❌ Error cargando productos:', error);
        this.loading = false;
      }
    });
  }

  getStockSeverity(stock: number): string {
    if (stock === 0) return 'badge-error';
    if (stock < 10) return 'badge-warning';
    return 'badge-success';
  }

  getStockLabel(stock: number): string {
    if (stock === 0) return 'Sin Stock';
    if (stock < 10) return 'Bajo Stock';
    return 'En Stock';
  }

  confirmarEliminar(producto: Producto): void {
    if (confirm(`¿Eliminar el producto "${producto.nombre}"?`)) {
      this.eliminarProducto(producto.id!);
    }
  }

  eliminarProducto(id: number): void {
    console.log('🗑️ Eliminando producto', id);
    this.productoService.deleteProducto(id).subscribe({
      next: () => {
        console.log('✅ Producto eliminado');
        this.loadProductos();
      },
      error: (error) => {
        console.error('❌ Error eliminando producto:', error);
      }
    });
  }

  get productosFiltrados(): Producto[] {
    if (!this.searchTerm) return this.productos;
    return this.productos.filter(p => 
      p.nombre.toLowerCase().includes(this.searchTerm.toLowerCase())
    );
  }
}