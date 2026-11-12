import { Component, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';

/**
 * Esse componente serve de moldura das telas internas: menu no topo com o nome de quem está logado,
 * o botão de sair e, embaixo, a tela da rota atual.
 */
@Component({
  selector: 'app-layout',
  imports: [RouterLink, RouterOutlet],
  template: `
    <header class="topo">
      <div class="topo-conteudo">
        <a routerLink="/dashboard" class="marca">Prova Fullstack</a>
        <div class="topo-usuario">
          <span class="topo-nome">{{ auth.usuario()?.nome }}</span>
          <span class="selo" [class.selo-admin]="auth.admin()">{{ auth.admin() ? 'Administrador' : 'Usuário' }}</span>
          <button type="button" class="btn btn-secundario btn-pequeno" (click)="auth.logout()">Sair</button>
        </div>
      </div>
    </header>
    <main class="conteudo">
      <router-outlet />
    </main>
  `,
})
export class LayoutComponent {
  protected readonly auth = inject(AuthService);
}
