package sptech.school.Lodgfy.business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DataReservaInvalidaException extends RuntimeException {
    public DataReservaInvalidaException(String message) {
        super(message);
    }
}

