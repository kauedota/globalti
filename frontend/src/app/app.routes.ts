import { Routes } from '@angular/router';

import { adminGuard, proprioOuAdminGuard } from './core/guards/admin.guard';
import { authGuard, visitanteGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [visitanteGuard],
    loadComponent: () => import('./features/auth/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'registro',
    canActivate: [visitanteGuard],
    loadComponent: () => import('./features/auth/register/register.component').then((m) => m.RegisterComponent),
  },
  {
    // Tudo aqui dentro exige login e usa o layout com o menu do topo.
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./features/layout/layout.component').then((m) => m.LayoutComponent),
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent),
      },
      {
        path: 'usuarios/novo',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/users/user-form/user-form.component').then((m) => m.UserFormComponent),
      },
      {
        path: 'usuarios/:id/editar',
        canActivate: [proprioOuAdminGuard],
        loadComponent: () => import('./features/users/user-form/user-form.component').then((m) => m.UserFormComponent),
      },
      {
        path: 'usuarios/:userId/enderecos/novo',
        loadComponent: () =>
          import('./features/addresses/address-form/address-form.component').then((m) => m.AddressFormComponent),
      },
      {
        path: 'enderecos/:id/editar',
        loadComponent: () =>
          import('./features/addresses/address-form/address-form.component').then((m) => m.AddressFormComponent),
      },
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
    ],
  },
  { path: '**', redirectTo: 'dashboard' },
];
