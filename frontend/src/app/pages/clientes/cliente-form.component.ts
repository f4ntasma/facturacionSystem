import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { ClienteService, Cliente } from '../../services/cliente.service';

@Component({
  selector: 'app-cliente-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  templateUrl: './cliente-form.component.html'
})
export class ClienteFormComponent implements OnInit {
  clienteForm: FormGroup;
  isEditing = false;
  clienteId: number | null = null;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private clienteService: ClienteService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.clienteForm = this.fb.group({
      nombre: ['', [Validators.required]],
      apellido: ['', [Validators.required]],
      dni: ['', [Validators.required]],
      direccion: [''],
      telefono: [''],
      email: ['', [Validators.email]]
    });
  }

  ngOnInit() {
    this.clienteId = this.route.snapshot.params['id'];
    this.isEditing = !!this.clienteId;

    if (this.isEditing) {
      this.loadCliente();
    }
  }

  loadCliente() {
    if (this.clienteId) {
      this.clienteService.getCliente(this.clienteId).subscribe({
        next: (cliente) => {
          this.clienteForm.patchValue(cliente);
        },
        error: (error) => {
          console.error('Error cargando cliente:', error);
        }
      });
    }
  }

  onSubmit() {
    if (this.clienteForm.valid) {
      this.loading = true;
      const clienteData: Cliente = this.clienteForm.value;

      const operation = this.isEditing 
        ? this.clienteService.updateCliente(this.clienteId!, clienteData)
        : this.clienteService.createCliente(clienteData);

      operation.subscribe({
        next: (cliente) => {
          this.loading = false;
          this.router.navigate(['/clientes']);
        },
        error: (error) => {
          console.error('Error guardando cliente:', error);
          this.loading = false;
        }
      });
    }
  }

  volver() {
    this.router.navigate(['/clientes']);
  }
}
