package sptech.school.Lodgfy.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "HospedeResponseDTO", description = "DTO de retorno de dados do hóspede")
public class HospedeResponseDTO {

    @Schema(example = "1", description = "ID do hóspede")
    private Long id;

    @Schema(example = "João da Silva", description = "Nome completo do hóspede")
    private String nome;

    @Schema(example = "joao.silva@email.com", description = "Email do hóspede")
    private String email;

    @Schema(example = "(11) 98765-4321", description = "Telefone do hóspede")
    private String telefone;

    @Schema(example = "1990-01-01", description = "Data de nascimento do hóspede")
    private LocalDate dataNascimento;

    @Schema(example = "123.456.789-00", description = "CPF do hóspede")
    private String cpf;
}
