package sptech.school.Lodgfy.controller;


import sptech.school.Lodgfy.entity.Hospede;
import sptech.school.Lodgfy.repository.HospedeRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/hospedes")
public class HospedeController {


    @Autowired
    private HospedeRepository hospedeRepository;

    @GetMapping
    public List<Hospede> getAllHospedes() {
        return hospedeRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Hospede> createHospede(@Valid @RequestBody Hospede hospede) {
        Hospede savedHospede = hospedeRepository.save(hospede);
        return ResponseEntity.ok(savedHospede);
    }

}
