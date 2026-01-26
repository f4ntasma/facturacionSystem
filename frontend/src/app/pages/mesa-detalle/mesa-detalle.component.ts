import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-mesa-detalle',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './mesa-detalle.component.html'
})
export class MesaDetalleComponent implements OnInit {
  mesaId: string = '';

  constructor(private route: ActivatedRoute) {}

  ngOnInit() {
    this.mesaId = this.route.snapshot.params['id'];
  }
}