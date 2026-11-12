// Essas interfaces servem para descrever os dados de endereço e de consulta de CEP.

export interface Endereco {
  id: number;
  logradouro: string;
  numero: string;
  complemento: string | null;
  bairro: string;
  cidade: string;
  estado: string;
  cep: string;
  userId: number;
}

export interface EnderecoRequest {
  cep: string;
  numero: string;
  complemento?: string;
  logradouro?: string;
  bairro?: string;
  cidade?: string;
  estado?: string;
}

export interface Cep {
  cep: string;
  logradouro: string;
  complemento: string;
  bairro: string;
  cidade: string;
  estado: string;
}
