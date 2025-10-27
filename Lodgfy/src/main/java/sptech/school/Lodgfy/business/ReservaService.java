package sptech.school.Lodgfy.business;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final ChaleRepository chaleRepository;
    private final HospedeRepository hospedeRepository;
    private final ReservaMapper mapper;

    public ReservaResponseDTO criarReserva(ReservaRequestDTO request) {
        // Validar datas
        validarDatas(request.getDataCheckIn(), request.getDataCheckOut());

        // Verificar se hóspede existe
        HospedeEntity hospede = hospedeRepository.findById(request.getHospedeId())
                .orElseThrow(() -> new HospedeNaoEncontradoException(
                        "Hóspede com ID " + request.getHospedeId() + " não encontrado"));

        // Verificar se chalé existe
        ChaleEntity chale = chaleRepository.findById(request.getChaleId())
                .orElseThrow(() -> new RuntimeException(
                        "Chalé com ID " + request.getChaleId() + " não encontrado"));

        // Verificar se chalé está disponível
        if (!chale.getDisponivel()) {
            throw new ChaleIndisponivelException(
                    "Chalé " + chale.getNome() + " não está disponível para reserva");
        }

        // Verificar conflitos de reserva
        if (reservaRepository.existeConflitoReserva(
                request.getChaleId(),
                request.getDataCheckIn(),
                request.getDataCheckOut(),
                null)) {
            throw new ReservaConflitanteException(
                    "Já existe uma reserva para este chalé no período solicitado");
        }

        // Criar reserva
        ReservaEntity reserva = mapper.paraReservaEntity(request);
        reserva.setHospede(hospede);
        reserva.setChale(chale);

        // Definir status padrão se não informado
        if (reserva.getStatusReserva() == null) {
            reserva.setStatusReserva(StatusReserva.PENDENTE);
        }

        // Calcular valor total
        BigDecimal valorTotal = calcularValorTotal(
                chale.getValorDiaria(),
                request.getDataCheckIn(),
                request.getDataCheckOut());
        reserva.setValorTotal(valorTotal);

        ReservaEntity reservaSalva = reservaRepository.save(reserva);

        return mapper.paraReservaResponseDTO(reservaSalva);
    }

    public List<ReservaResponseDTO> listarReservas() {
        return mapper.paraListaReservaResponseDTO(reservaRepository.findAll());
    }

    public Optional<ReservaResponseDTO> buscarPorId(Long id) {
        return reservaRepository.findById(id)
                .map(mapper::paraReservaResponseDTO);
    }

    public Optional<ReservaResponseDTO> atualizarReserva(Long id, ReservaRequestDTO request) {
        return reservaRepository.findById(id)
                .map(reserva -> {
                    // Não permitir atualizar reserva cancelada ou concluída
                    if (reserva.getStatusReserva() == StatusReserva.CANCELADA ||
                        reserva.getStatusReserva() == StatusReserva.CONCLUIDA) {
                        throw new IllegalArgumentException(
                                "Não é possível atualizar uma reserva " +
                                reserva.getStatusReserva().toString().toLowerCase());
                    }

                    // Validar novas datas
                    validarDatas(request.getDataCheckIn(), request.getDataCheckOut());

                    // Se mudou o chalé, verificar se existe e está disponível
                    if (!reserva.getChale().getIdChale().equals(request.getChaleId())) {
                        ChaleEntity novoChale = chaleRepository.findById(request.getChaleId())
                                .orElseThrow(() -> new RuntimeException(
                                        "Chalé com ID " + request.getChaleId() + " não encontrado"));

                        if (!novoChale.getDisponivel()) {
                            throw new ChaleIndisponivelException(
                                    "Chalé " + novoChale.getNome() + " não está disponível");
                        }

                        reserva.setChale(novoChale);
                    }

                    // Se mudou o hóspede, verificar se existe
                    if (!reserva.getHospede().getId().equals(request.getHospedeId())) {
                        HospedeEntity novoHospede = hospedeRepository.findById(request.getHospedeId())
                                .orElseThrow(() -> new HospedeNaoEncontradoException(
                                        "Hóspede com ID " + request.getHospedeId() + " não encontrado"));

                        reserva.setHospede(novoHospede);
                    }

                    // Verificar conflitos (excluindo a própria reserva)
                    if (reservaRepository.existeConflitoReserva(
                            request.getChaleId(),
                            request.getDataCheckIn(),
                            request.getDataCheckOut(),
                            id)) {
                        throw new ReservaConflitanteException(
                                "Já existe uma reserva para este chalé no período solicitado");
                    }

                    // Atualizar campos
                    reserva.setDataCheckIn(request.getDataCheckIn());
                    reserva.setDataCheckOut(request.getDataCheckOut());

                    if (request.getStatusReserva() != null) {
                        reserva.setStatusReserva(request.getStatusReserva());
                    }

                    // Recalcular valor total
                    BigDecimal valorTotal = calcularValorTotal(
                            reserva.getChale().getValorDiaria(),
                            request.getDataCheckIn(),
                            request.getDataCheckOut());
                    reserva.setValorTotal(valorTotal);

                    ReservaEntity reservaSalva = reservaRepository.save(reserva);

                    return mapper.paraReservaResponseDTO(reservaSalva);
                });
    }

    public void cancelarReserva(Long id) {
        ReservaEntity reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ReservaNaoEncontradaException(
                        "Reserva com ID " + id + " não encontrada"));

        if (reserva.getStatusReserva() == StatusReserva.CONCLUIDA) {
            throw new IllegalArgumentException("Não é possível cancelar uma reserva já concluída");
        }

        if (reserva.getStatusReserva() == StatusReserva.CANCELADA) {
            throw new IllegalArgumentException("Esta reserva já está cancelada");
        }

        reserva.setStatusReserva(StatusReserva.CANCELADA);
        reservaRepository.save(reserva);
    }

    public List<ReservaResponseDTO> listarReservasPorHospede(Long hospedeId) {
        if (!hospedeRepository.existsById(hospedeId)) {
            throw new HospedeNaoEncontradoException(
                    "Hóspede com ID " + hospedeId + " não encontrado");
        }

        return mapper.paraListaReservaResponseDTO(
                reservaRepository.findByHospedeId(hospedeId));
    }

    public List<ReservaResponseDTO> listarReservasPorChale(Long chaleId) {
        if (!chaleRepository.existsById(chaleId)) {
            throw new RuntimeException("Chalé com ID " + chaleId + " não encontrado");
        }

        return mapper.paraListaReservaResponseDTO(
                reservaRepository.findByChaleIdChale(chaleId));
    }

    public List<ReservaResponseDTO> listarReservasPorStatus(StatusReserva status) {
        return mapper.paraListaReservaResponseDTO(
                reservaRepository.findByStatusReserva(status));
    }

    // Métodos auxiliares privados

    private void validarDatas(LocalDate checkIn, LocalDate checkOut) {
        LocalDate hoje = LocalDate.now();

        if (checkIn == null || checkOut == null) {
            throw new DataReservaInvalidaException("As datas de check-in e check-out são obrigatórias");
        }

        if (checkIn.isBefore(hoje)) {
            throw new DataReservaInvalidaException("A data de check-in não pode ser anterior à data atual");
        }

        if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
            throw new DataReservaInvalidaException(
                    "A data de check-out deve ser posterior à data de check-in");
        }

        long dias = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (dias > 365) {
            throw new DataReservaInvalidaException(
                    "O período de reserva não pode exceder 365 dias");
        }
    }

    private BigDecimal calcularValorTotal(BigDecimal valorDiaria, LocalDate checkIn, LocalDate checkOut) {
        if (valorDiaria == null) {
            valorDiaria = BigDecimal.ZERO;
        }

        long numeroDias = ChronoUnit.DAYS.between(checkIn, checkOut);
        return valorDiaria.multiply(BigDecimal.valueOf(numeroDias));
    }
}

