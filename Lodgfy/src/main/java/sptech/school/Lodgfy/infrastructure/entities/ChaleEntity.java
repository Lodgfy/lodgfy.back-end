package sptech.school.Lodgfy.infrastructure.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Entity
@Table(name = "chale")
public class ChaleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_chale")
    private Long idChale;

    @NotBlank
    private String nome;

    @NotBlank
    @Column(unique = true)
    private String numero;

    private String tipo; // Standard, Luxo, Família, Suíte

    @Column(name = "valor_diaria")
    private BigDecimal valorDiaria;

    @NotNull
    private Integer capacidade;

    private String descricao;

    @Enumerated(EnumType.STRING)
    private StatusChale status;

    public enum StatusChale {
        DISPONIVEL, OCUPADO, LIMPEZA
    }

    // Getters e Setters


    public Long getIdChale() {
        return idChale;
    }

    public void setIdChale(Long idChale) {
        this.idChale = idChale;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getValorDiaria() {
        return valorDiaria;
    }

    public void setValorDiaria(BigDecimal valorDiaria) {
        this.valorDiaria = valorDiaria;
    }

    public Integer getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(Integer capacidade) {
        this.capacidade = capacidade;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public StatusChale getStatus() {
        return status;
    }

    public void setStatus(StatusChale status) {
        this.status = status;
    }
}
