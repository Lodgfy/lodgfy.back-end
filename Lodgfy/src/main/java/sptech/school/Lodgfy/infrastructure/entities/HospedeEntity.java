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
    private String nome;

    @Schema(example = "joao.silva@email.com", description = "Email do hóspede")
    @Column(nullable = false, unique = true, length = 255)
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
}