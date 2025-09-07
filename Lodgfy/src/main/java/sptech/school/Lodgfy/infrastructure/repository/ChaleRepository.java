package sptech.school.Lodgfy.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sptech.school.Lodgfy.infrastructure.entities.ChaleEntity;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ChaleRepository extends JpaRepository<ChaleEntity, Long> {

    List<ChaleEntity> findByNomeContainsIgnoreCaseOrNumeroContainsIgnoreCase(String nome, String numero);

    List<ChaleEntity> findByValorDiariaLessThanEqual(BigDecimal valor);

    boolean existsByNumero(String numero);
}

