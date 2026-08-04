import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-opciones',
  standalone: true,
  templateUrl: './opciones.component.html'
})
export class OpcionesComponent {
  constructor(private router: Router, private authService: AuthService) {}

  cerrarSesion() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
