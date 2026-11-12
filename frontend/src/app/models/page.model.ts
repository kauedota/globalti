// Essas interfaces servem para a paginação e a ordenação das listagens.

export interface Pagina<T> {
  content: T[];
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
}

export type Direcao = 'asc' | 'desc';

export interface Ordenacao {
  campo: string;
  direcao: Direcao;
}

// Monta o parâmetro que o back espera, ex.: "nome,asc".
export function paraParametroSort(ordem: Ordenacao): string {
  return `${ordem.campo},${ordem.direcao}`;
}
