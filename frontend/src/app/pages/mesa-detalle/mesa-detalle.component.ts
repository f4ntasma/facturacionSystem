import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';

interface Plato {
  id: number;
  nombre: string;
  precio: number;
}

interface PedidoItem {
  plato: Plato;
  cantidad: number;
  comentario: string;
}

@Component({
  selector: 'app-mesa-detalle',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './mesa-detalle.component.html',
  styleUrl: './mesa-detalle.component.css'
})
export class MesaDetalleComponent implements OnInit {

  mesaId!: number;

  platos: Plato[] = [
    { id: 1, nombre: 'Lomo Saltado', precio: 15 },
    { id: 2, nombre: 'Ají de Gallina', precio: 12 },
    { id: 3, nombre: 'Gaseosa', precio: 8 }
  ];

  pedido: PedidoItem[] = [];

  // MODAL
  mostrarModal = false;
  platoSeleccionado!: Plato;
  cantidad = 1;
  comentario = '';

  constructor(
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.mesaId = Number(this.route.snapshot.paramMap.get('id'));
  }

  volver() {
    this.router.navigate(['/mesas']);
  }

  abrirModal(plato: Plato) {
    console.log('CLICK EN PRODUCTO:', plato);
    this.platoSeleccionado = plato;
    this.cantidad = 1;
    this.comentario = '';
    this.mostrarModal = true;
  }

  agregarPlato() {
    this.pedido.push({
      plato: this.platoSeleccionado,
      cantidad: this.cantidad,
      comentario: this.comentario
    });
    this.mostrarModal = false;
  }

  quitarPlato(index: number) {
    this.pedido.splice(index, 1);
  }

  total(): number {
    return this.pedido.reduce(
      (sum, p) => sum + p.plato.precio * p.cantidad,
      0
    );
  }
}
