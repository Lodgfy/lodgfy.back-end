package sptech.school.Lodgfy.business.exceptions;

public class EmailJaExisteException extends RuntimeException {
    public EmailJaExisteException() {
        super("Email já está em uso");
    }
}

