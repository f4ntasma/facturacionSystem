import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-factura-wizard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './factura-wizard.component.html'
})
export class FacturaWizardComponent {
  constructor(private router: Router) {}

  sections = [
    {
      id: 'documento',
      title: 'Información del Documento',
      description: 'Tipo de documento, serie, correlativo, fechas y moneda',
      icon: '',
      route: '/app/facturas/nuevo/documento',
      completed: false
    },
    {
      id: 'cliente',
      title: 'Información del Cliente',
      description: 'Datos del cliente, tipo de documento y dirección fiscal',
      icon: '',
      route: '/app/facturas/nuevo/cliente',
      completed: false
    },
    {
      id: 'items',
      title: 'Items de la Factura',
      description: 'Productos, cantidades, precios e impuestos',
      icon: '',
      route: '/app/facturas/nuevo/items',
      completed: false
    },
    {
      id: 'totales',
      title: 'Totales e Impuestos',
      description: 'Resumen de montos, IGV y valor total',
      icon: '',
      route: '/app/facturas/nuevo/totales',
      completed: false
    },
    {
      id: 'adicional',
      title: 'Información Adicional',
      description: 'Leyendas, guías de remisión y documentos relacionados',
      icon: '',
      route: '/app/facturas/nuevo/adicional',
      completed: false
    }
  ];

  navigateToSection(section: any) {
    this.router.navigate([section.route]);
  }

  allSectionsCompleted(): boolean {
    return this.sections.every(s => s.completed);
  }

  volver() {
    this.router.navigate(['/app/facturas']);
  }
}
