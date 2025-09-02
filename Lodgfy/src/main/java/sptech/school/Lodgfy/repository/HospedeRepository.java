package sptech.school.Lodgfy.repository;

import sptech.school.Lodgfy.entity.Hospede;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HospedeRepository extends JpaRepository<Hospede, Long> {

    List<Hospede> findByCpf(String cpf);
}