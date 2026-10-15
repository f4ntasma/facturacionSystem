import { ApplicationConfig, LOCALE_ID, provideBrowserGlobalErrorListeners, provideZonelessChangeDetection } from '@angular/core';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { provideRouter, withComponentInputBinding, withInMemoryScrolling } from '@angular/router';
import { registerLocaleData } from '@angular/common';
import localeEsPe from '@angular/common/locales/es-PE';
import { routes } from './app.routes';
import { authInterceptor } from './core/auth.service';

registerLocaleData(localeEsPe);

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZonelessChangeDetection(),
    provideRouter(routes, withComponentInputBinding(), withInMemoryScrolling({ scrollPositionRestoration: 'top', anchorScrolling: 'enabled' })),
    provideHttpClient(withFetch(), withInterceptors([authInterceptor])),
    { provide: LOCALE_ID, useValue: 'es-PE' },
  ],
};
