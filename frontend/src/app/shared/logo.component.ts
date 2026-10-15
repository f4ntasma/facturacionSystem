import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

type Variante = 'claro' | 'amarillo' | 'tinta' | 'magenta' | 'unatinta';

/** Boleta con check, con las «líneas de velocidad» a la izquierda. Variantes según el manual de marca. */
@Component({
  selector: 'at-logo',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <span class="logo" [style.gap.px]="tam() * 0.22">
      <svg viewBox="0 0 132 124" [attr.width]="tam()" [attr.height]="tam() * 0.94" aria-hidden="true" style="transform: skewX(-8deg)">
        @if (lineas()) {
          <g fill="none" [attr.stroke]="c().velocidad" stroke-width="8" stroke-linecap="round">
            <path d="M8 40 H26"></path><path d="M0 58 H22"></path><path d="M8 76 H26"></path>
          </g>
        }
        <path
          d="M44 4 H112 Q122 4 122 14 V108 L116.5 117 L111 108 L105.5 117 L100 108 L94.5 117 L89 108 L83.5 117 L78 108 L72.5 117 L67 108 L61.5 117 L56 108 L50.5 117 L45 108 L39.5 117 L34 108 V14 Q34 4 44 4 Z"
          [attr.fill]="c().papel" [attr.stroke]="c().borde" stroke-width="7" stroke-linejoin="round"></path>
        <path d="M50 24 H84" [attr.stroke]="c().raya" stroke-width="6" stroke-linecap="round"></path>
        <path d="M52 66 L68 82 L104 42" fill="none" [attr.stroke]="c().check" stroke-width="13" stroke-linecap="round" stroke-linejoin="round"></path>
      </svg>
      @if (texto()) {
        <span class="palabra display" [style.font-size.px]="tam() * 0.86" [style.color]="c().texto">al toque</span>
      }
    </span>
  `,
  styles: `
    :host { display: inline-flex; }
    .logo { display: inline-flex; align-items: center; }
    .palabra { white-space: nowrap; font-family: var(--f-texto); }
  `,
})
export class LogoComponent {
  readonly variante = input<Variante>('claro');
  readonly tam = input(36);
  readonly texto = input(true);
  readonly lineas = input(true);

  protected readonly c = computed(() => {
    switch (this.variante()) {
      case 'amarillo':
        return { velocidad: '#14141F', papel: '#FFC81E', borde: '#14141F', raya: '#14141F', check: '#D1126B', texto: '#14141F' };
      case 'tinta':
        return { velocidad: '#FFC81E', papel: '#FFC81E', borde: '#FFC81E', raya: '#14141F', check: '#14141F', texto: '#FFFFFF' };
      case 'magenta':
        return { velocidad: '#FFFFFF', papel: '#FFFFFF', borde: '#FFFFFF', raya: '#D1126B', check: '#D1126B', texto: '#FFFFFF' };
      case 'unatinta':
        return { velocidad: '#14141F', papel: 'none', borde: '#14141F', raya: '#14141F', check: '#14141F', texto: '#14141F' };
      default:
        return { velocidad: '#14141F', papel: '#FFFFFF', borde: '#14141F', raya: '#14141F', check: '#D1126B', texto: '#14141F' };
    }
  });
}
