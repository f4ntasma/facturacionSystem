import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-soporte',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
<div style="min-height: 100vh; background: #f8f9ff; padding: 4rem 2rem;">
  <div style="max-width: 800px; margin: 0 auto;">

    <button (click)="volver()" style="background: none; border: none; color: #6b7280; font-size: 14px; cursor: pointer; margin-bottom: 2rem; display: flex; align-items: center; gap: 0.5rem; padding: 0;">
      &larr; Volver
    </button>

    <div style="text-align: center; margin-bottom: 3rem;">
      <h1 style="font-size: 36px; font-weight: 800; color: #1e3a8a; margin-bottom: 0.75rem;">Centro de Soporte</h1>
      <p style="color: #6b7280; font-size: 16px;">Estamos aqui para ayudarte. Encuentra respuestas rapidas o contactanos directamente.</p>
    </div>

    <!-- Canales de contacto -->
    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1.25rem; margin-bottom: 3rem;">

      <div style="background: white; border-radius: 0.75rem; padding: 1.5rem; box-shadow: 0 2px 8px rgba(0,0,0,0.07); text-align: center;">
        <div style="font-size: 28px; margin-bottom: 0.75rem;">&#9993;</div>
        <div style="font-weight: 700; color: #1f2937; margin-bottom: 0.25rem;">Correo electronico</div>
        <div style="font-size: 13px; color: #6b7280; margin-bottom: 1rem;">Respuesta en 24h habiles</div>
        <a href="mailto:soporte@factullama.site" style="color: #1e3a8a; font-weight: 600; font-size: 14px; text-decoration: none;">soporte&#64;factullama.site</a>
      </div>

      <div style="background: white; border-radius: 0.75rem; padding: 1.5rem; box-shadow: 0 2px 8px rgba(0,0,0,0.07); text-align: center;">
        <div style="font-size: 28px; margin-bottom: 0.75rem;">&#128172;</div>
        <div style="font-weight: 700; color: #1f2937; margin-bottom: 0.25rem;">WhatsApp</div>
        <div style="font-size: 13px; color: #6b7280; margin-bottom: 1rem;">Solo plan Enterprise</div>
        <span style="color: #9ca3af; font-size: 14px;">Disponible con plan Enterprise</span>
      </div>

    </div>

    <!-- Preguntas frecuentes -->
    <div style="background: white; border-radius: 1rem; padding: 2.5rem; box-shadow: 0 2px 12px rgba(0,0,0,0.08);">
      <h2 style="font-size: 22px; font-weight: 700; color: #1e3a8a; margin-bottom: 2rem;">Preguntas frecuentes</h2>

      <div *ngFor="let faq of faqs; let i = index" style="border-bottom: 1px solid #f3f4f6; padding: 1.25rem 0;">
        <button (click)="toggleFaq(i)"
          style="width: 100%; background: none; border: none; text-align: left; cursor: pointer; display: flex; justify-content: space-between; align-items: center; padding: 0;">
          <span style="font-weight: 600; color: #1f2937; font-size: 15px;">{{ faq.pregunta }}</span>
          <span style="font-size: 20px; color: #6b7280; transition: transform 0.2s;"
                [style.transform]="faq.abierto ? 'rotate(45deg)' : 'rotate(0)'">+</span>
        </button>
        <p *ngIf="faq.abierto" style="color: #4b5563; font-size: 14px; line-height: 1.7; margin: 0.75rem 0 0;">
          {{ faq.respuesta }}
        </p>
      </div>

    </div>

  </div>
</div>
  `
})
export class SoporteComponent {
  constructor(private router: Router) {}

  faqs = [
    {
      pregunta: '¿Como emito mi primera factura electronica?',
      respuesta: 'Primero configura tu empresa en la seccion Empresas, luego ve a Facturas y sigue el asistente paso a paso. Necesitaras el RUC y datos del cliente.',
      abierto: false
    },
    {
      pregunta: '¿Puedo cambiar de plan en cualquier momento?',
      respuesta: 'Si, puedes actualizar tu plan desde la seccion de Opciones. El cambio aplica al siguiente ciclo de facturacion.',
      abierto: false
    },
    {
      pregunta: '¿Que pasa si SUNAT rechaza un comprobante?',
      respuesta: 'El sistema te mostrara el codigo de error de SUNAT con una descripcion. Los errores mas comunes son datos del cliente incorrectos o RUC no activo. Puedes corregir y volver a enviar.',
      abierto: false
    },
    {
      pregunta: '¿Los datos de mis clientes estan seguros?',
      respuesta: 'Si. Toda la informacion se transmite cifrada por HTTPS y cada usuario solo puede ver sus propios datos. Revisa nuestra Politica de Privacidad para mas detalles.',
      abierto: false
    },
    {
      pregunta: '¿Como cancelo mi suscripcion?',
      respuesta: 'Puedes cancelar escribiendonos a soporte@factullama.site con el asunto "Cancelacion de cuenta". Procesaremos tu solicitud en un plazo de 2 dias habiles.',
      abierto: false
    },
    {
      pregunta: '¿El sistema funciona para boletas y facturas?',
      respuesta: 'Si, Factullama soporta Facturas, Boletas de Venta y Notas de Credito/Debito, todos los comprobantes validos ante SUNAT en ambiente beta y produccion.',
      abierto: false
    }
  ];

  toggleFaq(i: number) {
    this.faqs[i].abierto = !this.faqs[i].abierto;
  }

  volver() { this.router.navigate(['/']); }
}
