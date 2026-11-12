import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { aplicarErrosDoServidor } from '../../../core/form-utils';
import { AuthService } from '../../../core/services/auth.service';
import { FooterComponent } from '../../../shared/footer/footer.component';
import { FieldErrorComponent } from '../../../shared/field-error/field-error.component';
import { ToastService } from '../../../shared/toast/toast.service';

// Confere se "senha" e "confirmar senha" são iguais. Marco o erro no campo de confirmação.
function senhasIguais(grupo: AbstractControl): ValidationErrors | null {
  const confirmacao = grupo.get('confirmarSenha');
  if (confirmacao && grupo.get('senha')?.value !== confirmacao.value) {
    confirmacao.setErrors({ ...confirmacao.errors, senhasDiferentes: true });
  } else if (confirmacao?.errors?.['senhasDiferentes']) {
    const { senhasDiferentes, ...outrosErros } = confirmacao.errors;
    confirmacao.setErrors(Object.keys(outrosErros).length ? outrosErros : null);
  }
  return null;
}

/**
 * Esse componente serve para a tela de registro. Quem se registra sempre vira usuário comum.
 */
@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink, FieldErrorComponent, FooterComponent],
  template: `
    <div class="pagina">
    <div class="auth-pagina">
      <form class="cartao auth-cartao" [formGroup]="form" (ngSubmit)="registrar()" novalidate>
        <h1>Criar conta</h1>
        <p class="texto-suave">Preencha os dados para se cadastrar.</p>

        <div class="campo">
          <label for="nome">Nome</label>
          <input id="nome" type="text" formControlName="nome" autocomplete="name" />
          <app-field-error [control]="form.controls.nome" />
        </div>

        <div class="campo">
          <label for="email">Email</label>
          <input id="email" type="email" formControlName="email" autocomplete="username" />
          <app-field-error [control]="form.controls.email" />
        </div>

        <div class="campo">
          <label for="senha">Senha</label>
          <input id="senha" type="password" formControlName="senha" autocomplete="new-password" />
          <app-field-error [control]="form.controls.senha" />
        </div>

        <div class="campo">
          <label for="confirmarSenha">Confirmar senha</label>
          <input id="confirmarSenha" type="password" formControlName="confirmarSenha" autocomplete="new-password" />
          <app-field-error [control]="form.controls.confirmarSenha" />
        </div>

        <button type="submit" class="btn btn-primario btn-bloco" [disabled]="enviando()">
          {{ enviando() ? 'Cadastrando...' : 'Cadastrar' }}
        </button>

        <p class="auth-rodape">Já tem conta? <a routerLink="/login">Entrar</a></p>
      </form>
    </div>
    <app-footer />
    </div>
  `,
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder).nonNullable;
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  protected readonly enviando = signal(false);

  protected readonly form = this.fb.group(
    {
      nome: ['', [Validators.required, Validators.maxLength(120)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(180)]],
      senha: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
      confirmarSenha: ['', [Validators.required]],
    },
    { validators: senhasIguais },
  );

  protected registrar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { nome, email, senha } = this.form.getRawValue();
    this.enviando.set(true);
    this.auth.registrar({ nome, email, senha }).subscribe({
      next: () => {
        this.toast.sucesso('Conta criada com sucesso!');
        this.router.navigate(['/dashboard']);
      },
      error: (erro) => {
        aplicarErrosDoServidor(this.form, erro);
        this.enviando.set(false);
      },
    });
  }
}
