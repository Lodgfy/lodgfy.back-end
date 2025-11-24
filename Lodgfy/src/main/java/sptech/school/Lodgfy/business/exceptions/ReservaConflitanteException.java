package sptech.school.Lodgfy.business.exceptions;

public class ReservaConflitanteException extends RuntimeException {
    public ReservaConflitanteException() {
        super("Já existe uma reserva confirmada para este chalé no período solicitado");
    }
}

