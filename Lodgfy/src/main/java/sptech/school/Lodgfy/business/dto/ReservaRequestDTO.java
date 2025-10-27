package sptech.school.Lodgfy.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReservaRequestDTO {

    @Schema(example = "2025-11-01", description = "Data de check-in")
    @NotNull(message = "Data de check-in é obrigatória")
    @Future(message = "Data de check-in deve ser futura")
    private LocalDate dataCheckIn;

    @Schema(example = "2025-11-05", description = "Data de check-out")
    @NotNull(message = "Data de check-out é obrigatória")
    @Future(message = "Data de check-out deve ser futura")
    private LocalDate dataCheckOut;

    @Schema(example = "1", description = "ID do hóspede")
    @NotNull(message = "ID do hóspede é obrigatório")
    private Long hospedeId;

    @Schema(example = "1", description = "ID do chalé")
    @NotNull(message = "ID do chalé é obrigatório")
    private Long chaleId;

    @Schema(example = "PENDENTE", description = "Status da reserva")
    private StatusReserva statusReserva;
}

