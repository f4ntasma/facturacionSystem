import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService, MessageService } from 'primeng/api';
import { FacturaService, Factura } from '../../services/factura.service';

@Component({
  selector: 'app-facturas-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ToastModule, ConfirmDialogModule],
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
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudieron cargar las facturas' });
        this.loading = false;
      }
    });
  }

  get facturasFiltradas(): Factura[] {
    if (!this.searchTerm) return this.facturas;
    const term = this.searchTerm.toLowerCase();
    return this.facturas.filter(f =>
      (f.numero || '').toLowerCase().includes(term) ||
      (f.clienteNombre || '').toLowerCase().includes(term) ||
      (f.clienteRuc || '').toLowerCase().includes(term)
    );
  }

  getEstadoClass(estado: string | undefined): string {
    switch (estado) {
      case 'PAGADA':    return 'badge-success';
      case 'PENDIENTE': return 'badge-warning';
      case 'CANCELADA': return 'badge-error';
      default:          return 'badge-warning';
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
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo descargar el PDF' });
      }
    });
  }

  confirmarEliminar(factura: Factura) {
    this.confirmationService.confirm({
      message: `¿Está seguro de eliminar la factura "${factura.numero}"?`,
      header: 'Confirmar Eliminación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => this.eliminarFactura(factura.id!)
    });
  }

  eliminarFactura(id: number) {
    this.facturaService.deleteFactura(id).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Éxito', detail: 'Factura eliminada correctamente' });
        this.loadFacturas();
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo eliminar la factura' });
      }
    });
  }
}
