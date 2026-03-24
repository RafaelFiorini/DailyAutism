// ProfissionalPlanoSaudeRestResource
package com.clinica.dailyautism.infraestructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.ProfissionalPlanoSaudeService;
import com.clinica.dailyautism.domain.entity.ProfissionalPlanoSaude;
import com.clinica.dailyautism.infraestructure.dto.SaveProfissionalPlanoSaudeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/profissional-plano-saude")
@RequiredArgsConstructor
public class ProfissionalPlanoSaudeRestResource {

    private final ProfissionalPlanoSaudeService profissionalPlanoSaudeService;

    @PostMapping
    public ResponseEntity<ProfissionalPlanoSaude> vincular(@RequestBody SaveProfissionalPlanoSaudeDTO dto) {
        return ResponseEntity.ok(profissionalPlanoSaudeService.vincular(dto));
    }

    @GetMapping("/profissional/{idProf}")
    public ResponseEntity<List<ProfissionalPlanoSaude>> listarPorProfissional(@PathVariable String idProf) {
        return ResponseEntity.ok(profissionalPlanoSaudeService.listarPorProfissional(idProf));
    }

    @GetMapping("/plano/{idPlanoSaude}")
    public ResponseEntity<List<ProfissionalPlanoSaude>> listarPorPlano(@PathVariable String idPlanoSaude) {
        return ResponseEntity.ok(profissionalPlanoSaudeService.listarPorPlano(idPlanoSaude));
    }

    @PutMapping("/{idVinculo}")
    public ResponseEntity<ProfissionalPlanoSaude> atualizar(@PathVariable String idVinculo,
                                                            @RequestBody SaveProfissionalPlanoSaudeDTO dto) {
        return ResponseEntity.ok(profissionalPlanoSaudeService.atualizar(idVinculo, dto));
    }

    @DeleteMapping("/{idVinculo}")
    public ResponseEntity<Void> desvincular(@PathVariable String idVinculo) {
        profissionalPlanoSaudeService.desvincular(idVinculo);
        return ResponseEntity.noContent().build();
    }
}