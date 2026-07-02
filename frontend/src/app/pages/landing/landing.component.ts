import { Component, ViewEncapsulation } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-landing',
  standalone: true,
  templateUrl: './landing.component.html',
  imports: [CommonModule],
  encapsulation: ViewEncapsulation.None
})
export class LandingComponent {
  constructor(private router: Router) {}

  irARegistro() {
    this.router.navigate(['/registro']);
  }

  irADemo() {
    this.router.navigate(['/demo']);
  }

  comprar(plan: string, precio: string) {
    this.router.navigate(['/registro'], { queryParams: { plan, precio } });
  }

  scrollToSection(sectionId: string) {
    const element = document.getElementById(sectionId);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  }

  faqs = [
    {
      pregunta: '¿Como emito mi primera factura electronica?',
      respuesta: 'Primero configura tu empresa en la seccion Empresas ingresando tu RUC y datos. Luego ve a Facturas y sigue el asistente paso a paso: elige el tipo de comprobante, busca el cliente por DNI o RUC, agrega los productos y confirma. El comprobante se envia automaticamente a SUNAT.',
      abierto: false
    },
    {
      pregunta: '¿Factullama esta homologado con SUNAT?',
      respuesta: 'Si. Factullama opera como Proveedor de Servicios Electronicos (PSE) y transmite los comprobantes al sistema de SUNAT en formato XML segun las especificaciones del Reglamento de Comprobantes de Pago vigente.',
      abierto: false
    },
    {
      pregunta: '¿Que pasa si SUNAT rechaza un comprobante?',
      respuesta: 'El sistema te muestra el codigo de error de SUNAT con una descripcion clara. Los errores mas comunes son datos del cliente incorrectos o RUC inactivo. Puedes corregir la informacion y volver a enviar sin costo adicional.',
      abierto: false
    },
    {
      pregunta: '¿Puedo cambiar de plan en cualquier momento?',
      respuesta: 'Si. Puedes actualizar tu plan desde la seccion Opciones dentro de la aplicacion. El cambio aplica al siguiente ciclo mensual de facturacion.',
      abierto: false
    },
    {
      pregunta: '¿Mis datos y los de mis clientes estan seguros?',
      respuesta: 'Absolutamente. Toda la comunicacion usa cifrado HTTPS. Cada usuario accede unicamente a sus propios datos mediante autenticacion JWT. No compartimos ni vendemos informacion a terceros. Cumplimos con la Ley N° 29733 de Proteccion de Datos Personales del Peru.',
      abierto: false
    },
    {
      pregunta: '¿Que medios de pago acepta Factullama?',
      respuesta: 'El pago de tu suscripcion se procesa a traves de MercadoPago, que acepta tarjetas de credito y debito Visa, Mastercard, American Express, asi como Yape y Plin segun disponibilidad.',
      abierto: false
    },
    {
      pregunta: '¿Puedo probar Factullama antes de pagar?',
      respuesta: 'El plan Basico incluye 7 dias gratis y el plan Pro incluye 15 dias gratis. Podras explorar todas las funciones sin ningun compromiso durante ese periodo.',
      abierto: false
    },
    {
      pregunta: '¿Como cancelo mi suscripcion?',
      respuesta: 'Puedes cancelar en cualquier momento escribiendonos a soporte@factullama.site con el asunto "Cancelacion de cuenta". La cancelacion aplica al termino del mes en curso y no genera reembolsos proporcionales.',
      abierto: false
    }
  ];

  toggleFaq(i: number) {
    this.faqs[i].abierto = !this.faqs[i].abierto;
  }
}