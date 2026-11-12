import { Injectable, signal } from '@angular/core';

export interface PedidoDeConfirmacao {
  titulo: string;
  mensagem: string;
  textoConfirmar: string;
}

/**
 * Esse serviço serve para perguntar "tem certeza?" antes de uma ação perigosa (como excluir).
 * Uso: if (await confirm.confirmar({...})) { ... }
 */
@Injectable({ providedIn: 'root' })
export class ConfirmService {
  readonly pedido = signal<PedidoDeConfirmacao | null>(null);
  private resolver: ((resposta: boolean) => void) | null = null;

  confirmar(pedido: PedidoDeConfirmacao): Promise<boolean> {
    this.pedido.set(pedido);
    return new Promise<boolean>((resolve) => {
      this.resolver = resolve;
    });
  }

  responder(resposta: boolean): void {
    this.resolver?.(resposta);
    this.resolver = null;
    this.pedido.set(null);
  }
}
