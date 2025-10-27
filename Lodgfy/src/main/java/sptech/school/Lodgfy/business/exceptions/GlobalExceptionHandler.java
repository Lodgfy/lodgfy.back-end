package sptech.school.Lodgfy.business.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailJaExisteException.class)
    public ResponseEntity<Map<String, Object>> handleEmailJaExiste(EmailJaExisteException ex) {
        log.error("Email já existe: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(createErrorResponse(ex.getMessage(), HttpStatus.CONFLICT));
    }

    @ExceptionHandler(CpfJaExisteException.class)
    public ResponseEntity<Map<String, Object>> handleCpfJaExiste(CpfJaExisteException ex) {
        log.error("CPF já existe: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(createErrorResponse(ex.getMessage(), HttpStatus.CONFLICT));
    }

    @ExceptionHandler(SenhaIncorretaException.class)
    public ResponseEntity<Map<String, Object>> handleSenhaIncorreta(SenhaIncorretaException ex) {
        log.warn("Tentativa de login com senha incorreta: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED));
    }

    @ExceptionHandler(CpfNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleCpfNaoEncontrado(CpfNaoEncontradoException ex) {
        log.warn("CPF não encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(ReservaNaoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> handleReservaNaoEncontrada(ReservaNaoEncontradaException ex) {
        log.warn("Reserva não encontrada: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(ChaleIndisponivelException.class)
    public ResponseEntity<Map<String, Object>> handleChaleIndisponivel(ChaleIndisponivelException ex) {
        log.warn("Chalé indisponível: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(HospedeNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleHospedeNaoEncontrado(HospedeNaoEncontradoException ex) {
        log.warn("Hóspede não encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(DataReservaInvalidaException.class)
    public ResponseEntity<Map<String, Object>> handleDataReservaInvalida(DataReservaInvalidaException ex) {
        log.warn("Data de reserva inválida: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(ReservaConflitanteException.class)
    public ResponseEntity<Map<String, Object>> handleReservaConflitante(ReservaConflitanteException ex) {
        log.warn("Conflito de reserva: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(createErrorResponse(ex.getMessage(), HttpStatus.CONFLICT));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Argumento inválido: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        log.error("Erro interno: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST));
    }

    private Map<String, Object> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        return errorResponse;
    }
}
