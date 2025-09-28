package sptech.school.Lodgfy.business.exceptions;

public class CpfNaoEncontradoException extends RuntimeException {
    public CpfNaoEncontradoException() {
        super("CPF não encontrado");
    }
}

