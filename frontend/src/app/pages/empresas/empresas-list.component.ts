import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ToastModule } from 'primeng/toast';
import { ConfirmationService, MessageService } from 'primeng/api';
import { EmpresaService, Empresa } from '../../services/empresa.service';

@Component({
  selector: 'app-empresas-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    TableModule,
    ButtonModule,
    CardModule,
    InputTextModule,
    ConfirmDialogModule,
    ToastModule
  ],
  providers: [ConfirmationService, MessageService],
  template: `
    <p-card>
      <ng-template pTemplate="header">
        <div class="flex align-items-center justify-content-between p-3 pb-0">
          <h3 class="m-0">Gestión de Empresas</h3>
          <button pButton type="button" icon="pi pi-plus" label="Nueva Empresa" 
                  routerLink="/empresas/nueva" class="p-button-success"></button>
        </div>
      </ng-template>

      <div class="mb-3">
        <span class="p-input-icon-left w-full">
          <i class="pi pi-search"></i>
          <input pInputText type="text" placeholder="Buscar empresas..." 
                 class="w-full" (input)="onGlobalFilter($event)">
        </span>
      </div>

      <p-table #dt [value]="empresas" [loading]="loading" [paginator]="true" [rows]="10"
               [globalFilterFields]="['nombre', 'ruc', 'email']" responsiveLayout="scroll">
        
        <ng-template pTemplate="header">
          <tr>
            <th pSortableColumn="nombre">
              Nombre <p-sortIcon field="nombre"></p-sortIcon>
            </th>
            <th pSortableColumn="ruc">
              RUC <p-sortIcon field="ruc"></p-sortIcon>
            </th>
            <th>Dirección</th>
            <th>Teléfono</th>
            <th>Email</th>
            <th>Acciones</th>
          </tr>
        </ng-template>

        <ng-template pTemplate="body" let-empresa>
          <tr>
            <td>
              <span class="font-medium">{{ empresa.nombre }}</span>
            </td>
            <td>{{ empresa.ruc }}</td>
            <td>{{ empresa.direccion }}</td>
            <td>{{ empresa.telefono || '-' }}</td>
            <td>{{ empresa.email || '-' }}</td>
            <td>
              <div class="flex gap-2">
                <button pButton type="button" icon="pi pi-eye" 
                        class="p-button-rounded p-button-text p-button-info"
                        [routerLink]="['/empresas', empresa.id]"
                        pTooltip="Ver detalles"></button>
                <button pButton type="button" icon="pi pi-pencil" 
                        class="p-button-rounded p-button-text p-button-warning"
                        [routerLink]="['/empresas', empresa.id, 'editar']"
                        pTooltip="Editar"></button>
                <button pButton type="button" icon="pi pi-trash" 
                        class="p-button-rounded p-button-text p-button-danger"
                        (click)="confirmarEliminar(empresa)"
                        pTooltip="Eliminar"></button>
              </div>
            </td>
          </tr>
        </ng-template>

        <ng-template pTemplate="emptymessage">
          <tr>
            <td colspan="6" class="text-center p-4">
              <div class="flex flex-column align-items-center gap-3">
                <i class="pi pi-building text-4xl text-400"></i>
                <span class="text-lg">No hay empresas registradas</span>
                <button pButton type="button" label="Crear Primera Empresa" 
                        routerLink="/empresas/nueva" class="p-button-sm"></button>
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
export class EmpresasListComponent implements OnInit {
  empresas: Empresa[] = [];
  loading = false;

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
    this.empresaService.getEmpresas().subscribe({
      next: (empresas) => {
        this.empresas = empresas;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error cargando empresas:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudieron cargar las empresas'
        });
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