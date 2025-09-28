package sptech.school.Lodgfy.business.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import sptech.school.Lodgfy.security.enums.Role;

@Data
@AllArgsConstructor
public class LoginResponseDTO {
    private String token;
    private String tipo = "Bearer";
    private Long id;
    private String cpf;
    private String nome;
    private Role role;
    private Long expiresIn;

    public LoginResponseDTO(String token, Long id, String cpf, String nome, Role role, Long expiresIn) {
        this.token = token;
        this.id = id;
        this.cpf = cpf;
        this.nome = nome;
        this.role = role;
        this.expiresIn = expiresIn;
        this.tipo = "Bearer";
    }
}
