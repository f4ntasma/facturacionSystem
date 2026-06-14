import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { ClienteService, Cliente } from '../../services/cliente.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-cliente-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './cliente-form.component.html'
})
export class ClienteFormComponent implements OnInit {
  clienteForm: FormGroup;
  isEditing = false;
  clienteId: string | null = null;
  loading = false;
  buscandoDni = false;
  errorDni = '';

  constructor(
    private fb: FormBuilder,
    private clienteService: ClienteService,
    private router: Router,
    private route: ActivatedRoute,
    private http: HttpClient,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {
    this.clienteForm = this.fb.group({
      nombre:    ['', [Validators.required]],
      apellido:  ['', [Validators.required]],
      dni:       ['', [Validators.required]],
      direccion: [''],
      telefono:  [''],
      email:     ['', [Validators.email]]
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
          this.cdr.detectChanges();
        },
        error: (error) => console.error('Error cargando cliente:', error)
      });
    }
  }

  buscarDni() {
    const dni = this.clienteForm.get('dni')?.value;
    if (!dni || dni.length !== 8) {
      this.errorDni = 'El DNI debe tener 8 dígitos';
      return;
    }

    this.buscandoDni = true;
    this.errorDni = '';

    const token = this.authService.getToken();
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });

    this.http.get<any>(`http://localhost:8081/api/v1/consulta/dni/${dni}`, { headers }).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.clienteForm.patchValue({
            nombre:    res.data.nombres,
            apellido:  `${res.data.apellido_paterno} ${res.data.apellido_materno}`.trim(),
            direccion: res.data.direccion_completa || res.data.direccion || ''
          });
        } else {
          this.errorDni = 'No se encontró el DNI';
        }
        this.buscandoDni = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorDni = 'Error al consultar el DNI';
        this.buscandoDni = false;
        this.cdr.detectChanges();
      }
    });
  }

  onSubmit() {
    if (this.clienteForm.valid) {
      this.loading = true;
      const clienteData: Cliente = this.clienteForm.value;

      const operation = this.isEditing
        ? this.clienteService.updateCliente(this.clienteId!, clienteData)
        : this.clienteService.createCliente(clienteData);

      operation.subscribe({
        next: () => {
          this.loading = false;
          this.router.navigate(['/app/clientes']);
        },
        error: (error) => {
          console.error('Error guardando cliente:', error);
          this.loading = false;
        }
      });
    }
  }

  volver() {
    this.router.navigate(['/app/clientes']);
  }
}