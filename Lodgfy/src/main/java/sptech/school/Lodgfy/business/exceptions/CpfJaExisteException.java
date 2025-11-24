package sptech.school.Lodgfy.business.exceptions;

public class CpfJaExisteException extends RuntimeException {
    public CpfJaExisteException() {
        super("CPF já está em uso");
    }
}

