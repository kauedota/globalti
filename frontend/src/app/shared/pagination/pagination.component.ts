import { Component, computed, input, output } from '@angular/core';

/**
 * Esse componente serve para os botões de paginação (anterior, próxima e tamanho da página).
 * Ele só avisa o componente pai qual página ou tamanho foi escolhido; quem busca os dados é o pai.
 */
@Component({
  selector: 'app-pagination',
  template: `
    <div class="paginacao">
      <span class="paginacao-info">{{ resumo() }}</span>
      <div class="paginacao-controles">
        <label>
          Por página
          <select [value]="tamanho()" (change)="mudarTamanho($event)">
            @for (opcao of opcoesDeTamanho; track opcao) {
              <option [value]="opcao" [selected]="opcao === tamanho()">{{ opcao }}</option>
            }
          </select>
        </label>
        <button type="button" class="btn btn-secundario btn-pequeno" [disabled]="pagina() === 0"
                (click)="paginaAlterada.emit(pagina() - 1)">Anterior</button>
        <span>{{ pagina() + 1 }} de {{ totalPaginas() || 1 }}</span>
        <button type="button" class="btn btn-secundario btn-pequeno" [disabled]="pagina() + 1 >= totalPaginas()"
                (click)="paginaAlterada.emit(pagina() + 1)">Próxima</button>
      </div>
    </div>
  `,
})
export class PaginationComponent {
  readonly pagina = input.required<number>();
  readonly tamanho = input.required<number>();
  readonly totalPaginas = input.required<number>();
  readonly totalElementos = input.required<number>();

  readonly paginaAlterada = output<number>();
  readonly tamanhoAlterado = output<number>();

  protected readonly opcoesDeTamanho = [5, 10, 20, 50];

  protected readonly resumo = computed(() => {
    const total = this.totalElementos();
    return total === 1 ? '1 registro' : `${total} registros`;
  });

  protected mudarTamanho(evento: Event): void {
    const valor = Number((evento.target as HTMLSelectElement).value);
    this.tamanhoAlterado.emit(valor);
  }
}
