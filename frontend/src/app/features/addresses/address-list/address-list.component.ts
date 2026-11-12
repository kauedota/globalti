import { Component, effect, inject, input, signal, untracked } from '@angular/core';
import { RouterLink } from '@angular/router';

import { AddressService } from '../../../core/services/address.service';
import { Endereco } from '../../../models/address.model';
import { Ordenacao } from '../../../models/page.model';
import { ConfirmService } from '../../../shared/confirm/confirm.service';
import { PaginationComponent } from '../../../shared/pagination/pagination.component';
import { SortButtonComponent } from '../../../shared/sort-button/sort-button.component';
import { ToastService } from '../../../shared/toast/toast.service';

/**
 * Esse componente serve para a tabela de endereços de um usuário: paginada, ordenável,
 * com editar e excluir. Quando o usuário recebido (userId) muda, ele recarrega do começo.
 */
@Component({
  selector: 'app-address-list',
  imports: [RouterLink, PaginationComponent, SortButtonComponent],
  template: `
    <section class="cartao">
      <div class="cartao-topo">
        <h2>{{ titulo() }}</h2>
        <a [routerLink]="['/usuarios', userId(), 'enderecos', 'novo']" class="btn btn-primario">Novo endereço</a>
      </div>

      <div class="tabela-rolagem">
        <table class="tabela">
          <thead>
            <tr>
              <th><app-sort-button campo="logradouro" [atual]="ordem()" (ordenar)="ordenarPor($event)">Logradouro</app-sort-button></th>
              <th>Número</th>
              <th>Bairro</th>
              <th><app-sort-button campo="cidade" [atual]="ordem()" (ordenar)="ordenarPor($event)">Cidade/UF</app-sort-button></th>
              <th><app-sort-button campo="cep" [atual]="ordem()" (ordenar)="ordenarPor($event)">CEP</app-sort-button></th>
              <th class="coluna-acoes">Ações</th>
            </tr>
          </thead>
          <tbody>
            @for (endereco of enderecos(); track endereco.id) {
              <tr>
                <td>{{ endereco.logradouro }}</td>
                <td>{{ endereco.numero }}{{ endereco.complemento ? ' - ' + endereco.complemento : '' }}</td>
                <td>{{ endereco.bairro }}</td>
                <td>{{ endereco.cidade }}/{{ endereco.estado }}</td>
                <td>{{ formatarCep(endereco.cep) }}</td>
                <td class="coluna-acoes">
                  <a [routerLink]="['/enderecos', endereco.id, 'editar']" class="btn btn-secundario btn-pequeno">Editar</a>
                  <button type="button" class="btn btn-perigo btn-pequeno" (click)="excluir(endereco)">Excluir</button>
                </td>
              </tr>
            } @empty {
              <tr><td colspan="6" class="tabela-vazia">{{ carregando() ? 'Carregando...' : 'Nenhum endereço cadastrado.' }}</td></tr>
            }
          </tbody>
        </table>
      </div>

      <app-pagination
        [pagina]="pagina()" [tamanho]="tamanho()" [totalPaginas]="totalPaginas()" [totalElementos]="totalElementos()"
        (paginaAlterada)="irParaPagina($event)" (tamanhoAlterado)="mudarTamanho($event)" />
    </section>
  `,
})
export class AddressListComponent {
  private readonly addressService = inject(AddressService);
  private readonly confirm = inject(ConfirmService);
  private readonly toast = inject(ToastService);

  readonly userId = input.required<number>();
  readonly titulo = input('Endereços');

  protected readonly enderecos = signal<Endereco[]>([]);
  protected readonly pagina = signal(0);
  protected readonly tamanho = signal(5);
  protected readonly totalPaginas = signal(0);
  protected readonly totalElementos = signal(0);
  protected readonly carregando = signal(false);
  protected readonly ordem = signal<Ordenacao>({ campo: 'id', direcao: 'asc' });

  constructor() {
    // Sempre que o dashboard troca o usuário selecionado, volto para a primeira página e busco de novo.
    effect(() => {
      this.userId();
      untracked(() => {
        this.pagina.set(0);
        this.carregar();
      });
    });
  }

  private carregar(): void {
    this.carregando.set(true);
    this.addressService.listarDoUsuario(this.userId(), this.pagina(), this.tamanho(), this.ordem()).subscribe({
      next: (resultado) => {
        this.enderecos.set(resultado.content);
        this.totalPaginas.set(resultado.page.totalPages);
        this.totalElementos.set(resultado.page.totalElements);
        this.carregando.set(false);
      },
      error: () => {
        this.enderecos.set([]);
        this.carregando.set(false);
      },
    });
  }

  protected ordenarPor(campo: string): void {
    const atual = this.ordem();
    const direcao = atual.campo === campo && atual.direcao === 'asc' ? 'desc' : 'asc';
    this.ordem.set({ campo, direcao });
    this.pagina.set(0);
    this.carregar();
  }

  protected irParaPagina(pagina: number): void {
    this.pagina.set(pagina);
    this.carregar();
  }

  protected mudarTamanho(tamanho: number): void {
    this.tamanho.set(tamanho);
    this.pagina.set(0);
    this.carregar();
  }

  // No banco o CEP fica só com dígitos; na tela mostro com o hífen.
  protected formatarCep(cep: string): string {
    return cep.length === 8 ? `${cep.slice(0, 5)}-${cep.slice(5)}` : cep;
  }

  protected async excluir(endereco: Endereco): Promise<void> {
    const confirmado = await this.confirm.confirmar({
      titulo: 'Excluir endereço',
      mensagem: `Tem certeza que deseja excluir o endereço "${endereco.logradouro}, ${endereco.numero}"?`,
      textoConfirmar: 'Excluir',
    });
    if (!confirmado) return;

    this.addressService.excluir(endereco.id).subscribe(() => {
      this.toast.sucesso('Endereço excluído.');
      if (this.enderecos().length === 1 && this.pagina() > 0) {
        this.pagina.update((p) => p - 1);
      }
      this.carregar();
    });
  }
}
