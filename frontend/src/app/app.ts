import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ToastsComponent } from './shared/toasts.component';

@Component({
  selector: 'at-root',
  imports: [RouterOutlet, ToastsComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<router-outlet /><at-toasts />`,
})
export class App {}
