package sptech.school.Lodgfy.infrastructure.repository;

import jakarta.transaction.Transactional;
import sptech.school.Lodgfy.infrastructure.entities.HospedeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HospedeRepository extends JpaRepository<HospedeEntity, Long> {

    List<HospedeEntity> findByCpf(String cpf);

    List<HospedeEntity> findByNomeContainingIgnoreCase(String nome);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

}