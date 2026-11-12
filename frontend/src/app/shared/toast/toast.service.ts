import { Injectable, signal } from '@angular/core';

export type TipoToast = 'sucesso' | 'erro' | 'alerta' | 'info';

export interface Toast {
  id: number;
  tipo: TipoToast;
  mensagem: string;
}

const TEMPO_NA_TELA_MS = 5000;

/**
 * Esse serviço serve para mostrar mensagens rápidas (toasts) no canto da tela.
 * Qualquer parte do app chama toast.sucesso(...) ou toast.erro(...).
 */
@Injectable({ providedIn: 'root' })
export class ToastService {
  private proximoId = 1;
  readonly toasts = signal<Toast[]>([]);

  sucesso(mensagem: string): void {
    this.adicionar('sucesso', mensagem);
  }

  erro(mensagem: string): void {
    this.adicionar('erro', mensagem);
  }

  alerta(mensagem: string): void {
    this.adicionar('alerta', mensagem);
  }

  info(mensagem: string): void {
    this.adicionar('info', mensagem);
  }

  remover(id: number): void {
    this.toasts.update((lista) => lista.filter((toast) => toast.id !== id));
  }

  // Cada toast some sozinho depois de uns segundos.
  private adicionar(tipo: TipoToast, mensagem: string): void {
    const id = this.proximoId++;
    this.toasts.update((lista) => [...lista, { id, tipo, mensagem }]);
    setTimeout(() => this.remover(id), TEMPO_NA_TELA_MS);
  }
}
