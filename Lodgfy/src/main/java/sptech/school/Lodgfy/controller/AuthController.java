package sptech.school.Lodgfy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sptech.school.Lodgfy.business.dto.LoginRequestDTO;
import sptech.school.Lodgfy.business.dto.LoginResponseDTO;
import sptech.school.Lodgfy.business.HospedeService;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Endpoints para autenticação de usuários")
@RequiredArgsConstructor
public class AuthController {

    private final HospedeService hospedeService;

    @PostMapping("/login")
    @Operation(
        summary = "Login de usuário",
        description = "Realiza login usando CPF e senha, retorna token JWT"
    )
    @ApiResponse(responseCode = "200", description = "Login realizado com sucesso")
    @ApiResponse(responseCode = "401", description = "CPF ou senha incorretos")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {
        LoginResponseDTO response = hospedeService.login(loginRequest);
        return ResponseEntity.ok(response);
    }
}
