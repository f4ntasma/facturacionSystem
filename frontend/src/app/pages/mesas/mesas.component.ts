import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-mesas',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './mesas.component.html',
  styleUrls: ['./mesas.component.css']
})
export class MesasComponent {

  mesas = [
    { id: 1, nombre: 'Mesa 1', total: 0 }
  ];

  constructor(private router: Router) {}

  agregarMesa() {
    const id = this.mesas.length + 1;
    this.mesas.push({
      id,
      nombre: `Mesa ${id}`,
      total: 0
    });
  }

  verMesa(mesa: any) {
    this.router.navigate(['/mesa', mesa.id]);
  }
}
