import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';
import { Usuario } from '../../models/user.model';
import { AddressListComponent } from '../addresses/address-list/address-list.component';
import { UserListComponent } from '../users/user-list/user-list.component';

/**
 * Esse componente serve para a tela principal.
 * ADMIN: vê a lista de todos os usuários e, ao clicar em um, os endereços dele.
 * USER: vê o próprio perfil e os próprios endereços.
 */
@Component({
  selector: 'app-dashboard',
  imports: [DatePipe, RouterLink, UserListComponent, AddressListComponent],
  template: `
    @if (auth.admin()) {
      <app-user-list [selecionadoId]="selecionado()?.id ?? null" (selecionar)="selecionado.set($event)"
                     (excluido)="aoExcluirUsuario($event)" />

      @if (selecionado(); as usuario) {
        <app-address-list [userId]="usuario.id" [titulo]="'Endereços de ' + usuario.nome" />
      } @else {
        <p class="dica">Clique em um usuário da lista para ver os endereços dele.</p>
      }
    } @else {
      @if (auth.usuario(); as eu) {
        <section class="cartao">
          <div class="cartao-topo">
            <h2>Meu perfil</h2>
            <a [routerLink]="['/usuarios', eu.id, 'editar']" class="btn btn-secundario">Editar perfil</a>
          </div>
          <dl class="perfil">
            <div><dt>Nome</dt><dd>{{ eu.nome }}</dd></div>
            <div><dt>Email</dt><dd>{{ eu.email }}</dd></div>
            <div><dt>Cadastrado em</dt><dd>{{ eu.criadoEm | date: 'dd/MM/yyyy HH:mm' }}</dd></div>
          </dl>
        </section>
        <app-address-list [userId]="eu.id" titulo="Meus endereços" />
      }
    }
  `,
})
export class DashboardComponent implements OnInit {
  protected readonly auth = inject(AuthService);
  private readonly userService = inject(UserService);
  private readonly route = inject(ActivatedRoute);

  protected readonly selecionado = signal<Usuario | null>(null);

  // Depois de cadastrar/editar um endereço o formulário volta para cá com ?usuario=ID,
  // assim o admin continua vendo os endereços do mesmo usuário.
  ngOnInit(): void {
    const id = Number(this.route.snapshot.queryParamMap.get('usuario'));
    if (this.auth.admin() && id) {
      this.userService.buscar(id).subscribe((usuario) => this.selecionado.set(usuario));
    }
  }

  protected aoExcluirUsuario(id: number): void {
    if (this.selecionado()?.id === id) {
      this.selecionado.set(null);
    }
  }
}
