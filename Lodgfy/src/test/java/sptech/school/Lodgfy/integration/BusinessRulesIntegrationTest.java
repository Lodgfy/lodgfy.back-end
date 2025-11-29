package sptech.school.Lodgfy.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sptech.school.Lodgfy.business.ChaleService;
import sptech.school.Lodgfy.business.HospedeService;
import sptech.school.Lodgfy.business.ReservaService;
import sptech.school.Lodgfy.business.dto.*;
import sptech.school.Lodgfy.infrastructure.entities.ChaleEntity.StatusChale;
import sptech.school.Lodgfy.security.enums.Role;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de Integração - Validação de Fluxos Completos
 *
 * Este arquivo contém apenas testes de integração que validam fluxos completos
 * envolvendo múltiplos componentes do sistema (Service + Repository + Mapper + DB).
 *
 * Testes unitários de regras individuais estão em:
 * - HospedeServiceTest (testes unitários do HospedeService)
 * - ChaleServiceTest (testes unitários do ChaleService)
 * - ReservaServiceTest (testes unitários do ReservaService)
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Testes de Integração - Fluxos Completos do Sistema")
class BusinessRulesIntegrationTest {

    @Autowired
    private HospedeService hospedeService;

    @Autowired
    private ChaleService chaleService;

    @Autowired
    private ReservaService reservaService;

    // ======================== FLUXOS DE INTEGRAÇÃO - HÓSPEDE ========================

    @Test
    @DisplayName("Fluxo completo: Cadastro de hóspede → Login → Consulta de dados")
    void fluxoCompletoHospede() {
        // Arrange
        HospedeRequestDTO novoHospede = criarHospedeValido();
        String senhaOriginal = novoHospede.getSenha(); // Salvar senha original antes de cadastrar

        // Act & Assert - Cadastro
        HospedeResponseDTO hospedeCadastrado = hospedeService.salvarHospede(novoHospede);
        assertNotNull(hospedeCadastrado);
        assertNotNull(hospedeCadastrado.getId());
        assertEquals(novoHospede.getNome(), hospedeCadastrado.getNome());
        assertEquals(novoHospede.getEmail(), hospedeCadastrado.getEmail());

        // Act & Assert - Login (integração com JWT e autenticação)
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setCpf(novoHospede.getCpf());
        loginRequest.setSenha(senhaOriginal);

        LoginResponseDTO loginResponse = hospedeService.login(loginRequest);
        assertNotNull(loginResponse);
        assertNotNull(loginResponse.getToken());
        assertEquals(hospedeCadastrado.getId(), loginResponse.getId());
        assertEquals(hospedeCadastrado.getNome(), loginResponse.getNome());
        assertEquals(Role.HOSPEDE, loginResponse.getRole());

        // Act & Assert - Consulta (verifica persistência no banco)
        Optional<HospedeResponseDTO> hospedeBuscado = hospedeService.buscarPorCpf(novoHospede.getCpf());
        assertTrue(hospedeBuscado.isPresent());
        assertEquals(hospedeCadastrado.getId(), hospedeBuscado.get().getId());
        assertEquals(hospedeCadastrado.getEmail(), hospedeBuscado.get().getEmail());
    }

    @Test
    @DisplayName("Fluxo completo: Autocadastro público → Login → Verificação de role")
    void fluxoAutocadastroComLogin() {
        // Arrange
        HospedeSignUpRequestDTO signUpDTO = criarSignUpValido();
        String senhaOriginal = signUpDTO.getSenha();

        // Act & Assert - Autocadastro
        HospedeResponseDTO hospedeCadastrado = hospedeService.autocadastrar(signUpDTO);
        assertNotNull(hospedeCadastrado);
        assertNotNull(hospedeCadastrado.getId());
        assertEquals(Role.HOSPEDE, hospedeCadastrado.getRole()); // Role sempre HOSPEDE

        // Act & Assert - Login após autocadastro
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setCpf(signUpDTO.getCpf());
        loginRequest.setSenha(senhaOriginal);

        LoginResponseDTO loginResponse = hospedeService.login(loginRequest);
        assertNotNull(loginResponse);
        assertNotNull(loginResponse.getToken());
        assertEquals(Role.HOSPEDE, loginResponse.getRole());
    }

    @Test
    @DisplayName("Fluxo completo: Cadastro → Atualização de dados → Login com nova senha")
    void fluxoAtualizacaoHospedeComNovaSenha() {
        // Arrange
        HospedeRequestDTO hospede = criarHospedeValido();
        String senhaOriginal = hospede.getSenha();

        // Act - Cadastro inicial
        HospedeResponseDTO hospedeCadastrado = hospedeService.salvarHospede(hospede);

        // Act - Atualização com nova senha
        HospedeRequestDTO hospedeAtualizado = new HospedeRequestDTO();
        hospedeAtualizado.setNome("João Silva Atualizado");
        hospedeAtualizado.setEmail(hospede.getEmail());
        hospedeAtualizado.setCpf(hospede.getCpf());
        hospedeAtualizado.setTelefone("11999999999");
        hospedeAtualizado.setDataNascimento(hospede.getDataNascimento());
        hospedeAtualizado.setSenha("NovaSenha@456");

        Optional<HospedeResponseDTO> resultado = hospedeService.atualizarHospede(
                hospedeCadastrado.getId(),
                hospedeAtualizado
        );

        // Assert - Atualização
        assertTrue(resultado.isPresent());
        assertEquals("João Silva Atualizado", resultado.get().getNome());

        // Act & Assert - Login com senha antiga deve falhar
        LoginRequestDTO loginAntigo = new LoginRequestDTO();
        loginAntigo.setCpf(hospede.getCpf());
        loginAntigo.setSenha(senhaOriginal);

        assertThrows(Exception.class, () -> hospedeService.login(loginAntigo));

        // Act & Assert - Login com nova senha deve funcionar
        LoginRequestDTO loginNovo = new LoginRequestDTO();
        loginNovo.setCpf(hospede.getCpf());
        loginNovo.setSenha("NovaSenha@456");

        LoginResponseDTO loginResponse = hospedeService.login(loginNovo);
        assertNotNull(loginResponse);
        assertNotNull(loginResponse.getToken());
    }

    // ======================== FLUXOS DE INTEGRAÇÃO - CHALÉ ========================

    @Test
    @DisplayName("Fluxo completo: Cadastro de chalé → Consulta → Atualização → Consulta")
    void fluxoCompletoChale() {
        // Arrange
        ChaleRequestDTO novoChale = criarChaleValido("A101");

        // Act & Assert - Cadastro
        ChaleResponseDTO chaleCadastrado = chaleService.salvarChale(novoChale);
        assertNotNull(chaleCadastrado);
        assertNotNull(chaleCadastrado.getIdChale());
        assertEquals("A101", chaleCadastrado.getNumero());
        assertEquals(new BigDecimal("350.00"), chaleCadastrado.getValorDiaria());

        // Act & Assert - Consulta inicial
        Optional<ChaleResponseDTO> chaleBuscado1 = chaleService.buscarPorId(chaleCadastrado.getIdChale());
        assertTrue(chaleBuscado1.isPresent());
        assertEquals("Chalé das Montanhas", chaleBuscado1.get().getNome());

        // Act & Assert - Atualização
        ChaleRequestDTO chaleAtualizado = criarChaleValido("A101");
        chaleAtualizado.setNome("Chalé Atualizado Premium");
        chaleAtualizado.setValorDiaria(new BigDecimal("500.00"));
        chaleAtualizado.setCapacidade(6);

        Optional<ChaleResponseDTO> resultado = chaleService.atualizarChale(
                chaleCadastrado.getIdChale(),
                chaleAtualizado
        );

        assertTrue(resultado.isPresent());
        assertEquals("Chalé Atualizado Premium", resultado.get().getNome());
        assertEquals(new BigDecimal("500.00"), resultado.get().getValorDiaria());
        assertEquals(6, resultado.get().getCapacidade());

        // Act & Assert - Consulta após atualização (verifica persistência)
        Optional<ChaleResponseDTO> chaleBuscado2 = chaleService.buscarPorId(chaleCadastrado.getIdChale());
        assertTrue(chaleBuscado2.isPresent());
        assertEquals("Chalé Atualizado Premium", chaleBuscado2.get().getNome());
        assertEquals(new BigDecimal("500.00"), chaleBuscado2.get().getValorDiaria());
    }

    @Test
    @DisplayName("Fluxo completo: Cadastro de múltiplos chalés → Busca por preço → Validação de ordenação")
    void fluxoBuscaChalesPorFaixaPreco() {
        // Arrange & Act - Cadastro de múltiplos chalés
        ChaleResponseDTO chaleEconomico = chaleService.salvarChale(criarChaleComPreco("A101", "200.00"));
        ChaleResponseDTO chaleMedio = chaleService.salvarChale(criarChaleComPreco("A102", "350.00"));
        ChaleResponseDTO chaleLuxo = chaleService.salvarChale(criarChaleComPreco("A103", "600.00"));

        // Act - Busca por preço máximo
        var chalesAte400 = chaleService.buscarPorPrecoMaximo(new BigDecimal("400.00"));

        // Assert - Validação de filtro e integração
        assertNotNull(chalesAte400);
        assertTrue(chalesAte400.size() >= 2);
        assertTrue(chalesAte400.stream()
                .allMatch(c -> c.getValorDiaria().compareTo(new BigDecimal("400.00")) <= 0));
        assertTrue(chalesAte400.stream()
                .anyMatch(c -> c.getNumero().equals("A101")));
        assertTrue(chalesAte400.stream()
                .anyMatch(c -> c.getNumero().equals("A102")));
        assertFalse(chalesAte400.stream()
                .anyMatch(c -> c.getNumero().equals("A103")));
    }

    // ======================== FLUXOS DE INTEGRAÇÃO - CENÁRIOS COMPLEXOS ========================

    @Test
    @DisplayName("Fluxo completo: Múltiplos hóspedes → Busca por nome → Validação de filtro")
    void fluxoBuscaHospedesPorNome() {
        // Arrange & Act - Cadastro de múltiplos hóspedes
        HospedeRequestDTO hospede1 = criarHospedeValido();
        hospede1.setNome("João da Silva");
        hospede1.setCpf("11111111111");
        hospede1.setEmail("joao.silva@email.com");

        HospedeRequestDTO hospede2 = criarHospedeValido();
        hospede2.setNome("Maria Silva Santos");
        hospede2.setCpf("22222222222");
        hospede2.setEmail("maria.silva@email.com");

        HospedeRequestDTO hospede3 = criarHospedeValido();
        hospede3.setNome("Pedro Oliveira");
        hospede3.setCpf("33333333333");
        hospede3.setEmail("pedro.oliveira@email.com");

        hospedeService.salvarHospede(hospede1);
        hospedeService.salvarHospede(hospede2);
        hospedeService.salvarHospede(hospede3);

        // Act - Busca por nome contendo "Silva"
        var hospedesComSilva = hospedeService.buscarPorNome("Silva");

        // Assert - Validação de filtro
        assertNotNull(hospedesComSilva);
        assertTrue(hospedesComSilva.size() >= 2);
        assertTrue(hospedesComSilva.stream()
                .allMatch(h -> h.getNome().contains("Silva")));
        assertFalse(hospedesComSilva.stream()
                .anyMatch(h -> h.getNome().contains("Pedro")));
    }

    @Test
    @DisplayName("Fluxo completo: Cadastro → Deleção → Validação de remoção completa")
    void fluxoDelecaoCompletoHospede() {
        // Arrange
        HospedeRequestDTO hospede = criarHospedeValido();

        // Act - Cadastro
        HospedeResponseDTO hospedeSalvo = hospedeService.salvarHospede(hospede);
        Long id = hospedeSalvo.getId();
        String cpf = hospedeSalvo.getCpf();

        // Verify - Existe no banco
        assertTrue(hospedeService.buscarPorCpf(cpf).isPresent());

        // Act - Deleção
        hospedeService.deletarHospedePorId(id);

        // Assert - Não existe mais no banco
        assertFalse(hospedeService.buscarPorCpf(cpf).isPresent());
    }

    @Test
    @DisplayName("Fluxo completo: Cadastro → Deleção → Validação de remoção completa de chalé")
    void fluxoDelecaoCompletoChale() {
        // Arrange
        ChaleRequestDTO chale = criarChaleValido("A101");

        // Act - Cadastro
        ChaleResponseDTO chaleSalvo = chaleService.salvarChale(chale);
        Long id = chaleSalvo.getIdChale();

        // Verify - Existe no banco
        assertTrue(chaleService.buscarPorId(id).isPresent());

        // Act - Deleção
        chaleService.deletarChalePorId(id);

        // Assert - Não existe mais no banco
        assertFalse(chaleService.buscarPorId(id).isPresent());
    }

    @Test
    @DisplayName("Fluxo completo: Normalização de CPF em cadastro e login")
    void fluxoNormalizacaoCpfCompleto() {
        // Arrange
        HospedeRequestDTO hospede = criarHospedeValido();
        hospede.setCpf("123.456.789-00"); // CPF formatado
        String senhaOriginal = hospede.getSenha();

        // Act - Cadastro (deve normalizar CPF)
        HospedeResponseDTO hospedeSalvo = hospedeService.salvarHospede(hospede);

        // Assert - CPF normalizado no cadastro
        assertEquals("12345678900", hospedeSalvo.getCpf());

        // Act - Login com CPF formatado (deve normalizar e encontrar)
        LoginRequestDTO loginFormatado = new LoginRequestDTO();
        loginFormatado.setCpf("123.456.789-00");
        loginFormatado.setSenha(senhaOriginal);

        LoginResponseDTO loginResponse1 = hospedeService.login(loginFormatado);
        assertNotNull(loginResponse1);
        assertNotNull(loginResponse1.getToken());

        // Act - Login com CPF sem formatação (deve encontrar)
        LoginRequestDTO loginSemFormato = new LoginRequestDTO();
        loginSemFormato.setCpf("12345678900");
        loginSemFormato.setSenha(senhaOriginal);

        LoginResponseDTO loginResponse2 = hospedeService.login(loginSemFormato);
        assertNotNull(loginResponse2);
        assertNotNull(loginResponse2.getToken());

        // Assert - Ambos logins retornam o mesmo hóspede
        assertEquals(loginResponse1.getId(), loginResponse2.getId());
    }

    // ======================== FLUXOS DE INTEGRAÇÃO - RESERVA ========================

    @Test
    @DisplayName("Fluxo completo: Hóspede cria reserva → Chalé fica ocupado → Consulta reserva")
    void fluxoCompletoReserva() {
        // Arrange - Cadastrar hóspede e chalé
        HospedeRequestDTO hospede = criarHospedeValido();
        hospede.setEmail("reserva@test.com");
        hospede.setCpf("99999999999");
        HospedeResponseDTO hospedeSalvo = hospedeService.salvarHospede(hospede);

        ChaleRequestDTO chale = criarChaleValido("R101");
        ChaleResponseDTO chaleSalvo = chaleService.salvarChale(chale);

        // Act - Criar reserva
        ReservaRequestDTO reservaRequest = new ReservaRequestDTO();
        reservaRequest.setHospedeId(hospedeSalvo.getId());
        reservaRequest.setChaleId(chaleSalvo.getIdChale());
        reservaRequest.setDataCheckIn(LocalDate.now().plusDays(5));
        reservaRequest.setDataCheckOut(LocalDate.now().plusDays(8));

        ReservaResponseDTO reservaCriada = reservaService.criarReserva(reservaRequest);

        // Assert - Reserva criada
        assertNotNull(reservaCriada);
        assertNotNull(reservaCriada.getIdReserva());
        assertEquals(StatusReserva.PENDENTE, reservaCriada.getStatusReserva());
        assertEquals(0, new BigDecimal("1050.00").compareTo(reservaCriada.getValorTotal())); // 3 diárias x 350

        // Assert - Consulta reserva
        Optional<ReservaResponseDTO> reservaBuscada = reservaService.buscarPorId(reservaCriada.getIdReserva());
        assertTrue(reservaBuscada.isPresent());
        assertEquals(hospedeSalvo.getId(), reservaBuscada.get().getHospedeId());
        assertEquals(chaleSalvo.getIdChale(), reservaBuscada.get().getChaleId());
    }

    @Test
    @DisplayName("Fluxo completo: Criar reserva → Confirmar → Concluir")
    void fluxoCicloVidaReserva() {
        // Arrange
        HospedeRequestDTO hospede = criarHospedeValido();
        hospede.setEmail("ciclo@test.com");
        hospede.setCpf("88888888888");
        HospedeResponseDTO hospedeSalvo = hospedeService.salvarHospede(hospede);

        ChaleRequestDTO chale = criarChaleValido("C101");
        ChaleResponseDTO chaleSalvo = chaleService.salvarChale(chale);

        ReservaRequestDTO reservaRequest = new ReservaRequestDTO();
        reservaRequest.setHospedeId(hospedeSalvo.getId());
        reservaRequest.setChaleId(chaleSalvo.getIdChale());
        reservaRequest.setDataCheckIn(LocalDate.now().plusDays(10));
        reservaRequest.setDataCheckOut(LocalDate.now().plusDays(12));

        // Act & Assert - Criar (PENDENTE)
        ReservaResponseDTO reservaCriada = reservaService.criarReserva(reservaRequest);
        assertEquals(StatusReserva.PENDENTE, reservaCriada.getStatusReserva());

        // Act & Assert - Confirmar
        ReservaResponseDTO reservaConfirmada = reservaService.confirmarReserva(reservaCriada.getIdReserva());
        assertEquals(StatusReserva.CONFIRMADA, reservaConfirmada.getStatusReserva());

        // Act & Assert - Concluir
        ReservaResponseDTO reservaConcluida = reservaService.concluirReserva(reservaCriada.getIdReserva());
        assertEquals(StatusReserva.CONCLUIDA, reservaConcluida.getStatusReserva());
    }

    @Test
    @DisplayName("Fluxo completo: Não deve permitir reservas conflitantes no mesmo período")
    void fluxoReservasConflitantes() {
        // Arrange - Criar hóspedes e chalé
        HospedeRequestDTO hospede1 = criarHospedeValido();
        hospede1.setEmail("hospede1@test.com");
        hospede1.setCpf("77777777777");
        HospedeResponseDTO hospede1Salvo = hospedeService.salvarHospede(hospede1);

        HospedeRequestDTO hospede2 = criarHospedeValido();
        hospede2.setEmail("hospede2@test.com");
        hospede2.setCpf("66666666666");
        HospedeResponseDTO hospede2Salvo = hospedeService.salvarHospede(hospede2);

        ChaleRequestDTO chale = criarChaleValido("CONF01");
        ChaleResponseDTO chaleSalvo = chaleService.salvarChale(chale);

        // Act - Primeira reserva
        ReservaRequestDTO reserva1 = new ReservaRequestDTO();
        reserva1.setHospedeId(hospede1Salvo.getId());
        reserva1.setChaleId(chaleSalvo.getIdChale());
        reserva1.setDataCheckIn(LocalDate.now().plusDays(15));
        reserva1.setDataCheckOut(LocalDate.now().plusDays(20));

        ReservaResponseDTO reserva1Criada = reservaService.criarReserva(reserva1);
        reservaService.confirmarReserva(reserva1Criada.getIdReserva()); // Confirmar

        // Act - Tentar segunda reserva conflitante
        ReservaRequestDTO reserva2 = new ReservaRequestDTO();
        reserva2.setHospedeId(hospede2Salvo.getId());
        reserva2.setChaleId(chaleSalvo.getIdChale());
        reserva2.setDataCheckIn(LocalDate.now().plusDays(17)); // Sobrepõe
        reserva2.setDataCheckOut(LocalDate.now().plusDays(22));

        // Assert - Deve lançar exceção
        assertThrows(Exception.class, () -> reservaService.criarReserva(reserva2));
    }

    @Test
    @DisplayName("Fluxo completo: Cancelar reserva → Chalé deve aceitar nova reserva")
    void fluxoCancelamentoLiberaChale() {
        // Arrange
        HospedeRequestDTO hospede1 = criarHospedeValido();
        hospede1.setEmail("cancela1@test.com");
        hospede1.setCpf("55555555555");
        HospedeResponseDTO hospede1Salvo = hospedeService.salvarHospede(hospede1);

        HospedeRequestDTO hospede2 = criarHospedeValido();
        hospede2.setEmail("cancela2@test.com");
        hospede2.setCpf("44444444444");
        HospedeResponseDTO hospede2Salvo = hospedeService.salvarHospede(hospede2);

        ChaleRequestDTO chale = criarChaleValido("CANC01");
        ChaleResponseDTO chaleSalvo = chaleService.salvarChale(chale);

        // Act - Criar e confirmar primeira reserva
        ReservaRequestDTO reserva1 = new ReservaRequestDTO();
        reserva1.setHospedeId(hospede1Salvo.getId());
        reserva1.setChaleId(chaleSalvo.getIdChale());
        reserva1.setDataCheckIn(LocalDate.now().plusDays(25));
        reserva1.setDataCheckOut(LocalDate.now().plusDays(28));

        ReservaResponseDTO reserva1Criada = reservaService.criarReserva(reserva1);
        reservaService.confirmarReserva(reserva1Criada.getIdReserva());

        // Act - Cancelar primeira reserva
        ReservaResponseDTO reservaCancelada = reservaService.cancelarReserva(reserva1Criada.getIdReserva());
        assertEquals(StatusReserva.CANCELADA, reservaCancelada.getStatusReserva());

        // Act - Criar nova reserva no mesmo período (deve funcionar)
        ReservaRequestDTO reserva2 = new ReservaRequestDTO();
        reserva2.setHospedeId(hospede2Salvo.getId());
        reserva2.setChaleId(chaleSalvo.getIdChale());
        reserva2.setDataCheckIn(LocalDate.now().plusDays(25));
        reserva2.setDataCheckOut(LocalDate.now().plusDays(28));

        ReservaResponseDTO reserva2Criada = reservaService.criarReserva(reserva2);

        // Assert - Nova reserva criada com sucesso
        assertNotNull(reserva2Criada);
        assertEquals(StatusReserva.PENDENTE, reserva2Criada.getStatusReserva());
    }

    @Test
    @DisplayName("Fluxo completo: Buscar histórico de reservas do hóspede")
    void fluxoHistoricoReservasHospede() {
        // Arrange - Criar hóspede e chalés
        HospedeRequestDTO hospede = criarHospedeValido();
        hospede.setEmail("historico@test.com");
        hospede.setCpf("33333333333");
        HospedeResponseDTO hospedeSalvo = hospedeService.salvarHospede(hospede);

        ChaleResponseDTO chale1 = chaleService.salvarChale(criarChaleValido("H101"));
        ChaleResponseDTO chale2 = chaleService.salvarChale(criarChaleValido("H102"));

        // Act - Criar múltiplas reservas
        ReservaRequestDTO reserva1 = new ReservaRequestDTO();
        reserva1.setHospedeId(hospedeSalvo.getId());
        reserva1.setChaleId(chale1.getIdChale());
        reserva1.setDataCheckIn(LocalDate.now().plusDays(30));
        reserva1.setDataCheckOut(LocalDate.now().plusDays(32));
        reservaService.criarReserva(reserva1);

        ReservaRequestDTO reserva2 = new ReservaRequestDTO();
        reserva2.setHospedeId(hospedeSalvo.getId());
        reserva2.setChaleId(chale2.getIdChale());
        reserva2.setDataCheckIn(LocalDate.now().plusDays(35));
        reserva2.setDataCheckOut(LocalDate.now().plusDays(37));
        reservaService.criarReserva(reserva2);

        // Act - Buscar histórico
        List<ReservaResponseDTO> historico = reservaService.buscarPorHospede(hospedeSalvo.getId());

        // Assert
        assertNotNull(historico);
        assertTrue(historico.size() >= 2);
        assertTrue(historico.stream()
                .allMatch(r -> r.getHospedeId().equals(hospedeSalvo.getId())));
    }

    @Test
    @DisplayName("Fluxo completo: Cálculo correto do valor total da reserva")
    void fluxoCalculoValorReserva() {
        // Arrange
        HospedeRequestDTO hospede = criarHospedeValido();
        hospede.setEmail("calculo@test.com");
        hospede.setCpf("22222222222");
        HospedeResponseDTO hospedeSalvo = hospedeService.salvarHospede(hospede);

        ChaleRequestDTO chale = criarChaleValido("CALC01");
        chale.setValorDiaria(new BigDecimal("250.00"));
        ChaleResponseDTO chaleSalvo = chaleService.salvarChale(chale);

        // Act - Criar reserva de 5 diárias
        ReservaRequestDTO reservaRequest = new ReservaRequestDTO();
        reservaRequest.setHospedeId(hospedeSalvo.getId());
        reservaRequest.setChaleId(chaleSalvo.getIdChale());
        reservaRequest.setDataCheckIn(LocalDate.now().plusDays(40));
        reservaRequest.setDataCheckOut(LocalDate.now().plusDays(45)); // 5 dias

        ReservaResponseDTO reservaCriada = reservaService.criarReserva(reservaRequest);

        // Assert - Valor total = 5 x 250 = 1250
        assertEquals(new BigDecimal("1250.00"), reservaCriada.getValorTotal());
    }

    // ======================== MÉTODOS AUXILIARES ========================

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

    private HospedeSignUpRequestDTO criarSignUpValido() {
        HospedeSignUpRequestDTO signUp = new HospedeSignUpRequestDTO();
        signUp.setNome("João da Silva");
        signUp.setEmail("joao.signup@email.com");
        signUp.setTelefone("11987654321");
        signUp.setSenha("Senha@123");
        signUp.setDataNascimento(LocalDate.of(1990, 1, 1));
        signUp.setCpf("12345678900");
        return signUp;
    }

    private ChaleRequestDTO criarChaleValido(String numero) {
        ChaleRequestDTO chale = new ChaleRequestDTO();
        chale.setNome("Chalé das Montanhas");
        chale.setNumero(numero);
        chale.setTipo("Luxo");
        chale.setValorDiaria(new BigDecimal("350.00"));
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
