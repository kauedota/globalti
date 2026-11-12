import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { ToastService } from '../../shared/toast/toast.service';
import { AuthService } from '../services/auth.service';

/**
 * Esse guard serve para bloquear as rotas que só o ADMIN pode abrir.
 * Quem não é admin volta para o dashboard com um aviso.
 * (A API também bloqueia; esse guard é só para a pessoa não ver uma tela que não pode usar.)
 */
export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const toast = inject(ToastService);

  if (auth.admin()) {
    return true;
  }
  toast.erro('Essa área é restrita a administradores.');
  return router.createUrlTree(['/dashboard']);
};

/**
 * Esse guard serve para a edição de usuário: o ADMIN edita qualquer um,
 * o usuário comum só edita o próprio cadastro.
 */
export const proprioOuAdminGuard: CanActivateFn = (rota) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const toast = inject(ToastService);

  const id = Number(rota.paramMap.get('id'));
  if (auth.admin() || auth.usuario()?.id === id) {
    return true;
  }
  toast.erro('Você só pode editar o seu próprio cadastro.');
  return router.createUrlTree(['/dashboard']);
};
