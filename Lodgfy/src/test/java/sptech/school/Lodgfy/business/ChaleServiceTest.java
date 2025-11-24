package sptech.school.Lodgfy.business;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sptech.school.Lodgfy.business.dto.ChaleRequestDTO;
import sptech.school.Lodgfy.business.dto.ChaleResponseDTO;
import sptech.school.Lodgfy.business.exceptions.ChaleJaExisteException;
import sptech.school.Lodgfy.business.mapsstruct.ChaleMapper;
import sptech.school.Lodgfy.business.observer.ChaleManager;
import sptech.school.Lodgfy.business.observer.ChaleObserver;
import sptech.school.Lodgfy.infrastructure.entities.ChaleEntity;
import sptech.school.Lodgfy.infrastructure.repository.ChaleRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChaleService - Testes Unitários")
public class ChaleServiceTest {

    @Mock
    private ChaleRepository repository;

    @Mock
    private ChaleMapper mapper;

    @Mock
    private ChaleManager chaleManager;

    @InjectMocks
    private ChaleService service;

    private ChaleEntity chaleMock;
    private ChaleRequestDTO requestDTO;
    private ChaleResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        // Inicializar ChaleEntity
        chaleMock = new ChaleEntity();
        chaleMock.setIdChale(1L);
        chaleMock.setNome("Chalé das Montanhas");
        chaleMock.setNumero("A101");
        chaleMock.setTipo("Luxo");
        chaleMock.setValorDiaria(BigDecimal.valueOf(350.00));
        chaleMock.setDisponivel(true);
        chaleMock.setCapacidade(4);
        chaleMock.setDescricao("Chalé luxuoso com vista para as montanhas");
        chaleMock.setStatus(ChaleEntity.StatusChale.DISPONIVEL);

        // Inicializar ChaleRequestDTO
        requestDTO = new ChaleRequestDTO();
        requestDTO.setNome("Chalé das Montanhas");
        requestDTO.setNumero("A101");
        requestDTO.setTipo("Luxo");
        requestDTO.setValorDiaria(BigDecimal.valueOf(350.00));
        requestDTO.setDisponivel(true);
        requestDTO.setCapacidade(4);
        requestDTO.setDescricao("Chalé luxuoso com vista para as montanhas");
        requestDTO.setStatus(ChaleEntity.StatusChale.DISPONIVEL);

        // Inicializar ChaleResponseDTO
        responseDTO = new ChaleResponseDTO();
        responseDTO.setIdChale(1L);
        responseDTO.setNome("Chalé das Montanhas");
        responseDTO.setNumero("A101");
        responseDTO.setTipo("Luxo");
        responseDTO.setValorDiaria(BigDecimal.valueOf(350.00));
        responseDTO.setDisponivel(true);
        responseDTO.setCapacidade(4);
        responseDTO.setDescricao("Chalé luxuoso com vista para as montanhas");
        responseDTO.setStatus(ChaleEntity.StatusChale.DISPONIVEL);
    }

    // ======================== TESTES: salvarChale() ========================

    @Test
    @DisplayName("Deve salvar um chalé com sucesso quando o número não existe")
    void testSalvarChaleComSucesso() {
        // Arrange
        when(repository.existsByNumero(requestDTO.getNumero())).thenReturn(false);
        when(mapper.paraChaleEntity(requestDTO)).thenReturn(chaleMock);
        when(repository.save(any(ChaleEntity.class))).thenReturn(chaleMock);
        when(mapper.paraChaleResponseDTO(chaleMock)).thenReturn(responseDTO);

        // Act
        ChaleResponseDTO resultado = service.salvarChale(requestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdChale());
        assertEquals("Chalé das Montanhas", resultado.getNome());
        assertEquals("A101", resultado.getNumero());
        verify(repository, times(1)).existsByNumero(requestDTO.getNumero());
        verify(repository, times(1)).save(any(ChaleEntity.class));
        verify(mapper, times(1)).paraChaleEntity(requestDTO);
        verify(mapper, times(1)).paraChaleResponseDTO(chaleMock);
    }

    @Test
    @DisplayName("Deve lançar ChaleJaExisteException quando o número já existe")
    void testSalvarChaleComNumeroExistente() {
        // Arrange
        when(repository.existsByNumero(requestDTO.getNumero())).thenReturn(true);

        // Act & Assert
        assertThrows(ChaleJaExisteException.class, () -> service.salvarChale(requestDTO));
        verify(repository, times(1)).existsByNumero(requestDTO.getNumero());
        verify(repository, never()).save(any(ChaleEntity.class));
        verify(chaleManager, never()).notificar(any(ChaleEntity.class), any(ChaleObserver.ChaleEventType.class));
    }

    @Test
    @DisplayName("Deve notificar observers com evento CRIADO")
    void testSalvarChaleNotificaObservers() {
        // Arrange
        when(repository.existsByNumero(requestDTO.getNumero())).thenReturn(false);
        when(mapper.paraChaleEntity(requestDTO)).thenReturn(chaleMock);
        when(repository.save(any(ChaleEntity.class))).thenReturn(chaleMock);
        when(mapper.paraChaleResponseDTO(chaleMock)).thenReturn(responseDTO);

        // Act
        service.salvarChale(requestDTO);

        // Assert
        verify(chaleManager, times(1)).notificar(chaleMock, ChaleObserver.ChaleEventType.CRIADO);
    }

    @Test
    @DisplayName("Deve retornar ChaleResponseDTO correto ao salvar")
    void testSalvarChaleRetornaResponseDTOCorreto() {
        // Arrange
        when(repository.existsByNumero(requestDTO.getNumero())).thenReturn(false);
        when(mapper.paraChaleEntity(requestDTO)).thenReturn(chaleMock);
        when(repository.save(any(ChaleEntity.class))).thenReturn(chaleMock);
        when(mapper.paraChaleResponseDTO(chaleMock)).thenReturn(responseDTO);

        // Act
        ChaleResponseDTO resultado = service.salvarChale(requestDTO);

        // Assert
        assertEquals(responseDTO.getIdChale(), resultado.getIdChale());
        assertEquals(responseDTO.getNome(), resultado.getNome());
        assertEquals(responseDTO.getNumero(), resultado.getNumero());
        assertEquals(responseDTO.getTipo(), resultado.getTipo());
        assertEquals(responseDTO.getValorDiaria(), resultado.getValorDiaria());
    }

    // ======================== TESTES: listarChales() ========================

    @Test
    @DisplayName("Deve retornar lista vazia quando não há chalés")
    void testListarChalesVazia() {
        // Arrange
        when(repository.findAll()).thenReturn(new ArrayList<>());
        when(mapper.paraListaChaleResponseDTO(new ArrayList<>())).thenReturn(new ArrayList<>());

        // Act
        List<ChaleResponseDTO> resultado = service.listarChales();

        // Assert
        assertTrue(resultado.isEmpty());
        verify(repository, times(1)).findAll();
        verify(mapper, times(1)).paraListaChaleResponseDTO(new ArrayList<>());
    }

    @Test
    @DisplayName("Deve retornar lista com todos os chalés cadastrados")
    void testListarChalesComMultiplosChalés() {
        // Arrange
        ChaleEntity chale2 = new ChaleEntity();
        chale2.setIdChale(2L);
        chale2.setNome("Chalé da Praia");
        chale2.setNumero("B202");

        List<ChaleEntity> chales = List.of(chaleMock, chale2);
        ChaleResponseDTO response2 = new ChaleResponseDTO();
        response2.setIdChale(2L);
        response2.setNome("Chalé da Praia");
        response2.setNumero("B202");

        List<ChaleResponseDTO> responses = List.of(responseDTO, response2);

        when(repository.findAll()).thenReturn(chales);
        when(mapper.paraListaChaleResponseDTO(chales)).thenReturn(responses);

        // Act
        List<ChaleResponseDTO> resultado = service.listarChales();

        // Assert
        assertEquals(2, resultado.size());
        assertEquals("Chalé das Montanhas", resultado.get(0).getNome());
        assertEquals("Chalé da Praia", resultado.get(1).getNome());
        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve mapear corretamente para ChaleResponseDTO ao listar")
    void testListarChalesMapeiaCorretamente() {
        // Arrange
        List<ChaleEntity> chales = List.of(chaleMock);
        List<ChaleResponseDTO> responses = List.of(responseDTO);

        when(repository.findAll()).thenReturn(chales);
        when(mapper.paraListaChaleResponseDTO(chales)).thenReturn(responses);

        // Act
        List<ChaleResponseDTO> resultado = service.listarChales();

        // Assert
        assertFalse(resultado.isEmpty());
        assertEquals(responseDTO.getIdChale(), resultado.get(0).getIdChale());
        assertEquals(responseDTO.getNome(), resultado.get(0).getNome());
    }

    // ======================== TESTES: buscarPorId() ========================

    @Test
    @DisplayName("Deve retornar Optional com chalé quando existe")
    void testBuscarPorIdComSucesso() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(chaleMock));
        when(mapper.paraChaleResponseDTO(chaleMock)).thenReturn(responseDTO);

        // Act
        Optional<ChaleResponseDTO> resultado = service.buscarPorId(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(responseDTO.getIdChale(), resultado.get().getIdChale());
        assertEquals("Chalé das Montanhas", resultado.get().getNome());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando não existe")
    void testBuscarPorIdNaoEncontrado() {
        // Arrange
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<ChaleResponseDTO> resultado = service.buscarPorId(999L);

        // Assert
        assertFalse(resultado.isPresent());
        verify(repository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Deve mapear corretamente para ChaleResponseDTO ao buscar por ID")
    void testBuscarPorIdMapeiaCorretamente() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(chaleMock));
        when(mapper.paraChaleResponseDTO(chaleMock)).thenReturn(responseDTO);

        // Act
        Optional<ChaleResponseDTO> resultado = service.buscarPorId(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(responseDTO.getTipo(), resultado.get().getTipo());
        assertEquals(responseDTO.getValorDiaria(), resultado.get().getValorDiaria());
        assertEquals(responseDTO.getCapacidade(), resultado.get().getCapacidade());
    }

    // ======================== TESTES: deletarChalePorId() ========================

    @Test
    @DisplayName("Deve lançar exceção quando chalé não existe")
    void testDeletarChaleNaoExistente() {
        // Arrange
        when(repository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> service.deletarChalePorId(999L));
        verify(repository, times(1)).existsById(999L);
        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve deletar chalé com sucesso quando existe")
    void testDeletarChaleComSucesso() {
        // Arrange
        when(repository.existsById(1L)).thenReturn(true);
        when(repository.findById(1L)).thenReturn(Optional.of(chaleMock));

        // Act
        service.deletarChalePorId(1L);

        // Assert
        verify(repository, times(1)).existsById(1L);
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Deve notificar observers com evento REMOVIDO")
    void testDeletarChaleNotificaObservers() {
        // Arrange
        when(repository.existsById(1L)).thenReturn(true);
        when(repository.findById(1L)).thenReturn(Optional.of(chaleMock));

        // Act
        service.deletarChalePorId(1L);

        // Assert
        verify(chaleManager, times(1)).notificar(chaleMock, ChaleObserver.ChaleEventType.REMOVIDO);
    }

    // ======================== TESTES: atualizarChale() ========================

    @Test
    @DisplayName("Deve retornar Optional vazio quando chalé não existe")
    void testAtualizarChaleNaoExistente() {
        // Arrange
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<ChaleResponseDTO> resultado = service.atualizarChale(999L, requestDTO);

        // Assert
        assertFalse(resultado.isPresent());
        verify(repository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando número já está em uso")
    void testAtualizarChaleComNumeroEmUso() {
        // Arrange
        ChaleRequestDTO requestAtualizado = new ChaleRequestDTO();
        requestAtualizado.setNome("Novo Nome");
        requestAtualizado.setNumero("B202"); // Número diferente
        requestAtualizado.setTipo("Luxo");
        requestAtualizado.setValorDiaria(BigDecimal.valueOf(350.00));
        requestAtualizado.setDisponivel(true);
        requestAtualizado.setCapacidade(4);
        requestAtualizado.setDescricao("Descrição");
        requestAtualizado.setStatus(ChaleEntity.StatusChale.DISPONIVEL);

        when(repository.findById(1L)).thenReturn(Optional.of(chaleMock));
        when(repository.existsByNumero("B202")).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> service.atualizarChale(1L, requestAtualizado));
        verify(repository, times(1)).findById(1L);
    }

    // ======================== TESTES: buscarPorPrecoMaximo() ========================

    @Test
    @DisplayName("Deve retornar chalés com preço menor ou igual ao máximo")
    void testBuscarPorPrecoMaximo() {
        // Arrange
        BigDecimal precoMaximo = BigDecimal.valueOf(400.00);
        List<ChaleEntity> chales = List.of(chaleMock);
        List<ChaleResponseDTO> responses = List.of(responseDTO);

        when(repository.findByValorDiariaLessThanEqual(precoMaximo)).thenReturn(chales);
        when(mapper.paraListaChaleResponseDTO(chales)).thenReturn(responses);

        // Act
        List<ChaleResponseDTO> resultado = service.buscarPorPrecoMaximo(precoMaximo);

        // Assert
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getValorDiaria().compareTo(precoMaximo) <= 0);
        verify(repository, times(1)).findByValorDiariaLessThanEqual(precoMaximo);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nenhum chalé atende critério de preço")
    void testBuscarPorPrecoMaximoVazia() {
        // Arrange
        BigDecimal precoMaximo = BigDecimal.valueOf(100.00);
        when(repository.findByValorDiariaLessThanEqual(precoMaximo)).thenReturn(new ArrayList<>());
        when(mapper.paraListaChaleResponseDTO(new ArrayList<>())).thenReturn(new ArrayList<>());

        // Act
        List<ChaleResponseDTO> resultado = service.buscarPorPrecoMaximo(precoMaximo);

        // Assert
        assertTrue(resultado.isEmpty());
        verify(repository, times(1)).findByValorDiariaLessThanEqual(precoMaximo);
    }

    @Test
    @DisplayName("Deve mapear corretamente para ChaleResponseDTO ao buscar por preço")
    void testBuscarPorPrecoMaximoMapeiaCorretamente() {
        // Arrange
        BigDecimal precoMaximo = BigDecimal.valueOf(400.00);
        List<ChaleEntity> chales = List.of(chaleMock);
        List<ChaleResponseDTO> responses = List.of(responseDTO);

        when(repository.findByValorDiariaLessThanEqual(precoMaximo)).thenReturn(chales);
        when(mapper.paraListaChaleResponseDTO(chales)).thenReturn(responses);

        // Act
        List<ChaleResponseDTO> resultado = service.buscarPorPrecoMaximo(precoMaximo);

        // Assert
        assertFalse(resultado.isEmpty());
        assertEquals(responseDTO.getIdChale(), resultado.get(0).getIdChale());
        assertEquals(responseDTO.getNome(), resultado.get(0).getNome());
    }

    // ======================== TESTES: buscarPorNomeOuNumero() ========================

    @Test
    @DisplayName("Deve retornar chalés que contêm o nome (case-insensitive)")
    void testBuscarPorNome() {
        // Arrange
        String nome = "Montanhas";
        String numero = "";
        List<ChaleEntity> chales = List.of(chaleMock);
        List<ChaleResponseDTO> responses = List.of(responseDTO);

        when(repository.findByNomeContainsIgnoreCaseOrNumeroContainsIgnoreCase(nome, numero))
                .thenReturn(chales);
        when(mapper.paraListaChaleResponseDTO(chales)).thenReturn(responses);

        // Act
        List<ChaleResponseDTO> resultado = service.buscarPorNomeOuNumero(nome, numero);

        // Assert
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getNome().toLowerCase().contains(nome.toLowerCase()));
        verify(repository, times(1)).findByNomeContainsIgnoreCaseOrNumeroContainsIgnoreCase(nome, numero);
    }

    @Test
    @DisplayName("Deve retornar chalés que contêm o número (case-insensitive)")
    void testBuscarPorNumero() {
        // Arrange
        String nome = "";
        String numero = "A101";
        List<ChaleEntity> chales = List.of(chaleMock);
        List<ChaleResponseDTO> responses = List.of(responseDTO);

        when(repository.findByNomeContainsIgnoreCaseOrNumeroContainsIgnoreCase(nome, numero))
                .thenReturn(chales);
        when(mapper.paraListaChaleResponseDTO(chales)).thenReturn(responses);

        // Act
        List<ChaleResponseDTO> resultado = service.buscarPorNomeOuNumero(nome, numero);

        // Assert
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getNumero().contains(numero));
        verify(repository, times(1)).findByNomeContainsIgnoreCaseOrNumeroContainsIgnoreCase(nome, numero);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nenhum chalé atende critério")
    void testBuscarPorNomeOuNumeroVazia() {
        // Arrange
        String nome = "Inexistente";
        String numero = "Z999";
        when(repository.findByNomeContainsIgnoreCaseOrNumeroContainsIgnoreCase(nome, numero))
                .thenReturn(new ArrayList<>());
        when(mapper.paraListaChaleResponseDTO(new ArrayList<>())).thenReturn(new ArrayList<>());

        // Act
        List<ChaleResponseDTO> resultado = service.buscarPorNomeOuNumero(nome, numero);

        // Assert
        assertTrue(resultado.isEmpty());
    }

    // ======================== TESTES: atualizarStatus() ========================

    @Test
    @DisplayName("Deve retornar Optional vazio quando chalé não existe (atualizarStatus)")
    void testAtualizarStatusNaoExistente() {
        // Arrange
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<ChaleResponseDTO> resultado = service.atualizarStatus(999L, ChaleEntity.StatusChale.MANUTENCAO);

        // Assert
        assertFalse(resultado.isPresent());
        verify(repository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Deve atualizar status com sucesso")
    void testAtualizarStatusComSucesso() {
        // Arrange
        ChaleEntity chaleAtualizado = new ChaleEntity();
        chaleAtualizado.setIdChale(1L);
        chaleAtualizado.setNome("Chalé das Montanhas");
        chaleAtualizado.setNumero("A101");
        chaleAtualizado.setTipo("Luxo");
        chaleAtualizado.setValorDiaria(BigDecimal.valueOf(350.00));
        chaleAtualizado.setDisponivel(true);
        chaleAtualizado.setCapacidade(4);
        chaleAtualizado.setDescricao("Chalé luxuoso com vista para as montanhas");
        chaleAtualizado.setStatus(ChaleEntity.StatusChale.MANUTENCAO);

        ChaleResponseDTO responseAtualizado = new ChaleResponseDTO();
        responseAtualizado.setIdChale(1L);
        responseAtualizado.setStatus(ChaleEntity.StatusChale.MANUTENCAO);

        when(repository.findById(1L)).thenReturn(Optional.of(chaleMock));
        when(repository.save(any(ChaleEntity.class))).thenReturn(chaleAtualizado);
        when(mapper.paraChaleResponseDTO(chaleAtualizado)).thenReturn(responseAtualizado);

        // Act
        Optional<ChaleResponseDTO> resultado = service.atualizarStatus(1L, ChaleEntity.StatusChale.MANUTENCAO);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(ChaleEntity.StatusChale.MANUTENCAO, resultado.get().getStatus());
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(any(ChaleEntity.class));
    }

    @Test
    @DisplayName("Deve notificar com evento STATUS_ALTERADO ao atualizar status")
    void testAtualizarStatusNotificaObservers() {
        // Arrange
        ChaleEntity chaleAtualizado = new ChaleEntity();
        chaleAtualizado.setIdChale(1L);
        chaleAtualizado.setStatus(ChaleEntity.StatusChale.MANUTENCAO);

        when(repository.findById(1L)).thenReturn(Optional.of(chaleMock));
        when(repository.save(any(ChaleEntity.class))).thenReturn(chaleAtualizado);

        // Act
        service.atualizarStatus(1L, ChaleEntity.StatusChale.MANUTENCAO);

        // Assert
        verify(chaleManager, times(1)).notificar(chaleAtualizado, ChaleObserver.ChaleEventType.STATUS_ALTERADO);
    }

    // ======================== TESTES: atualizarDisponibilidade() ========================

    @Test
    @DisplayName("Deve retornar Optional vazio quando chalé não existe (atualizarDisponibilidade)")
    void testAtualizarDisponibilidadeNaoExistente() {
        // Arrange
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<ChaleResponseDTO> resultado = service.atualizarDisponibilidade(999L, false);

        // Assert
        assertFalse(resultado.isPresent());
        verify(repository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Deve atualizar disponibilidade com sucesso")
    void testAtualizarDisponibilidadeComSucesso() {
        // Arrange
        ChaleEntity chaleAtualizado = new ChaleEntity();
        chaleAtualizado.setIdChale(1L);
        chaleAtualizado.setNome("Chalé das Montanhas");
        chaleAtualizado.setNumero("A101");
        chaleAtualizado.setTipo("Luxo");
        chaleAtualizado.setValorDiaria(BigDecimal.valueOf(350.00));
        chaleAtualizado.setDisponivel(false);
        chaleAtualizado.setCapacidade(4);
        chaleAtualizado.setDescricao("Chalé luxuoso com vista para as montanhas");
        chaleAtualizado.setStatus(ChaleEntity.StatusChale.DISPONIVEL);

        ChaleResponseDTO responseAtualizado = new ChaleResponseDTO();
        responseAtualizado.setIdChale(1L);
        responseAtualizado.setDisponivel(false);

        when(repository.findById(1L)).thenReturn(Optional.of(chaleMock));
        when(repository.save(any(ChaleEntity.class))).thenReturn(chaleAtualizado);
        when(mapper.paraChaleResponseDTO(chaleAtualizado)).thenReturn(responseAtualizado);

        // Act
        Optional<ChaleResponseDTO> resultado = service.atualizarDisponibilidade(1L, false);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(false, resultado.get().getDisponivel());
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(any(ChaleEntity.class));
    }

    @Test
    @DisplayName("Deve notificar com evento DISPONIBILIDADE_ALTERADA ao atualizar disponibilidade")
    void testAtualizarDisponibilidadeNotificaObservers() {
        // Arrange
        ChaleEntity chaleAtualizado = new ChaleEntity();
        chaleAtualizado.setIdChale(1L);
        chaleAtualizado.setDisponivel(false);

        when(repository.findById(1L)).thenReturn(Optional.of(chaleMock));
        when(repository.save(any(ChaleEntity.class))).thenReturn(chaleAtualizado);

        // Act
        service.atualizarDisponibilidade(1L, false);

        // Assert
        verify(chaleManager, times(1)).notificar(chaleAtualizado, ChaleObserver.ChaleEventType.DISPONIBILIDADE_ALTERADA);
    }
}
