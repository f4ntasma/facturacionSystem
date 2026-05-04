import { Routes } from '@angular/router';

export const routes: Routes = [

  {
    path: '',
    loadComponent: () =>
      import('./pages/landing/landing.component').then(m => m.LandingComponent)
  },

  {
    path: 'login',
    loadComponent: () =>
      import('./pages/login/login').then(m => m.LoginComponent)
  },

  {
    path: 'app',
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
        path: 'facturas/nuevo',
        loadComponent: () =>
          import('./pages/facturas/factura-wizard.component')
            .then(m => m.FacturaWizardComponent)
      },
      {
        path: 'facturas/nuevo/documento',
        loadComponent: () =>
          import('./pages/facturas/sections/documento.component')
            .then(m => m.FacturaDocumentoComponent)
      },
      {
        path: 'facturas/nuevo/cliente',
        loadComponent: () =>
          import('./pages/facturas/sections/cliente.component')
            .then(m => m.FacturaClienteComponent)
      },
      {
        path: 'facturas/nuevo/items',
        loadComponent: () =>
          import('./pages/facturas/sections/items.component')
            .then(m => m.FacturaItemsComponent)
      },
      {
        path: 'facturas/nuevo/totales',
        loadComponent: () =>
          import('./pages/facturas/sections/totales.component')
            .then(m => m.FacturaTotalesComponent)
      },
      {
        path: 'facturas/nuevo/adicional',
        loadComponent: () =>
          import('./pages/facturas/sections/adicional.component')
            .then(m => m.FacturaAdicionalComponent)
      },
      {
        path: 'facturas/nueva',
        loadComponent: () =>
          import('./pages/facturas/factura-form.component')
            .then(m => m.FacturaFormComponent)
      },
      {
        path: 'facturas/:id',
        loadComponent: () =>
          import('./pages/facturas/factura-form.component')
            .then(m => m.FacturaFormComponent)
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
      },

      {
        path: 'clientes',
        loadComponent: () =>
          import('./pages/clientes/clientes-list.component')
            .then(m => m.ClientesListComponent)
      },
      {
        path: 'clientes/nuevo',
        loadComponent: () =>
          import('./pages/clientes/cliente-form.component')
            .then(m => m.ClienteFormComponent)
      },
      {
        path: 'clientes/:id',
        loadComponent: () =>
          import('./pages/clientes/cliente-form.component')
            .then(m => m.ClienteFormComponent)
      },

      {
        path: 'caja',
        loadComponent: () =>
          import('./pages/caja/caja.component')
            .then(m => m.CajaComponent)
      },

      {
        path: 'opciones',
        loadComponent: () =>
          import('./pages/opciones/opciones.component')
            .then(m => m.OpcionesComponent)
      }
    ]
  },

  {
    path: '**',
    redirectTo: ''
  }
];