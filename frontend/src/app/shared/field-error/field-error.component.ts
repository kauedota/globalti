import { Component, computed, effect, input, signal } from '@angular/core';
import { AbstractControl } from '@angular/forms';

/**
 * Esse componente serve para mostrar a mensagem de erro de um campo do formulário, em português.
 * Só aparece depois que a pessoa mexeu no campo (ou tentou enviar).
 */
@Component({
  selector: 'app-field-error',
  template: `
    @if (mensagem(); as texto) {
      <small class="campo-erro">{{ texto }}</small>
    }
  `,
})
export class FieldErrorComponent {
  readonly control = input.required<AbstractControl>();

  // O estado do campo muda fora do Angular "signals", então escuto os eventos do controle
  // e uso esse contador para avisar o componente que precisa recalcular a mensagem.
  private readonly versao = signal(0);

  protected readonly mensagem = computed(() => {
    this.versao();
    const controle = this.control();
    if (!controle.errors || !(controle.touched || controle.dirty)) {
      return '';
    }
    return this.traduzir(controle.errors);
  });

  constructor() {
    effect((aoLimpar) => {
      const assinatura = this.control().events.subscribe(() => this.versao.update((n) => n + 1));
      aoLimpar(() => assinatura.unsubscribe());
    });
  }

  private traduzir(erros: Record<string, any>): string {
    if (erros['servidor']) return erros['servidor'];
    if (erros['required']) return 'Campo obrigatório';
    if (erros['email']) return 'Informe um email válido';
    if (erros['minlength']) return `Mínimo de ${erros['minlength'].requiredLength} caracteres`;
    if (erros['maxlength']) return `Máximo de ${erros['maxlength'].requiredLength} caracteres`;
    if (erros['pattern']) return 'Formato inválido';
    if (erros['cepInvalido']) return 'CEP deve ter 8 dígitos';
    if (erros['senhasDiferentes']) return 'As senhas não conferem';
    return 'Valor inválido';
  }
}
