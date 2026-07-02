import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-terminos',
  standalone: true,
  imports: [CommonModule],
  template: `
<div style="min-height: 100vh; background: #f8f9ff; padding: 4rem 2rem;">
  <div style="max-width: 860px; margin: 0 auto; background: white; border-radius: 1rem; padding: 3rem 3.5rem; box-shadow: 0 2px 12px rgba(0,0,0,0.08);">

    <button (click)="volver()" style="background: none; border: none; color: #6b7280; font-size: 14px; cursor: pointer; margin-bottom: 2rem; display: flex; align-items: center; gap: 0.5rem; padding: 0;">
      &larr; Volver al inicio
    </button>

    <div style="border-left: 4px solid #1e3a8a; padding-left: 1.5rem; margin-bottom: 2.5rem;">
      <h1 style="font-size: 30px; font-weight: 900; color: #1e3a8a; margin-bottom: 0.4rem;">Terminos y Condiciones de Uso</h1>
      <p style="font-size: 13px; color: #9ca3af; margin: 0;">Factullama — Plataforma de Facturacion Electronica</p>
      <p style="font-size: 13px; color: #9ca3af; margin: 0.25rem 0 0;">Ultima actualizacion: Julio 2026 &nbsp;|&nbsp; Vigente desde: Julio 2026</p>
    </div>

    <div style="background: #fef9c3; border: 1px solid #fde047; border-radius: 0.5rem; padding: 1rem 1.25rem; margin-bottom: 2.5rem; font-size: 14px; color: #713f12; line-height: 1.6;">
      <strong>AVISO LEGAL IMPORTANTE:</strong> Al acceder, registrarte o utilizar cualquier funcion de la plataforma Factullama, manifiestas tu conformidad plena, expresa e irrevocable con la totalidad de los presentes Terminos y Condiciones. Si no aceptas alguna de estas disposiciones, la solucion es simple: no uses el servicio.
    </div>

    <!-- Indice -->
    <div style="background: #f8f9ff; border-radius: 0.75rem; padding: 1.5rem 2rem; margin-bottom: 2.5rem;">
      <p style="font-weight: 700; color: #1e3a8a; margin-bottom: 0.75rem; font-size: 15px;">Contenido</p>
      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.3rem 2rem; font-size: 13px; color: #4b5563;">
        <span>1. Partes del contrato</span>
        <span>10. Propiedad intelectual</span>
        <span>2. Objeto del contrato</span>
        <span>11. Proteccion de datos personales</span>
        <span>3. Capacidad legal</span>
        <span>12. Indemnizacion</span>
        <span>4. Registro y cuenta de usuario</span>
        <span>13. Fuerza mayor</span>
        <span>5. Planes y condiciones de pago</span>
        <span>14. Modificacion del servicio</span>
        <span>6. Obligaciones del usuario</span>
        <span>15. Suspension y terminacion</span>
        <span>7. Usos prohibidos</span>
        <span>16. Ley aplicable y jurisdiccion</span>
        <span>8. Responsabilidad tributaria (SUNAT)</span>
        <span>17. Divisibilidad</span>
        <span>9. Limitacion de responsabilidad</span>
        <span>18. Acuerdo integro</span>
      </div>
    </div>

    <!-- Secciones -->
    <div *ngFor="let s of secciones" style="margin-bottom: 2.25rem;">
      <h2 style="font-size: 17px; font-weight: 800; color: #1e3a8a; margin-bottom: 0.6rem; padding-bottom: 0.4rem; border-bottom: 2px solid #e5e7eb;">
        {{ s.titulo }}
      </h2>
      <div style="color: #374151; font-size: 14.5px; line-height: 1.85;" [innerHTML]="s.contenido"></div>
    </div>

    <div style="margin-top: 3rem; padding-top: 1.5rem; border-top: 1px solid #e5e7eb; font-size: 13px; color: #9ca3af; text-align: center;">
      &copy; 2026 Factullama. Todos los derechos reservados. &nbsp;|&nbsp; soporte&#64;factullama.site
    </div>

  </div>
</div>
  `
})
export class TerminosComponent {
  constructor(private router: Router) {}

  secciones = [
    {
      titulo: '1. Partes del contrato',
      contenido: `
        <p>El presente documento constituye un contrato vinculante celebrado entre:</p>
        <ul style="margin: 0.75rem 0 0.75rem 1.5rem; line-height: 2;">
          <li><strong>Factullama</strong> (en adelante "la Empresa", "nosotros", "el Proveedor"), plataforma de software como servicio (SaaS) de facturacion electronica.</li>
          <li><strong>El Usuario</strong>: toda persona natural o juridica que acceda, se registre o utilice los servicios de Factullama (en adelante "el Usuario", "tu", "el Cliente").</li>
        </ul>
        <p>Al aceptar estos terminos, el Usuario y la Empresa quedan sujetos a las obligaciones establecidas en el <strong>Decreto Legislativo N° 295 - Codigo Civil Peruano</strong> (articulos 1351° y ss. sobre contratos), la <strong>Ley N° 27291</strong> que reconoce la validez juridica de los contratos electronicos, y la <strong>Constitucion Politica del Peru de 1993</strong> (Art. 62°) que garantiza la libertad de contratar.</p>
      `
    },
    {
      titulo: '2. Objeto del contrato',
      contenido: `
        <p>Factullama provee al Usuario acceso a una plataforma digital de gestion empresarial que incluye, segun el plan contratado: emision de comprobantes electronicos (facturas, boletas, notas de credito/debito) ante SUNAT, gestion de ventas, clientes, productos, cotizaciones y caja diaria.</p>
        <p style="margin-top: 0.75rem;">El servicio se presta en modalidad SaaS a traves de Internet. <strong>Factullama no es un estudio contable, no presta servicios de asesoria tributaria ni representa al Usuario ante la SUNAT.</strong> La responsabilidad de la veracidad y legalidad de los comprobantes emitidos recae exclusivamente en el Usuario.</p>
      `
    },
    {
      titulo: '3. Capacidad legal',
      contenido: `
        <p>Para utilizar Factullama debes:</p>
        <ul style="margin: 0.75rem 0 0.75rem 1.5rem; line-height: 2;">
          <li>Ser mayor de 18 anos o contar con la representacion legal de un tutor o representante autorizado.</li>
          <li>Tener capacidad legal plena para celebrar contratos bajo la legislacion peruana (Art. 42° del Codigo Civil).</li>
          <li>Si actuas en nombre de una persona juridica, garantizas que tienes facultades suficientes para vincularla contractualmente.</li>
        </ul>
        <p>Factullama se reserva el derecho de solicitar documentacion que acredite la identidad o representacion del Usuario en cualquier momento.</p>
      `
    },
    {
      titulo: '4. Registro y cuenta de usuario',
      contenido: `
        <p><strong>4.1 Veracidad de datos:</strong> El Usuario se obliga a proporcionar informacion veraz, completa y actualizada durante el registro. Proporcionar datos falsos constituye incumplimiento contractual y puede dar lugar a la suspension inmediata de la cuenta sin reembolso.</p>
        <p style="margin-top: 0.75rem;"><strong>4.2 Seguridad de la cuenta:</strong> El Usuario es el unico responsable de mantener la confidencialidad de sus credenciales de acceso. Cualquier accion realizada desde su cuenta se presume realizada por el Usuario. Factullama no sera responsable por danos derivados del acceso no autorizado causado por negligencia del Usuario en la custodia de sus credenciales.</p>
        <p style="margin-top: 0.75rem;"><strong>4.3 Notificacion de brechas:</strong> El Usuario debe notificar a Factullama de inmediato ante cualquier uso no autorizado de su cuenta. El silencio implica confirmacion del uso legitimo.</p>
        <p style="margin-top: 0.75rem;"><strong>4.4 Cuenta unica:</strong> Cada cuenta es personal e intransferible. La cesion, venta o comparticion de credenciales a terceros esta expresamente prohibida y faculta a Factullama a cancelar la cuenta sin previo aviso ni reembolso.</p>
      `
    },
    {
      titulo: '5. Planes y condiciones de pago',
      contenido: `
        <p><strong>5.1 Precios:</strong> Los precios de los planes se expresan en Soles peruanos (S/) e incluyen el Impuesto General a las Ventas (IGV) vigente. Factullama se reserva el derecho de modificar precios con un preaviso minimo de 30 dias calendario notificado por correo electronico.</p>
        <p style="margin-top: 0.75rem;"><strong>5.2 Facturacion:</strong> La suscripcion se cobra mensualmente en la fecha de activacion. El pago se procesa a traves de MercadoPago. Al suscribirte, autorizas los cargos recurrentes segun el plan elegido.</p>
        <p style="margin-top: 0.75rem;"><strong>5.3 Politica de no reembolso:</strong> Los pagos realizados no son reembolsables, salvo que la Empresa haya incumplido el nivel de disponibilidad del servicio establecido en el apartado 9.3. No se realizan reembolsos proporcionales por cancelacion anticipada dentro del mes en curso.</p>
        <p style="margin-top: 0.75rem;"><strong>5.4 Impago:</strong> El incumplimiento en el pago en la fecha de vencimiento faculta a Factullama a suspender el acceso al servicio de forma inmediata. La reactivacion requerira el pago del monto adeudado mas los recargos por mora que apliquen conforme al Art. 1246° del Codigo Civil Peruano.</p>
        <p style="margin-top: 0.75rem;"><strong>5.5 Responsabilidad del procesador de pago:</strong> Factullama no almacena datos de tarjetas de credito ni debito. El procesamiento de pagos es gestionado exclusivamente por MercadoPago, sujeto a sus propios terminos. Factullama no se hace responsable por fallas, rechazos o demoras en el procesamiento imputables a MercadoPago o a las entidades financieras del Usuario.</p>
      `
    },
    {
      titulo: '6. Obligaciones del usuario',
      contenido: `
        <p>El Usuario se obliga irrevocablemente a:</p>
        <ul style="margin: 0.75rem 0 0.75rem 1.5rem; line-height: 2;">
          <li>Utilizar el servicio exclusivamente para fines legales y licitos.</li>
          <li>Emitir comprobantes electronicos unicamente por operaciones reales y con datos fidedignos, cumpliendo con el Reglamento de Comprobantes de Pago (R.S. N° 007-99/SUNAT) y sus modificatorias.</li>
          <li>Mantener actualizados sus datos de empresa, RUC, certificado digital y demas informacion requerida para la correcta operacion del sistema.</li>
          <li>Cumplir con todas las obligaciones tributarias que le correspondan como contribuyente ante la SUNAT, incluyendo la declaracion y pago de tributos derivados de las operaciones registradas en la plataforma.</li>
          <li>No interferir ni intentar interferir con el funcionamiento, seguridad o integridad de la plataforma.</li>
        </ul>
      `
    },
    {
      titulo: '7. Usos prohibidos',
      contenido: `
        <p>Queda expresamente prohibido, bajo pena de suspension inmediata y acciones legales:</p>
        <ul style="margin: 0.75rem 0 0.75rem 1.5rem; line-height: 2;">
          <li>Emitir comprobantes electronicos por operaciones inexistentes, falsas o simuladas.</li>
          <li>Usar la plataforma para actividades de blanqueo de activos, financiamiento del terrorismo o cualquier actividad tipificada como delito en el ordenamiento juridico peruano.</li>
          <li>Intentar acceder a cuentas, sistemas o datos de otros usuarios.</li>
          <li>Realizar ingenieria inversa, descompilar o desensamblar el software de Factullama.</li>
          <li>Introducir virus, codigo malicioso o cualquier elemento que perturbe el funcionamiento de la plataforma.</li>
          <li>Revender, sublicenciar o ceder el acceso a la plataforma a terceros sin autorizacion expresa y por escrito de Factullama.</li>
          <li>Usar bots, scrapers o cualquier medio automatizado para acceder a la plataforma sin autorizacion.</li>
        </ul>
        <p>El incumplimiento de cualquiera de estas prohibiciones habilita a Factullama a terminar el contrato de forma inmediata, retener los fondos si existiera sospecha de fraude, y ejercer todas las acciones civiles y penales que correspondan conforme al Codigo Penal Peruano (Ley N° 28251 y modificatorias).</p>
      `
    },
    {
      titulo: '8. Responsabilidad tributaria y cumplimiento SUNAT',
      contenido: `
        <p><strong>8.1 Responsabilidad exclusiva del Usuario:</strong> Factullama es un intermediario tecnologico. La veracidad, legalidad y validez tributaria de cada comprobante emitido es responsabilidad exclusiva e indelegable del Usuario emisor. Factullama actua como proveedor de servicios electronicos (PSE) y no como agente de retencion ni como responsable solidario de las obligaciones tributarias del Usuario.</p>
        <p style="margin-top: 0.75rem;"><strong>8.2 Disponibilidad de SUNAT:</strong> La transmision exitosa de comprobantes electronicos depende de la disponibilidad de los servicios web de SUNAT. Factullama no garantiza ni puede garantizar la disponibilidad continua de los servidores de SUNAT. Los rechazos, demoras o errores originados en los sistemas de la Administracion Tributaria no generan responsabilidad alguna para Factullama.</p>
        <p style="margin-top: 0.75rem;"><strong>8.3 Actualizaciones normativas:</strong> La normativa de facturacion electronica en Peru esta sujeta a constantes modificaciones por parte de SUNAT. Factullama hara sus mejores esfuerzos por mantener la plataforma actualizada, pero no garantiza la adaptacion inmediata ante cambios normativos. El Usuario es responsable de verificar la conformidad de sus comprobantes con la normativa vigente.</p>
        <p style="margin-top: 0.75rem;"><strong>8.4 Conservacion de documentos:</strong> De conformidad con el Art. 87° del Codigo Tributario (D.S. N° 133-2013-EF), el Usuario tiene la obligacion de conservar los comprobantes electronicos por el plazo que establezca la ley tributaria. Factullama ofrecera almacenamiento dentro del plazo de la suscripcion activa, sin asumir responsabilidad por la conservacion de registros una vez cancelada la cuenta.</p>
      `
    },
    {
      titulo: '9. Limitacion de responsabilidad',
      contenido: `
        <p><strong>9.1 Exclusion general:</strong> En la maxima medida permitida por la ley peruana, Factullama no sera responsable por danos indirectos, incidentales, especiales, punitivos o consecuentes, incluyendo pero no limitado a: perdida de beneficios, perdida de datos, dano reputacional, multas o sanciones de SUNAT, o interrupcion del negocio del Usuario.</p>
        <p style="margin-top: 0.75rem;"><strong>9.2 Tope de responsabilidad:</strong> En ningun caso la responsabilidad total acumulada de Factullama frente al Usuario superara el monto efectivamente pagado por el Usuario durante los tres (3) meses anteriores al evento que origino el dano. Esta limitacion aplica independientemente de la teoria legal invocada (contrato, extracontrato, garantia).</p>
        <p style="margin-top: 0.75rem;"><strong>9.3 Disponibilidad del servicio:</strong> Factullama se compromete a un nivel de disponibilidad (uptime) del 95% mensual. Por debajo de este umbral, el Usuario tendra derecho a un credito proporcional aplicable al siguiente ciclo de facturacion. Este credito constituye el unico y exclusivo remedio disponible por interrupciones del servicio.</p>
        <p style="margin-top: 0.75rem;"><strong>9.4 Servicios de terceros:</strong> Factullama integra servicios de terceros (SUNAT, MercadoPago, proveedores de infraestructura cloud). Factullama no asume responsabilidad por fallas, cambios o discontinuacion de estos servicios fuera de su control directo.</p>
      `
    },
    {
      titulo: '10. Propiedad intelectual',
      contenido: `
        <p><strong>10.1 Titularidad:</strong> Todo el software, diseno, codigo fuente, logotipos, marcas, nombres comerciales, interfaces, documentacion y contenido de Factullama son propiedad exclusiva de la Empresa y estan protegidos por la legislacion peruana de propiedad intelectual (Decreto Legislativo N° 822 - Ley sobre el Derecho de Autor, y Decreto Legislativo N° 1075 - Proteccion de Marcas).</p>
        <p style="margin-top: 0.75rem;"><strong>10.2 Licencia limitada:</strong> Factullama otorga al Usuario una licencia de uso no exclusiva, intransferible y revocable, limitada estrictamente al acceso y uso de la plataforma segun el plan contratado y estos Terminos. Nada en estos Terminos transfiere al Usuario derechos de propiedad sobre el software.</p>
        <p style="margin-top: 0.75rem;"><strong>10.3 Datos del Usuario:</strong> Los datos e informacion comercial que el Usuario ingresa en la plataforma son de su exclusiva propiedad. Factullama no reivindica derechos sobre dicho contenido, pero el Usuario otorga a Factullama una licencia para procesar, almacenar y transmitir dichos datos en la medida necesaria para prestar el servicio.</p>
      `
    },
    {
      titulo: '11. Proteccion de datos personales',
      contenido: `
        <p>El tratamiento de datos personales se rige por la <strong>Ley N° 29733 - Ley de Proteccion de Datos Personales</strong> y su Reglamento (D.S. N° 003-2013-JUS). Factullama actua como encargado del tratamiento de los datos que el Usuario, en calidad de titular o responsable, ingresa a la plataforma.</p>
        <p style="margin-top: 0.75rem;">Factullama implementa medidas de seguridad tecnicas, organizativas y juridicas para proteger los datos personales contra accesos no autorizados, perdida o destruccion. No se comercializan datos personales de usuarios ni de sus clientes a terceros.</p>
        <p style="margin-top: 0.75rem;">Para ejercer los derechos ARCO (Acceso, Rectificacion, Cancelacion, Oposicion) reconocidos por la Ley N° 29733, el Usuario debe dirigirse a <strong>soporte&#64;factullama.site</strong>. La respuesta se otorgara en el plazo legal de veinte (20) dias habiles.</p>
        <p style="margin-top: 0.75rem;">El detalle del tratamiento de datos se encuentra en nuestra <strong>Politica de Privacidad</strong>, cuyo contenido forma parte integrante de estos Terminos.</p>
      `
    },
    {
      titulo: '12. Indemnizacion',
      contenido: `
        <p>El Usuario se obliga a indemnizar, defender y mantener indemne a Factullama y a sus directivos, empleados, socios y representantes frente a cualquier reclamo, demanda, perdida, dano, multa, sancion, gasto o costo (incluyendo honorarios legales razonables) que surjan de o esten relacionados con:</p>
        <ul style="margin: 0.75rem 0 0.75rem 1.5rem; line-height: 2;">
          <li>El incumplimiento de cualquier disposicion de estos Terminos por parte del Usuario.</li>
          <li>El uso indebido o ilegal de la plataforma.</li>
          <li>La emision de comprobantes falsos, fraudulentos o que infrinjan la normativa tributaria.</li>
          <li>Cualquier reclamo de terceros (incluyendo SUNAT, clientes del Usuario, o autoridades competentes) derivado de las operaciones del Usuario registradas en la plataforma.</li>
          <li>La violacion de derechos de propiedad intelectual o datos personales de terceros.</li>
        </ul>
      `
    },
    {
      titulo: '13. Caso fortuito y fuerza mayor',
      contenido: `
        <p>De conformidad con el Art. 1315° del Codigo Civil Peruano, Factullama queda liberada de responsabilidad ante el incumplimiento o demora en la prestacion del servicio causados por eventos de caso fortuito o fuerza mayor, incluyendo sin limitacion: desastres naturales, pandemias, fallas masivas de infraestructura de Internet, ataques ciberneticos de gran escala, cambios normativos urgentes, interrupciones de servicios de terceros proveedores (AWS, Google Cloud, Azure u otros), o cualquier causa ajena al control razonable de Factullama.</p>
        <p style="margin-top: 0.75rem;">En tales casos, Factullama comunicara la situacion al Usuario en el menor tiempo posible y hara sus mejores esfuerzos por restablecer el servicio.</p>
      `
    },
    {
      titulo: '14. Modificacion del servicio y de los terminos',
      contenido: `
        <p><strong>14.1 Modificacion del servicio:</strong> Factullama se reserva el derecho de modificar, suspender temporalmente o discontinuar cualquier funcion del servicio en cualquier momento, con o sin previo aviso, sin que ello genere responsabilidad hacia el Usuario, salvo en los casos de discontinuacion total del servicio en los que se notificara con un minimo de 30 dias de anticipacion.</p>
        <p style="margin-top: 0.75rem;"><strong>14.2 Modificacion de los terminos:</strong> Factullama puede actualizar estos Terminos en cualquier momento. Los cambios materiales seran notificados por correo electronico y/o mediante aviso en la plataforma con al menos 15 dias de anticipacion. El uso continuado del servicio tras la fecha de vigencia de los nuevos terminos constituye la aceptacion expresa de los mismos. Si el Usuario no acepta los cambios, debe cancelar su suscripcion antes de la fecha de vigencia.</p>
      `
    },
    {
      titulo: '15. Suspension y terminacion del contrato',
      contenido: `
        <p><strong>15.1 Por el Usuario:</strong> El Usuario puede cancelar su suscripcion en cualquier momento enviando una solicitud a soporte&#64;factullama.site. La cancelacion surte efecto al termino del ciclo mensual en curso. No se realizan reembolsos proporcionales.</p>
        <p style="margin-top: 0.75rem;"><strong>15.2 Por Factullama — con previo aviso:</strong> Factullama puede terminar el contrato con 15 dias de anticipacion por razones de negocio, cambios normativos o discontinuacion del servicio.</p>
        <p style="margin-top: 0.75rem;"><strong>15.3 Por Factullama — de forma inmediata y sin reembolso:</strong> Factullama puede suspender o terminar la cuenta del Usuario de forma inmediata, sin previo aviso y sin reembolso, ante:</p>
        <ul style="margin: 0.75rem 0 0.75rem 1.5rem; line-height: 2;">
          <li>Violacion de cualquier uso prohibido del apartado 7.</li>
          <li>Impago por mas de 7 dias calendario.</li>
          <li>Provision de informacion falsa en el registro.</li>
          <li>Requerimiento de autoridad competente (judicial, tributaria o administrativa).</li>
          <li>Indicios razonables de fraude, suplantacion de identidad o actividad ilicita.</li>
        </ul>
        <p><strong>15.4 Efectos de la terminacion:</strong> Tras la cancelacion o terminacion, el Usuario tendra un periodo de 30 dias para exportar sus datos. Transcurrido dicho plazo, Factullama podra eliminar los datos sin responsabilidad alguna.</p>
      `
    },
    {
      titulo: '16. Ley aplicable y jurisdiccion',
      contenido: `
        <p>Estos Terminos se rigen exclusivamente por las leyes de la <strong>Republica del Peru</strong>. Para cualquier controversia derivada de o relacionada con estos Terminos o el uso de la plataforma, las partes se someten a la jurisdiccion de los <strong>Juzgados y Tribunales de la ciudad de Lima, Peru</strong>, renunciando expresamente a cualquier otro fuero que pudiera corresponderles.</p>
        <p style="margin-top: 0.75rem;">Sin perjuicio de lo anterior, Factullama podra solicitar medidas cautelares ante cualquier tribunal competente cuando sea necesario para proteger sus derechos de propiedad intelectual o prevenir un dano irreparable.</p>
        <p style="margin-top: 0.75rem;">Son de aplicacion supletoria: el Decreto Legislativo N° 295 (Codigo Civil), la Ley N° 29571 (Codigo de Proteccion y Defensa del Consumidor), la Ley N° 27291 (Contratos electronicos) y demas normas del ordenamiento juridico peruano que resulten aplicables.</p>
      `
    },
    {
      titulo: '17. Divisibilidad',
      contenido: `
        <p>Si alguna disposicion de estos Terminos fuera declarada invalida, ilegal o inaplicable por un tribunal competente, dicha declaracion no afectara la validez y plena vigencia del resto de las disposiciones, las cuales continuaran en pleno vigor. La clausula invalida sera sustituida por otra valida que se aproxime al maximo al proposito economico y juridico de la clausula original.</p>
      `
    },
    {
      titulo: '18. Acuerdo integro',
      contenido: `
        <p>Estos Terminos, junto con la Politica de Privacidad y cualquier acuerdo especifico vigente entre las partes, constituyen el acuerdo completo y exclusivo entre el Usuario y Factullama respecto al objeto aqui regulado, y reemplazan cualquier acuerdo, representacion o entendimiento anterior, oral o escrito, sobre dicho objeto.</p>
        <p style="margin-top: 0.75rem;">La renuncia por parte de Factullama al ejercicio de cualquier derecho contemplado en estos Terminos no implica renuncia definitiva ni puede interpretarse como la creacion de una practica vinculante. Cada derecho puede ser ejercido por Factullama en cualquier momento futuro.</p>
        <p style="margin-top: 0.75rem; font-style: italic; color: #6b7280;">Factullama — Porque en los negocios, los detalles no son un accesorio: son la diferencia entre ganar y perder.</p>
      `
    }
  ];

  volver() { this.router.navigate(['/']); }
}
