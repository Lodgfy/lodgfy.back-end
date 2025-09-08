package sptech.school.Lodgfy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import sptech.school.Lodgfy.business.HospedeService;
import sptech.school.Lodgfy.business.dto.HospedeRequestDTO;
import sptech.school.Lodgfy.business.dto.HospedeResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sptech.school.Lodgfy.business.dto.LoginRequestDTO;

import java.util.List;

@CrossOrigin(origins = "*") // ou "*" só para testes
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/hospedes")
@Tag(name = "Hospedes", description = "Gerenciamento de hóspedes")
public class HospedeController {

    private final HospedeService service;

    @Operation(summary = "Lista todos os hóspedes", description = "Retorna uma lista com todos os hóspedes cadastrados")
    @ApiResponse(responseCode = "200", description = "Lista de hóspedes encontrada com sucesso")
    @GetMapping
    public ResponseEntity<List<HospedeResponseDTO>> getAllHospedes() {
        return ResponseEntity.ok(service.listarHospedes());
    }

    @Operation(summary = "Busca hóspede por CPF", description = "Retorna um hóspede baseado no CPF informado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hóspede encontrado"),
            @ApiResponse(responseCode = "404", description = "Hóspede não encontrado")
    })
    @GetMapping("/{cpf}")
    public ResponseEntity<HospedeResponseDTO> getByCpf(@PathVariable String cpf) {
        return service.buscarPorCpf(cpf)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Cadastra novo hóspede", description = "Cria um novo registro de hóspede")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Hóspede cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<HospedeResponseDTO> createHospede(@Valid @RequestBody HospedeRequestDTO dto) {
        HospedeResponseDTO hospedeResponse = service.salvarHospede(dto);
        return ResponseEntity.status(201).body(hospedeResponse);
    }

    @Operation(summary = "Remove um hóspede", description = "Exclui o cadastro de um hóspede pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Hóspede removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Hóspede não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        service.deletarHospedePorId(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualiza um hóspede", description = "Atualiza os dados de um hóspede existente pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hóspede atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Hóspede não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<HospedeResponseDTO> updateHospede(@PathVariable Long id, @Valid @RequestBody HospedeRequestDTO hospedeAtualizado) {
        return service.atualizarHospede(id, hospedeAtualizado)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Busca hóspedes por nome", description = "Retorna uma lista de hóspedes que contém o nome informado")
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso")
    @GetMapping("/nome/{nome}")
    public ResponseEntity<List<HospedeResponseDTO>> getByNome(@PathVariable String nome) {
        return ResponseEntity.ok(service.buscarPorNome(nome));
    }

    @PostMapping("/login")
    public ResponseEntity<HospedeResponseDTO> login(@RequestBody LoginRequestDTO dto) {
        return service.login(dto.getCpf(), dto.getSenha())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}