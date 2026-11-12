import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Ordenacao, Pagina, paraParametroSort } from '../../models/page.model';
import { Usuario, UsuarioRequest } from '../../models/user.model';
import { API_URL } from '../api.config';

/**
 * Esse serviço serve para chamar a API de usuários. Só faz HTTP; nenhuma regra fica aqui.
 */
@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_URL}/users`;

  listar(pagina: number, tamanho: number, ordem: Ordenacao): Observable<Pagina<Usuario>> {
    const params = new HttpParams()
      .set('page', pagina)
      .set('size', tamanho)
      .set('sort', paraParametroSort(ordem));
    return this.http.get<Pagina<Usuario>>(this.url, { params });
  }

  buscar(id: number): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.url}/${id}`);
  }

  criar(dados: UsuarioRequest): Observable<Usuario> {
    return this.http.post<Usuario>(this.url, dados);
  }

  atualizar(id: number, dados: UsuarioRequest): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.url}/${id}`, dados);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
