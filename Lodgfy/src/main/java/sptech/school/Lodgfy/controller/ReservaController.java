package sptech.school.Lodgfy.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sptech.school.Lodgfy.business.ReservaService;
import sptech.school.Lodgfy.business.dto.ReservaRequestDTO;
import sptech.school.Lodgfy.business.dto.ReservaResponseDTO;
import sptech.school.Lodgfy.business.dto.StatusReserva;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;

    @PostMapping
    public ResponseEntity<ReservaResponseDTO> criarReserva(@Valid @RequestBody ReservaRequestDTO request) {
        ReservaResponseDTO reserva = reservaService.criarReserva(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reserva);
    }

    @GetMapping
    public ResponseEntity<List<ReservaResponseDTO>> listarReservas() {
        List<ReservaResponseDTO> reservas = reservaService.listarReservas();
        return ResponseEntity.ok(reservas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponseDTO> buscarPorId(@PathVariable Long id) {
        return reservaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/hospede/{hospedeId}")
    public ResponseEntity<List<ReservaResponseDTO>> buscarPorHospede(@PathVariable Long hospedeId) {
        List<ReservaResponseDTO> reservas = reservaService.buscarPorHospede(hospedeId);
        return ResponseEntity.ok(reservas);
    }

    @GetMapping("/chale/{chaleId}")
    public ResponseEntity<List<ReservaResponseDTO>> buscarPorChale(@PathVariable Long chaleId) {
        List<ReservaResponseDTO> reservas = reservaService.buscarPorChale(chaleId);
        return ResponseEntity.ok(reservas);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReservaResponseDTO>> buscarPorStatus(@PathVariable StatusReserva status) {
        List<ReservaResponseDTO> reservas = reservaService.buscarPorStatus(status);
        return ResponseEntity.ok(reservas);
    }

    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<ReservaResponseDTO> confirmarReserva(@PathVariable Long id) {
        ReservaResponseDTO reserva = reservaService.confirmarReserva(id);
        return ResponseEntity.ok(reserva);
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<ReservaResponseDTO> cancelarReserva(@PathVariable Long id) {
        ReservaResponseDTO reserva = reservaService.cancelarReserva(id);
        return ResponseEntity.ok(reserva);
    }

    @PatchMapping("/{id}/concluir")
    public ResponseEntity<ReservaResponseDTO> concluirReserva(@PathVariable Long id) {
        ReservaResponseDTO reserva = reservaService.concluirReserva(id);
        return ResponseEntity.ok(reserva);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarReserva(@PathVariable Long id) {
        reservaService.deletarReserva(id);
        return ResponseEntity.noContent().build();
    }
}

