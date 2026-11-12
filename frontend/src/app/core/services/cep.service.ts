import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Cep } from '../../models/address.model';
import { API_URL } from '../api.config';

/**
 * Esse serviço serve para consultar um CEP na API (que por sua vez consulta o ViaCEP).
 */
@Injectable({ providedIn: 'root' })
export class CepService {
  private readonly http = inject(HttpClient);

  consultar(cep: string): Observable<Cep> {
    return this.http.get<Cep>(`${API_URL}/cep/${cep}`);
  }
}
