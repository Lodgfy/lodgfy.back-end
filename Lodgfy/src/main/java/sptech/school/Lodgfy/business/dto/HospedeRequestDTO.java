package sptech.school.Lodgfy.business.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CPF;
import sptech.school.Lodgfy.security.enums.Role;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HospedeRequestDTO {

    @Schema(example = "João da Silva", description = "Nome completo do hóspede")
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 50, message = "Nome deve ter entre 3 e 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s]*$", message = "Nome deve conter apenas letras")
    private String nome;

    @Schema(example = "joao.silva@email.com", description = "Email do hóspede")
    @Email(message = "Email inválido")
    @NotBlank(message = "Email é obrigatório")
    @Size(max = 80, message = "Email deve ter no máximo 80 caracteres")
    private String email;

    @Schema(example = "(11) 98765-4321", description = "Telefone do hóspede")
    @NotBlank(message = "Telefone é obrigatório")
    @Pattern(
            regexp = "^\\(?\\d{2}\\)?[\\s-]?9?\\d{4}-?\\d{4}$",
            message = "Telefone deve estar no formato (XX) 9XXXX-XXXX"
    )
    private String telefone;

    @Schema(example = "Senha@123", description = "Senha do hóspede")
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, max = 60, message = "Senha deve ter entre 8 e 60 caracteres")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
            message = "Senha deve conter pelo menos uma letra, um número e um caractere especial"
    )
    private String senha;

    @Schema(example = "1990-01-01", description = "Data de nascimento do hóspede")
    @NotNull(message = "Data de nascimento é obrigatória")
    @Past(message = "Data de nascimento deve ser no passado")
    private LocalDate dataNascimento;

    @Schema(example = "123.456.789-00", description = "CPF do hóspede")
    @Pattern(
            regexp = "\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}",
            message = "CPF deve estar no formato 123.456.789-00 ou 12345678900"
    )
    @NotBlank(message = "CPF é obrigatório")
    private String cpf;

    @Schema(example = "HOSPEDE", description = "Tipo de usuário (sempre HOSPEDE no cadastro público)")
    private Role role;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
