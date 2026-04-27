import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ToastModule } from 'primeng/toast';
import { FormsModule } from '@angular/forms';
import { ConfirmationService, MessageService } from 'primeng/api';
import { EmpresaService, Empresa } from '../../services/empresa.service';

@Component({
  selector: 'app-empresas-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    TableModule,
    ButtonModule,
    CardModule,
    InputTextModule,
    ConfirmDialogModule,
    ToastModule
  ],
  providers: [ConfirmationService, MessageService],
  templateUrl: './empresas-list.component.html'
})
export class EmpresasListComponent implements OnInit {
  empresas: Empresa[] = [];
  loading = false;
  error = false;
  searchTerm = '';

  constructor(
    private empresaService: EmpresaService,
    private confirmationService: ConfirmationService,
    private messageService: MessageService
  ) {}

  ngOnInit() {
    this.loadEmpresas();
  }

  loadEmpresas() {
    this.loading = true;
    this.error = false;
    this.empresaService.getEmpresas().subscribe({
      next: (empresas) => {
        this.empresas = empresas;
        this.loading = false;
        this.error = false;
      },
      error: (error) => {
        console.error('Error cargando empresas:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudieron cargar las empresas'
        });
        this.error = true;
        this.loading = false;
      }
    });
  }

  onGlobalFilter(event: any) {
    const value = (event.target as HTMLInputElement).value;
    // Implementar filtro global si es necesario
  }

  confirmarEliminar(empresa: Empresa) {
    this.confirmationService.confirm({
      message: `¿Está seguro de eliminar la empresa "${empresa.nombre}"?`,
      header: 'Confirmar Eliminación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.eliminarEmpresa(empresa.id!);
      }
    });
  }

  eliminarEmpresa(id: number) {
    this.empresaService.deleteEmpresa(id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Éxito',
          detail: 'Empresa eliminada correctamente'
        });
        this.loadEmpresas();
      },
      error: (error) => {
        console.error('Error eliminando empresa:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo eliminar la empresa'
        });
      }
    });
  }
}