// Essas interfaces servem para descrever os dados de usuário que a API devolve e recebe.

export type Role = 'USER' | 'ADMIN';

export interface Usuario {
  id: number;
  nome: string;
  email: string;
  role: Role;
  criadoEm: string;
}

// Dados do formulário de usuário. A senha é opcional na edição.
export interface UsuarioRequest {
  nome: string;
  email: string;
  senha?: string;
  role?: Role;
}
