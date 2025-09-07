package sptech.school.Lodgfy.business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ChaleJaExisteException extends RuntimeException {
    public ChaleJaExisteException(String message) {
        super(message);
    }
}
