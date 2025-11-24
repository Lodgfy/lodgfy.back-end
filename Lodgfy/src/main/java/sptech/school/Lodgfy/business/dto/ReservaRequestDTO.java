package sptech.school.Lodgfy.business.dto;

import jakarta.validation.constraints.FutureOrPresent;
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

    @NotNull(message = "Data de check-in é obrigatória")
    @FutureOrPresent(message = "Data de check-in deve ser presente ou futura")
    private LocalDate dataCheckIn;

    @NotNull(message = "Data de check-out é obrigatória")
    @FutureOrPresent(message = "Data de check-out deve ser presente ou futura")
    private LocalDate dataCheckOut;

    @NotNull(message = "ID do hóspede é obrigatório")
    private Long hospedeId;

    @NotNull(message = "ID do chalé é obrigatório")
    private Long chaleId;
}

