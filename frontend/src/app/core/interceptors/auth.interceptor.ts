import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

import { AuthService } from '../services/auth.service';

/**
 * Esse interceptor serve para colocar o token JWT em toda requisição para a API.
 * Login e registro ficam de fora, porque ainda não existe token nessa hora.
 */
export const authInterceptor: HttpInterceptorFn = (requisicao, proximo) => {
  const token = inject(AuthService).token;
  const rotaPublica = requisicao.url.includes('/auth/');

  if (!token || rotaPublica) {
    return proximo(requisicao);
  }

  return proximo(requisicao.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
};
