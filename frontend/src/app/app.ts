import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { ConfirmComponent } from './shared/confirm/confirm.component';
import { LoaderComponent } from './shared/loader/loader.component';
import { ToastComponent } from './shared/toast/toast.component';

/**
 * Esse componente serve de raiz do app: guarda a área das rotas e os elementos globais
 * (barra de loading, toasts e janela de confirmação).
 */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, LoaderComponent, ToastComponent, ConfirmComponent],
  template: `
    <app-loader />
    <router-outlet />
    <app-toast />
    <app-confirm />
  `,
})
export class App {}
