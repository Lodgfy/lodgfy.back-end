package sptech.school.Lodgfy.business;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import sptech.school.Lodgfy.business.dto.HospedeRequestDTO;
import sptech.school.Lodgfy.business.dto.HospedeResponseDTO;
import sptech.school.Lodgfy.business.mapsstruct.HospedeMapper;
import sptech.school.Lodgfy.infrastructure.entities.HospedeEntity;
import sptech.school.Lodgfy.infrastructure.repository.HospedeRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HospedeService {

    private final HospedeRepository repository;
    private final HospedeMapper mapper;

    public HospedeResponseDTO salvarHospede(HospedeRequestDTO request) {

        return mapper.paraHospedeResponseDTO(
                repository.save(
                        mapper.paraHospedeEntity(request)));
    }

    public List<HospedeResponseDTO> listarHospedes() {
        return mapper.paraListaHospedeResponseDTO(
                repository.findAll());
    }

    public Optional<HospedeResponseDTO> buscarPorCpf(String cpf) {
        return repository.findByCpf(cpf)
                .stream()
                .findFirst()
                .map(mapper::paraHospedeResponseDTO);
    }

    public void deletarHospedePorId(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Hóspede não encontrado");
        }
        repository.deleteById(id);
    }

    public Optional<HospedeResponseDTO> atualizarHospede(Long id, HospedeRequestDTO hospedeAtualizado) {
        return repository.findById(id)
                .map(hospede -> {

                    if (!hospede.getEmail().equals(hospedeAtualizado.getEmail()) &&
                            repository.existsByEmail(hospedeAtualizado.getEmail())) {
                        throw new RuntimeException("Email já está em uso");
                    }


                    if (!hospede.getCpf().equals(hospedeAtualizado.getCpf()) &&
                            repository.existsByCpf(hospedeAtualizado.getCpf())) {
                        throw new RuntimeException("CPF já está em uso");
                    }

                    hospede.setNome(hospedeAtualizado.getNome());
                    hospede.setCpf(hospedeAtualizado.getCpf());
                    hospede.setEmail(hospedeAtualizado.getEmail());
                    hospede.setTelefone(hospedeAtualizado.getTelefone());
                    hospede.setDataNascimento(hospedeAtualizado.getDataNascimento());


                    if (hospedeAtualizado.getSenha() != null && !hospedeAtualizado.getSenha().isEmpty()) {
                        hospede.setSenha(hospedeAtualizado.getSenha());
                    }

                    return mapper.paraHospedeResponseDTO(repository.save(hospede));
                });
    }

    public List<HospedeResponseDTO> buscarPorNome(String nome) {
        return mapper.paraListaHospedeResponseDTO(
                repository.findByNomeContainingIgnoreCase(nome));
    }
}
