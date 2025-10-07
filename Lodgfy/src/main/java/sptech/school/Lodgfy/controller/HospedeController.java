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
import sptech.school.Lodgfy.business.dto.LoginRequestDTO;
import sptech.school.Lodgfy.business.dto.LoginResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sptech.school.Lodgfy.business.dto.LoginRequestDTO;
import sptech.school.Lodgfy.business.exceptions.ResourceNotFoundException;
import sptech.school.Lodgfy.security.enums.Role;

import java.util.List;

@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/hospedes")
@Tag(name = "Hospedes", description = "Gerenciamento de hóspedes")
public class HospedeController {

    private final HospedeService service;


    @Operation(summary = "Lista todos os hóspedes", description = "Retorna uma lista com todos os hóspedes cadastrados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de hóspedes encontrada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Nenhum hóspede encontrado")
    })
    @GetMapping
    public ResponseEntity<List<HospedeResponseDTO>> getAllHospedes() {
        return ResponseEntity.ok(service.listarHospedes());
    }

    @Operation(summary = "Busca hóspede por CPF", description = "Retorna um hóspede baseado no CPF informado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hóspede encontrado"),
            @ApiResponse(responseCode = "400", description = "CPF inválido ou não informado"),
            @ApiResponse(responseCode = "404", description = "Hóspede não encontrado")
    })
    @GetMapping("/{cpf}")
    public ResponseEntity<HospedeResponseDTO> getByCpf(@PathVariable String cpf) {
        return service.buscarPorCpf(cpf)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Hospede nao encontrado pelo cpf: " + cpf));
    }

    @Operation(summary = "Cadastra novo hóspede", description = "Cria um novo registro de hóspede")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Hóspede cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados obrigatórios não informados, CPF inválido ou email já em uso"),
            @ApiResponse(responseCode = "409", description = "CPF já cadastrado")
    })
    @PostMapping
    public ResponseEntity<HospedeResponseDTO> createHospede(@Valid @RequestBody HospedeRequestDTO dto) {
        // Garante que todo cadastro será sempre HOSPEDE
         dto.setRole(Role.HOSPEDE);
        HospedeResponseDTO hospedeResponse = service.salvarHospede(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(hospedeResponse);
    }

    @Operation(summary = "Remove um hóspede", description = "Exclui o cadastro de um hóspede pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Hóspede removido com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
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
            @ApiResponse(responseCode = "400", description = "ID inválido, dados obrigatórios não informados, CPF inválido ou email já em uso"),
            @ApiResponse(responseCode = "404", description = "Hóspede não encontrado"),
            @ApiResponse(responseCode = "409", description = "CPF já cadastrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<HospedeResponseDTO> updateHospede(@PathVariable Long id, @Valid @RequestBody HospedeRequestDTO hospedeAtualizado) {
        return service.atualizarHospede(id, hospedeAtualizado)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Hospede nao encontrado pelo id: " + id));
    }

    @Operation(summary = "Busca hóspedes por nome", description = "Retorna uma lista de hóspedes que contém o nome informado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Nome inválido ou muito curto"),
            @ApiResponse(responseCode = "404", description = "Nenhum hóspede encontrado com o nome informado")
    })
    @GetMapping("/nome/{nome}")
    public ResponseEntity<List<HospedeResponseDTO>> getByNome(@PathVariable String nome) {
        return ResponseEntity.ok(service.buscarPorNome(nome));
    }

    @Operation(summary = "Login do hóspede", description = "Realiza login com CPF e senha")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "CPF ou senha não informados ou CPF inválido"),
            @ApiResponse(responseCode = "401", description = "CPF ou senha inválidos")
    @Operation(summary = "Login de hóspede", description = "Autentica um hóspede e retorna token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            LoginResponseDTO response = service.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}