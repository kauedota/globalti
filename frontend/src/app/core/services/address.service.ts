import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Endereco, EnderecoRequest } from '../../models/address.model';
import { Ordenacao, Pagina, paraParametroSort } from '../../models/page.model';
import { API_URL } from '../api.config';

/**
 * Esse serviço serve para chamar a API de endereços. Só faz HTTP; nenhuma regra fica aqui.
 */
@Injectable({ providedIn: 'root' })
export class AddressService {
  private readonly http = inject(HttpClient);

  listarDoUsuario(userId: number, pagina: number, tamanho: number, ordem: Ordenacao): Observable<Pagina<Endereco>> {
    const params = new HttpParams()
      .set('page', pagina)
      .set('size', tamanho)
      .set('sort', paraParametroSort(ordem));
    return this.http.get<Pagina<Endereco>>(`${API_URL}/users/${userId}/addresses`, { params });
  }

  buscar(id: number): Observable<Endereco> {
    return this.http.get<Endereco>(`${API_URL}/addresses/${id}`);
  }

  criar(userId: number, dados: EnderecoRequest): Observable<Endereco> {
    return this.http.post<Endereco>(`${API_URL}/users/${userId}/addresses`, dados);
  }

  atualizar(id: number, dados: EnderecoRequest): Observable<Endereco> {
    return this.http.put<Endereco>(`${API_URL}/addresses/${id}`, dados);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/addresses/${id}`);
  }
}
