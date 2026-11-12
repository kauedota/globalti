import { DatePipe } from '@angular/common';
import { Component, inject, input, OnInit, output, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { UserService } from '../../../core/services/user.service';
import { Ordenacao } from '../../../models/page.model';
import { Usuario } from '../../../models/user.model';
import { ConfirmService } from '../../../shared/confirm/confirm.service';
import { PaginationComponent } from '../../../shared/pagination/pagination.component';
import { SortButtonComponent } from '../../../shared/sort-button/sort-button.component';
import { ToastService } from '../../../shared/toast/toast.service';

/**
 * Esse componente serve para a tabela de usuários (só o ADMIN vê): paginada, ordenável por nome,
 * email e data de criação, com as ações de editar e excluir.
 * Quando o admin clica em uma linha, aviso o dashboard para mostrar os endereços daquele usuário.
 */
@Component({
  selector: 'app-user-list',
  imports: [DatePipe, RouterLink, PaginationComponent, SortButtonComponent],
  template: `
    <section class="cartao">
      <div class="cartao-topo">
        <h2>Usuários</h2>
        <a routerLink="/usuarios/novo" class="btn btn-primario">Novo usuário</a>
      </div>

      <div class="tabela-rolagem">
        <table class="tabela">
          <thead>
            <tr>
              <th><app-sort-button campo="nome" [atual]="ordem()" (ordenar)="ordenarPor($event)">Nome</app-sort-button></th>
              <th><app-sort-button campo="email" [atual]="ordem()" (ordenar)="ordenarPor($event)">Email</app-sort-button></th>
              <th>Perfil</th>
              <th><app-sort-button campo="criadoEm" [atual]="ordem()" (ordenar)="ordenarPor($event)">Criado em</app-sort-button></th>
              <th class="coluna-acoes">Ações</th>
            </tr>
          </thead>
          <tbody>
            @for (usuario of usuarios(); track usuario.id) {
              <tr [class.linha-selecionada]="usuario.id === selecionadoId()" (click)="selecionar.emit(usuario)">
                <td data-label="Nome">{{ usuario.nome }}</td>
                <td data-label="Email">{{ usuario.email }}</td>
                <td data-label="Perfil"><span class="selo" [class.selo-admin]="usuario.role === 'ADMIN'">{{ usuario.role }}</span></td>
                <td data-label="Criado em">{{ usuario.criadoEm | date: 'dd/MM/yyyy HH:mm' }}</td>
                <td class="coluna-acoes" (click)="$event.stopPropagation()">
                  <a [routerLink]="['/usuarios', usuario.id, 'editar']" class="btn btn-secundario btn-pequeno">Editar</a>
                  <button type="button" class="btn btn-perigo btn-pequeno" (click)="excluir(usuario)">Excluir</button>
                </td>
              </tr>
            } @empty {
              <tr><td colspan="5" class="tabela-vazia">{{ carregando() ? 'Carregando...' : 'Nenhum usuário encontrado.' }}</td></tr>
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
export class UserListComponent implements OnInit {
  private readonly userService = inject(UserService);
  private readonly confirm = inject(ConfirmService);
  private readonly toast = inject(ToastService);

  // Id do usuário que está com os endereços abertos (para destacar a linha).
  readonly selecionadoId = input<number | null>(null);
  readonly selecionar = output<Usuario>();
  // Avisa o dashboard quando um usuário é excluído, para ele limpar a seleção se for o caso.
  readonly excluido = output<number>();

  protected readonly usuarios = signal<Usuario[]>([]);
  protected readonly pagina = signal(0);
  protected readonly tamanho = signal(10);
  protected readonly totalPaginas = signal(0);
  protected readonly totalElementos = signal(0);
  protected readonly carregando = signal(false);
  protected readonly ordem = signal<Ordenacao>({ campo: 'nome', direcao: 'asc' });

  ngOnInit(): void {
    this.carregar();
  }

  // Busca na API só a página atual, já ordenada.
  carregar(): void {
    this.carregando.set(true);
    this.userService.listar(this.pagina(), this.tamanho(), this.ordem()).subscribe({
      next: (resultado) => {
        this.usuarios.set(resultado.content);
        this.totalPaginas.set(resultado.page.totalPages);
        this.totalElementos.set(resultado.page.totalElements);
        this.carregando.set(false);
      },
      error: () => this.carregando.set(false),
    });
  }

  // Clicar de novo na mesma coluna inverte a direção; em outra coluna, começa crescente.
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

  protected async excluir(usuario: Usuario): Promise<void> {
    const confirmado = await this.confirm.confirmar({
      titulo: 'Excluir usuário',
      mensagem: `Tem certeza que deseja excluir ${usuario.nome}? Os endereços dele também serão apagados.`,
      textoConfirmar: 'Excluir',
    });
    if (!confirmado) return;

    this.userService.excluir(usuario.id).subscribe(() => {
      this.toast.sucesso('Usuário excluído.');
      this.excluido.emit(usuario.id);
      // Se era o último item da página, volto uma página para não ficar numa página vazia.
      if (this.usuarios().length === 1 && this.pagina() > 0) {
        this.pagina.update((p) => p - 1);
      }
      this.carregar();
    });
  }
}
