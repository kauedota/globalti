import { registerLocaleData } from '@angular/common';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import localePt from '@angular/common/locales/pt';
import { ApplicationConfig, LOCALE_ID, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { errorInterceptor } from './core/interceptors/error.interceptor';
import { loaderInterceptor } from './core/interceptors/loader.interceptor';

// Registro o português para as datas aparecerem como dd/MM/yyyy.
registerLocaleData(localePt);

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    // A ordem importa: o loader envolve tudo, depois entra o token e, por fim, o tratamento de erro.
    provideHttpClient(withInterceptors([loaderInterceptor, authInterceptor, errorInterceptor])),
    { provide: LOCALE_ID, useValue: 'pt' },
  ],
};
