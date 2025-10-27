package sptech.school.Lodgfy.business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ReservaNaoEncontradaException extends RuntimeException {
    public ReservaNaoEncontradaException(String message) {
        super(message);
    }
}

