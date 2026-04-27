import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
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
    FormsModule,
    TableModule,
    ButtonModule,
    CardModule,
    InputTextModule,
    TagModule,
    ConfirmDialogModule,
    ToastModule
  ],
  providers: [ConfirmationService, MessageService],
  templateUrl: './facturas-list.component.html'
})
export class FacturasListComponent implements OnInit {
  facturas: Factura[] = [];
  loading = false;
  searchTerm = '';

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

  getEstadoClass(estado: string | undefined): string {
    switch (estado) {
      case 'PAGADA': return 'badge-success';
      case 'PENDIENTE': return 'badge-warning';
      case 'CANCELADA': return 'badge-error';
      default: return 'badge-info';
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