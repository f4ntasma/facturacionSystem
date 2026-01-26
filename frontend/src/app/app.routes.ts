import { Routes } from '@angular/router';
import { AuthGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login').then(m => m.LoginComponent)
  },
  {
    path: '',
    redirectTo: '/login',
    pathMatch: 'full'
  },
  {
    path: 'pisos/:piso',
    canActivate: [AuthGuard],
    loadComponent: () => import('./pages/mesas/mesas.component').then(m => m.MesasComponent)
  },
  {
    path: 'mesa/:id',
    canActivate: [AuthGuard],
    loadComponent: () => import('./pages/mesa-detalle/mesa-detalle.component').then(m => m.MesaDetalleComponent)
  },
  {
    path: 'admin',
    canActivate: [AuthGuard],
    loadComponent: () => import('./layout/main-layout.component').then(m => m.MainLayoutComponent),
    children: [
      {
        path: '',
        redirectTo: '/admin/empresas',
        pathMatch: 'full'
      },
      {
        path: 'empresas',
        loadComponent: () => import('./pages/empresas/empresas-list.component').then(m => m.EmpresasListComponent)
      },
      {
        path: 'empresas/nueva',
        loadComponent: () => import('./pages/empresas/empresa-form.component').then(m => m.EmpresaFormComponent)
      },
      {
        path: 'empresas/:id/editar',
        loadComponent: () => import('./pages/empresas/empresa-form.component').then(m => m.EmpresaFormComponent)
      },
      {
        path: 'productos',
        loadComponent: () => import('./pages/productos/productos-list.component').then(m => m.ProductosListComponent)
      },
      {
        path: 'productos/nuevo',
        loadComponent: () => import('./pages/productos/producto-form.component').then(m => m.ProductoFormComponent)
      },
      {
        path: 'productos/:id/editar',
        loadComponent: () => import('./pages/productos/producto-form.component').then(m => m.ProductoFormComponent)
      },
      {
        path: 'facturas',
        loadComponent: () => import('./pages/facturas/facturas-list.component').then(m => m.FacturasListComponent)
      },
      {
        path: 'facturas/nueva',
        loadComponent: () => import('./pages/facturas/factura-form.component').then(m => m.FacturaFormComponent)
      },
      {
        path: 'facturas/:id/editar',
        loadComponent: () => import('./pages/facturas/factura-form.component').then(m => m.FacturaFormComponent)
      }
    ]
  },
  {
    path: '**',
    redirectTo: '/login'
  }
];