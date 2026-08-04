import 'bootstrap/dist/js/bootstrap.bundle.min.js';

import { bootstrapApplication } from '@angular/platform-browser';
import { App } from './app/app';
import { appConfig } from './app/app.config';

// FIX: antes se hacia bootstrap con provideHttpClient() sin interceptores,
// ignorando app.config.ts. Por eso AuthInterceptor nunca se ejecutaba y
// ninguna peticion llevaba el token (403 en todo al quitar headers manuales).
bootstrapApplication(App, appConfig);
