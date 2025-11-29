package sptech.school.Lodgfy.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sptech.school.Lodgfy.infrastructure.entities.ChaleEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ChaleRepository extends JpaRepository<ChaleEntity, Long> {

    List<ChaleEntity> findByNomeContainsIgnoreCaseOrNumeroContainsIgnoreCase(String nome, String numero);

    List<ChaleEntity> findByValorDiariaLessThanEqual(BigDecimal valor);

    boolean existsByNumero(String numero);

    @Query("SELECT c FROM ChaleEntity c WHERE c.capacidade >= :quantidadePessoas " +
           "AND c.status = 'DISPONIVEL' " +
           "AND c.idChale NOT IN (" +
           "  SELECT r.chale.idChale FROM ReservaEntity r " +
           "  WHERE r.statusReserva IN ('CONFIRMADA', 'PENDENTE') " +
           "  AND ((r.dataCheckIn < :checkOut AND r.dataCheckOut > :checkIn))" +
           ")")
    List<ChaleEntity> buscarChalesDisponiveis(
            @Param("quantidadePessoas") Integer quantidadePessoas,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );
}
