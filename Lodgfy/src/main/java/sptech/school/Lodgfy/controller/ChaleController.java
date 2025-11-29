package sptech.school.Lodgfy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sptech.school.Lodgfy.business.ChaleService;
import sptech.school.Lodgfy.business.dto.ChaleDisponibilidadeRequestDTO;
import sptech.school.Lodgfy.business.dto.ChaleRequestDTO;
import sptech.school.Lodgfy.business.dto.ChaleResponseDTO;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chales")
@Tag(name = "Chalés", description = "Gerenciamento de chalés")
public class ChaleController {

    private final ChaleService service;

    @Operation(summary = "Lista todos os chalés", description = "Retorna uma lista com todos os chalés cadastrados")
    @ApiResponse(responseCode = "200", description = "Lista de chalés encontrada com sucesso")
    @GetMapping
    public ResponseEntity<List<ChaleResponseDTO>> getAllChales() {
        return ResponseEntity.ok(service.listarChales());
    }

    @Operation(summary = "Cadastra novo chalé", description = "Cria um novo registro de chalé")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Chalé cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<ChaleResponseDTO> createChale(@Valid @RequestBody ChaleRequestDTO dto) {
        ChaleResponseDTO chaleResponse = service.salvarChale(dto);
        return chaleResponse != null ? ResponseEntity.status(201).body(chaleResponse)
                : ResponseEntity.status(400).build();
    }

    @Operation(summary = "Busca chalé por ID", description = "Retorna um chalé baseado no ID informado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chalé encontrado"),
            @ApiResponse(responseCode = "404", description = "Chalé não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ChaleResponseDTO> getById(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Atualiza um chalé", description = "Atualiza os dados de um chalé existente pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chalé atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Chalé não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ChaleResponseDTO> updateChale(@PathVariable Long id, @Valid @RequestBody ChaleRequestDTO chaleAtualizado) {
        return service.atualizarChale(id, chaleAtualizado)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Remove um chalé", description = "Exclui o cadastro de um chalé pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Chalé removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Chalé não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        service.deletarChalePorId(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Busca chalés por preço máximo", description = "Retorna uma lista de chalés com diária menor ou igual ao valor informado")
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso")
    @GetMapping("/preco-maximo")
    public ResponseEntity<List<ChaleResponseDTO>> getByPrecoMaximo(@RequestParam BigDecimal precoMaximo) {
        List<ChaleResponseDTO> chales = service.buscarPorPrecoMaximo(precoMaximo);
        return ResponseEntity.ok(chales);
    }

    @Operation(summary = "Busca chalés por nome ou numero", description = "Retorna uma lista de chalés com nome ou numero igual ao informado")
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso")
    @ApiResponse(responseCode = "204", description = "Busca realizada com sucesso, porém sem conteúdo")
    @GetMapping("/buscar")
    public ResponseEntity<List<ChaleResponseDTO>> getByNameOrNumber(@RequestParam String nome, String numero) {
        List<ChaleResponseDTO> chales = service.buscarPorNomeOuNumero(nome, numero);

        return chales.isEmpty() ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(chales);
    }

    @Operation(summary = "Busca chalés disponíveis", description = "Retorna uma lista de chalés disponíveis para o período e quantidade de pessoas informados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "204", description = "Nenhum chalé disponível encontrado")
    })
    @PostMapping("/disponiveis")
    public ResponseEntity<List<ChaleResponseDTO>> buscarChalesDisponiveis(
            @Valid @RequestBody ChaleDisponibilidadeRequestDTO request) {
        List<ChaleResponseDTO> chalesDisponiveis = service.buscarChalesDisponiveis(request);

        return chalesDisponiveis.isEmpty() ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(chalesDisponiveis);
    }
}