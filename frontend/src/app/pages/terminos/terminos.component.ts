import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-terminos',
  standalone: true,
  template: `
<div style="min-height: 100vh; background: #f8f9ff; padding: 4rem 2rem;">
  <div style="max-width: 800px; margin: 0 auto; background: white; border-radius: 1rem; padding: 3rem; box-shadow: 0 2px 12px rgba(0,0,0,0.08);">

    <button (click)="volver()" style="background: none; border: none; color: #6b7280; font-size: 14px; cursor: pointer; margin-bottom: 2rem; display: flex; align-items: center; gap: 0.5rem; padding: 0;">
      &larr; Volver
    </button>

    <h1 style="font-size: 32px; font-weight: 800; color: #1e3a8a; margin-bottom: 0.5rem;">Terminos y Condiciones</h1>
    <p style="font-size: 13px; color: #9ca3af; margin-bottom: 2.5rem;">Ultima actualizacion: Julio 2026</p>

    <section style="margin-bottom: 2rem;">
      <h2 style="font-size: 18px; font-weight: 700; color: #1f2937; margin-bottom: 0.75rem;">1. Aceptacion de los terminos</h2>
      <p style="color: #4b5563; line-height: 1.8; font-size: 15px;">
        Al acceder y utilizar Factullama, aceptas quedar vinculado por estos Terminos y Condiciones de uso. Si no estas de acuerdo con alguna parte de estos terminos, no podras acceder al servicio.
      </p>
    </section>

    <section style="margin-bottom: 2rem;">
      <h2 style="font-size: 18px; font-weight: 700; color: #1f2937; margin-bottom: 0.75rem;">2. Descripcion del servicio</h2>
      <p style="color: #4b5563; line-height: 1.8; font-size: 15px;">
        Factullama es una plataforma de facturacion electronica que permite a empresas y negocios emitir comprobantes de pago electronicos (facturas, boletas y notas) de acuerdo con las normativas de SUNAT en Peru.
      </p>
    </section>

    <section style="margin-bottom: 2rem;">
      <h2 style="font-size: 18px; font-weight: 700; color: #1f2937; margin-bottom: 0.75rem;">3. Cuenta de usuario</h2>
      <p style="color: #4b5563; line-height: 1.8; font-size: 15px;">
        Eres responsable de mantener la confidencialidad de tu cuenta y contrasena. Aceptas notificarnos de inmediato ante cualquier uso no autorizado de tu cuenta. Factullama no se hace responsable por perdidas derivadas del incumplimiento de esta obligacion.
      </p>
    </section>

    <section style="margin-bottom: 2rem;">
      <h2 style="font-size: 18px; font-weight: 700; color: #1f2937; margin-bottom: 0.75rem;">4. Planes y pagos</h2>
      <p style="color: #4b5563; line-height: 1.8; font-size: 15px;">
        Los planes de suscripcion se cobran mensualmente. Los precios estan expresados en Soles peruanos (S/) e incluyen IGV. El pago se procesa a traves de MercadoPago. No se realizan reembolsos una vez procesado el pago del mes en curso.
      </p>
    </section>

    <section style="margin-bottom: 2rem;">
      <h2 style="font-size: 18px; font-weight: 700; color: #1f2937; margin-bottom: 0.75rem;">5. Uso aceptable</h2>
      <p style="color: #4b5563; line-height: 1.8; font-size: 15px;">
        El usuario se compromete a utilizar el servicio unicamente para fines legales y de acuerdo con la normativa tributaria peruana vigente. Queda prohibido el uso del sistema para emitir comprobantes falsos o fraudulentos.
      </p>
    </section>

    <section style="margin-bottom: 2rem;">
      <h2 style="font-size: 18px; font-weight: 700; color: #1f2937; margin-bottom: 0.75rem;">6. Limitacion de responsabilidad</h2>
      <p style="color: #4b5563; line-height: 1.8; font-size: 15px;">
        Factullama no sera responsable por danos indirectos, incidentales o consecuentes derivados del uso o la imposibilidad de uso del servicio. La responsabilidad maxima de Factullama se limita al monto pagado por el usuario en el mes en que ocurrio el incidente.
      </p>
    </section>

    <section style="margin-bottom: 2rem;">
      <h2 style="font-size: 18px; font-weight: 700; color: #1f2937; margin-bottom: 0.75rem;">7. Modificaciones</h2>
      <p style="color: #4b5563; line-height: 1.8; font-size: 15px;">
        Nos reservamos el derecho de modificar estos terminos en cualquier momento. Los cambios entran en vigor al publicarse en esta pagina. El uso continuo del servicio despues de los cambios constituye la aceptacion de los nuevos terminos.
      </p>
    </section>

    <section>
      <h2 style="font-size: 18px; font-weight: 700; color: #1f2937; margin-bottom: 0.75rem;">8. Contacto</h2>
      <p style="color: #4b5563; line-height: 1.8; font-size: 15px;">
        Para consultas sobre estos terminos puedes escribirnos a <strong>soporte&#64;factullama.site</strong>
      </p>
    </section>

  </div>
</div>
  `
})
export class TerminosComponent {
  constructor(private router: Router) {}
  volver() { this.router.navigate(['/']); }
}
