package sptech.school.Lodgfy.business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class HospedeJaExisteException extends ConflictException {
    public HospedeJaExisteException(String cpf) {
        super("Já existe um hóspede com o CPF: " + cpf);
    }
}
