package sptech.school.Lodgfy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sptech.school.Lodgfy.business.ReservaService;
import sptech.school.Lodgfy.business.dto.ReservaRequestDTO;
import sptech.school.Lodgfy.business.dto.ReservaResponseDTO;
import sptech.school.Lodgfy.business.dto.StatusReserva;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reservas")
@Tag(name = "Reservas", description = "Gerenciamento de reservas")
public class ReservaController {

    private final ReservaService service;

    @Operation(summary = "Lista todas as reservas", description = "Retorna uma lista com todas as reservas cadastradas")
    @ApiResponse(responseCode = "200", description = "Lista de reservas encontrada com sucesso")
    @GetMapping
    public ResponseEntity<List<ReservaResponseDTO>> listarReservas() {
        return ResponseEntity.ok(service.listarReservas());
    }

    @Operation(summary = "Cadastra nova reserva", description = "Cria um novo registro de reserva")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reserva cadastrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Hóspede ou chalé não encontrado"),
            @ApiResponse(responseCode = "409", description = "Conflito de reserva - período já reservado")
    })
    @PostMapping
    public ResponseEntity<ReservaResponseDTO> criarReserva(@Valid @RequestBody ReservaRequestDTO dto) {
        ReservaResponseDTO reservaResponse = service.criarReserva(dto);
        return ResponseEntity.status(201).body(reservaResponse);
    }

    @Operation(summary = "Busca reserva por ID", description = "Retorna uma reserva baseada no ID informado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reserva encontrada"),
            @ApiResponse(responseCode = "404", description = "Reserva não encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponseDTO> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Atualiza reserva", description = "Atualiza os dados de uma reserva existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reserva atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Reserva não encontrada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Conflito de reserva")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ReservaResponseDTO> atualizarReserva(
            @PathVariable Long id,
            @Valid @RequestBody ReservaRequestDTO dto) {
        return service.atualizarReserva(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Cancela reserva", description = "Cancela uma reserva existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Reserva cancelada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Reserva não encontrada"),
            @ApiResponse(responseCode = "400", description = "Não é possível cancelar esta reserva")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarReserva(@PathVariable Long id) {
        service.cancelarReserva(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Lista reservas por hóspede", description = "Retorna todas as reservas de um hóspede específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de reservas encontrada"),
            @ApiResponse(responseCode = "404", description = "Hóspede não encontrado")
    })
    @GetMapping("/hospede/{hospedeId}")
    public ResponseEntity<List<ReservaResponseDTO>> listarReservasPorHospede(@PathVariable Long hospedeId) {
        return ResponseEntity.ok(service.listarReservasPorHospede(hospedeId));
    }

    @Operation(summary = "Lista reservas por chalé", description = "Retorna todas as reservas de um chalé específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de reservas encontrada"),
            @ApiResponse(responseCode = "404", description = "Chalé não encontrado")
    })
    @GetMapping("/chale/{chaleId}")
    public ResponseEntity<List<ReservaResponseDTO>> listarReservasPorChale(@PathVariable Long chaleId) {
        return ResponseEntity.ok(service.listarReservasPorChale(chaleId));
    }

    @Operation(summary = "Lista reservas por status", description = "Retorna todas as reservas com um status específico")
    @ApiResponse(responseCode = "200", description = "Lista de reservas encontrada")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReservaResponseDTO>> listarReservasPorStatus(@PathVariable StatusReserva status) {
        return ResponseEntity.ok(service.listarReservasPorStatus(status));
    }
}

