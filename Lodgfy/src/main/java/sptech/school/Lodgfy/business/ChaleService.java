package sptech.school.Lodgfy.business;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sptech.school.Lodgfy.business.dto.ChaleRequestDTO;
import sptech.school.Lodgfy.business.dto.ChaleResponseDTO;
import sptech.school.Lodgfy.business.exceptions.ChaleJaExisteException;
import sptech.school.Lodgfy.business.mapsstruct.ChaleMapper;
import sptech.school.Lodgfy.business.observer.ChaleManager;
import sptech.school.Lodgfy.business.observer.ChaleObserver;
import sptech.school.Lodgfy.infrastructure.entities.ChaleEntity;
import sptech.school.Lodgfy.infrastructure.repository.ChaleRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChaleService {

    private final ChaleRepository repository;
    private final ChaleMapper mapper;
    private final ChaleManager chaleManager; // Observer pattern

    public ChaleResponseDTO salvarChale(ChaleRequestDTO request) {
        if (repository.existsByNumero(request.getNumero())) {
            throw new ChaleJaExisteException("Já existe um chalé cadastrado com este número");
        }

        ChaleEntity chaleSalvo = repository.save(mapper.paraChaleEntity(request));

        // Notifica observers (padrão clássico)
        chaleManager.notificar(chaleSalvo, ChaleObserver.ChaleEventType.CRIADO);

        return mapper.paraChaleResponseDTO(chaleSalvo);
    }


    public List<ChaleResponseDTO> listarChales() {
        return mapper.paraListaChaleResponseDTO(
                repository.findAll());
    }

    public Optional<ChaleResponseDTO> buscarPorId(Long id) {
        return repository.findById(id)
                .map(mapper::paraChaleResponseDTO);
    }

    public void deletarChalePorId(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Chalé não encontrado");
        }

        // Busca o chalé antes de deletar para notificar
        Optional<ChaleEntity> chaleOpt = repository.findById(id);

        repository.deleteById(id);

        // Notifica sobre a remoção
        chaleOpt.ifPresent(chale -> {
            chaleManager.notificar(chale, ChaleObserver.ChaleEventType.REMOVIDO);
        });
    }

    public Optional<ChaleResponseDTO> atualizarChale(Long id, ChaleRequestDTO chaleAtualizado) {
        return repository.findById(id)
                .map(chale -> {
                    if (!chale.getNumero().equals(chaleAtualizado.getNumero()) &&
                            repository.existsByNumero(chaleAtualizado.getNumero())) {
                        throw new RuntimeException("Número de chalé já está em uso");
                    }

                    // Salva estado anterior para comparação
                    ChaleEntity chaleAnterior = copiarChale(chale);

                    // Atualiza campos
                    chale.setNome(chaleAtualizado.getNome());
                    chale.setNumero(chaleAtualizado.getNumero());
                    chale.setTipo(chaleAtualizado.getTipo());
                    chale.setValorDiaria(chaleAtualizado.getValorDiaria());
                    chale.setDisponivel(chaleAtualizado.getDisponivel());
                    chale.setCapacidade(chaleAtualizado.getCapacidade());
                    chale.setDescricao(chaleAtualizado.getDescricao());
                    chale.setStatus(chaleAtualizado.getStatus());

                    ChaleEntity chaleSalvo = repository.save(chale);

                    // Detecta tipo específico de mudança
                    ChaleObserver.ChaleEventType tipoEvento = detectarTipoMudanca(chaleAnterior, chaleSalvo);

                    // Notifica mudanças
                    chaleManager.notificar(chaleSalvo, tipoEvento);

                    return mapper.paraChaleResponseDTO(chaleSalvo);
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


    public Optional<ChaleResponseDTO> atualizarStatus(Long id, ChaleEntity.StatusChale novoStatus) {
        return repository.findById(id)
                .map(chale -> {
                    chale.setStatus(novoStatus);
                    ChaleEntity chaleSalvo = repository.save(chale);

                    // Notifica mudança específica de status
                    chaleManager.notificar(chaleSalvo, ChaleObserver.ChaleEventType.STATUS_ALTERADO);

                    return mapper.paraChaleResponseDTO(chaleSalvo);
                });
    }

    public Optional<ChaleResponseDTO> atualizarDisponibilidade(Long id, Boolean disponivel) {
        return repository.findById(id)
                .map(chale -> {
                    chale.setDisponivel(disponivel);
                    ChaleEntity chaleSalvo = repository.save(chale);

                    chaleManager.notificar(chaleSalvo, ChaleObserver.ChaleEventType.DISPONIBILIDADE_ALTERADA);

                    return mapper.paraChaleResponseDTO(chaleSalvo);
                });
    }


    private ChaleObserver.ChaleEventType detectarTipoMudanca(ChaleEntity anterior, ChaleEntity atual) {
        if (!anterior.getStatus().equals(atual.getStatus())) {
            return ChaleObserver.ChaleEventType.STATUS_ALTERADO;
        }
        if (!anterior.getDisponivel().equals(atual.getDisponivel())) {
            return ChaleObserver.ChaleEventType.DISPONIBILIDADE_ALTERADA;
        }
        if (anterior.getValorDiaria() != null &&
            !anterior.getValorDiaria().equals(atual.getValorDiaria())) {
            return ChaleObserver.ChaleEventType.PRECO_ALTERADO;
        }
        return ChaleObserver.ChaleEventType.ATUALIZADO;
    }

    /**
     * Cria uma cópia do chalé para preservar estado anterior
     */
    private ChaleEntity copiarChale(ChaleEntity chale) {
        ChaleEntity copia = new ChaleEntity();
        copia.setIdChale(chale.getIdChale());
        copia.setNome(chale.getNome());
        copia.setNumero(chale.getNumero());
        copia.setTipo(chale.getTipo());
        copia.setValorDiaria(chale.getValorDiaria());
        copia.setDisponivel(chale.getDisponivel());
        copia.setCapacidade(chale.getCapacidade());
        copia.setDescricao(chale.getDescricao());
        copia.setStatus(chale.getStatus());
        return copia;
    }
}
