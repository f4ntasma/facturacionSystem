import { ChangeDetectionStrategy, Component, ElementRef, effect, input, output, viewChild } from '@angular/core';
import { IconoComponent } from './icono.component';

/** Diálogo nativo (<dialog>): foco atrapado y Esc para cerrar sin librerías. */
@Component({
  selector: 'at-modal',
  imports: [IconoComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <dialog #dlg (close)="cerrar.emit()" (click)="fuera($event)" [attr.aria-labelledby]="'t-' + id">
      <div class="caja" [style.max-width.px]="ancho()">
        <header class="entre">
          <h2 class="subtitulo" [id]="'t-' + id">{{ titulo() }}</h2>
          <button type="button" class="btn btn-fantasma btn-icono" aria-label="Cerrar" (click)="dlg.close()">
            <at-icono nombre="cerrar" />
          </button>
        </header>
        <div class="cuerpo"><ng-content /></div>
      </div>
    </dialog>
  `,
  styles: `
    dialog { border: 0; padding: 0; background: transparent; max-width: 100vw; max-height: 100dvh; width: 100%; height: 100%; margin: 0; }
    dialog[open] { display: flex; align-items: center; justify-content: center; padding: 16px; }
    dialog::backdrop { background: rgba(20, 20, 31, .55); }
    .caja { background: var(--papel); border-radius: 20px; width: 100%; max-height: calc(100dvh - 32px); overflow: auto;
      box-shadow: var(--sombra-alta); display: flex; flex-direction: column; }
    header { padding: 14px 14px 0 20px; position: sticky; top: 0; background: var(--papel); z-index: 1; }
    .cuerpo { padding: 12px 20px 20px; }
    @media (max-width: 600px) {
      dialog[open] { align-items: flex-end; padding: 0; }
      .caja { border-radius: 20px 20px 0 0; max-height: 92dvh; }
    }
  `,
})
export class ModalComponent {
  readonly abierto = input(false);
  readonly titulo = input('');
  readonly ancho = input(520);
  readonly cerrar = output<void>();
  protected readonly id = Math.random().toString(36).slice(2, 8);
  private dlg = viewChild.required<ElementRef<HTMLDialogElement>>('dlg');

  constructor() {
    effect(() => {
      const d = this.dlg().nativeElement;
      if (this.abierto() && !d.open) d.showModal();
      if (!this.abierto() && d.open) d.close();
    });
  }

  protected fuera(ev: MouseEvent) {
    if (ev.target === this.dlg().nativeElement) this.dlg().nativeElement.close();
  }
}
