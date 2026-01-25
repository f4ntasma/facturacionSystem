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
import { FacturaService, Factura } from '../../services/factura.service';

@Component({
  selector: 'app-facturas-list',
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
          <h3 class="m-0">Gestión de Facturas</h3>
          <button pButton type="button" icon="pi pi-plus" label="Nueva Factura" 
                  routerLink="/facturas/nueva" class="p-button-success"></button>
        </div>
      </ng-template>

      <div class="mb-3">
        <span class="p-input-icon-left w-full">
          <i class="pi pi-search"></i>
          <input pInputText type="text" placeholder="Buscar facturas..." 
                 class="w-full" (input)="onGlobalFilter($event)">
        </span>
      </div>

      <p-table #dt [value]="facturas" [loading]="loading" [paginator]="true" [rows]="10"
               [globalFilterFields]="['numero', 'clienteNombre', 'clienteRuc']" responsiveLayout="scroll">
        
        <ng-template pTemplate="header">
          <tr>
            <th pSortableColumn="numero">
              Número <p-sortIcon field="numero"></p-sortIcon>
            </th>
            <th pSortableColumn="fecha">
              Fecha <p-sortIcon field="fecha"></p-sortIcon>
            </th>
            <th>Cliente</th>
            <th>RUC</th>
            <th pSortableColumn="total">
              Total <p-sortIcon field="total"></p-sortIcon>
            </th>
            <th>Estado</th>
            <th>Acciones</th>
          </tr>
        </ng-template>

        <ng-template pTemplate="body" let-factura>
          <tr>
            <td>
              <span class="font-medium">{{ factura.numero }}</span>
            </td>
            <td>{{ factura.fecha | date:'dd/MM/yyyy' }}</td>
            <td>{{ factura.clienteNombre }}</td>
            <td>{{ factura.clienteRuc || '-' }}</td>
            <td>\${{ factura.total | number:'1.2-2' }}</td>
            <td>
              <p-tag [value]="factura.estado || 'PENDIENTE'" 
                     [severity]="getEstadoSeverity(factura.estado)">
              </p-tag>
            </td>
            <td>
              <div class="flex gap-2">
                <button pButton type="button" icon="pi pi-eye" 
                        class="p-button-rounded p-button-text p-button-info"
                        [routerLink]="['/facturas', factura.id]"
                        pTooltip="Ver detalles"></button>
                <button pButton type="button" icon="pi pi-file-pdf" 
                        class="p-button-rounded p-button-text p-button-warning"
                        (click)="descargarPDF(factura.id!)"
                        pTooltip="Descargar PDF"></button>
                <button pButton type="button" icon="pi pi-pencil" 
                        class="p-button-rounded p-button-text p-button-warning"
                        [routerLink]="['/facturas', factura.id, 'editar']"
                        pTooltip="Editar"></button>
                <button pButton type="button" icon="pi pi-trash" 
                        class="p-button-rounded p-button-text p-button-danger"
                        (click)="confirmarEliminar(factura)"
                        pTooltip="Eliminar"></button>
              </div>
            </td>
          </tr>
        </ng-template>

        <ng-template pTemplate="emptymessage">
          <tr>
            <td colspan="7" class="text-center p-4">
              <div class="flex flex-column align-items-center gap-3">
                <i class="pi pi-file-o text-4xl text-400"></i>
                <span class="text-lg">No hay facturas registradas</span>
                <button pButton type="button" label="Crear Primera Factura" 
                        routerLink="/facturas/nueva" class="p-button-sm"></button>
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
export class FacturasListComponent implements OnInit {
  facturas: Factura[] = [];
  loading = false;

  constructor(
    private facturaService: FacturaService,
    private confirmationService: ConfirmationService,
    private messageService: MessageService
  ) {}

  ngOnInit() {
    this.loadFacturas();
  }

  loadFacturas() {
    this.loading = true;
    this.facturaService.getFacturas().subscribe({
      next: (facturas) => {
        this.facturas = facturas;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error cargando facturas:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudieron cargar las facturas'
        });
        this.loading = false;
      }
    });
  }

  onGlobalFilter(event: any) {
    const value = (event.target as HTMLInputElement).value;
    // Implementar filtro global si es necesario
  }

  getEstadoSeverity(estado: string | undefined): "success" | "warn" | "danger" | "info" {
    switch (estado) {
      case 'PAGADA': return 'success';
      case 'PENDIENTE': return 'warn';
      case 'CANCELADA': return 'danger';
      default: return 'info';
    }
  }

  descargarPDF(id: number) {
    this.facturaService.generarPDF(id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `factura-${id}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Error descargando PDF:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo descargar el PDF'
        });
      }
    });
  }

  confirmarEliminar(factura: Factura) {
    this.confirmationService.confirm({
      message: `¿Está seguro de eliminar la factura "${factura.numero}"?`,
      header: 'Confirmar Eliminación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.eliminarFactura(factura.id!);
      }
    });
  }

  eliminarFactura(id: number) {
    this.facturaService.deleteFactura(id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Éxito',
          detail: 'Factura eliminada correctamente'
        });
        this.loadFacturas();
      },
      error: (error) => {
        console.error('Error eliminando factura:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo eliminar la factura'
        });
      }
    });
  }
}