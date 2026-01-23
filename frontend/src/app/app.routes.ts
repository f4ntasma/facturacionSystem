import { Routes } from '@angular/router';
import { MesasComponent } from './pages/mesas/mesas.component';
import { MesaDetalleComponent } from './pages/mesa-detalle/mesa-detalle.component';
import { LoginComponent } from './pages/login/login';

export const routes: Routes = [
  // Login
  { path: 'login', component: LoginComponent },
  
  // Redirigir a login por defecto
  { path: '', redirectTo: '/login', pathMatch: 'full' },

  // Ventana por piso
  { path: 'pisos/:piso', component: MesasComponent },

  // Detalle de mesa
  { path: 'mesa/:id', component: MesaDetalleComponent },

  // Cualquier otra cosa redirige a login
  { path: '**', redirectTo: '/login' }
];
