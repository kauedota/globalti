import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { finalize } from 'rxjs';

import { LoaderService } from '../../shared/loader/loader.service';

/**
 * Esse interceptor serve para ligar o loader quando uma requisição começa e desligar quando termina
 * (dando certo ou errado).
 */
export const loaderInterceptor: HttpInterceptorFn = (requisicao, proximo) => {
  const loader = inject(LoaderService);
  loader.iniciar();
  return proximo(requisicao).pipe(finalize(() => loader.finalizar()));
};
