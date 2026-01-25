import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { MenubarModule } from 'primeng/menubar';
import { ButtonModule } from 'primeng/button';
import { MenuItem } from 'primeng/api';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MenubarModule,
    ButtonModule
  ],
  template: `
    <div class="layout-wrapper">
      <!-- Top Navigation -->
      <p-menubar [model]="menuItems">
        <ng-template pTemplate="start">
          <div class="flex align-items-center gap-2">
            <span class="font-bold text-xl">FacturaApp</span>
          </div>
        </ng-template>
        <ng-template pTemplate="end">
          <div class="flex align-items-center gap-2">
            <span class="text-sm">Bienvenido, Admin</span>
            <button pButton type="button" icon="pi pi-sign-out" 
                    class="p-button-text p-button-rounded" 
                    (click)="logout()" pTooltip="Cerrar Sesión"></button>
          </div>
        </ng-template>
      </p-menubar>

      <!-- Main Content -->
      <div class="layout-main">
        <div class="layout-content">
          <router-outlet></router-outlet>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .layout-wrapper {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
    }

    .layout-main {
      flex: 1;
      display: flex;
    }

    .layout-content {
      flex: 1;
      padding: 1rem;
      background-color: #f8f9fa;
    }
  `]
})
export class MainLayoutComponent implements OnInit {
  menuItems: MenuItem[] = [];

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.menuItems = [
      {
        label: 'Gestión',
        icon: 'pi pi-cog',
        items: [
          {
            label: 'Empresas',
            icon: 'pi pi-building',
            routerLink: '/empresas'
          },
          {
            label: 'Productos',
            icon: 'pi pi-box',
            routerLink: '/productos'
          }
        ]
      },
      {
        label: 'Facturación',
        icon: 'pi pi-file-o',
        items: [
          {
            label: 'Facturas',
            icon: 'pi pi-file-o',
            routerLink: '/facturas'
          },
          {
            label: 'Nueva Factura',
            icon: 'pi pi-plus',
            routerLink: '/facturas/nueva'
          }
        ]
      }
    ];
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}