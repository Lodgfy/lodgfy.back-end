package sptech.school.Lodgfy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sptech.school.Lodgfy.model.Chale;

import java.math.BigDecimal;
import java.util.List;

public interface ChaleRepository extends JpaRepository<Chale, Long> {
    List<Chale> findByNomeContainsIgnoreCaseOrNumeroContainsIgnoreCase(String nome, String numero);
    List<Chale> findByValorDiariaLessThanEqual(BigDecimal valor);

}

