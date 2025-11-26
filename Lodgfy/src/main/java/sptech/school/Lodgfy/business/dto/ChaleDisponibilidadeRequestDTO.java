package sptech.school.Lodgfy.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "ChaleDisponibilidadeRequestDTO", description = "DTO de requisição para buscar chalés disponíveis")
public class ChaleDisponibilidadeRequestDTO {

    @Schema(example = "4", description = "Quantidade de pessoas")
    @NotNull(message = "Quantidade de pessoas é obrigatória")
    @Min(value = 1, message = "Quantidade de pessoas deve ser maior que zero")
    private Integer quantidadePessoas;

    @Schema(example = "2025-12-01", description = "Data de check-in")
    @NotNull(message = "Data de check-in é obrigatória")
    @FutureOrPresent(message = "Data de check-in deve ser hoje ou futura")
    private LocalDate dataCheckIn;

    @Schema(example = "2025-12-05", description = "Data de check-out")
    @NotNull(message = "Data de check-out é obrigatória")
    @Future(message = "Data de check-out deve ser futura")
    private LocalDate dataCheckOut;
}

