import { Component, inject } from '@angular/core';

import { LoaderService } from './loader.service';

/**
 * Esse componente serve para mostrar uma barra de carregamento no topo da página
 * enquanto qualquer requisição à API estiver rodando.
 */
@Component({
  selector: 'app-loader',
  template: `
    @if (loader.carregando()) {
      <div class="loader-barra" role="progressbar" aria-label="Carregando"></div>
    }
  `,
})
export class LoaderComponent {
  protected readonly loader = inject(LoaderService);
}
