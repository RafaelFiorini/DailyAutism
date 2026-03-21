package com.clinica.dailyautism.infraestructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.CompromissoService;
import com.clinica.dailyautism.domain.entity.Compromisso;
import com.clinica.dailyautism.infraestructure.dto.CompromissoDTO;
import com.clinica.dailyautism.infraestructure.dto.SaveCompromissoDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/compromissos")
public class CompromissoRestResource {

    private final CompromissoService compromissoService;

    @PostMapping
    public ResponseEntity<CompromissoDTO> createCompromisso(@Valid @RequestBody SaveCompromissoDTO saveCompromissoDTO) {
        Compromisso compromisso = compromissoService.createCompromisso(saveCompromissoDTO);
        return ResponseEntity.created(URI.create("/compromissos/" + compromisso.getIdCompromisso()))
                .body(CompromissoDTO.create(compromisso));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompromissoDTO> loadCompromisso(@PathVariable String id) {
        Compromisso compromisso = compromissoService.loadCompromisso(id);
        return ResponseEntity.ok(CompromissoDTO.create(compromisso));
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<CompromissoDTO>> listCompromissosByPaciente(@PathVariable String pacienteId) {
        List<CompromissoDTO> compromissos = compromissoService.listCompromissosByPaciente(pacienteId)
                .stream()
                .map(CompromissoDTO::create)
                .toList();
        return ResponseEntity.ok(compromissos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompromissoDTO> updateCompromisso(@PathVariable String id,
                                                            @Valid @RequestBody SaveCompromissoDTO saveCompromissoDTO) {
        Compromisso compromisso = compromissoService.updateCompromisso(id, saveCompromissoDTO);
        return ResponseEntity.ok(CompromissoDTO.create(compromisso));
    }

    @PatchMapping("/{id}/aprovar")
    public ResponseEntity<CompromissoDTO> aprovarCompromisso(@PathVariable String id) {
        Compromisso compromisso = compromissoService.aprovarCompromisso(id);
        return ResponseEntity.ok(CompromissoDTO.create(compromisso));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompromisso(@PathVariable String id) {
        compromissoService.deleteCompromisso(id);
        return ResponseEntity.noContent().build();
    }
}