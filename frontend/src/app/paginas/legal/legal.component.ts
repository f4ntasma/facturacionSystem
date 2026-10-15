import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LogoComponent } from '../../shared/logo.component';

const ESTILOS = `
  .legal { max-width: 760px; margin: 0 auto; padding: 32px 20px 80px; display: flex; flex-direction: column; gap: 18px; font-size: 17px; line-height: 1.6; }
  .legal h1 { font-size: clamp(44px, 7vw, 72px); }
  .legal h2 { font-size: 22px; margin-top: 10px; }
  .legal ul { margin: 0; padding-left: 22px; }
  .cab { background: var(--amarillo); padding: 18px 20px; }
  .cab-in { max-width: 760px; margin: 0 auto; }
`;

@Component({
  selector: 'at-terminos',
  imports: [RouterLink, LogoComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: ESTILOS,
  template: `
    <header class="cab"><div class="cab-in"><a routerLink="/" aria-label="Al Toque, inicio"><at-logo [tam]="34" /></a></div></header>
    <main class="legal">
      <h1 class="display">Términos de uso</h1>
      <p>Al Toque es un servicio para emitir comprobantes de pago electrónicos (boletas, facturas y notas) y enviarlos a SUNAT a nombre de tu negocio.</p>
      <h2>Tu cuenta</h2>
      <ul>
        <li>Eres responsable de que los datos de tu negocio, tu RUC, tu clave SOL y tu certificado digital sean tuyos y estén vigentes.</li>
        <li>Cada comprobante que emites es tu declaración ante SUNAT; Al Toque lo genera, firma y envía tal como lo registras.</li>
        <li>Cuida tu contraseña y la de tu equipo. Puedes desactivar usuarios cuando quieras.</li>
      </ul>
      <h2>Planes y pagos</h2>
      <ul>
        <li>La prueba gratis dura 15 días. Luego eliges un plan mensual; puedes cambiarlo o cancelarlo cuando quieras.</li>
        <li>Si un pago no se completa, la emisión se pausa hasta regularizarlo; tus comprobantes emitidos siguen disponibles.</li>
      </ul>
      <h2>Disponibilidad</h2>
      <p>Si SUNAT no responde, tus comprobantes quedan firmados y se reenvían automáticamente. Guardamos el XML y la constancia (CDR) de cada uno.</p>
      <p class="suave chico">Última actualización: octubre de 2026.</p>
    </main>
  `,
})
export class TerminosComponent {}

@Component({
  selector: 'at-privacidad',
  imports: [RouterLink, LogoComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: ESTILOS,
  template: `
    <header class="cab"><div class="cab-in"><a routerLink="/" aria-label="Al Toque, inicio"><at-logo [tam]="34" /></a></div></header>
    <main class="legal">
      <h1 class="display">Privacidad</h1>
      <p>Tratamos tus datos conforme a la Ley N.º 29733 de Protección de Datos Personales.</p>
      <h2>Qué guardamos</h2>
      <ul>
        <li>Datos de tu negocio y de tu equipo (nombre, correo, rol).</li>
        <li>Los comprobantes que emites y los datos de tus clientes que registras en ellos.</li>
        <li>Tu clave SOL y la clave de tu certificado, siempre cifradas.</li>
      </ul>
      <h2>Para qué</h2>
      <p>Solo para emitir y enviar tus comprobantes a SUNAT, mostrártelos y mandarlos a tus clientes cuando tú lo pidas. No vendemos ni compartimos tus datos con terceros.</p>
      <h2>Tus derechos</h2>
      <p>Puedes pedir acceso, corrección o eliminación de tus datos escribiendo a soporte. Los comprobantes electrónicos se conservan el plazo que exige la normativa tributaria.</p>
    </main>
  `,
})
export class PrivacidadComponent {}

@Component({
  selector: 'at-no-encontrado',
  imports: [RouterLink, LogoComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <main class="nf">
      <at-logo [tam]="44" />
      <h1 class="display">Uy, esta página<br />no existe, causa.</h1>
      <a routerLink="/" class="btn btn-tinta btn-grande">Volver al inicio</a>
    </main>
  `,
  styles: `
    .nf { min-height: 100dvh; background: var(--amarillo); display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 28px; padding: 24px; text-align: center; }
    h1 { font-size: clamp(48px, 9vw, 96px); }
  `,
})
export class NoEncontradoComponent {}
