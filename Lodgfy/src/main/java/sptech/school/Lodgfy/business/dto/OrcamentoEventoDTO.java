// ...existing code...
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
public class OrcamentoEventoDTO {

    @NotBlank(message = "Tipo de evento é obrigatório")
    @Schema(example = "Casamento", description = "Tipo do evento")
    private String tipoEvento;

    @NotBlank(message = "Nome é obrigatório")
    @Schema(example = "João Silva", description = "Nome do solicitante")
    private String nome;

    @NotNull(message = "Data é obrigatória")
    @Future(message = "Data deve ser futura")
    @Schema(example = "2025-12-31", description = "Data desejada para o evento")
    private LocalDate data;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Schema(example = "joao@email.com", description = "E-mail de contato")
    private String email;

    @NotNull(message = "Número de convidados é obrigatório")
    @Min(value = 1, message = "Deve haver ao menos 1 convidado")
    @Schema(example = "50", description = "Quantidade de convidados")
    private Integer convidados;

    public String getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(String tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getConvidados() {
        return convidados;
    }

    public void setConvidados(Integer convidados) {
        this.convidados = convidados;
    }
}

