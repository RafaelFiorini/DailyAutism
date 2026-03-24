package com.clinica.dailyautism.infraestructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.PlanoSaudeService;
import com.clinica.dailyautism.domain.entity.PlanoSaude;
import com.clinica.dailyautism.infraestructure.dto.PlanoSaudeDTO;
import com.clinica.dailyautism.infraestructure.dto.SavePlanoSaudeDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/planos-saude")
public class PlanoSaudeRestResource {

    private final PlanoSaudeService planoSaudeService;

    @PostMapping
    public ResponseEntity<PlanoSaudeDTO> createPlanoSaude(@Valid @RequestBody SavePlanoSaudeDTO dto) {
        PlanoSaude plano = planoSaudeService.createPlanoSaude(dto);
        return ResponseEntity.created(URI.create("/planos-saude/" + plano.getIdPlanoSaude()))
                .body(PlanoSaudeDTO.create(plano));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanoSaudeDTO> loadPlanoSaude(@PathVariable String id) {
        return ResponseEntity.ok(PlanoSaudeDTO.create(planoSaudeService.loadPlanoSaude(id)));
    }

    @GetMapping
    public ResponseEntity<List<PlanoSaudeDTO>> listPlanosSaude() {
        List<PlanoSaudeDTO> planos = planoSaudeService.listPlanosSaude()
                .stream()
                .map(PlanoSaudeDTO::create)
                .toList();
        return ResponseEntity.ok(planos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlanoSaudeDTO> updatePlanoSaude(@PathVariable String id,
                                                          @Valid @RequestBody SavePlanoSaudeDTO dto) {
        return ResponseEntity.ok(PlanoSaudeDTO.create(planoSaudeService.updatePlanoSaude(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlanoSaude(@PathVariable String id) {
        planoSaudeService.deletePlanoSaude(id);
        return ResponseEntity.noContent().build();
    }
}