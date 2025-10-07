package sptech.school.Lodgfy.business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ReservaConflitanteException extends ConflictException {
    public ReservaConflitanteException(String mensagem) {
        super("Conflito na reserva: " + mensagem);
    }
}
