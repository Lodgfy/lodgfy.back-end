package sptech.school.Lodgfy.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sptech.school.Lodgfy.business.ChaleService;
import sptech.school.Lodgfy.business.HospedeService;
import sptech.school.Lodgfy.business.dto.*;
import sptech.school.Lodgfy.business.exceptions.*;
import sptech.school.Lodgfy.infrastructure.entities.ChaleEntity.StatusChale;
import sptech.school.Lodgfy.security.enums.Role;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Testes de Integração - Validação de Regras de Negócio")
class BusinessRulesIntegrationTest {

    @Autowired
    private HospedeService hospedeService;

    @Autowired
    private ChaleService chaleService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Fluxo completo: Cadastro de hóspede, login e consulta")
    void fluxoCompletoHospede() {
        // Arrange
        HospedeRequestDTO novoHospede = criarHospedeValido();

        // Act - Cadastro
        HospedeResponseDTO hospedeCadastrado = hospedeService.salvarHospede(novoHospede);

        // Assert - Cadastro
        assertNotNull(hospedeCadastrado);
        assertNotNull(hospedeCadastrado.getId());
        assertEquals(novoHospede.getNome(), hospedeCadastrado.getNome());
        assertEquals(novoHospede.getEmail(), hospedeCadastrado.getEmail());

        // Act - Login
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setCpf(novoHospede.getCpf());
        loginRequest.setSenha(novoHospede.getSenha());

        LoginResponseDTO loginResponse = hospedeService.login(loginRequest);

        // Assert - Login
        assertNotNull(loginResponse);
        assertNotNull(loginResponse.getToken());
        assertEquals(hospedeCadastrado.getId(), loginResponse.getId());

        // Act - Busca
        Optional<HospedeResponseDTO> hospedeBuscado = hospedeService.buscarPorCpf(novoHospede.getCpf());

        // Assert - Busca
        assertTrue(hospedeBuscado.isPresent());
        assertEquals(hospedeCadastrado.getId(), hospedeBuscado.get().getId());
    }

    @Test
    @DisplayName("Fluxo completo: Cadastro de chalé, consulta e atualização")
    void fluxoCompletoChale() {
        // Arrange
        ChaleRequestDTO novoChale = criarChaleValido("A101");

        // Act - Cadastro
        ChaleResponseDTO chaleCadastrado = chaleService.salvarChale(novoChale);

        // Assert - Cadastro
        assertNotNull(chaleCadastrado);
        assertNotNull(chaleCadastrado.getIdChale());
        assertEquals(novoChale.getNome(), chaleCadastrado.getNome());
        assertEquals(novoChale.getNumero(), chaleCadastrado.getNumero());

        // Act - Atualização
        ChaleRequestDTO chaleAtualizado = criarChaleValido("A101");
        chaleAtualizado.setNome("Chalé Atualizado");
        chaleAtualizado.setValorDiaria(new BigDecimal("500.00"));

        Optional<ChaleResponseDTO> resultado = chaleService.atualizarChale(
                chaleCadastrado.getIdChale(),
                chaleAtualizado
        );

        // Assert - Atualização
        assertTrue(resultado.isPresent());

        // Act - Busca
        Optional<ChaleResponseDTO> chaleBuscado = chaleService.buscarPorId(chaleCadastrado.getIdChale());

        // Assert - Busca
        assertTrue(chaleBuscado.isPresent());
    }

    @Test
    @DisplayName("Validação: Não deve permitir cadastro de hóspede com email duplicado")
    void naoDevePermitirEmailDuplicado() {
        // Arrange
        HospedeRequestDTO hospede1 = criarHospedeValido();
        HospedeRequestDTO hospede2 = criarHospedeValido();
        hospede2.setCpf("98765432100");

        // Act
        hospedeService.salvarHospede(hospede1);

        // Assert
        assertThrows(EmailJaExisteException.class, () -> {
            hospedeService.salvarHospede(hospede2);
        });
    }

    @Test
    @DisplayName("Validação: Não deve permitir cadastro de hóspede com CPF duplicado")
    void naoDevePermitirCpfDuplicado() {
        // Arrange
        HospedeRequestDTO hospede1 = criarHospedeValido();
        HospedeRequestDTO hospede2 = criarHospedeValido();
        hospede2.setEmail("outro@email.com");

        // Act
        hospedeService.salvarHospede(hospede1);

        // Assert
        assertThrows(CpfJaExisteException.class, () -> {
            hospedeService.salvarHospede(hospede2);
        });
    }

    @Test
    @DisplayName("Validação: Não deve permitir cadastro de chalé com número duplicado")
    void naoDevePermitirNumeroChuleDuplicado() {
        // Arrange
        ChaleRequestDTO chale1 = criarChaleValido("A101");
        ChaleRequestDTO chale2 = criarChaleValido("A101");
        chale2.setNome("Outro Chalé");

        // Act
        chaleService.salvarChale(chale1);

        // Assert
        assertThrows(ChaleJaExisteException.class, () -> {
            chaleService.salvarChale(chale2);
        });
    }

    @Test
    @DisplayName("Validação: Deve criptografar senha ao cadastrar hóspede")
    void deveCriptografarSenhaAoCadastrar() {
        // Arrange
        HospedeRequestDTO hospede = criarHospedeValido();
        String senhaOriginal = hospede.getSenha();

        // Act
        hospedeService.salvarHospede(hospede);
        Optional<HospedeResponseDTO> hospedeSalvo = hospedeService.buscarPorCpf(hospede.getCpf());

        // Assert
        assertTrue(hospedeSalvo.isPresent());
        // A senha no DTO de request foi modificada para a versão criptografada
        assertNotEquals(senhaOriginal, hospede.getSenha());
    }

    @Test
    @DisplayName("Validação: Deve normalizar CPF ao cadastrar")
    void deveNormalizarCpfAoCadastrar() {
        // Arrange
        HospedeRequestDTO hospede = criarHospedeValido();
        hospede.setCpf("123.456.789-00");

        // Act
        hospedeService.salvarHospede(hospede);

        // Assert
        assertEquals("12345678900", hospede.getCpf());
        Optional<HospedeResponseDTO> hospedeSalvo = hospedeService.buscarPorCpf("12345678900");
        assertTrue(hospedeSalvo.isPresent());
    }

    @Test
    @DisplayName("Validação: Deve falhar login com CPF inexistente")
    void deveFalharLoginComCpfInexistente() {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setCpf("99999999999");
        loginRequest.setSenha("Senha@123");

        // Act & Assert
        assertThrows(CpfNaoEncontradoException.class, () -> {
            hospedeService.login(loginRequest);
        });
    }

    @Test
    @DisplayName("Validação: Deve falhar login com senha incorreta")
    void deveFalharLoginComSenhaIncorreta() {
        // Arrange
        HospedeRequestDTO hospede = criarHospedeValido();
        hospedeService.salvarHospede(hospede);

        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setCpf(hospede.getCpf());
        loginRequest.setSenha("SenhaErrada@123");

        // Act & Assert
        assertThrows(SenhaIncorretaException.class, () -> {
            hospedeService.login(loginRequest);
        });
    }

    @Test
    @DisplayName("Validação: Deve buscar chalés por preço máximo")
    void deveBuscarChalesPorPrecoMaximo() {
        // Arrange
        chaleService.salvarChale(criarChaleComPreco("A101", "300.00"));
        chaleService.salvarChale(criarChaleComPreco("A102", "400.00"));
        chaleService.salvarChale(criarChaleComPreco("A103", "500.00"));

        // Act
        List<ChaleResponseDTO> chalesAte400 = chaleService.buscarPorPrecoMaximo(new BigDecimal("400.00"));

        // Assert
        assertNotNull(chalesAte400);
        assertTrue(chalesAte400.size() >= 2);
    }

    @Test
    @DisplayName("Validação: Deve buscar hóspedes por nome")
    void deveBuscarHospedesPorNome() {
        // Arrange
        HospedeRequestDTO hospede1 = criarHospedeValido();
        hospede1.setNome("João da Silva");
        hospede1.setCpf("11111111111");
        hospede1.setEmail("joao1@email.com");

        HospedeRequestDTO hospede2 = criarHospedeValido();
        hospede2.setNome("Maria Silva");
        hospede2.setCpf("22222222222");
        hospede2.setEmail("maria@email.com");

        hospedeService.salvarHospede(hospede1);
        hospedeService.salvarHospede(hospede2);

        // Act
        List<HospedeResponseDTO> hospedes = hospedeService.buscarPorNome("Silva");

        // Assert
        assertNotNull(hospedes);
        assertTrue(hospedes.size() >= 2);
    }

    @Test
    @DisplayName("Validação: Deve deletar hóspede existente")
    void deveDeletarHospedeExistente() {
        // Arrange
        HospedeRequestDTO hospede = criarHospedeValido();
        HospedeResponseDTO hospedeSalvo = hospedeService.salvarHospede(hospede);

        // Act
        hospedeService.deletarHospedePorId(hospedeSalvo.getId());

        // Assert
        Optional<HospedeResponseDTO> hospedeBuscado = hospedeService.buscarPorCpf(hospede.getCpf());
        assertFalse(hospedeBuscado.isPresent());
    }

    @Test
    @DisplayName("Validação: Deve deletar chalé existente")
    void deveDeletarChaleExistente() {
        // Arrange
        ChaleRequestDTO chale = criarChaleValido("A101");
        ChaleResponseDTO chaleSalvo = chaleService.salvarChale(chale);

        // Act
        chaleService.deletarChalePorId(chaleSalvo.getIdChale());

        // Assert
        Optional<ChaleResponseDTO> chaleBuscado = chaleService.buscarPorId(chaleSalvo.getIdChale());
        assertFalse(chaleBuscado.isPresent());
    }

    @Test
    @DisplayName("Validação: Deve listar todos os hóspedes")
    void deveListarTodosHospedes() {
        // Arrange
        HospedeRequestDTO hospede1 = criarHospedeValido();
        hospede1.setCpf("11111111111");
        hospede1.setEmail("h1@email.com");

        HospedeRequestDTO hospede2 = criarHospedeValido();
        hospede2.setCpf("22222222222");
        hospede2.setEmail("h2@email.com");

        hospedeService.salvarHospede(hospede1);
        hospedeService.salvarHospede(hospede2);

        // Act
        List<HospedeResponseDTO> hospedes = hospedeService.listarHospedes();

        // Assert
        assertNotNull(hospedes);
        assertTrue(hospedes.size() >= 2);
    }

    @Test
    @DisplayName("Validação: Deve listar todos os chalés")
    void deveListarTodosChales() {
        // Arrange
        chaleService.salvarChale(criarChaleValido("A101"));
        chaleService.salvarChale(criarChaleValido("A102"));

        // Act
        List<ChaleResponseDTO> chales = chaleService.listarChales();

        // Assert
        assertNotNull(chales);
        assertTrue(chales.size() >= 2);
    }

    // Métodos auxiliares

    private HospedeRequestDTO criarHospedeValido() {
        HospedeRequestDTO hospede = new HospedeRequestDTO();
        hospede.setNome("João da Silva");
        hospede.setEmail("joao@email.com");
        hospede.setTelefone("11987654321");
        hospede.setSenha("Senha@123");
        hospede.setDataNascimento(LocalDate.of(1990, 1, 1));
        hospede.setCpf("12345678900");
        hospede.setRole(Role.HOSPEDE);
        return hospede;
    }

    private ChaleRequestDTO criarChaleValido(String numero) {
        ChaleRequestDTO chale = new ChaleRequestDTO();
        chale.setNome("Chalé das Montanhas");
        chale.setNumero(numero);
        chale.setTipo("Luxo");
        chale.setValorDiaria(new BigDecimal("350.00"));
        chale.setDisponivel(true);
        chale.setCapacidade(4);
        chale.setDescricao("Chalé luxuoso");
        chale.setStatus(StatusChale.DISPONIVEL);
        return chale;
    }

    private ChaleRequestDTO criarChaleComPreco(String numero, String preco) {
        ChaleRequestDTO chale = criarChaleValido(numero);
        chale.setValorDiaria(new BigDecimal(preco));
        return chale;
    }
}

