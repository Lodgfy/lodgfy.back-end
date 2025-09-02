package sptech.school.Lodgfy.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sptech.school.Lodgfy.model.Chale;
import sptech.school.Lodgfy.repository.ChaleRepository;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/chales")
public class ChaleController {

    private final ChaleRepository repository;

    public ChaleController(ChaleRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<Chale>> getChales(
            @RequestParam(required = false) String pesquisa
    ) {
        List<Chale> chales;
        if (pesquisa == null) {
            chales = repository.findAll();
        } else {
            // Caso queira pesquisar pelo nome ou número do chalé
            chales = repository.findByNomeContainsIgnoreCaseOrNumeroContainsIgnoreCase(pesquisa, pesquisa);
        }

        return chales.isEmpty()
                ? ResponseEntity.status(204).build()
                : ResponseEntity.status(200).body(chales);
    }

    @PostMapping
    public ResponseEntity<Chale> postChale(@RequestBody @Valid Chale chale) {
        Chale chaleSalvo = repository.save(chale);
        return ResponseEntity.status(201).body(chaleSalvo);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Chale> getChalePorId(@PathVariable Long id) {
        if (repository.existsById(id)) {
            Chale chaleEncontrado = repository.findById(id).get();
            return ResponseEntity.status(200).body(chaleEncontrado);
        } else {
            return ResponseEntity.status(404).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Chale> putChale(
            @PathVariable Long id,
            @RequestBody @Valid Chale chale
    ) {
        if (!repository.existsById(id)) {
            return ResponseEntity.status(404).build();
        }
        chale.setIdChale(id);
        Chale chaleSalvo = repository.save(chale);
        return ResponseEntity.status(200).body(chaleSalvo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChalePorId(@PathVariable Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.status(204).build();
        } else {
            return ResponseEntity.status(404).build();
        }
    }

    @GetMapping("/menor-preco")
    public ResponseEntity<List<Chale>> getChalesPorPreco(
            @RequestParam BigDecimal precoMaximo) {

        List<Chale> chales = repository.findByValorDiariaLessThanEqual(precoMaximo);

        return chales.isEmpty()
                ? ResponseEntity.status(204).build()
                : ResponseEntity.status(200).body(chales);
    }


}
