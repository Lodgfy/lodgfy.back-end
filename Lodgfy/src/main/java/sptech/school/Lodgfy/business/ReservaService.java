package sptech.school.Lodgfy.business;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final HospedeRepository hospedeRepository;
    private final ChaleRepository chaleRepository;
    private final ReservaMapper mapper;

    @Transactional
    public ReservaResponseDTO criarReserva(ReservaRequestDTO request) {
        log.info("Criando reserva - Hóspede: {}, Chalé: {}, Check-in: {}, Check-out: {}",
                request.getHospedeId(), request.getChaleId(), request.getDataCheckIn(), request.getDataCheckOut());

        // Validações de data
        validarDatas(request.getDataCheckIn(), request.getDataCheckOut());

        // Buscar hóspede
        HospedeEntity hospede = hospedeRepository.findById(request.getHospedeId())
                .orElseThrow(() -> {
                    log.error("Hóspede não encontrado: {}", request.getHospedeId());
                    return new RuntimeException("Hóspede não encontrado");
                });

        // Buscar chalé
        ChaleEntity chale = chaleRepository.findById(request.getChaleId())
                .orElseThrow(() -> {
                    log.error("Chalé não encontrado: {}", request.getChaleId());
                    return new RuntimeException("Chalé não encontrado");
                });

        // Validar disponibilidade do chalé
        if (chale.getStatus() != ChaleEntity.StatusChale.DISPONIVEL) {
            log.warn("Chalé indisponível: {} - Status: {}", chale.getIdChale(), chale.getStatus());
            throw new ChaleIndisponivelException();
        }

        // Verificar conflitos de reserva
        List<ReservaEntity> reservasConflitantes = reservaRepository.findReservasConflitantes(
                chale.getIdChale(),
                request.getDataCheckIn(),
                request.getDataCheckOut()
        );

        if (!reservasConflitantes.isEmpty()) {
            log.warn("Reserva conflitante encontrada para chalé: {}", chale.getIdChale());
            throw new ReservaConflitanteException();
        }

        // Calcular valor total
        BigDecimal valorTotal = calcularValorTotal(
                chale.getValorDiaria(),
                request.getDataCheckIn(),
                request.getDataCheckOut()
        );

        // Criar reserva
        ReservaEntity reserva = new ReservaEntity();
        reserva.setHospede(hospede);
        reserva.setChale(chale);
        reserva.setDataCheckIn(request.getDataCheckIn());
        reserva.setDataCheckOut(request.getDataCheckOut());
        reserva.setValorTotal(valorTotal);
        reserva.setStatusReserva(StatusReserva.PENDENTE);

        ReservaEntity reservaSalva = reservaRepository.save(reserva);
        log.info("Reserva criada com sucesso - ID: {}, Valor: {}", reservaSalva.getIdReserva(), valorTotal);

        return mapper.paraReservaResponseDTO(reservaSalva);
    }

    public List<ReservaResponseDTO> listarReservas() {
        log.info("Listando todas as reservas");
        return mapper.paraListaReservaResponseDTO(reservaRepository.findAll());
    }

    public Optional<ReservaResponseDTO> buscarPorId(Long id) {
        log.info("Buscando reserva por ID: {}", id);
        return reservaRepository.findById(id)
                .map(mapper::paraReservaResponseDTO);
    }

    public List<ReservaResponseDTO> buscarPorHospede(Long hospedeId) {
        log.info("Buscando reservas do hóspede: {}", hospedeId);
        return mapper.paraListaReservaResponseDTO(
                reservaRepository.findHistoricoByHospede(hospedeId)
        );
    }

    public List<ReservaResponseDTO> buscarPorChale(Long chaleId) {
        log.info("Buscando reservas do chalé: {}", chaleId);
        return mapper.paraListaReservaResponseDTO(
                reservaRepository.findByChaleIdChale(chaleId)
        );
    }

    public List<ReservaResponseDTO> buscarPorStatus(StatusReserva status) {
        log.info("Buscando reservas com status: {}", status);
        return mapper.paraListaReservaResponseDTO(
                reservaRepository.findByStatusReserva(status)
        );
    }

    @Transactional
    public ReservaResponseDTO confirmarReserva(Long id) {
        log.info("Confirmando reserva: {}", id);
        ReservaEntity reserva = reservaRepository.findById(id)
                .orElseThrow(ReservaNaoEncontradaException::new);

        if (reserva.getStatusReserva() != StatusReserva.PENDENTE) {
            log.warn("Tentativa de confirmar reserva que não está pendente: {}", id);
            throw new IllegalStateException("Apenas reservas pendentes podem ser confirmadas");
        }

        reserva.setStatusReserva(StatusReserva.CONFIRMADA);

        // Atualizar status do chalé para OCUPADO
        ChaleEntity chale = reserva.getChale();
        chale.setStatus(ChaleEntity.StatusChale.OCUPADO);
        chaleRepository.save(chale);

        ReservaEntity reservaAtualizada = reservaRepository.save(reserva);
        log.info("Reserva confirmada e chalé marcado como OCUPADO: {}", id);

        return mapper.paraReservaResponseDTO(reservaAtualizada);
    }

    @Transactional
    public ReservaResponseDTO cancelarReserva(Long id) {
        log.info("Cancelando reserva: {}", id);
        ReservaEntity reserva = reservaRepository.findById(id)
                .orElseThrow(ReservaNaoEncontradaException::new);

        if (reserva.getStatusReserva() == StatusReserva.CANCELADA) {
            log.warn("Tentativa de cancelar reserva já cancelada: {}", id);
            throw new IllegalStateException("Reserva já está cancelada");
        }

        if (reserva.getStatusReserva() == StatusReserva.CONCLUIDA) {
            log.warn("Tentativa de cancelar reserva já concluída: {}", id);
            throw new IllegalStateException("Não é possível cancelar reserva já concluída");
        }

        reserva.setStatusReserva(StatusReserva.CANCELADA);

        // Se a reserva estava confirmada, liberar o chalé
        if (reserva.getStatusReserva() == StatusReserva.CONFIRMADA) {
            ChaleEntity chale = reserva.getChale();
            chale.setStatus(ChaleEntity.StatusChale.DISPONIVEL);
            chaleRepository.save(chale);
        }

        ReservaEntity reservaAtualizada = reservaRepository.save(reserva);
        log.info("Reserva cancelada: {}", id);

        return mapper.paraReservaResponseDTO(reservaAtualizada);
    }

    @Transactional
    public ReservaResponseDTO concluirReserva(Long id) {
        log.info("Concluindo reserva: {}", id);
        ReservaEntity reserva = reservaRepository.findById(id)
                .orElseThrow(ReservaNaoEncontradaException::new);

        if (reserva.getStatusReserva() != StatusReserva.CONFIRMADA) {
            log.warn("Tentativa de concluir reserva que não está confirmada: {}", id);
            throw new IllegalStateException("Apenas reservas confirmadas podem ser concluídas");
        }

        reserva.setStatusReserva(StatusReserva.CONCLUIDA);

        // Atualizar status do chalé para LIMPEZA
        ChaleEntity chale = reserva.getChale();
        chale.setStatus(ChaleEntity.StatusChale.LIMPEZA);
        chaleRepository.save(chale);

        ReservaEntity reservaAtualizada = reservaRepository.save(reserva);
        log.info("Reserva concluída e chalé marcado como LIMPEZA: {}", id);

        return mapper.paraReservaResponseDTO(reservaAtualizada);
    }

    @Transactional
    public void deletarReserva(Long id) {
        log.info("Deletando reserva: {}", id);
        if (!reservaRepository.existsById(id)) {
            log.error("Tentativa de deletar reserva inexistente: {}", id);
            throw new ReservaNaoEncontradaException();
        }
        reservaRepository.deleteById(id);
        log.info("Reserva deletada: {}", id);
    }

    // ======================== MÉTODOS AUXILIARES ========================

    private void validarDatas(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn.isAfter(checkOut)) {
            throw new DataReservaInvalidaException("Data de check-in não pode ser posterior à data de check-out");
        }

        if (checkIn.isEqual(checkOut)) {
            throw new DataReservaInvalidaException("Data de check-in não pode ser igual à data de check-out");
        }

        if (checkIn.isBefore(LocalDate.now())) {
            throw new DataReservaInvalidaException("Data de check-in não pode ser anterior à data atual");
        }
    }

    private BigDecimal calcularValorTotal(BigDecimal valorDiaria, LocalDate checkIn, LocalDate checkOut) {
        long numeroDiarias = ChronoUnit.DAYS.between(checkIn, checkOut);
        return valorDiaria.multiply(BigDecimal.valueOf(numeroDiarias));
    }
}
