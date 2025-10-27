package sptech.school.Lodgfy.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sptech.school.Lodgfy.business.dto.StatusReserva;
import sptech.school.Lodgfy.infrastructure.entities.ReservaEntity;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<ReservaEntity, Long> {

    List<ReservaEntity> findByHospedeId(Long hospedeId);

    List<ReservaEntity> findByChaleIdChale(Long chaleId);

    List<ReservaEntity> findByStatusReserva(StatusReserva statusReserva);

    /**
     * Verifica se existe conflito de datas para um chalé específico
     * Exclui a própria reserva da verificação (para atualização)
     */
    @Query("SELECT COUNT(r) > 0 FROM ReservaEntity r WHERE r.chale.idChale = :chaleId " +
           "AND r.statusReserva NOT IN ('CANCELADA', 'CONCLUIDA') " +
           "AND (:reservaId IS NULL OR r.idReserva != :reservaId) " +
           "AND ((r.dataCheckIn <= :checkOut AND r.dataCheckOut >= :checkIn))")
    boolean existeConflitoReserva(
            @Param("chaleId") Long chaleId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("reservaId") Long reservaId
    );
}

