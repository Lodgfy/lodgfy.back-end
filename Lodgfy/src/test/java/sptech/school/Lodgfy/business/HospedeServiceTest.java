package sptech.school.Lodgfy.business;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import sptech.school.Lodgfy.business.dto.*;
import sptech.school.Lodgfy.business.exceptions.*;
import sptech.school.Lodgfy.business.mapsstruct.HospedeMapper;
import sptech.school.Lodgfy.infrastructure.entities.HospedeEntity;
import sptech.school.Lodgfy.infrastructure.repository.HospedeRepository;
import sptech.school.Lodgfy.security.enums.Role;
import sptech.school.Lodgfy.security.jwt.JwtService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HospedeService - Testes Unitários")
public class HospedeServiceTest {

    @Mock
    private HospedeRepository repository;

    @Mock
    private HospedeMapper mapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private HospedeService service;

    private HospedeEntity hospedeMock;
    private HospedeRequestDTO requestDTO;
    private HospedeResponseDTO responseDTO;
    private LoginRequestDTO loginRequestDTO;
    private HospedeSignUpRequestDTO signUpRequestDTO;

    @BeforeEach
    void setUp() {
        // Inicializar HospedeEntity
        hospedeMock = new HospedeEntity();
        hospedeMock.setId(1L);
        hospedeMock.setNome("João da Silva");
        hospedeMock.setEmail("joao@email.com");
        hospedeMock.setTelefone("11987654321");
        hospedeMock.setCpf("12345678900");
        hospedeMock.setSenha("$2a$10$hashedPassword123");
        hospedeMock.setDataNascimento(LocalDate.of(1990, 1, 1));
        hospedeMock.setRole(Role.HOSPEDE);

        // Inicializar HospedeRequestDTO
        requestDTO = new HospedeRequestDTO();
        requestDTO.setNome("João da Silva");
        requestDTO.setEmail("joao@email.com");
        requestDTO.setTelefone("11987654321");
        requestDTO.setCpf("12345678900");
        requestDTO.setSenha("Senha@123");
        requestDTO.setDataNascimento(LocalDate.of(1990, 1, 1));
        requestDTO.setRole(Role.HOSPEDE);

        // Inicializar HospedeResponseDTO
        responseDTO = new HospedeResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setNome("João da Silva");
        responseDTO.setEmail("joao@email.com");
        responseDTO.setTelefone("11987654321");
        responseDTO.setCpf("12345678900");
        responseDTO.setDataNascimento(LocalDate.of(1990, 1, 1));
        responseDTO.setRole(Role.HOSPEDE);

        // Inicializar LoginRequestDTO
        loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setCpf("12345678900");
        loginRequestDTO.setSenha("Senha@123");

        // Inicializar HospedeSignUpRequestDTO
        signUpRequestDTO = new HospedeSignUpRequestDTO();
        signUpRequestDTO.setNome("João da Silva");
        signUpRequestDTO.setEmail("joao@email.com");
        signUpRequestDTO.setTelefone("11987654321");
        signUpRequestDTO.setCpf("12345678900");
        signUpRequestDTO.setSenha("Senha@123");
        signUpRequestDTO.setDataNascimento(LocalDate.of(1990, 1, 1));
    }

    // ======================== TESTES: salvarHospede() ========================

    @Test
    @DisplayName("Deve salvar um hóspede com sucesso quando email e CPF não existem")
    void testSalvarHospedeComSucesso() {
        // Arrange
        when(repository.existsByEmail(requestDTO.getEmail())).thenReturn(false);
        when(repository.existsByCpf(requestDTO.getCpf())).thenReturn(false);
        when(passwordEncoder.encode(requestDTO.getSenha())).thenReturn("$2a$10$hashedPassword123");
        when(mapper.paraHospedeEntity(requestDTO)).thenReturn(hospedeMock);
        when(repository.save(any(HospedeEntity.class))).thenReturn(hospedeMock);
        when(mapper.paraHospedeResponseDTO(hospedeMock)).thenReturn(responseDTO);

        // Act
        HospedeResponseDTO resultado = service.salvarHospede(requestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("João da Silva", resultado.getNome());
        assertEquals("joao@email.com", resultado.getEmail());
        verify(repository, times(1)).existsByEmail(requestDTO.getEmail());
        verify(repository, times(1)).existsByCpf(requestDTO.getCpf());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(repository, times(1)).save(any(HospedeEntity.class));
    }

    @Test
    @DisplayName("Deve lançar EmailJaExisteException quando email já existe")
    void testSalvarHospedeComEmailExistente() {
        // Arrange
        when(repository.existsByEmail(requestDTO.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(EmailJaExisteException.class, () -> service.salvarHospede(requestDTO));
        verify(repository, times(1)).existsByEmail(requestDTO.getEmail());
        verify(repository, never()).existsByCpf(anyString());
        verify(repository, never()).save(any(HospedeEntity.class));
    }

    @Test
    @DisplayName("Deve lançar CpfJaExisteException quando CPF já existe")
    void testSalvarHospedeComCpfExistente() {
        // Arrange
        when(repository.existsByEmail(requestDTO.getEmail())).thenReturn(false);
        when(repository.existsByCpf(requestDTO.getCpf())).thenReturn(true);

        // Act & Assert
        assertThrows(CpfJaExisteException.class, () -> service.salvarHospede(requestDTO));
        verify(repository, times(1)).existsByEmail(requestDTO.getEmail());
        verify(repository, times(1)).existsByCpf(requestDTO.getCpf());
        verify(repository, never()).save(any(HospedeEntity.class));
    }

    @Test
    @DisplayName("Deve normalizar CPF removendo caracteres especiais ao salvar")
    void testSalvarHospedeNormalizaCpf() {
        // Arrange
        requestDTO.setCpf("123.456.789-00");
        when(repository.existsByEmail(requestDTO.getEmail())).thenReturn(false);
        when(repository.existsByCpf("12345678900")).thenReturn(false);
        when(passwordEncoder.encode(requestDTO.getSenha())).thenReturn("$2a$10$hashedPassword123");
        when(mapper.paraHospedeEntity(requestDTO)).thenReturn(hospedeMock);
        when(repository.save(any(HospedeEntity.class))).thenReturn(hospedeMock);
        when(mapper.paraHospedeResponseDTO(hospedeMock)).thenReturn(responseDTO);

        // Act
        service.salvarHospede(requestDTO);

        // Assert
        assertEquals("12345678900", requestDTO.getCpf());
        verify(repository, times(1)).existsByCpf("12345678900");
    }

    @Test
    @DisplayName("Deve criptografar senha ao salvar hóspede")
    void testSalvarHospedeCriptografaSenha() {
        // Arrange
        String senhaOriginal = "Senha@123";
        requestDTO.setSenha(senhaOriginal);
        when(repository.existsByEmail(requestDTO.getEmail())).thenReturn(false);
        when(repository.existsByCpf(requestDTO.getCpf())).thenReturn(false);
        when(passwordEncoder.encode(senhaOriginal)).thenReturn("$2a$10$hashedPassword123");
        when(mapper.paraHospedeEntity(requestDTO)).thenReturn(hospedeMock);
        when(repository.save(any(HospedeEntity.class))).thenReturn(hospedeMock);
        when(mapper.paraHospedeResponseDTO(hospedeMock)).thenReturn(responseDTO);

        // Act
        service.salvarHospede(requestDTO);

        // Assert
        verify(passwordEncoder, times(1)).encode(senhaOriginal);
        assertEquals("$2a$10$hashedPassword123", requestDTO.getSenha());
    }

    // ======================== TESTES: login() ========================

    @Test
    @DisplayName("Deve realizar login com sucesso quando credenciais estão corretas")
    void testLoginComSucesso() {
        // Arrange
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        when(repository.findByCpf("12345678900")).thenReturn(Optional.of(hospedeMock));
        when(passwordEncoder.matches("Senha@123", hospedeMock.getSenha())).thenReturn(true);
        when(jwtService.generateToken(
                hospedeMock.getCpf(),
                hospedeMock.getRole(),
                hospedeMock.getId()
        )).thenReturn(token);

        // Act
        LoginResponseDTO resultado = service.login(loginRequestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(token, resultado.getToken());
        assertEquals(hospedeMock.getId(), resultado.getId());
        assertEquals(hospedeMock.getCpf(), resultado.getCpf());
        assertEquals(hospedeMock.getNome(), resultado.getNome());
        verify(repository, times(1)).findByCpf("12345678900");
        verify(passwordEncoder, times(1)).matches("Senha@123", hospedeMock.getSenha());
    }

    @Test
    @DisplayName("Deve lançar CpfNaoEncontradoException quando CPF não existe")
    void testLoginComCpfInexistente() {
        // Arrange
        when(repository.findByCpf("12345678900")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CpfNaoEncontradoException.class, () -> service.login(loginRequestDTO));
        verify(repository, times(1)).findByCpf("12345678900");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Deve lançar SenhaIncorretaException quando senha está incorreta")
    void testLoginComSenhaIncorreta() {
        // Arrange
        when(repository.findByCpf("12345678900")).thenReturn(Optional.of(hospedeMock));
        when(passwordEncoder.matches("Senha@123", hospedeMock.getSenha())).thenReturn(false);

        // Act & Assert
        assertThrows(SenhaIncorretaException.class, () -> service.login(loginRequestDTO));
        verify(repository, times(1)).findByCpf("12345678900");
        verify(passwordEncoder, times(1)).matches("Senha@123", hospedeMock.getSenha());
        verify(jwtService, never()).generateToken(anyString(), any(), anyLong());
    }

    @Test
    @DisplayName("Deve lançar CpfNaoEncontradoException quando CPF é nulo")
    void testLoginComCpfNulo() {
        // Arrange
        loginRequestDTO.setCpf(null);

        // Act & Assert
        assertThrows(CpfNaoEncontradoException.class, () -> service.login(loginRequestDTO));
        verify(repository, never()).findByCpf(anyString());
    }

    @Test
    @DisplayName("Deve lançar SenhaIncorretaException quando senha é nula")
    void testLoginComSenhaNula() {
        // Arrange
        loginRequestDTO.setSenha(null);

        // Act & Assert
        assertThrows(SenhaIncorretaException.class, () -> service.login(loginRequestDTO));
        verify(repository, never()).findByCpf(anyString());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando senha é hash BCrypt")
    void testLoginComHashBCrypt() {
        // Arrange
        loginRequestDTO.setSenha("$2a$10$hashedPassword123");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.login(loginRequestDTO));
        verify(repository, never()).findByCpf(anyString());
    }

    @Test
    @DisplayName("Deve normalizar CPF ao fazer login")
    void testLoginNormalizaCpf() {
        // Arrange
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        loginRequestDTO.setCpf("123.456.789-00");
        when(repository.findByCpf("12345678900")).thenReturn(Optional.of(hospedeMock));
        when(passwordEncoder.matches("Senha@123", hospedeMock.getSenha())).thenReturn(true);
        when(jwtService.generateToken(anyString(), any(), anyLong())).thenReturn(token);

        // Act
        service.login(loginRequestDTO);

        // Assert
        verify(repository, times(1)).findByCpf("12345678900");
    }

    // ======================== TESTES: listarHospedes() ========================

    @Test
    @DisplayName("Deve retornar lista vazia quando não há hóspedes")
    void testListarHospedesVazia() {
        // Arrange
        when(repository.findAll()).thenReturn(new ArrayList<>());
        when(mapper.paraListaHospedeResponseDTO(new ArrayList<>())).thenReturn(new ArrayList<>());

        // Act
        List<HospedeResponseDTO> resultado = service.listarHospedes();

        // Assert
        assertTrue(resultado.isEmpty());
        verify(repository, times(1)).findAll();
        verify(mapper, times(1)).paraListaHospedeResponseDTO(new ArrayList<>());
    }

    @Test
    @DisplayName("Deve retornar lista com todos os hóspedes cadastrados")
    void testListarHospedesComMultiplosHospedes() {
        // Arrange
        HospedeEntity hospede2 = new HospedeEntity();
        hospede2.setId(2L);
        hospede2.setNome("Maria Silva");
        hospede2.setEmail("maria@email.com");
        hospede2.setCpf("98765432100");

        List<HospedeEntity> hospedes = List.of(hospedeMock, hospede2);
        HospedeResponseDTO response2 = new HospedeResponseDTO();
        response2.setId(2L);
        response2.setNome("Maria Silva");
        response2.setEmail("maria@email.com");

        List<HospedeResponseDTO> responses = List.of(responseDTO, response2);

        when(repository.findAll()).thenReturn(hospedes);
        when(mapper.paraListaHospedeResponseDTO(hospedes)).thenReturn(responses);

        // Act
        List<HospedeResponseDTO> resultado = service.listarHospedes();

        // Assert
        assertEquals(2, resultado.size());
        assertEquals("João da Silva", resultado.get(0).getNome());
        assertEquals("Maria Silva", resultado.get(1).getNome());
        verify(repository, times(1)).findAll();
    }

    // ======================== TESTES: buscarPorCpf() ========================

    @Test
    @DisplayName("Deve retornar Optional com hóspede quando CPF existe")
    void testBuscarPorCpfComSucesso() {
        // Arrange
        when(repository.findByCpf("12345678900")).thenReturn(Optional.of(hospedeMock));
        when(mapper.paraHospedeResponseDTO(hospedeMock)).thenReturn(responseDTO);

        // Act
        Optional<HospedeResponseDTO> resultado = service.buscarPorCpf("12345678900");

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(responseDTO.getId(), resultado.get().getId());
        assertEquals("João da Silva", resultado.get().getNome());
        verify(repository, times(1)).findByCpf("12345678900");
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando CPF não existe")
    void testBuscarPorCpfNaoEncontrado() {
        // Arrange
        when(repository.findByCpf("99999999999")).thenReturn(Optional.empty());

        // Act
        Optional<HospedeResponseDTO> resultado = service.buscarPorCpf("99999999999");

        // Assert
        assertFalse(resultado.isPresent());
        verify(repository, times(1)).findByCpf("99999999999");
    }

    // ======================== TESTES: deletarHospedePorId() ========================

    @Test
    @DisplayName("Deve lançar exceção quando hóspede não existe")
    void testDeletarHospedeNaoExistente() {
        // Arrange
        when(repository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> service.deletarHospedePorId(999L));
        verify(repository, times(1)).existsById(999L);
        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve deletar hóspede com sucesso quando existe")
    void testDeletarHospedeComSucesso() {
        // Arrange
        when(repository.existsById(1L)).thenReturn(true);

        // Act
        service.deletarHospedePorId(1L);

        // Assert
        verify(repository, times(1)).existsById(1L);
        verify(repository, times(1)).deleteById(1L);
    }

    // ======================== TESTES: atualizarHospede() ========================

    @Test
    @DisplayName("Deve retornar Optional vazio quando hóspede não existe")
    void testAtualizarHospedeNaoExistente() {
        // Arrange
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<HospedeResponseDTO> resultado = service.atualizarHospede(999L, requestDTO);

        // Assert
        assertFalse(resultado.isPresent());
        verify(repository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Deve atualizar hóspede com sucesso quando existe")
    void testAtualizarHospedeComSucesso() {
        // Arrange
        HospedeRequestDTO requestAtualizado = new HospedeRequestDTO();
        requestAtualizado.setNome("João Silva Atualizado");
        requestAtualizado.setEmail("joao@email.com");
        requestAtualizado.setCpf("12345678900");
        requestAtualizado.setTelefone("11999999999");
        requestAtualizado.setDataNascimento(LocalDate.of(1990, 1, 1));
        requestAtualizado.setSenha("");

        when(repository.findById(1L)).thenReturn(Optional.of(hospedeMock));
        when(repository.save(hospedeMock)).thenReturn(hospedeMock);
        when(mapper.paraHospedeResponseDTO(hospedeMock)).thenReturn(responseDTO);

        // Act
        Optional<HospedeResponseDTO> resultado = service.atualizarHospede(1L, requestAtualizado);

        // Assert
        assertTrue(resultado.isPresent());
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(hospedeMock);
    }

    @Test
    @DisplayName("Deve lançar EmailJaExisteException quando email já está em uso por outro hóspede")
    void testAtualizarHospedeComEmailEmUso() {
        // Arrange
        HospedeRequestDTO requestAtualizado = new HospedeRequestDTO();
        requestAtualizado.setNome("João Silva");
        requestAtualizado.setEmail("outro@email.com");
        requestAtualizado.setCpf("12345678900");
        requestAtualizado.setTelefone("11987654321");
        requestAtualizado.setDataNascimento(LocalDate.of(1990, 1, 1));

        when(repository.findById(1L)).thenReturn(Optional.of(hospedeMock));
        when(repository.existsByEmail("outro@email.com")).thenReturn(true);

        // Act & Assert
        assertThrows(EmailJaExisteException.class, () -> service.atualizarHospede(1L, requestAtualizado));
        verify(repository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar CpfJaExisteException quando CPF já está em uso por outro hóspede")
    void testAtualizarHospedeComCpfEmUso() {
        // Arrange
        HospedeRequestDTO requestAtualizado = new HospedeRequestDTO();
        requestAtualizado.setNome("João Silva");
        requestAtualizado.setEmail("joao@email.com");
        requestAtualizado.setCpf("98765432100");
        requestAtualizado.setTelefone("11987654321");
        requestAtualizado.setDataNascimento(LocalDate.of(1990, 1, 1));

        when(repository.findById(1L)).thenReturn(Optional.of(hospedeMock));
        when(repository.existsByCpf("98765432100")).thenReturn(true);

        // Act & Assert
        assertThrows(CpfJaExisteException.class, () -> service.atualizarHospede(1L, requestAtualizado));
        verify(repository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve criptografar nova senha ao atualizar")
    void testAtualizarHospedeCriptografaNovaSenha() {
        // Arrange
        HospedeRequestDTO requestAtualizado = new HospedeRequestDTO();
        requestAtualizado.setNome("João Silva");
        requestAtualizado.setEmail("joao@email.com");
        requestAtualizado.setCpf("12345678900");
        requestAtualizado.setTelefone("11987654321");
        requestAtualizado.setDataNascimento(LocalDate.of(1990, 1, 1));
        requestAtualizado.setSenha("NovaSenha@123");

        when(repository.findById(1L)).thenReturn(Optional.of(hospedeMock));
        when(passwordEncoder.encode("NovaSenha@123")).thenReturn("$2a$10$newHashedPassword");
        when(repository.save(hospedeMock)).thenReturn(hospedeMock);
        when(mapper.paraHospedeResponseDTO(hospedeMock)).thenReturn(responseDTO);

        // Act
        service.atualizarHospede(1L, requestAtualizado);

        // Assert
        verify(passwordEncoder, times(1)).encode("NovaSenha@123");
    }

    // ======================== TESTES: buscarPorNome() ========================

    @Test
    @DisplayName("Deve buscar hóspedes por nome contendo texto")
    void testBuscarPorNome() {
        // Arrange
        List<HospedeEntity> hospedes = List.of(hospedeMock);
        List<HospedeResponseDTO> responses = List.of(responseDTO);

        when(repository.findByNomeContainingIgnoreCase("Silva")).thenReturn(hospedes);
        when(mapper.paraListaHospedeResponseDTO(hospedes)).thenReturn(responses);

        // Act
        List<HospedeResponseDTO> resultado = service.buscarPorNome("Silva");

        // Assert
        assertEquals(1, resultado.size());
        assertEquals("João da Silva", resultado.get(0).getNome());
        verify(repository, times(1)).findByNomeContainingIgnoreCase("Silva");
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nenhum nome corresponde")
    void testBuscarPorNomeVazia() {
        // Arrange
        when(repository.findByNomeContainingIgnoreCase("Inexistente")).thenReturn(new ArrayList<>());
        when(mapper.paraListaHospedeResponseDTO(new ArrayList<>())).thenReturn(new ArrayList<>());

        // Act
        List<HospedeResponseDTO> resultado = service.buscarPorNome("Inexistente");

        // Assert
        assertTrue(resultado.isEmpty());
        verify(repository, times(1)).findByNomeContainingIgnoreCase("Inexistente");
    }

    // ======================== TESTES: autocadastrar() ========================

    @Test
    @DisplayName("Deve realizar autocadastro com sucesso")
    void testAutocadastrarComSucesso() {
        // Arrange
        when(repository.existsByCpf("12345678900")).thenReturn(false);
        when(repository.existsByEmail("joao@email.com")).thenReturn(false);
        when(passwordEncoder.encode("Senha@123")).thenReturn("$2a$10$hashedPassword123");
        when(repository.save(any(HospedeEntity.class))).thenReturn(hospedeMock);
        when(mapper.paraHospedeResponseDTO(hospedeMock)).thenReturn(responseDTO);

        // Act
        HospedeResponseDTO resultado = service.autocadastrar(signUpRequestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(responseDTO.getId(), resultado.getId());
        assertEquals(responseDTO.getNome(), resultado.getNome());
        verify(repository, times(1)).existsByCpf("12345678900");
        verify(repository, times(1)).existsByEmail("joao@email.com");
        verify(passwordEncoder, times(1)).encode("Senha@123");
        verify(repository, times(1)).save(any(HospedeEntity.class));
    }

    @Test
    @DisplayName("Deve lançar CpfJaExisteException no autocadastro quando CPF existe")
    void testAutocadastrarComCpfExistente() {
        // Arrange
        when(repository.existsByCpf("12345678900")).thenReturn(true);

        // Act & Assert
        assertThrows(CpfJaExisteException.class, () -> service.autocadastrar(signUpRequestDTO));
        verify(repository, times(1)).existsByCpf("12345678900");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar EmailJaExisteException no autocadastro quando email existe")
    void testAutocadastrarComEmailExistente() {
        // Arrange
        when(repository.existsByCpf("12345678900")).thenReturn(false);
        when(repository.existsByEmail("joao@email.com")).thenReturn(true);

        // Act & Assert
        assertThrows(EmailJaExisteException.class, () -> service.autocadastrar(signUpRequestDTO));
        verify(repository, times(1)).existsByCpf("12345678900");
        verify(repository, times(1)).existsByEmail("joao@email.com");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve normalizar CPF ao realizar autocadastro")
    void testAutocadastrarNormalizaCpf() {
        // Arrange
        signUpRequestDTO.setCpf("123.456.789-00");
        when(repository.existsByCpf("12345678900")).thenReturn(false);
        when(repository.existsByEmail("joao@email.com")).thenReturn(false);
        when(passwordEncoder.encode("Senha@123")).thenReturn("$2a$10$hashedPassword123");
        when(repository.save(any(HospedeEntity.class))).thenReturn(hospedeMock);
        when(mapper.paraHospedeResponseDTO(hospedeMock)).thenReturn(responseDTO);

        // Act
        service.autocadastrar(signUpRequestDTO);

        // Assert
        verify(repository, times(1)).existsByCpf("12345678900");
    }

    @Test
    @DisplayName("Deve atribuir Role.HOSPEDE automaticamente no autocadastro")
    void testAutocadastrarAtribuiRoleHospede() {
        // Arrange
        when(repository.existsByCpf("12345678900")).thenReturn(false);
        when(repository.existsByEmail("joao@email.com")).thenReturn(false);
        when(passwordEncoder.encode("Senha@123")).thenReturn("$2a$10$hashedPassword123");
        when(repository.save(any(HospedeEntity.class))).thenAnswer(invocation -> {
            HospedeEntity entity = invocation.getArgument(0);
            assertEquals(Role.HOSPEDE, entity.getRole());
            return hospedeMock;
        });
        when(mapper.paraHospedeResponseDTO(hospedeMock)).thenReturn(responseDTO);

        // Act
        service.autocadastrar(signUpRequestDTO);

        // Assert
        verify(repository, times(1)).save(any(HospedeEntity.class));
    }
}

