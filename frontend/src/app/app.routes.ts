import { Routes } from '@angular/router';

export const routes: Routes = [

  {
    path: 'login',
    loadComponent: () =>
      import('./pages/login/login').then(m => m.LoginComponent)
  },

  {
    path: '',
    loadComponent: () =>
      import('./layout/main-layout.component').then(m => m.MainLayoutComponent),
    children: [

      {
        path: 'pisos/:piso',
        loadComponent: () =>
          import('./pages/mesas/mesas.component').then(m => m.MesasComponent)
      },
      {
        path: 'mesa/:id',
        loadComponent: () =>
          import('./pages/mesa-detalle/mesa-detalle.component')
            .then(m => m.MesaDetalleComponent)
      },

      {
        path: 'empresas',
        loadComponent: () =>
          import('./pages/empresas/empresas-list.component')
            .then(m => m.EmpresasListComponent)
      },
      {
        path: 'empresas/nueva',
        loadComponent: () =>
          import('./pages/empresas/empresa-form.component')
            .then(m => m.EmpresaFormComponent)
      },

      {
        path: 'productos',
        loadComponent: () =>
          import('./pages/productos/productos-list.component')
            .then(m => m.ProductosListComponent)
      },
      {
        path: 'productos/nuevo',
        loadComponent: () =>
          import('./pages/productos/producto-form.component')
            .then(m => m.ProductoFormComponent)
      },

      {
        path: 'facturas',
        loadComponent: () =>
          import('./pages/facturas/facturas-list.component')
            .then(m => m.FacturasListComponent)
      },

      {
        path: 'ventas',
        loadComponent: () =>
          import('./pages/ventas/ventas.component')
            .then(m => m.VentasComponent)
      },

      {
        path: 'cotizaciones',
        loadComponent: () =>
          import('./pages/cotizaciones/cotizaciones.component')
            .then(m => m.CotizacionesComponent)
      }
    ]
  },

  {
    path: '**',
    redirectTo: '/pisos/1'
  }
];