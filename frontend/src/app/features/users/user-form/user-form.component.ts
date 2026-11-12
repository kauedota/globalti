import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { aplicarErrosDoServidor } from '../../../core/form-utils';
import { AuthService } from '../../../core/services/auth.service';
import { UserService } from '../../../core/services/user.service';
import { UsuarioRequest } from '../../../models/user.model';
import { FieldErrorComponent } from '../../../shared/field-error/field-error.component';
import { ToastService } from '../../../shared/toast/toast.service';

/**
 * Esse componente serve para o formulário de usuário, tanto no cadastro (só ADMIN) quanto na edição.
 * Na edição, a senha é opcional (em branco mantém a atual) e só o ADMIN vê o campo de perfil.
 */
@Component({
  selector: 'app-user-form',
  imports: [ReactiveFormsModule, RouterLink, FieldErrorComponent],
  template: `
    <section class="cartao cartao-formulario">
      <h2>{{ id() ? 'Editar usuário' : 'Novo usuário' }}</h2>

      <form [formGroup]="form" (ngSubmit)="salvar()" novalidate>
        <div class="campo">
          <label for="nome">Nome</label>
          <input id="nome" type="text" formControlName="nome" />
          <app-field-error [control]="form.controls.nome" />
        </div>

        <div class="campo">
          <label for="email">Email</label>
          <input id="email" type="email" formControlName="email" />
          <app-field-error [control]="form.controls.email" />
        </div>

        <div class="campo">
          <label for="senha">Senha {{ id() ? '(deixe em branco para manter a atual)' : '' }}</label>
          <input id="senha" type="password" formControlName="senha" autocomplete="new-password" />
          <app-field-error [control]="form.controls.senha" />
        </div>

        @if (auth.admin()) {
          <div class="campo">
            <label for="role">Perfil</label>
            <select id="role" formControlName="role">
              <option value="USER">Usuário</option>
              <option value="ADMIN">Administrador</option>
            </select>
          </div>
        }

        <div class="formulario-acoes">
          <a routerLink="/dashboard" class="btn btn-secundario">Cancelar</a>
          <button type="submit" class="btn btn-primario" [disabled]="salvando()">
            {{ salvando() ? 'Salvando...' : 'Salvar' }}
          </button>
        </div>
      </form>
    </section>
  `,
})
export class UserFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder).nonNullable;
  private readonly userService = inject(UserService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);
  protected readonly auth = inject(AuthService);

  // Se a rota tem :id é edição; se não, é cadastro de usuário novo.
  protected readonly id = signal<number | null>(null);
  protected readonly salvando = signal(false);

  protected readonly form = this.fb.group({
    nome: ['', [Validators.required, Validators.maxLength(120)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(180)]],
    senha: ['', [Validators.minLength(8), Validators.maxLength(72)]],
    role: ['USER' as 'USER' | 'ADMIN'],
  });

  ngOnInit(): void {
    const idDaRota = this.route.snapshot.paramMap.get('id');

    if (idDaRota) {
      this.id.set(Number(idDaRota));
      this.userService.buscar(Number(idDaRota)).subscribe({
        next: (usuario) => this.form.patchValue({ nome: usuario.nome, email: usuario.email, role: usuario.role }),
        error: () => this.router.navigate(['/dashboard']),
      });
    } else {
      // No cadastro a senha é obrigatória.
      this.form.controls.senha.addValidators(Validators.required);
    }
  }

  protected salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { nome, email, senha, role } = this.form.getRawValue();
    const dados: UsuarioRequest = { nome, email, senha: senha || undefined, role: this.auth.admin() ? role : undefined };
    const idAtual = this.id();

    this.salvando.set(true);
    const chamada = idAtual ? this.userService.atualizar(idAtual, dados) : this.userService.criar(dados);

    chamada.subscribe({
      next: (usuario) => {
        // Se a pessoa editou o próprio cadastro, atualizo o nome que aparece no topo.
        if (usuario.id === this.auth.usuario()?.id) {
          this.auth.atualizarUsuarioLogado(usuario);
        }
        this.toast.sucesso(idAtual ? 'Usuário atualizado.' : 'Usuário cadastrado.');
        this.router.navigate(['/dashboard']);
      },
      error: (erro) => {
        aplicarErrosDoServidor(this.form, erro);
        this.salvando.set(false);
      },
    });
  }
}
