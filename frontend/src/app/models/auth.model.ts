// Essas interfaces servem para o login e o registro.
import { Usuario } from './user.model';

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface RegisterRequest {
  nome: string;
  email: string;
  senha: string;
}

export interface AuthResponse {
  token: string;
  tipo: string;
  expiraEmSegundos: number;
  usuario: Usuario;
}
