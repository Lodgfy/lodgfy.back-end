package sptech.school.Lodgfy.business;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sptech.school.Lodgfy.business.dto.ChaleRequestDTO;
import sptech.school.Lodgfy.business.dto.ChaleResponseDTO;
import sptech.school.Lodgfy.business.exceptions.ChaleJaExisteException;
import sptech.school.Lodgfy.business.exceptions.ResourceNotFoundException;
import sptech.school.Lodgfy.business.mapsstruct.ChaleMapper;
import sptech.school.Lodgfy.infrastructure.repository.ChaleRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChaleService {

    private final ChaleRepository repository;
    private final ChaleMapper mapper;

    public ChaleResponseDTO salvarChale(ChaleRequestDTO request) {
        if (repository.existsByNumero(request.getNumero())) {
            throw new ChaleJaExisteException(request.getNumero());
        }

        return mapper.paraChaleResponseDTO(
                repository.save(mapper.paraChaleEntity(request)));
    }


    public List<ChaleResponseDTO> listarChales() {
        return mapper.paraListaChaleResponseDTO(
                repository.findAll());
    }

    public Optional<ChaleResponseDTO> buscarPorId(Long id) {
        return Optional.ofNullable(repository.findById(id)
                .map(mapper::paraChaleResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Chalé não encontrado com id: " + id)));
    }

    public void deletarChalePorId(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Chalé não encontrado com id: " + id);
        }
        repository.deleteById(id);
    }

    public Optional<ChaleResponseDTO> atualizarChale(Long id, ChaleRequestDTO chaleAtualizado) {
        if (!chaleAtualizado.getNumero().equals(chaleAtualizado.getNumero()) &&
                repository.existsByNumero(chaleAtualizado.getNumero())) {
            throw new ChaleJaExisteException("Número de chalé já está em uso");
        }

        return repository.findById(id)
                .map(chale -> {
                    if (!chale.getNumero().equals(chaleAtualizado.getNumero()) &&
                            repository.existsByNumero(chaleAtualizado.getNumero())) {
                        throw new ChaleJaExisteException(chaleAtualizado.getNumero());
                    }

                    chale.setNome(chaleAtualizado.getNome());
                    chale.setNumero(chaleAtualizado.getNumero());
                    chale.setTipo(chaleAtualizado.getTipo());
                    chale.setValorDiaria(chaleAtualizado.getValorDiaria());
                    chale.setDisponivel(chaleAtualizado.getDisponivel());
                    chale.setCapacidade(chaleAtualizado.getCapacidade());
                    chale.setDescricao(chaleAtualizado.getDescricao());
                    chale.setStatus(chaleAtualizado.getStatus());

                    return mapper.paraChaleResponseDTO(repository.save(chale));
                });
    }

    public List<ChaleResponseDTO> buscarPorPrecoMaximo(BigDecimal precoMaximo) {
        return mapper.paraListaChaleResponseDTO(
                repository.findByValorDiariaLessThanEqual(precoMaximo));
    }

    public List<ChaleResponseDTO> buscarPorNomeOuNumero(String nome, String numero) {
        return mapper.paraListaChaleResponseDTO(
                repository.findByNomeContainsIgnoreCaseOrNumeroContainsIgnoreCase(nome, numero));
    }
}