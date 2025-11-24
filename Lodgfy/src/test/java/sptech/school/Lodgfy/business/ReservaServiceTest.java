package sptech.school.Lodgfy.business;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sptech.school.Lodgfy.business.dto.ReservaRequestDTO;
import sptech.school.Lodgfy.business.dto.ReservaResponseDTO;
import sptech.school.Lodgfy.business.dto.StatusReserva;
import sptech.school.Lodgfy.business.exceptions.*;
import sptech.school.Lodgfy.business.mapsstruct.ReservaMapper;
import sptech.school.Lodgfy.infrastructure.entities.ChaleEntity;
import sptech.school.Lodgfy.infrastructure.entities.HospedeEntity;
import sptech.school.Lodgfy.infrastructure.entities.ReservaEntity;
import sptech.school.Lodgfy.infrastructure.repository.ChaleRepository;
import sptech.school.Lodgfy.infrastructure.repository.HospedeRepository;
import sptech.school.Lodgfy.infrastructure.repository.ReservaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservaService - Testes Unitários")
public class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private HospedeRepository hospedeRepository;

    @Mock
    private ChaleRepository chaleRepository;

    @Mock
    private ReservaMapper mapper;

    @InjectMocks
    private ReservaService service;

    private ReservaEntity reservaMock;
    private ReservaRequestDTO requestDTO;
    private ReservaResponseDTO responseDTO;
    private HospedeEntity hospedeMock;
    private ChaleEntity chaleMock;

    @BeforeEach
    void setUp() {
        // Inicializar HospedeEntity
        hospedeMock = new HospedeEntity();
        hospedeMock.setId(1L);
        hospedeMock.setNome("João da Silva");
        hospedeMock.setCpf("12345678900");

        // Inicializar ChaleEntity
        chaleMock = new ChaleEntity();
        chaleMock.setIdChale(1L);
        chaleMock.setNome("Chalé das Montanhas");
        chaleMock.setNumero("A101");
        chaleMock.setValorDiaria(BigDecimal.valueOf(350.00));
        chaleMock.setDisponivel(true);

        // Inicializar ReservaEntity
        reservaMock = new ReservaEntity();
        reservaMock.setIdReserva(1L);
        reservaMock.setHospede(hospedeMock);
        reservaMock.setChale(chaleMock);
        reservaMock.setDataCheckIn(LocalDate.now().plusDays(1));
        reservaMock.setDataCheckOut(LocalDate.now().plusDays(4));
        reservaMock.setValorTotal(BigDecimal.valueOf(1050.00)); // 3 diárias x 350
        reservaMock.setStatusReserva(StatusReserva.PENDENTE);

        // Inicializar ReservaRequestDTO
        requestDTO = new ReservaRequestDTO();
        requestDTO.setHospedeId(1L);
        requestDTO.setChaleId(1L);
        requestDTO.setDataCheckIn(LocalDate.now().plusDays(1));
        requestDTO.setDataCheckOut(LocalDate.now().plusDays(4));

        // Inicializar ReservaResponseDTO
        responseDTO = new ReservaResponseDTO();
        responseDTO.setIdReserva(1L);
        responseDTO.setHospedeId(1L);
        responseDTO.setHospedeNome("João da Silva");
        responseDTO.setChaleId(1L);
        responseDTO.setChaleNumero("A101");
        responseDTO.setChaleNome("Chalé das Montanhas");
        responseDTO.setDataCheckIn(LocalDate.now().plusDays(1));
        responseDTO.setDataCheckOut(LocalDate.now().plusDays(4));
        responseDTO.setValorTotal(BigDecimal.valueOf(1050.00));
        responseDTO.setStatusReserva(StatusReserva.PENDENTE);
    }

    // ======================== TESTES: criarReserva() ========================

    @Test
    @DisplayName("Deve criar reserva com sucesso")
    void testCriarReservaComSucesso() {
        // Arrange
        when(hospedeRepository.findById(1L)).thenReturn(Optional.of(hospedeMock));
        when(chaleRepository.findById(1L)).thenReturn(Optional.of(chaleMock));
        when(reservaRepository.findReservasConflitantes(anyLong(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(reservaRepository.save(any(ReservaEntity.class))).thenReturn(reservaMock);
        when(mapper.paraReservaResponseDTO(reservaMock)).thenReturn(responseDTO);

        // Act
        ReservaResponseDTO resultado = service.criarReserva(requestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdReserva());
        assertEquals(BigDecimal.valueOf(1050.00), resultado.getValorTotal());
        assertEquals(StatusReserva.PENDENTE, resultado.getStatusReserva());
        verify(reservaRepository, times(1)).save(any(ReservaEntity.class));
        verify(mapper, times(1)).paraReservaResponseDTO(reservaMock);
    }

    @Test
    @DisplayName("Deve lançar exceção quando hóspede não existe")
    void testCriarReservaComHospedeInexistente() {
        // Arrange
        when(hospedeRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> service.criarReserva(requestDTO));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando chalé não existe")
    void testCriarReservaComChaleInexistente() {
        // Arrange
        when(hospedeRepository.findById(1L)).thenReturn(Optional.of(hospedeMock));
        when(chaleRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> service.criarReserva(requestDTO));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar ChaleIndisponivelException quando chalé não está disponível")
    void testCriarReservaComChaleIndisponivel() {
        // Arrange
        chaleMock.setDisponivel(false);
        when(hospedeRepository.findById(1L)).thenReturn(Optional.of(hospedeMock));
        when(chaleRepository.findById(1L)).thenReturn(Optional.of(chaleMock));

        // Act & Assert
        assertThrows(ChaleIndisponivelException.class, () -> service.criarReserva(requestDTO));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar ReservaConflitanteException quando há conflito de datas")
    void testCriarReservaComConflito() {
        // Arrange
        ReservaEntity reservaConflitante = new ReservaEntity();
        reservaConflitante.setIdReserva(2L);
        when(hospedeRepository.findById(1L)).thenReturn(Optional.of(hospedeMock));
        when(chaleRepository.findById(1L)).thenReturn(Optional.of(chaleMock));
        when(reservaRepository.findReservasConflitantes(anyLong(), any(), any()))
                .thenReturn(List.of(reservaConflitante));

        // Act & Assert
        assertThrows(ReservaConflitanteException.class, () -> service.criarReserva(requestDTO));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar DataReservaInvalidaException quando check-in é após check-out")
    void testCriarReservaComDataInvalida() {
        // Arrange
        requestDTO.setDataCheckIn(LocalDate.now().plusDays(5));
        requestDTO.setDataCheckOut(LocalDate.now().plusDays(2));

        // Act & Assert
        assertThrows(DataReservaInvalidaException.class, () -> service.criarReserva(requestDTO));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando check-in é igual ao check-out")
    void testCriarReservaComDatasIguais() {
        // Arrange
        LocalDate data = LocalDate.now().plusDays(1);
        requestDTO.setDataCheckIn(data);
        requestDTO.setDataCheckOut(data);

        // Act & Assert
        assertThrows(DataReservaInvalidaException.class, () -> service.criarReserva(requestDTO));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve calcular valor total corretamente")
    void testCalculoValorTotal() {
        // Arrange - 3 diárias x 350 = 1050
        when(hospedeRepository.findById(1L)).thenReturn(Optional.of(hospedeMock));
        when(chaleRepository.findById(1L)).thenReturn(Optional.of(chaleMock));
        when(reservaRepository.findReservasConflitantes(anyLong(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(reservaRepository.save(any(ReservaEntity.class))).thenAnswer(invocation -> {
            ReservaEntity reserva = invocation.getArgument(0);
            assertEquals(BigDecimal.valueOf(1050.00), reserva.getValorTotal());
            return reservaMock;
        });
        when(mapper.paraReservaResponseDTO(any())).thenReturn(responseDTO);

        // Act
        service.criarReserva(requestDTO);

        // Assert
        verify(reservaRepository, times(1)).save(any(ReservaEntity.class));
    }

    // ======================== TESTES: listarReservas() ========================

    @Test
    @DisplayName("Deve retornar lista vazia quando não há reservas")
    void testListarReservasVazia() {
        // Arrange
        when(reservaRepository.findAll()).thenReturn(new ArrayList<>());
        when(mapper.paraListaReservaResponseDTO(new ArrayList<>())).thenReturn(new ArrayList<>());

        // Act
        List<ReservaResponseDTO> resultado = service.listarReservas();

        // Assert
        assertTrue(resultado.isEmpty());
        verify(reservaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista com todas as reservas")
    void testListarReservasComDados() {
        // Arrange
        List<ReservaEntity> reservas = List.of(reservaMock);
        List<ReservaResponseDTO> responses = List.of(responseDTO);

        when(reservaRepository.findAll()).thenReturn(reservas);
        when(mapper.paraListaReservaResponseDTO(reservas)).thenReturn(responses);

        // Act
        List<ReservaResponseDTO> resultado = service.listarReservas();

        // Assert
        assertEquals(1, resultado.size());
        assertEquals(responseDTO.getIdReserva(), resultado.get(0).getIdReserva());
        verify(reservaRepository, times(1)).findAll();
    }

    // ======================== TESTES: buscarPorId() ========================

    @Test
    @DisplayName("Deve retornar reserva quando existe")
    void testBuscarPorIdComSucesso() {
        // Arrange
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reservaMock));
        when(mapper.paraReservaResponseDTO(reservaMock)).thenReturn(responseDTO);

        // Act
        Optional<ReservaResponseDTO> resultado = service.buscarPorId(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getIdReserva());
        verify(reservaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando não existe")
    void testBuscarPorIdNaoEncontrado() {
        // Arrange
        when(reservaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<ReservaResponseDTO> resultado = service.buscarPorId(999L);

        // Assert
        assertFalse(resultado.isPresent());
        verify(reservaRepository, times(1)).findById(999L);
    }

    // ======================== TESTES: buscarPorHospede() ========================

    @Test
    @DisplayName("Deve buscar reservas por hóspede")
    void testBuscarPorHospede() {
        // Arrange
        List<ReservaEntity> reservas = List.of(reservaMock);
        List<ReservaResponseDTO> responses = List.of(responseDTO);

        when(reservaRepository.findHistoricoByHospede(1L)).thenReturn(reservas);
        when(mapper.paraListaReservaResponseDTO(reservas)).thenReturn(responses);

        // Act
        List<ReservaResponseDTO> resultado = service.buscarPorHospede(1L);

        // Assert
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getHospedeId());
        verify(reservaRepository, times(1)).findHistoricoByHospede(1L);
    }

    // ======================== TESTES: buscarPorChale() ========================

    @Test
    @DisplayName("Deve buscar reservas por chalé")
    void testBuscarPorChale() {
        // Arrange
        List<ReservaEntity> reservas = List.of(reservaMock);
        List<ReservaResponseDTO> responses = List.of(responseDTO);

        when(reservaRepository.findByChaleIdChale(1L)).thenReturn(reservas);
        when(mapper.paraListaReservaResponseDTO(reservas)).thenReturn(responses);

        // Act
        List<ReservaResponseDTO> resultado = service.buscarPorChale(1L);

        // Assert
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getChaleId());
        verify(reservaRepository, times(1)).findByChaleIdChale(1L);
    }

    // ======================== TESTES: buscarPorStatus() ========================

    @Test
    @DisplayName("Deve buscar reservas por status")
    void testBuscarPorStatus() {
        // Arrange
        List<ReservaEntity> reservas = List.of(reservaMock);
        List<ReservaResponseDTO> responses = List.of(responseDTO);

        when(reservaRepository.findByStatusReserva(StatusReserva.PENDENTE)).thenReturn(reservas);
        when(mapper.paraListaReservaResponseDTO(reservas)).thenReturn(responses);

        // Act
        List<ReservaResponseDTO> resultado = service.buscarPorStatus(StatusReserva.PENDENTE);

        // Assert
        assertEquals(1, resultado.size());
        assertEquals(StatusReserva.PENDENTE, resultado.get(0).getStatusReserva());
        verify(reservaRepository, times(1)).findByStatusReserva(StatusReserva.PENDENTE);
    }

    // ======================== TESTES: confirmarReserva() ========================

    @Test
    @DisplayName("Deve confirmar reserva pendente com sucesso")
    void testConfirmarReservaComSucesso() {
        // Arrange
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reservaMock));
        when(reservaRepository.save(any(ReservaEntity.class))).thenAnswer(invocation -> {
            ReservaEntity reserva = invocation.getArgument(0);
            reserva.setStatusReserva(StatusReserva.CONFIRMADA);
            return reserva;
        });
        when(mapper.paraReservaResponseDTO(any())).thenReturn(responseDTO);

        // Act
        ReservaResponseDTO resultado = service.confirmarReserva(1L);

        // Assert
        assertNotNull(resultado);
        verify(reservaRepository, times(1)).save(any(ReservaEntity.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao confirmar reserva inexistente")
    void testConfirmarReservaInexistente() {
        // Arrange
        when(reservaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ReservaNaoEncontradaException.class, () -> service.confirmarReserva(999L));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao confirmar reserva que não está pendente")
    void testConfirmarReservaNaoPendente() {
        // Arrange
        reservaMock.setStatusReserva(StatusReserva.CONFIRMADA);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reservaMock));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> service.confirmarReserva(1L));
        verify(reservaRepository, never()).save(any());
    }

    // ======================== TESTES: cancelarReserva() ========================

    @Test
    @DisplayName("Deve cancelar reserva com sucesso")
    void testCancelarReservaComSucesso() {
        // Arrange
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reservaMock));
        when(reservaRepository.save(any(ReservaEntity.class))).thenAnswer(invocation -> {
            ReservaEntity reserva = invocation.getArgument(0);
            reserva.setStatusReserva(StatusReserva.CANCELADA);
            return reserva;
        });
        when(mapper.paraReservaResponseDTO(any())).thenReturn(responseDTO);

        // Act
        ReservaResponseDTO resultado = service.cancelarReserva(1L);

        // Assert
        assertNotNull(resultado);
        verify(reservaRepository, times(1)).save(any(ReservaEntity.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao cancelar reserva já cancelada")
    void testCancelarReservaJaCancelada() {
        // Arrange
        reservaMock.setStatusReserva(StatusReserva.CANCELADA);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reservaMock));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> service.cancelarReserva(1L));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao cancelar reserva concluída")
    void testCancelarReservaConcluida() {
        // Arrange
        reservaMock.setStatusReserva(StatusReserva.CONCLUIDA);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reservaMock));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> service.cancelarReserva(1L));
        verify(reservaRepository, never()).save(any());
    }

    // ======================== TESTES: concluirReserva() ========================

    @Test
    @DisplayName("Deve concluir reserva confirmada com sucesso")
    void testConcluirReservaComSucesso() {
        // Arrange
        reservaMock.setStatusReserva(StatusReserva.CONFIRMADA);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reservaMock));
        when(reservaRepository.save(any(ReservaEntity.class))).thenAnswer(invocation -> {
            ReservaEntity reserva = invocation.getArgument(0);
            reserva.setStatusReserva(StatusReserva.CONCLUIDA);
            return reserva;
        });
        when(mapper.paraReservaResponseDTO(any())).thenReturn(responseDTO);

        // Act
        ReservaResponseDTO resultado = service.concluirReserva(1L);

        // Assert
        assertNotNull(resultado);
        verify(reservaRepository, times(1)).save(any(ReservaEntity.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao concluir reserva que não está confirmada")
    void testConcluirReservaNaoConfirmada() {
        // Arrange
        reservaMock.setStatusReserva(StatusReserva.PENDENTE);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reservaMock));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> service.concluirReserva(1L));
        verify(reservaRepository, never()).save(any());
    }

    // ======================== TESTES: deletarReserva() ========================

    @Test
    @DisplayName("Deve deletar reserva com sucesso")
    void testDeletarReservaComSucesso() {
        // Arrange
        when(reservaRepository.existsById(1L)).thenReturn(true);

        // Act
        service.deletarReserva(1L);

        // Assert
        verify(reservaRepository, times(1)).existsById(1L);
        verify(reservaRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar reserva inexistente")
    void testDeletarReservaInexistente() {
        // Arrange
        when(reservaRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ReservaNaoEncontradaException.class, () -> service.deletarReserva(999L));
        verify(reservaRepository, times(1)).existsById(999L);
        verify(reservaRepository, never()).deleteById(any());
    }
}

