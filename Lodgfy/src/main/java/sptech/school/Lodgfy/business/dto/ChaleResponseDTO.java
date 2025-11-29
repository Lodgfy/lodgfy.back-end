package sptech.school.Lodgfy.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "ChaleResponseDTO", description = "DTO de retorno de dados do chalé")
public class ChaleResponseDTO {

    @Schema(example = "1", description = "ID do chalé")
    private Long idChale;

    @Schema(example = "Chalé das Montanhas", description = "Nome do chalé")
    private String nome;

    @Schema(example = "A101", description = "Número do chalé")
    private String numero;

    @Schema(example = "Luxo", description = "Tipo do chalé")
    private String tipo;

    @Schema(example = "350.00", description = "Valor da diária")
    private BigDecimal valorDiaria;

    @Schema(example = "4", description = "Capacidade de pessoas")
    private Integer capacidade;

    @Schema(example = "Chalé luxuoso com vista para as montanhas", description = "Descrição do chalé")
    private String descricao;

    @Schema(example = "DISPONIVEL", description = "Status do chalé (DISPONIVEL, OCUPADO, LIMPEZA)")
    private StatusChale status;
}