import { Injectable, computed, signal } from '@angular/core';

/**
 * Esse serviço serve para saber se existe alguma requisição em andamento.
 * Eu conto quantas estão rodando: o loader só some quando todas terminarem.
 */
@Injectable({ providedIn: 'root' })
export class LoaderService {
  private readonly emAndamento = signal(0);
  readonly carregando = computed(() => this.emAndamento() > 0);

  iniciar(): void {
    this.emAndamento.update((n) => n + 1);
  }

  finalizar(): void {
    this.emAndamento.update((n) => Math.max(0, n - 1));
  }
}
