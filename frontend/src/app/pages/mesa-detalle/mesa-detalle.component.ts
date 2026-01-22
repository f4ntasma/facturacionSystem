import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  standalone: true,
  templateUrl: './mesa-detalle.component.html'
})
export class MesaDetalleComponent {
  mesaId!: number;

  constructor(private route: ActivatedRoute) {
    this.mesaId = Number(this.route.snapshot.paramMap.get('id'));
  }
}
