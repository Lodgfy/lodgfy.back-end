package sptech.school.Lodgfy.business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ChaleIndisponivelException extends RuntimeException {
    public ChaleIndisponivelException(String message) {
        super(message);
    }
}


