import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';
import { FieldErrorComponent } from '../../../shared/field-error/field-error.component';
import { ToastService } from '../../../shared/toast/toast.service';

/**
 * Esse componente serve para a tela de login.
 */
@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink, FieldErrorComponent],
  template: `
    <div class="auth-pagina">
      <form class="cartao auth-cartao" [formGroup]="form" (ngSubmit)="entrar()" novalidate>
        <h1>Entrar</h1>
        <p class="texto-suave">Acesse com o seu email e senha.</p>

        <div class="campo">
          <label for="email">Email</label>
          <input id="email" type="email" formControlName="email" autocomplete="username" />
          <app-field-error [control]="form.controls.email" />
        </div>

        <div class="campo">
          <label for="senha">Senha</label>
          <input id="senha" type="password" formControlName="senha" autocomplete="current-password" />
          <app-field-error [control]="form.controls.senha" />
        </div>

        <button type="submit" class="btn btn-primario btn-bloco" [disabled]="enviando()">
          {{ enviando() ? 'Entrando...' : 'Entrar' }}
        </button>

        <p class="auth-rodape">Ainda não tem conta? <a routerLink="/registro">Cadastre-se</a></p>
      </form>
    </div>
  `,
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder).nonNullable;
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  protected readonly enviando = signal(false);

  protected readonly form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    senha: ['', [Validators.required]],
  });

  protected entrar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.enviando.set(true);
    this.auth.login(this.form.getRawValue()).subscribe({
      next: (resposta) => {
        this.toast.sucesso(`Bem-vindo, ${resposta.usuario.nome}!`);
        this.router.navigate(['/dashboard']);
      },
      // O toast de erro já é mostrado pelo interceptor; aqui só libero o botão.
      error: () => this.enviando.set(false),
    });
  }
}
