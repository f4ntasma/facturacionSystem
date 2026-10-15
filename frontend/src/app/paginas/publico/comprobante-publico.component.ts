import { ChangeDetectionStrategy, Component, inject, input, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Api } from '../../core/api.service';
import { Comprobante } from '../../core/modelos';
import { ComprobanteDocComponent, imprimir } from '../../shared/comprobante-doc.component';
import { IconoComponent } from '../../shared/icono.component';
import { LogoComponent } from '../../shared/logo.component';

/** Lo que ve el cliente al abrir el enlace de WhatsApp o escanear el QR. */
@Component({
  selector: 'at-comprobante-publico',
  imports: [ComprobanteDocComponent, LogoComponent, IconoComponent, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <main class="pub">
      <header class="no-imprimir cab">
        <a routerLink="/" aria-label="Al Toque"><at-logo [tam]="30" /></a>
        @if (comp()) {
          <div class="segmentado" role="group" aria-label="Formato">
            <button type="button" [attr.aria-pressed]="formato() === 'ticket'" (click)="formato.set('ticket')">Ticket</button>
            <button type="button" [attr.aria-pressed]="formato() === 'a4'" (click)="formato.set('a4')">A4</button>
          </div>
        }
      </header>
      @if (comp(); as c) {
        <div class="hoja" [class.ticket]="formato() === 'ticket'">
          <at-comprobante-doc [comp]="c" [formato]="formato()" />
        </div>
        <div class="acciones no-imprimir">
          <button class="btn btn-tinta" type="button" (click)="imprimir()"><at-icono nombre="imprimir" /> Imprimir o guardar PDF</button>
          <a class="btn btn-borde" [href]="'/api/public/c/' + token() + '/xml'"><at-icono nombre="descargar" /> Descargar XML</a>
        </div>
        <p class="no-imprimir centro suave chico">¿Tienes un negocio? <a routerLink="/">Factura al toque tú también</a>.</p>
      } @else if (error()) {
        <div class="vacio">
          <strong>No encontramos este comprobante</strong>
          <span>Revisa que el enlace esté completo.</span>
        </div>
      } @else {
        <div class="esqueleto" style="width: 300px; height: 520px; margin: 24px auto"></div>
      }
    </main>
  `,
  styles: `
    .pub { min-height: 100dvh; background: var(--amarillo); padding: 16px 16px 48px; display: flex; flex-direction: column; gap: 20px; }
    .cab { display: flex; justify-content: space-between; align-items: center; max-width: 820px; width: 100%; margin: 0 auto; }
    .hoja { width: 100%; max-width: 820px; margin: 0 auto; box-shadow: var(--sombra-alta); }
    .hoja.ticket { max-width: 300px; box-shadow: none; filter: drop-shadow(0 18px 30px rgba(20,20,31,.25)); }
    .acciones { display: flex; gap: 10px; justify-content: center; flex-wrap: wrap; }
  `,
})
export class ComprobantePublicoComponent implements OnInit {
  private api = inject(Api);
  readonly token = input.required<string>();
  protected comp = signal<Comprobante | null>(null);
  protected error = signal(false);
  protected formato = signal<'ticket' | 'a4'>(window.innerWidth >= 900 ? 'a4' : 'ticket');

  ngOnInit() {
    this.api.get<Comprobante>(`/public/c/${this.token()}`).then((c) => this.comp.set(c)).catch(() => this.error.set(true));
  }

  protected imprimir() {
    imprimir(this.formato());
  }
}
