import { Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';
import { FooterComponent } from '../../shared/footer/footer.component';

/**
 * Esse componente serve de moldura das telas internas: navbar no topo (com menu que vira
 * "hambúrguer" no celular), a tela da rota atual no meio e o rodapé embaixo.
 */
@Component({
  selector: 'app-layout',
  imports: [RouterLink, RouterLinkActive, RouterOutlet, FooterComponent],
  template: `
    <div class="pagina">
      <header class="topo">
        <div class="topo-conteudo">
          <a routerLink="/dashboard" class="marca" (click)="fecharMenu()">Prova Fullstack</a>

          <button type="button" class="menu-botao" aria-label="Abrir menu" [attr.aria-expanded]="menuAberto()"
                  (click)="alternarMenu()">
            <span></span><span></span><span></span>
          </button>

          <nav class="menu" [class.menu-aberto]="menuAberto()">
            <a routerLink="/dashboard" routerLinkActive="ativo" (click)="fecharMenu()">Dashboard</a>
            @if (auth.admin()) {
              <a routerLink="/usuarios/novo" routerLinkActive="ativo" (click)="fecharMenu()">Novo usuário</a>
            }
            @if (auth.usuario(); as eu) {
              <a [routerLink]="['/usuarios', eu.id, 'editar']" routerLinkActive="ativo" (click)="fecharMenu()">Meu cadastro</a>
            }

            <div class="menu-usuario">
              <span class="topo-nome">{{ auth.usuario()?.nome }}</span>
              <span class="selo" [class.selo-admin]="auth.admin()">{{ auth.admin() ? 'Administrador' : 'Usuário' }}</span>
              <button type="button" class="btn btn-secundario btn-pequeno" (click)="auth.logout()">Sair</button>
            </div>
          </nav>
        </div>
      </header>

      <main class="conteudo">
        <router-outlet />
      </main>

      <app-footer />
    </div>
  `,
})
export class LayoutComponent {
  protected readonly auth = inject(AuthService);
  protected readonly menuAberto = signal(false);

  protected alternarMenu(): void {
    this.menuAberto.update((aberto) => !aberto);
  }

  // Depois de clicar em um link no celular, o menu fecha sozinho.
  protected fecharMenu(): void {
    this.menuAberto.set(false);
  }
}
