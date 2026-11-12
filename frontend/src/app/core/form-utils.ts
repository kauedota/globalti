import { HttpErrorResponse } from '@angular/common/http';
import { FormGroup } from '@angular/forms';

import { ApiError } from '../models/api-error.model';

// Quando a API devolve 400 com a lista de campos inválidos, marco cada campo do formulário
// com a mensagem do servidor. Assim o erro aparece embaixo do campo certo.
export function aplicarErrosDoServidor(form: FormGroup, erro: HttpErrorResponse): void {
  const corpo = erro.error as ApiError | null;

  for (const campo of corpo?.campos ?? []) {
    const controle = form.get(campo.campo);
    if (controle) {
      controle.setErrors({ servidor: campo.mensagem });
      controle.markAsTouched();
    }
  }
}
