// Essa interface serve para descrever o JSON de erro padronizado que o back devolve.

export interface CampoInvalido {
  campo: string;
  mensagem: string;
}

export interface ApiError {
  timestamp: string;
  status: number;
  erro: string;
  mensagem: string;
  caminho: string;
  campos?: CampoInvalido[];
}
