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

    List<ReservaEntity> findByStatusReserva(StatusReserva status);

    @Query("SELECT r FROM ReservaEntity r WHERE r.chale.idChale = :chaleId " +
            "AND r.statusReserva IN ('PENDENTE', 'CONFIRMADA') " +
            "AND ((r.dataCheckIn <= :checkOut AND r.dataCheckOut >= :checkIn))")
    List<ReservaEntity> findReservasConflitantes(
            @Param("chaleId") Long chaleId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    @Query("SELECT r FROM ReservaEntity r WHERE r.hospede.id = :hospedeId " +
            "ORDER BY r.dataCheckIn DESC")
    List<ReservaEntity> findHistoricoByHospede(@Param("hospedeId") Long hospedeId);
}

