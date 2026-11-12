import { Component, computed, input, output } from '@angular/core';

import { Ordenacao } from '../../models/page.model';

/**
 * Esse componente serve para o título clicável de uma coluna que ordena a tabela.
 * Mostra uma setinha quando a coluna é a que está ordenando.
 */
@Component({
  selector: 'app-sort-button',
  template: `
    <button type="button" class="ordenar" (click)="ordenar.emit(campo())">
      <ng-content />
      <span class="ordenar-seta" aria-hidden="true">{{ seta() }}</span>
    </button>
  `,
})
export class SortButtonComponent {
  readonly campo = input.required<string>();
  readonly atual = input.required<Ordenacao>();
  readonly ordenar = output<string>();

  protected readonly seta = computed(() => {
    if (this.atual().campo !== this.campo()) return '';
    return this.atual().direcao === 'asc' ? '▲' : '▼';
  });
}
