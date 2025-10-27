package sptech.school.Lodgfy.business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ReservaConflitanteException extends RuntimeException {
    public ReservaConflitanteException(String message) {
        super(message);
    }
}

