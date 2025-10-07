package sptech.school.Lodgfy.business;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sptech.school.Lodgfy.business.dto.HospedeRequestDTO;
import sptech.school.Lodgfy.business.dto.HospedeResponseDTO;
import sptech.school.Lodgfy.business.exceptions.HospedeJaExisteException;
import sptech.school.Lodgfy.business.exceptions.BadRequestException;
import sptech.school.Lodgfy.business.exceptions.ConflictException;
import sptech.school.Lodgfy.business.exceptions.ResourceNotFoundException;
import sptech.school.Lodgfy.business.dto.LoginRequestDTO;
import sptech.school.Lodgfy.business.dto.LoginResponseDTO;
import sptech.school.Lodgfy.business.mapsstruct.HospedeMapper;
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
        // Validação de dados obrigatórios
        if (request == null) {
            throw new BadRequestException("Dados do hóspede são obrigatórios");
        }

        if (request.getCpf() == null || request.getCpf().trim().isEmpty()) {
            throw new BadRequestException("CPF é obrigatório");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Email é obrigatório");
        }

        if (request.getNome() == null || request.getNome().trim().isEmpty()) {
            throw new BadRequestException("Nome é obrigatório");
        }

        String cpfNormalizado = request.getCpf().replaceAll("\\D", "");

        // Validação de CPF
        if (cpfNormalizado.length() != 11) {
            throw new BadRequestException("CPF deve conter 11 dígitos");
        }

        request.setCpf(cpfNormalizado);

        if (repository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email já está em uso");
        }

        if (repository.existsByCpf(request.getCpf())) {
            throw new HospedeJaExisteException(request.getCpf());
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
        List<HospedeResponseDTO> hospedes = mapper.paraListaHospedeResponseDTO(repository.findAll());

        if (hospedes.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum hóspede encontrado");
        }

        return hospedes;
    }

    public Optional<HospedeResponseDTO> buscarPorCpf(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            throw new BadRequestException("CPF é obrigatório para busca");
        }

        String cpfNormalizado = cpf.replaceAll("\\D", "");

        if (cpfNormalizado.length() != 11) {
            throw new BadRequestException("CPF deve conter 11 dígitos");
        }

        return repository.findByCpf(cpfNormalizado)
        return repository.findByCpf(cpf)
                .map(mapper::paraHospedeResponseDTO);
    }

    public void deletarHospedePorId(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("ID inválido");
        }

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Hóspede não encontrado com ID: " + id);
        }

        repository.deleteById(id);
    }

    public Optional<HospedeResponseDTO> atualizarHospede(Long id, HospedeRequestDTO hospedeAtualizado) {
        if (id == null || id <= 0) {
            throw new BadRequestException("ID inválido");
        }

        if (hospedeAtualizado == null) {
            throw new BadRequestException("Dados do hóspede são obrigatórios");
        }

        if (hospedeAtualizado.getCpf() == null || hospedeAtualizado.getCpf().trim().isEmpty()) {
            throw new BadRequestException("CPF é obrigatório");
        }

        if (hospedeAtualizado.getEmail() == null || hospedeAtualizado.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Email é obrigatório");
        }

        if (hospedeAtualizado.getNome() == null || hospedeAtualizado.getNome().trim().isEmpty()) {
            throw new BadRequestException("Nome é obrigatório");
        }

        return repository.findById(id)
                .map(hospede -> {
                    String cpfNormalizado = hospedeAtualizado.getCpf().replaceAll("\\D", "");

                    if (cpfNormalizado.length() != 11) {
                        throw new BadRequestException("CPF deve conter 11 dígitos");
                    }

                    hospedeAtualizado.setCpf(cpfNormalizado);

                    if (!hospede.getEmail().equals(hospedeAtualizado.getEmail()) &&
                            repository.existsByEmail(hospedeAtualizado.getEmail())) {
                        throw new BadRequestException("Email já está em uso");
                        throw new EmailJaExisteException();
                    }

                    if (!hospede.getCpf().equals(hospedeAtualizado.getCpf()) &&
                            repository.existsByCpf(hospedeAtualizado.getCpf())) {
                        throw new HospedeJaExisteException(hospedeAtualizado.getCpf());
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
        if (nome == null || nome.trim().isEmpty()) {
            throw new BadRequestException("Nome é obrigatório para busca");
        }

        if (nome.trim().length() < 2) {
            throw new BadRequestException("Nome deve ter pelo menos 2 caracteres");
        }

        List<HospedeResponseDTO> hospedes = mapper.paraListaHospedeResponseDTO(
                repository.findByNomeContainingIgnoreCase(nome.trim()));

        if (hospedes.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum hóspede encontrado com o nome: " + nome);
        }

        return hospedes;
    }

    public Optional<HospedeResponseDTO> login(String cpf, String senha) {
        if (cpf == null || cpf.trim().isEmpty()) {
            throw new BadRequestException("CPF é obrigatório para login");
        }

        if (senha == null || senha.trim().isEmpty()) {
            throw new BadRequestException("Senha é obrigatória para login");
        }

        String cpfNormalizado = cpf.replaceAll("\\D", "");

        if (cpfNormalizado.length() != 11) {
            throw new BadRequestException("CPF deve conter 11 dígitos");
        }

        return repository.findByCpf(cpfNormalizado)
                .stream()
                .filter(h -> h.getSenha().equals(senha))
                .findFirst()
                .map(mapper::paraHospedeResponseDTO);
    }

}
