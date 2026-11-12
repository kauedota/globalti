import { Component } from '@angular/core';

/**
 * Esse componente serve para o rodapé das páginas, com o nome do projeto e as tecnologias usadas.
 */
@Component({
  selector: 'app-footer',
  template: `
    <footer class="rodape">
      <div class="rodape-conteudo">
        <span>&copy; {{ ano }} Prova Fullstack Java</span>
        <span class="rodape-tecnologias">Angular &middot; Spring Boot &middot; PostgreSQL</span>
      </div>
    </footer>
  `,
})
export class FooterComponent {
  protected readonly ano = new Date().getFullYear();
}
