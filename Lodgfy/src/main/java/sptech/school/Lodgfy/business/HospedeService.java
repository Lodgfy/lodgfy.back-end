package sptech.school.Lodgfy.business;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import sptech.school.Lodgfy.business.dto.HospedeRequestDTO;
import sptech.school.Lodgfy.business.dto.HospedeResponseDTO;
import sptech.school.Lodgfy.business.dto.LoginRequestDTO;
import sptech.school.Lodgfy.business.dto.LoginResponseDTO;
import sptech.school.Lodgfy.business.mapsstruct.HospedeMapper;
import sptech.school.Lodgfy.infrastructure.entities.HospedeEntity;
import sptech.school.Lodgfy.infrastructure.repository.HospedeRepository;
import sptech.school.Lodgfy.security.jwt.JwtService;
import sptech.school.Lodgfy.business.exceptions.EmailJaExisteException;
import sptech.school.Lodgfy.business.exceptions.CpfJaExisteException;
import sptech.school.Lodgfy.business.exceptions.SenhaIncorretaException;
import sptech.school.Lodgfy.business.exceptions.CpfNaoEncontradoException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class HospedeService {

    private final HospedeRepository repository;
    private final HospedeMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public HospedeResponseDTO salvarHospede(HospedeRequestDTO request) {
        String cpfNormalizado = request.getCpf().replaceAll("\\D", "");
        request.setCpf(cpfNormalizado);

        if (repository.existsByEmail(request.getEmail())) {
            throw new EmailJaExisteException();
        }

        if (repository.existsByCpf(request.getCpf())) {
            throw new CpfJaExisteException();
        }

        // Hash da senha antes de salvar
        request.setSenha(passwordEncoder.encode(request.getSenha()));

        return mapper.paraHospedeResponseDTO(
                repository.save(
                        mapper.paraHospedeEntity(request)));
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        log.info("Iniciando processo de login para CPF: {}", loginRequest.getCpf());

        // Validações básicas com logs mais detalhados
        if (loginRequest.getCpf() == null || loginRequest.getCpf().trim().isEmpty()) {
            log.error("CPF não fornecido na requisição de login");
            throw new CpfNaoEncontradoException();
        }

        if (loginRequest.getSenha() == null || loginRequest.getSenha().trim().isEmpty()) {
            log.error("Senha não fornecida na requisição de login");
            throw new SenhaIncorretaException();
        }

        String cpfNormalizado = loginRequest.getCpf().replaceAll("\\D", "");
        log.info("CPF normalizado: {}", cpfNormalizado);

        // Busca o hóspede no banco
        Optional<HospedeEntity> hospedeOpt = repository.findByCpf(cpfNormalizado);
        if (hospedeOpt.isEmpty()) {
            log.warn("CPF não encontrado no banco de dados: {}", cpfNormalizado);
            throw new CpfNaoEncontradoException();
        }

        HospedeEntity hospede = hospedeOpt.get();
        log.info("Hóspede encontrado: {} (ID: {})", hospede.getNome(), hospede.getId());

        // Verifica a senha
        boolean senhaCorreta = passwordEncoder.matches(loginRequest.getSenha(), hospede.getSenha());
        log.info("Resultado da verificação de senha: {}", senhaCorreta);

        if (!senhaCorreta) {
            log.warn("Senha incorreta para CPF: {}", cpfNormalizado);
            throw new SenhaIncorretaException();
        }

        log.info("Senha validada com sucesso para CPF: {}", cpfNormalizado);

        // Gera o token JWT
        try {
            String token = jwtService.generateToken(
                    hospede.getCpf(),
                    hospede.getRole(),
                    hospede.getId()
            );
            log.info("Token JWT gerado com sucesso para CPF: {}", cpfNormalizado);

            LoginResponseDTO response = new LoginResponseDTO(
                    token,
                    hospede.getId(),
                    hospede.getCpf(),
                    hospede.getNome(),
                    hospede.getRole(),
                    86400000L // 24 horas em milissegundos
            );

            log.info("Login concluído com sucesso para CPF: {}", cpfNormalizado);
            return response;

        } catch (Exception e) {
            log.error("Erro ao gerar token JWT para CPF {}: {}", cpfNormalizado, e.getMessage());
            throw new RuntimeException("Erro interno durante o login");
        }
    }

    public List<HospedeResponseDTO> listarHospedes() {
        return mapper.paraListaHospedeResponseDTO(
                repository.findAll());
    }

    public Optional<HospedeResponseDTO> buscarPorCpf(String cpf) {
        return repository.findByCpf(cpf)
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

                    String cpfNormalizado = hospedeAtualizado.getCpf().replaceAll("\\D", "");
                    hospedeAtualizado.setCpf(cpfNormalizado);

                    if (!hospede.getEmail().equals(hospedeAtualizado.getEmail()) &&
                            repository.existsByEmail(hospedeAtualizado.getEmail())) {
                        throw new EmailJaExisteException();
                    }

                    if (!hospede.getCpf().equals(hospedeAtualizado.getCpf()) &&
                            repository.existsByCpf(hospedeAtualizado.getCpf())) {
                        throw new CpfJaExisteException();
                    }

                    hospede.setNome(hospedeAtualizado.getNome());
                    hospede.setCpf(hospedeAtualizado.getCpf());
                    hospede.setEmail(hospedeAtualizado.getEmail());
                    hospede.setTelefone(hospedeAtualizado.getTelefone());
                    hospede.setDataNascimento(hospedeAtualizado.getDataNascimento());

                    if (hospedeAtualizado.getSenha() != null && !hospedeAtualizado.getSenha().isEmpty()) {
                        hospede.setSenha(passwordEncoder.encode(hospedeAtualizado.getSenha()));
                    }

                    return mapper.paraHospedeResponseDTO(repository.save(hospede));
                });
    }

    public List<HospedeResponseDTO> buscarPorNome(String nome) {
        return mapper.paraListaHospedeResponseDTO(
                repository.findByNomeContainingIgnoreCase(nome));
    }


}
