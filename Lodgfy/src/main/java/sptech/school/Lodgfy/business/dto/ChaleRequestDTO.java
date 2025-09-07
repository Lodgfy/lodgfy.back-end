package sptech.school.Lodgfy.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sptech.school.Lodgfy.infrastructure.entities.ChaleEntity.StatusChale;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChaleRequestDTO {

    @Schema(example = "Chalé das Montanhas", description = "Nome do chalé")
    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @Schema(example = "A101", description = "Número do chalé")
    @NotBlank(message = "Número é obrigatório")
    private String numero;

    @Schema(example = "Luxo", description = "Tipo do chalé")
    private String tipo;

    @Schema(example = "350.00", description = "Valor da diária")
    @DecimalMin(value = "0.0", message = "Valor da diária deve ser positivo")
    private BigDecimal valorDiaria;

    @Schema(example = "true", description = "Indica se o chalé está disponível")
    private Boolean disponivel;

    @Schema(example = "4", description = "Capacidade de pessoas")
    @NotNull(message = "Capacidade é obrigatória")
    @Min(value = 1, message = "Capacidade deve ser maior que zero")
    private Integer capacidade;

    @Schema(example = "Chalé luxuoso com vista para as montanhas", description = "Descrição do chalé")
    private String descricao;

    @Schema(example = "DISPONIVEL", description = "Status do chalé")
    private StatusChale status;
}