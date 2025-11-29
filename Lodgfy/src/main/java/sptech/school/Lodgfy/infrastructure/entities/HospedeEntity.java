package sptech.school.Lodgfy.infrastructure.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
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
@Entity
@Table(name = "hospedes")
@AllArgsConstructor
@NoArgsConstructor
public class HospedeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Schema(example = "João da Silva", description = "Nome completo do hóspede")
    @Column(nullable = false, length = 100)
    @Size(max = 50, message = "Nome deve ter no máximo 50 caracteres")
    private String nome;

    @Schema(example = "joao.silva@email.com", description = "Email do hóspede")
    @Column(nullable = false, unique = true, length = 255)
    @Size(max = 80, message = "Email deve ter no máximo 80 caracteres")
    private String email;

    @Schema(example = "(11) 98765-4321", description = "Telefone do hóspede")
    @Column(nullable = false, length = 20)
    private String telefone;

    @Schema(example = "Senha@123", description = "Senha do hóspede")
    @Column(nullable = false, length = 60)
    private String senha;

    @Schema(example = "1990-01-01", description = "Data de nascimento do hóspede")
    @Column(nullable = false)
    private LocalDate dataNascimento;

    @Schema(example = "123.456.789-00", description = "CPF do hóspede")
    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.HOSPEDE;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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