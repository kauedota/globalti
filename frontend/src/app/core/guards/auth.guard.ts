import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from '../services/auth.service';

/**
 * Esse guard serve para bloquear as telas internas de quem não está logado.
 */
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return auth.logado() ? true : router.createUrlTree(['/login']);
};

/**
 * Esse guard serve para o contrário: quem já está logado não precisa ver login/registro.
 */
export const visitanteGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return auth.logado() ? router.createUrlTree(['/dashboard']) : true;
};
