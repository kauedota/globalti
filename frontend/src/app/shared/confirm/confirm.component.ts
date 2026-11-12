import { Component, inject } from '@angular/core';

import { ConfirmService } from './confirm.service';

/**
 * Esse componente serve para desenhar a janelinha de confirmação.
 * Fica uma vez só, no componente raiz, e aparece quando alguém chama confirm.confirmar().
 */
@Component({
  selector: 'app-confirm',
  template: `
    @if (confirm.pedido(); as pedido) {
      <div class="modal-fundo" (click)="confirm.responder(false)">
        <div class="modal" role="dialog" aria-modal="true" (click)="$event.stopPropagation()">
          <h2>{{ pedido.titulo }}</h2>
          <p>{{ pedido.mensagem }}</p>
          <div class="modal-acoes">
            <button type="button" class="btn btn-secundario" (click)="confirm.responder(false)">Cancelar</button>
            <button type="button" class="btn btn-perigo" (click)="confirm.responder(true)">
              {{ pedido.textoConfirmar }}
            </button>
          </div>
        </div>
      </div>
    }
  `,
})
export class ConfirmComponent {
  protected readonly confirm = inject(ConfirmService);
}
