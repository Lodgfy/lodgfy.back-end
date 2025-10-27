package sptech.school.Lodgfy.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "ReservaResponseDTO", description = "DTO de retorno de dados da reserva")
public class ReservaResponseDTO {

    @Schema(example = "1", description = "ID da reserva")
    private Long idReserva;

    @Schema(example = "2025-11-01", description = "Data de check-in")
    private LocalDate dataCheckIn;

    @Schema(example = "2025-11-05", description = "Data de check-out")
    private LocalDate dataCheckOut;

    @Schema(example = "1400.00", description = "Valor total da reserva")
    private BigDecimal valorTotal;

    @Schema(example = "CONFIRMADA", description = "Status da reserva")
    private StatusReserva statusReserva;

    @Schema(description = "Dados do hóspede")
    private HospedeResponseDTO hospede;

    @Schema(description = "Dados do chalé")
    private ChaleResponseDTO chale;
}

