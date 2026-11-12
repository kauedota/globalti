import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, distinctUntilChanged, EMPTY, filter, map, switchMap, tap } from 'rxjs';

import { aplicarErrosDoServidor } from '../../../core/form-utils';
import { AddressService } from '../../../core/services/address.service';
import { AuthService } from '../../../core/services/auth.service';
import { CepService } from '../../../core/services/cep.service';
import { EnderecoRequest } from '../../../models/address.model';
import { FieldErrorComponent } from '../../../shared/field-error/field-error.component';
import { ToastService } from '../../../shared/toast/toast.service';

const ESTADOS = [
  'AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA', 'MT', 'MS', 'MG', 'PA',
  'PB', 'PR', 'PE', 'PI', 'RJ', 'RN', 'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO',
];

const apenasDigitos = (texto: string): string => texto.replace(/\D/g, '');

// O CEP é válido quando tem exatamente 8 dígitos (com ou sem hífen).
function cepValido(controle: AbstractControl): ValidationErrors | null {
  return apenasDigitos(controle.value ?? '').length === 8 ? null : { cepInvalido: true };
}

/**
 * Esse componente serve para o formulário de endereço (cadastro e edição).
 * A pessoa digita o CEP e, quando completa 8 dígitos, eu consulto a API e preencho
 * logradouro, bairro, cidade e estado sozinho.
 */
@Component({
  selector: 'app-address-form',
  imports: [ReactiveFormsModule, RouterLink, FieldErrorComponent],
  template: `
    <section class="cartao cartao-formulario">
      <h2>{{ idEndereco() ? 'Editar endereço' : 'Novo endereço' }}</h2>

      <form [formGroup]="form" (ngSubmit)="salvar()" novalidate>
        <div class="campo">
          <label for="cep">CEP</label>
          <input id="cep" type="text" inputmode="numeric" placeholder="00000-000" formControlName="cep"
                 (input)="mascararCep($event)" />
          @if (buscandoCep()) {
            <small class="campo-ajuda">Buscando CEP...</small>
          }
          <app-field-error [control]="form.controls.cep" />
        </div>

        <div class="grade">
          <div class="campo campo-largo">
            <label for="logradouro">Logradouro</label>
            <input id="logradouro" type="text" formControlName="logradouro" />
            <app-field-error [control]="form.controls.logradouro" />
          </div>
          <div class="campo">
            <label for="numero">Número</label>
            <input id="numero" type="text" formControlName="numero" />
            <app-field-error [control]="form.controls.numero" />
          </div>
        </div>

        <div class="campo">
          <label for="complemento">Complemento</label>
          <input id="complemento" type="text" formControlName="complemento" />
          <app-field-error [control]="form.controls.complemento" />
        </div>

        <div class="grade">
          <div class="campo">
            <label for="bairro">Bairro</label>
            <input id="bairro" type="text" formControlName="bairro" />
            <app-field-error [control]="form.controls.bairro" />
          </div>
          <div class="campo">
            <label for="cidade">Cidade</label>
            <input id="cidade" type="text" formControlName="cidade" />
            <app-field-error [control]="form.controls.cidade" />
          </div>
          <div class="campo campo-estreito">
            <label for="estado">Estado</label>
            <select id="estado" formControlName="estado">
              <option value="">UF</option>
              @for (uf of estados; track uf) {
                <option [value]="uf">{{ uf }}</option>
              }
            </select>
            <app-field-error [control]="form.controls.estado" />
          </div>
        </div>

        <div class="formulario-acoes">
          <a [routerLink]="['/dashboard']" [queryParams]="voltarParams()" class="btn btn-secundario">Cancelar</a>
          <button type="submit" class="btn btn-primario" [disabled]="salvando() || buscandoCep()">
            {{ salvando() ? 'Salvando...' : 'Salvar' }}
          </button>
        </div>
      </form>
    </section>
  `,
})
export class AddressFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder).nonNullable;
  private readonly addressService = inject(AddressService);
  private readonly cepService = inject(CepService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);
  private readonly auth = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly estados = ESTADOS;
  protected readonly idEndereco = signal<number | null>(null);
  protected readonly userId = signal<number | null>(null);
  protected readonly salvando = signal(false);
  protected readonly buscandoCep = signal(false);

  protected readonly form = this.fb.group({
    cep: ['', [Validators.required, cepValido]],
    numero: ['', [Validators.required, Validators.maxLength(20)]],
    complemento: ['', [Validators.maxLength(100)]],
    logradouro: ['', [Validators.required, Validators.maxLength(200)]],
    bairro: ['', [Validators.required, Validators.maxLength(100)]],
    cidade: ['', [Validators.required, Validators.maxLength(100)]],
    estado: ['', [Validators.required]],
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    const userId = this.route.snapshot.paramMap.get('userId');

    if (id) {
      this.carregarParaEdicao(Number(id));
    } else if (userId) {
      this.userId.set(Number(userId));
    }
    this.observarCep();
  }

  // Ao voltar para o dashboard, o admin continua vendo os endereços do mesmo usuário.
  protected voltarParams(): Record<string, number> | null {
    const dono = this.userId();
    return this.auth.admin() && dono ? { usuario: dono } : null;
  }

  // Deixa o CEP no formato 00000-000 enquanto a pessoa digita.
  protected mascararCep(evento: Event): void {
    const digitos = apenasDigitos((evento.target as HTMLInputElement).value).slice(0, 8);
    const formatado = digitos.length > 5 ? `${digitos.slice(0, 5)}-${digitos.slice(5)}` : digitos;
    this.form.controls.cep.setValue(formatado);
  }

  // Fica escutando o campo CEP. Quando chega em 8 dígitos, consulta a API e preenche o resto.
  // O switchMap cancela a consulta anterior se a pessoa mudar o CEP no meio do caminho.
  private observarCep(): void {
    this.form.controls.cep.valueChanges
      .pipe(
        map((valor) => apenasDigitos(valor)),
        filter((digitos) => digitos.length === 8),
        distinctUntilChanged(),
        tap(() => this.buscandoCep.set(true)),
        switchMap((digitos) =>
          this.cepService.consultar(digitos).pipe(
            catchError(() => {
              // O toast de erro já veio do interceptor; aqui só libero o loader do campo.
              this.buscandoCep.set(false);
              return EMPTY;
            }),
          ),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((cep) => {
        this.form.patchValue({
          logradouro: cep.logradouro,
          bairro: cep.bairro,
          cidade: cep.cidade,
          estado: cep.estado,
        });
        this.buscandoCep.set(false);
      });
  }

  private carregarParaEdicao(id: number): void {
    this.idEndereco.set(id);
    this.addressService.buscar(id).subscribe({
      next: (endereco) => {
        this.userId.set(endereco.userId);
        // emitEvent: false para não disparar a consulta de CEP e sobrescrever o que já está salvo.
        this.form.patchValue(
          {
            cep: `${endereco.cep.slice(0, 5)}-${endereco.cep.slice(5)}`,
            numero: endereco.numero,
            complemento: endereco.complemento ?? '',
            logradouro: endereco.logradouro,
            bairro: endereco.bairro,
            cidade: endereco.cidade,
            estado: endereco.estado,
          },
          { emitEvent: false },
        );
      },
      error: () => this.router.navigate(['/dashboard']),
    });
  }

  protected salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const valores = this.form.getRawValue();
    const dados: EnderecoRequest = {
      ...valores,
      cep: apenasDigitos(valores.cep),
      complemento: valores.complemento || undefined,
    };
    const idAtual = this.idEndereco();
    const dono = this.userId();
    if (!idAtual && !dono) return;

    this.salvando.set(true);
    const chamada = idAtual
      ? this.addressService.atualizar(idAtual, dados)
      : this.addressService.criar(dono!, dados);

    chamada.subscribe({
      next: () => {
        this.toast.sucesso(idAtual ? 'Endereço atualizado.' : 'Endereço cadastrado.');
        this.router.navigate(['/dashboard'], { queryParams: this.voltarParams() });
      },
      error: (erro) => {
        aplicarErrosDoServidor(this.form, erro);
        this.salvando.set(false);
      },
    });
  }
}
