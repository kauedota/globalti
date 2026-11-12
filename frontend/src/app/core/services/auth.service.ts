import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

import { AuthResponse, LoginRequest, RegisterRequest } from '../../models/auth.model';
import { Usuario } from '../../models/user.model';
import { API_URL } from '../api.config';

const CHAVE_TOKEN = 'prova.token';
const CHAVE_USUARIO = 'prova.usuario';
const CHAVE_EXPIRACAO = 'prova.expiraEm';

/**
 * Esse serviço serve para cuidar da sessão: login, registro, logout e guardar o token.
 * O token fica no localStorage para a pessoa continuar logada ao recarregar a página.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  readonly usuario = signal<Usuario | null>(this.lerUsuarioSalvo());
  readonly logado = computed(() => this.usuario() !== null);
  readonly admin = computed(() => this.usuario()?.role === 'ADMIN');

  get token(): string | null {
    return localStorage.getItem(CHAVE_TOKEN);
  }

  login(dados: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API_URL}/auth/login`, dados).pipe(
      tap((resposta) => this.salvarSessao(resposta)),
    );
  }

  registrar(dados: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API_URL}/auth/register`, dados).pipe(
      tap((resposta) => this.salvarSessao(resposta)),
    );
  }

  // Limpa tudo e volta para a tela de login. Também é chamado quando a API responde 401.
  logout(): void {
    localStorage.removeItem(CHAVE_TOKEN);
    localStorage.removeItem(CHAVE_USUARIO);
    localStorage.removeItem(CHAVE_EXPIRACAO);
    this.usuario.set(null);
    this.router.navigate(['/login']);
  }

  // Quando o próprio usuário edita o perfil, atualizo o nome que aparece no topo.
  atualizarUsuarioLogado(usuario: Usuario): void {
    localStorage.setItem(CHAVE_USUARIO, JSON.stringify(usuario));
    this.usuario.set(usuario);
  }

  private salvarSessao(resposta: AuthResponse): void {
    const expiraEm = Date.now() + resposta.expiraEmSegundos * 1000;
    localStorage.setItem(CHAVE_TOKEN, resposta.token);
    localStorage.setItem(CHAVE_USUARIO, JSON.stringify(resposta.usuario));
    localStorage.setItem(CHAVE_EXPIRACAO, String(expiraEm));
    this.usuario.set(resposta.usuario);
  }

  // Ao abrir o app, recupero a sessão salva, mas só se o token ainda não venceu.
  private lerUsuarioSalvo(): Usuario | null {
    const token = localStorage.getItem(CHAVE_TOKEN);
    const salvo = localStorage.getItem(CHAVE_USUARIO);
    const expiraEm = Number(localStorage.getItem(CHAVE_EXPIRACAO));

    if (!token || !salvo || Date.now() >= expiraEm) {
      return null;
    }
    try {
      return JSON.parse(salvo) as Usuario;
    } catch {
      return null;
    }
  }
}
