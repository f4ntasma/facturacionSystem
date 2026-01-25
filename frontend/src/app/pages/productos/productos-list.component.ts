import { Component, OnInit } from '@angular/core';
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
          <button pButton type="button" icon="pi pi-plus" label="Nuevo Producto" 
                  routerLink="/productos/nuevo" class="p-button-success"></button>
        </div>
      </ng-template>

      <div class="mb-3">
        <span class="p-input-icon-left w-full">
          <i class="pi pi-search"></i>
          <input pInputText type="text" placeholder="Buscar productos..." 
                 class="w-full" (input)="onGlobalFilter($event)">
        </span>
      </div>

      <p-table #dt [value]="productos" [loading]="loading" [paginator]="true" [rows]="10"
               [globalFilterFields]="['nombre', 'descripcion', 'categoria']" responsiveLayout="scroll">
        
        <ng-template pTemplate="header">
          <tr>
            <th pSortableColumn="nombre">
              Nombre <p-sortIcon field="nombre"></p-sortIcon>
            </th>
            <th>Descripción</th>
            <th pSortableColumn="precio">
              Precio <p-sortIcon field="precio"></p-sortIcon>
            </th>
            <th pSortableColumn="stock">
              Stock <p-sortIcon field="stock"></p-sortIcon>
            </th>
            <th>Categoría</th>
            <th>Estado</th>
            <th>Acciones</th>
          </tr>
        </ng-template>

        <ng-template pTemplate="body" let-producto>
          <tr>
            <td>
              <span class="font-medium">{{ producto.nombre }}</span>
            </td>
            <td>{{ producto.descripcion || '-' }}</td>
            <td>\${{ producto.precio | number:'1.2-2' }}</td>
            <td>
              <p-tag [value]="producto.stock.toString()" 
                     [severity]="getStockSeverity(producto.stock)">
              </p-tag>
            </td>
            <td>{{ producto.categoria || '-' }}</td>
            <td>
              <p-tag [value]="producto.stock > 0 ? 'Disponible' : 'Agotado'" 
                     [severity]="producto.stock > 0 ? 'success' : 'danger'">
              </p-tag>
            </td>
            <td>
              <div class="flex gap-2">
                <button pButton type="button" icon="pi pi-eye" 
                        class="p-button-rounded p-button-text p-button-info"
                        [routerLink]="['/productos', producto.id]"
                        pTooltip="Ver detalles"></button>
                <button pButton type="button" icon="pi pi-pencil" 
                        class="p-button-rounded p-button-text p-button-warning"
                        [routerLink]="['/productos', producto.id, 'editar']"
                        pTooltip="Editar"></button>
                <button pButton type="button" icon="pi pi-trash" 
                        class="p-button-rounded p-button-text p-button-danger"
                        (click)="confirmarEliminar(producto)"
                        pTooltip="Eliminar"></button>
              </div>
            </td>
          </tr>
        </ng-template>

        <ng-template pTemplate="emptymessage">
          <tr>
            <td colspan="7" class="text-center p-4">
              <div class="flex flex-column align-items-center gap-3">
                <i class="pi pi-box text-4xl text-400"></i>
                <span class="text-lg">No hay productos registrados</span>
                <button pButton type="button" label="Crear Primer Producto" 
                        routerLink="/productos/nuevo" class="p-button-sm"></button>
              </div>
            </td>
          </tr>
        </ng-template>
      </p-table>
    </p-card>

    <p-confirmDialog></p-confirmDialog>
    <p-toast></p-toast>
  `
})
export class ProductosListComponent implements OnInit {
  productos: Producto[] = [];
  loading = false;

  constructor(
    private productoService: ProductoService,
    private confirmationService: ConfirmationService,
    private messageService: MessageService
  ) {}

  ngOnInit() {
    this.loadProductos();
  }

  loadProductos() {
    this.loading = true;
    this.productoService.getProductos().subscribe({
      next: (productos) => {
        this.productos = productos;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error cargando productos:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudieron cargar los productos'
        });
        this.loading = false;
      }
    });
  }

  onGlobalFilter(event: any) {
    const value = (event.target as HTMLInputElement).value;
    // Implementar filtro global si es necesario
  }

  getStockSeverity(stock: number): "success" | "warn" | "danger" {
    if (stock === 0) return 'danger';
    if (stock < 10) return 'warn';
    return 'success';
  }

  confirmarEliminar(producto: Producto) {
    this.confirmationService.confirm({
      message: `¿Está seguro de eliminar el producto "${producto.nombre}"?`,
      header: 'Confirmar Eliminación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.eliminarProducto(producto.id!);
      }
    });
  }

  eliminarProducto(id: number) {
    this.productoService.deleteProducto(id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Éxito',
          detail: 'Producto eliminado correctamente'
        });
        this.loadProductos();
      },
      error: (error) => {
        console.error('Error eliminando producto:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo eliminar el producto'
        });
      }
    });
  }
}