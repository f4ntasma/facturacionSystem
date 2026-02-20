import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { TagModule } from 'primeng/tag';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ToastModule } from 'primeng/toast';
import { ConfirmationService, MessageService } from 'primeng/api';

import { ProductoService, Producto } from '../../services/producto.service';
import { NavbarService } from '../../layout/navbar.service';

@Component({
  selector: 'app-productos-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    TableModule,
    ButtonModule,
    CardModule,
    InputTextModule,
    TagModule,
    ConfirmDialogModule,
    ToastModule
  ],
  providers: [ConfirmationService, MessageService],
  template: `
    <p-card>
      <ng-template pTemplate="header">
        <div class="flex align-items-center justify-content-between p-3 pb-0">
          <h3 class="m-0">Gestión de Productos</h3>
          <button
            pButton
            icon="pi pi-plus"
            label="Nuevo Producto"
            class="p-button-success"
            routerLink="/productos/nuevo">
          </button>
        </div>
      </ng-template>

      <p-table
        [value]="productos"
        [loading]="loading"
        [paginator]="true"
        [rows]="10"
        responsiveLayout="scroll">

        <ng-template pTemplate="header">
          <tr>
            <th>Nombre</th>
            <th>Precio</th>
            <th>Stock</th>
            <th>Acciones</th>
          </tr>
        </ng-template>

        <ng-template pTemplate="body" let-producto>
          <tr>
            <td>{{ producto.nombre }}</td>
            <td>{{ producto.precio | number:'1.2-2' }}</td>
            <td>
              <p-tag
                [value]="producto.stock.toString()"
                [severity]="getStockSeverity(producto.stock)">
              </p-tag>
            </td>
            <td>
              <button
                pButton
                icon="pi pi-trash"
                class="p-button-text p-button-danger"
                (click)="confirmarEliminar(producto)">
              </button>
            </td>
          </tr>
        </ng-template>

      </p-table>
    </p-card>

    <p-confirmDialog></p-confirmDialog>
    <p-toast></p-toast>
  `
})
export class ProductosListComponent implements OnInit, OnDestroy {

  productos: Producto[] = [];
  loading = false;

  constructor(
    private productoService: ProductoService,
    private confirmationService: ConfirmationService,
    private messageService: MessageService,
    private navbarService: NavbarService
  ) {}

  // 👉 ENTRADA A /productos
  ngOnInit(): void {
    console.log('🟢 ProductosListComponent INIT → navbar = productos');
    this.navbarService.cambiarNavbar('productos');
    //this.loadProductos();
  }

  // 👉 SALIDA DE /productos
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
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudieron cargar los productos'
        });
      }
    });
  }

  getStockSeverity(stock: number): 'success' | 'warn' | 'danger' {
    if (stock === 0) return 'danger';
    if (stock < 10) return 'warn';
    return 'success';
  }

  confirmarEliminar(producto: Producto): void {
    this.confirmationService.confirm({
      message: `¿Eliminar el producto "${producto.nombre}"?`,
      header: 'Confirmación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => this.eliminarProducto(producto.id!)
    });
  }

  eliminarProducto(id: number): void {
    console.log('🗑️ Eliminando producto', id);
    this.productoService.deleteProducto(id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Eliminado',
          detail: 'Producto eliminado correctamente'
        });
        this.loadProductos();
      },
      error: (error) => {
        console.error('❌ Error eliminando producto:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo eliminar el producto'
        });
      }
    });
  }
}