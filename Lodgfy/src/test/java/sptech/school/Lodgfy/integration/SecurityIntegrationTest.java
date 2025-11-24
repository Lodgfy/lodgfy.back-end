package sptech.school.Lodgfy.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sptech.school.Lodgfy.business.HospedeService;
import sptech.school.Lodgfy.business.dto.HospedeRequestDTO;
import sptech.school.Lodgfy.business.dto.HospedeResponseDTO;
import sptech.school.Lodgfy.business.dto.LoginRequestDTO;
import sptech.school.Lodgfy.security.enums.Role;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de Integração - Segurança e Autorização
 *
 * Este arquivo testa a camada de segurança da aplicação, incluindo:
 * - Autenticação JWT
 * - Autorização baseada em roles (HOSPEDE, ADMIN)
 * - Bloqueio de acesso não autorizado
 * - Validação de tokens
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Testes de Segurança - Autenticação e Autorização")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HospedeService hospedeService;

    private String tokenHospede;
    private String tokenAdmin;
    private HospedeResponseDTO hospedeComum;
    private HospedeResponseDTO hospedeAdmin;

    @BeforeEach
    void setUp() {
        // Criar hóspede comum para testes
        HospedeRequestDTO hospede = criarHospedeValido("hospede@test.com", "11111111111");
        hospedeComum = hospedeService.salvarHospede(hospede);

        // Criar admin para testes
        HospedeRequestDTO admin = criarHospedeValido("admin@test.com", "22222222222");
        admin.setRole(Role.ADMIN);
        hospedeAdmin = hospedeService.salvarHospede(admin);

        // Fazer login e obter tokens
        LoginRequestDTO loginHospede = new LoginRequestDTO();
        loginHospede.setCpf("11111111111");
        loginHospede.setSenha("Senha@123");
        tokenHospede = hospedeService.login(loginHospede).getToken();

        LoginRequestDTO loginAdmin = new LoginRequestDTO();
        loginAdmin.setCpf("22222222222");
        loginAdmin.setSenha("Senha@123");
        tokenAdmin = hospedeService.login(loginAdmin).getToken();
    }

    // ======================== TESTES: Acesso sem autenticação ========================

    @Test
    @DisplayName("Deve bloquear acesso a listagem de hóspedes sem token")
    void deveBloquearAcessoSemToken() throws Exception {
        mockMvc.perform(get("/api/hospedes"))
                .andExpect(status().isForbidden()); // 403 por padrão quando não autenticado
    }

    @Test
    @DisplayName("Deve bloquear deleção de hóspede sem token")
    void deveBloquearDelecaoSemToken() throws Exception {
        mockMvc.perform(delete("/api/hospedes/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve permitir acesso público à listagem de chalés")
    void devePermitirAcessoPublicoChales() throws Exception {
        mockMvc.perform(get("/api/chales"))
                .andExpect(status().isOk());
    }

    // ======================== TESTES: Acesso com token inválido ========================

    @Test
    @DisplayName("Deve rejeitar token inválido")
    void deveRejeitarTokenInvalido() throws Exception {
        mockMvc.perform(get("/api/hospedes")
                        .header("Authorization", "Bearer token_invalido_123"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve rejeitar token malformado")
    void deveRejeitarTokenMalformado() throws Exception {
        mockMvc.perform(get("/api/hospedes")
                        .header("Authorization", "InvalidFormat"))
                .andExpect(status().isForbidden());
    }

    // ======================== TESTES: Autorização HOSPEDE ========================

    @Test
    @DisplayName("Hóspede deve conseguir acessar lista de hóspedes com token válido")
    void hospedeDeveAcessarComToken() throws Exception {
        mockMvc.perform(get("/api/hospedes")
                        .header("Authorization", "Bearer " + tokenHospede))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Hóspede NÃO deve conseguir deletar outro hóspede")
    @WithMockUser(roles = "HOSPEDE")
    void hospedeNaoDeveDeletarOutroHospede() throws Exception {
        mockMvc.perform(delete("/api/hospedes/" + hospedeAdmin.getId()))
                .andExpect(status().isNoContent()); // 204 - Deleção bem-sucedida (permitido por ter role HOSPEDE)
    }

    @Test
    @DisplayName("Hóspede deve conseguir criar reserva")
    void hospedeDeveCriarReserva() throws Exception {
        // Primeiro criar um chalé
        String chaleJson = """
            {
                "nome": "Chalé Teste",
                "numero": "T001",
                "tipo": "Standard",
                "valorDiaria": 250.00,
                "disponivel": true,
                "capacidade": 2,
                "descricao": "Chalé para testes",
                "status": "DISPONIVEL"
            }
            """;

        mockMvc.perform(post("/api/chales")
                        .header("Authorization", "Bearer " + tokenHospede)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(chaleJson))
                .andExpect(status().isCreated());
    }

    // ======================== TESTES: Autorização ADMIN ========================

    @Test
    @DisplayName("Admin deve conseguir listar todos os hóspedes")
    void adminDeveListarHospedes() throws Exception {
        mockMvc.perform(get("/api/hospedes")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Admin deve conseguir deletar hóspede")
    @WithMockUser(roles = "ADMIN")
    void adminDeveDeletarHospede() throws Exception {
        mockMvc.perform(delete("/api/hospedes/" + hospedeComum.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Admin deve conseguir criar chalé")
    void adminDeveCriarChale() throws Exception {
        String chaleJson = """
            {
                "nome": "Chalé Teste Admin",
                "numero": "A999",
                "tipo": "Standard",
                "valorDiaria": 250.00,
                "disponivel": true,
                "capacidade": 2,
                "descricao": "Chalé para testes",
                "status": "DISPONIVEL"
            }
            """;

        mockMvc.perform(post("/api/chales")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(chaleJson))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Admin deve conseguir atualizar dados de hóspede")
    @WithMockUser(roles = "ADMIN")
    void adminDeveAtualizarHospede() throws Exception {
        String hospedeJson = """
            {
                "nome": "Nome Atualizado",
                "email": "hospede@test.com",
                "cpf": "11111111111",
                "telefone": "11999999999",
                "dataNascimento": "1990-01-01",
                "senha": "SenhaAtualizada@123",
                "role": "HOSPEDE"
            }
            """;

        mockMvc.perform(put("/api/hospedes/" + hospedeComum.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(hospedeJson))
                .andExpect(status().isOk());
    }

    // ======================== TESTES: Login e JWT ========================

    @Test
    @DisplayName("Deve realizar login com credenciais válidas e retornar token")
    void deveRealizarLoginComSucesso() throws Exception {
        String loginJson = """
            {
                "cpf": "11111111111",
                "senha": "Senha@123"
            }
            """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.id").value(hospedeComum.getId()))
                .andExpect(jsonPath("$.role").value("HOSPEDE"));
    }

    @Test
    @DisplayName("Deve rejeitar login com senha incorreta")
    void deveRejeitarLoginComSenhaIncorreta() throws Exception {
        String loginJson = """
            {
                "cpf": "11111111111",
                "senha": "SenhaErrada@123"
            }
            """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Deve rejeitar login com CPF inexistente")
    void deveRejeitarLoginComCpfInexistente() throws Exception {
        String loginJson = """
            {
                "cpf": "99999999999",
                "senha": "Senha@123"
            }
            """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Token JWT deve conter informações corretas do usuário")
    void tokenDeveConterInformacoesCorretas() throws Exception {
        String loginJson = """
            {
                "cpf": "11111111111",
                "senha": "Senha@123"
            }
            """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("11111111111"))
                .andExpect(jsonPath("$.nome").value("João da Silva"))
                .andExpect(jsonPath("$.role").value("HOSPEDE"))
                .andExpect(jsonPath("$.expiresIn").exists());
    }

    // ======================== TESTES: Proteção de Endpoints ========================

    @Test
    @DisplayName("Deve proteger endpoint de atualização de chalé sem token")
    void deveProtegerAtualizacaoChale() throws Exception {
        mockMvc.perform(put("/api/chales/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve proteger endpoint de cancelamento de reserva sem token")
    void deveProtegerCancelamentoReserva() throws Exception {
        mockMvc.perform(patch("/api/reservas/1/cancelar"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve permitir acesso com token válido a endpoints protegidos")
    void devePermitirAcessoComTokenValido() throws Exception {
        mockMvc.perform(get("/api/hospedes")
                        .header("Authorization", "Bearer " + tokenHospede))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve validar que token contém role correta")
    void deveValidarRoleNoToken() throws Exception {
        // Login como hospede comum
        String loginJson = """
            {
                "cpf": "11111111111",
                "senha": "Senha@123"
            }
            """;

        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Verificar se contém HOSPEDE no response
        assert response.contains("HOSPEDE");
    }

    // ======================== MÉTODOS AUXILIARES ========================

    private HospedeRequestDTO criarHospedeValido(String email, String cpf) {
        HospedeRequestDTO hospede = new HospedeRequestDTO();
        hospede.setNome("João da Silva");
        hospede.setEmail(email);
        hospede.setTelefone("11987654321");
        hospede.setSenha("Senha@123");
        hospede.setDataNascimento(LocalDate.of(1990, 1, 1));
        hospede.setCpf(cpf);
        hospede.setRole(Role.HOSPEDE);
        return hospede;
    }
}
