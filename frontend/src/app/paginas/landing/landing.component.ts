import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Api } from '../../core/api.service';
import { Auth } from '../../core/auth.service';
import { PlanPublico } from '../../core/modelos';
import { LogoComponent } from '../../shared/logo.component';

const PLANES_BASE: PlanPublico[] = [
  { codigo: 'CASERITO', nombre: 'Caserito', descripcion: "Pa' la bodega y el emprendedor que recién arranca.", precioMensual: 29, limiteComprobantes: 300, maxUsuarios: 1, maxEstablecimientos: 1, notasCredito: true, reportesAvanzados: false, soportePrioritario: false, destacado: false },
  { codigo: 'NEGOCIANTE', nombre: 'Negociante', descripcion: "Pa' el negocio que ya se mueve todos los días.", precioMensual: 59, limiteComprobantes: 1500, maxUsuarios: 3, maxEstablecimientos: 1, notasCredito: true, reportesAvanzados: true, soportePrioritario: false, destacado: true },
  { codigo: 'EMPRESA', nombre: 'Empresa', descripcion: "Pa' varias sedes, usuarios y mucho movimiento.", precioMensual: 149, limiteComprobantes: null, maxUsuarios: 20, maxEstablecimientos: 10, notasCredito: true, reportesAvanzados: true, soportePrioritario: true, destacado: false },
];

@Component({
  selector: 'at-landing',
  imports: [LogoComponent, RouterLink, FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.css',
})
export class LandingComponent {
  private api = inject(Api);
  private router = inject(Router);
  protected auth = inject(Auth);
  protected planes = signal<PlanPublico[]>(PLANES_BASE);
  protected celular = '';
  protected readonly anio = new Date().getFullYear();

  constructor() {
    this.api.get<PlanPublico[]>('/planes').then((p) => p.length && this.planes.set(p)).catch(() => {});
  }

  protected beneficios(p: PlanPublico): string[] {
    const l: string[] = [p.limiteComprobantes ? `${p.limiteComprobantes.toLocaleString('es-PE')} comprobantes al mes` : 'Comprobantes ilimitados'];
    if (p.codigo === 'CASERITO') l.push('Boletas y facturas', 'Envío por WhatsApp');
    else if (p.codigo === 'NEGOCIANTE') l.push('Notas de crédito', 'Ticketera y reportes', `Hasta ${p.maxUsuarios} usuarios`);
    else l.push('Varios usuarios y locales', p.soportePrioritario ? 'Soporte prioritario' : 'Reportes completos');
    return l;
  }

  protected empezar(plan?: string) {
    const queryParams: Record<string, string> = {};
    if (plan) queryParams['plan'] = plan;
    const cel = this.celular.replace(/\D/g, '');
    if (cel) queryParams['cel'] = cel;
    this.router.navigate([this.auth.logueado() ? '/app' : '/registro'], { queryParams });
  }
}
