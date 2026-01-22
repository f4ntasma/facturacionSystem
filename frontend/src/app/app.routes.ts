import { Routes } from '@angular/router';
import { MesasComponent } from './pages/mesas/mesas.component';
import { MesaDetalleComponent } from './pages/mesa-detalle/mesa-detalle.component';

export const routes: Routes = [
  // Piso por defecto
  { path: '', redirectTo: 'pisos/1', pathMatch: 'full' },

  // Ventana por piso
  { path: 'pisos/:piso', component: MesasComponent },

  // Detalle de mesa (no se toca)
  { path: 'mesa/:id', component: MesaDetalleComponent },

  // Cualquier otra cosa
  { path: '**', redirectTo: 'pisos/1' }
];
