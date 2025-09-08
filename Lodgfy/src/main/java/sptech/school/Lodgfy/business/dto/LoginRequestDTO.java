package sptech.school.Lodgfy.business.dto;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String cpf;
    private String senha;
}

