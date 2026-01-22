import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-mesa-detalle',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './mesa-detalle.component.html',
})
export class MesaDetalleComponent {
  mesaId!: number;

  constructor(private route: ActivatedRoute) {
    this.mesaId = Number(this.route.snapshot.paramMap.get('id'));
  }
}
