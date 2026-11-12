import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';

import { ApiError } from '../../models/api-error.model';
import { ToastService } from '../../shared/toast/toast.service';
import { AuthService } from '../services/auth.service';

/**
 * Esse interceptor serve para tratar os erros da API em um lugar só: mostra um toast com a mensagem
 * e, se o token não vale mais (401), faz logout e manda a pessoa para o login.
 * O erro continua sendo repassado, para cada tela poder reagir também (ex.: marcar o campo inválido).
 */
export const errorInterceptor: HttpInterceptorFn = (requisicao, proximo) => {
  const toast = inject(ToastService);
  const auth = inject(AuthService);

  return proximo(requisicao).pipe(
    catchError((erro: HttpErrorResponse) => {
      const rotaDeLogin = requisicao.url.includes('/auth/');

      if (erro.status === 0) {
        toast.erro('Não foi possível conectar ao servidor. Verifique se a API está no ar.');
      } else if (erro.status === 401 && !rotaDeLogin) {
        // Token vencido ou inválido: a sessão acabou.
        toast.erro('Sua sessão expirou. Entre novamente.');
        auth.logout();
      } else {
        const corpo = erro.error as ApiError | null;
        toast.erro(corpo?.mensagem ?? 'Ocorreu um erro inesperado. Tente novamente.');
      }

      return throwError(() => erro);
    }),
  );
};
