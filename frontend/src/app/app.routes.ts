import { Routes } from '@angular/router';
import { MesasComponent } from './pages/mesas/mesas.component';
import { MesaDetalleComponent } from './pages/mesa-detalle/mesa-detalle.component';

export const routes: Routes = [
  { path: '', component: MesasComponent },
  { path: 'mesa/:id', component: MesaDetalleComponent }
];

