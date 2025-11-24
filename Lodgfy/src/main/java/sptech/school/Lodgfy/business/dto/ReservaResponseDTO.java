package sptech.school.Lodgfy.business.dto;

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
public class ReservaResponseDTO {

    private Long idReserva;
    private LocalDate dataCheckIn;
    private LocalDate dataCheckOut;
    private BigDecimal valorTotal;
    private StatusReserva statusReserva;
    private Long hospedeId;
    private String hospedeNome;
    private Long chaleId;
    private String chaleNumero;
    private String chaleNome;
}

