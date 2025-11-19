// ...existing code...
package sptech.school.Lodgfy.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sptech.school.Lodgfy.business.EmailService;
import sptech.school.Lodgfy.business.dto.OrcamentoEventoDTO;

import java.util.Map;

@RestController
@RequestMapping("/api/orcamentos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class OrcamentoController {

    private final EmailService emailService;

//    public OrcamentoController(EmailService emailService) {
//        this.emailService = emailService;
//    }

    @PostMapping("/eventos")
    public ResponseEntity<Map<String, String>> solicitarOrcamento(@Valid @RequestBody OrcamentoEventoDTO dto) {
        emailService.enviarOrcamentoEvento(dto);
        return ResponseEntity.ok(Map.of(
                "mensagem", "Orçamento enviado com sucesso",
                "status", "Entraremos em contato em até 24 horas"
        ));
    }
}

