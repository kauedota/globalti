import { Component, inject } from '@angular/core';

import { ToastService } from './toast.service';

/**
 * Esse componente serve para desenhar os toasts na tela. Fica uma vez só, no componente raiz.
 */
@Component({
  selector: 'app-toast',
  template: `
    <div class="toast-area" aria-live="polite">
      @for (toast of toastService.toasts(); track toast.id) {
        <div class="toast" [class]="'toast toast-' + toast.tipo" role="status">
          <span>{{ toast.mensagem }}</span>
          <button type="button" class="toast-fechar" aria-label="Fechar" (click)="toastService.remover(toast.id)">
            &times;
          </button>
        </div>
      }
    </div>
  `,
})
export class ToastComponent {
  protected readonly toastService = inject(ToastService);
}
