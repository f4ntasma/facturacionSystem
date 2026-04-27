import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ClienteService, Cliente } from '../../services/cliente.service';

@Component({
  selector: 'app-clientes-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule
  ],
  templateUrl: './clientes-list.component.html'
})
export class ClientesListComponent implements OnInit {
  clientes: Cliente[] = [];
  loading = false;
  searchTerm = '';

  constructor(
    private clienteService: ClienteService
  ) {}

  ngOnInit() {
    this.loadClientes();
  }

  loadClientes() {
    this.loading = true;
    this.clienteService.getClientes().subscribe({
      next: (clientes) => {
        this.clientes = clientes;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error cargando clientes:', error);
        this.loading = false;
      }
    });
  }

  onGlobalFilter(event: any) {
    const value = (event.target as HTMLInputElement).value;
    // Implementar filtro si es necesario
  }

  eliminarCliente(id: number) {
    if (confirm('¿Está seguro de eliminar este cliente?')) {
      this.clienteService.deleteCliente(id).subscribe({
        next: () => {
          this.loadClientes();
        },
        error: (error) => {
          console.error('Error eliminando cliente:', error);
        }
      });
    }
  }
}
