import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-privacidad',
  standalone: true,
  template: `
<div style="min-height: 100vh; background: #f8f9ff; padding: 4rem 2rem;">
  <div style="max-width: 800px; margin: 0 auto; background: white; border-radius: 1rem; padding: 3rem; box-shadow: 0 2px 12px rgba(0,0,0,0.08);">

    <button (click)="volver()" style="background: none; border: none; color: #6b7280; font-size: 14px; cursor: pointer; margin-bottom: 2rem; display: flex; align-items: center; gap: 0.5rem; padding: 0;">
      &larr; Volver
    </button>

    <h1 style="font-size: 32px; font-weight: 800; color: #1e3a8a; margin-bottom: 0.5rem;">Politica de Privacidad</h1>
    <p style="font-size: 13px; color: #9ca3af; margin-bottom: 2.5rem;">Ultima actualizacion: Julio 2026</p>

    <section style="margin-bottom: 2rem;">
      <h2 style="font-size: 18px; font-weight: 700; color: #1f2937; margin-bottom: 0.75rem;">1. Informacion que recopilamos</h2>
      <p style="color: #4b5563; line-height: 1.8; font-size: 15px;">
        Recopilamos informacion que nos proporcionas directamente al crear tu cuenta: nombre, correo electronico y datos de tu empresa. Tambien recopilamos datos de uso del servicio para mejorar la experiencia de la plataforma.
      </p>
    </section>

    <section style="margin-bottom: 2rem;">
      <h2 style="font-size: 18px; font-weight: 700; color: #1f2937; margin-bottom: 0.75rem;">2. Como usamos tu informacion</h2>
      <p style="color: #4b5563; line-height: 1.8; font-size: 15px;">
        Utilizamos tu informacion para: proveer y mantener el servicio, procesar pagos, enviarte notificaciones del sistema, y cumplir con obligaciones legales. No vendemos ni compartimos tu informacion personal con terceros salvo que sea estrictamente necesario para operar el servicio.
      </p>
    </section>

    <section style="margin-bottom: 2rem;">
      <h2 style="font-size: 18px; font-weight: 700; color: #1f2937; margin-bottom: 0.75rem;">3. Datos de facturacion</h2>
      <p style="color: #4b5563; line-height: 1.8; font-size: 15px;">
        Los datos de tus comprobantes electronicos (facturas, boletas) son transmitidos a SUNAT a traves de los servicios oficiales de la Administracion Tributaria peruana. Estos datos son tratados con la maxima confidencialidad.
      </p>
    </section>

    <section style="margin-bottom: 2rem;">
      <h2 style="font-size: 18px; font-weight: 700; color: #1f2937; margin-bottom: 0.75rem;">4. Seguridad</h2>
      <p style="color: #4b5563; line-height: 1.8; font-size: 15px;">
        Implementamos medidas de seguridad tecnicas y organizativas para proteger tu informacion: cifrado de datos en transito (HTTPS), autenticacion mediante tokens JWT, y acceso restringido a los datos por usuario.
      </p>
    </section>

    <section style="margin-bottom: 2rem;">
      <h2 style="font-size: 18px; font-weight: 700; color: #1f2937; margin-bottom: 0.75rem;">5. Cookies</h2>
      <p style="color: #4b5563; line-height: 1.8; font-size: 15px;">
        Utilizamos almacenamiento local (localStorage) para mantener tu sesion activa. No utilizamos cookies de rastreo publicitario ni compartimos datos de navegacion con redes publicitarias.
      </p>
    </section>

    <section style="margin-bottom: 2rem;">
      <h2 style="font-size: 18px; font-weight: 700; color: #1f2937; margin-bottom: 0.75rem;">6. Tus derechos</h2>
      <p style="color: #4b5563; line-height: 1.8; font-size: 15px;">
        Tienes derecho a acceder, corregir o eliminar tu informacion personal. Para ejercer estos derechos contactanos en <strong>soporte&#64;factullama.site</strong>. Atenderemos tu solicitud en un plazo maximo de 30 dias habiles.
      </p>
    </section>

    <section>
      <h2 style="font-size: 18px; font-weight: 700; color: #1f2937; margin-bottom: 0.75rem;">7. Cambios a esta politica</h2>
      <p style="color: #4b5563; line-height: 1.8; font-size: 15px;">
        Podemos actualizar esta politica periodicamente. Te notificaremos de cambios significativos por correo electronico. El uso continuo de Factullama tras los cambios implica tu aceptacion de la nueva politica.
      </p>
    </section>

  </div>
</div>
  `
})
export class PrivacidadComponent {
  constructor(private router: Router) {}
  volver() { this.router.navigate(['/']); }
}
