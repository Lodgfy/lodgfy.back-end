package sptech.school.Lodgfy.business.exceptions;

public class ChaleIndisponivelException extends RuntimeException {
    public ChaleIndisponivelException() {
        super("Chalé não está disponível para reserva");
    }
}

