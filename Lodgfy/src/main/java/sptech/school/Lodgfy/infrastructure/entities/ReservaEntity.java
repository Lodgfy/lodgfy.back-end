package sptech.school.Lodgfy.infrastructure.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sptech.school.Lodgfy.business.dto.StatusReserva;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "reservas")
@AllArgsConstructor
@NoArgsConstructor
public class ReservaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private Long idReserva;

    @NotNull
    @Column(name = "data_check_in", nullable = false)
    private LocalDate dataCheckIn;

    @NotNull
    @Column(name = "data_check_out", nullable = false)
    private LocalDate dataCheckOut;

    @NotNull
    @Column(name = "valor_total", nullable = false)
    private BigDecimal valorTotal;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status_reserva", nullable = false)
    private StatusReserva statusReserva;

    @ManyToOne
    @JoinColumn(name = "hospede_id", nullable = false)
    @NotNull
    private HospedeEntity hospede;

    @ManyToOne
    @JoinColumn(name = "chale_id", nullable = false)
    @NotNull
    private ChaleEntity chale;
}

