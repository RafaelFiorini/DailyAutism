// ProfissionalPlanoSaudeRestResource
package com.clinica.dailyautism.infraestructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.ProfissionalPlanoSaudeService;
import com.clinica.dailyautism.domain.entity.ProfissionalPlanoSaude;
import com.clinica.dailyautism.infraestructure.dto.SaveProfissionalPlanoSaudeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/profissional-plano-saude")
@RequiredArgsConstructor
public class ProfissionalPlanoSaudeRestResource {

    private final ProfissionalPlanoSaudeService profissionalPlanoSaudeService;

    @PostMapping
    @PreAuthorize("hasAuthority('VINCULAR_PROFISSIONAL_PLANO_SAUDE')")
    public ResponseEntity<ProfissionalPlanoSaude> vincular(@RequestBody SaveProfissionalPlanoSaudeDTO dto) {
        return ResponseEntity.ok(profissionalPlanoSaudeService.vincular(dto));
    }

    @GetMapping("/profissional/{idProf}")
    @PreAuthorize("hasAuthority('VER_PROFISSIONAL_PLANO_SAUDE')")
    public ResponseEntity<List<ProfissionalPlanoSaude>> listarPorProfissional(@PathVariable String idProf) {
        return ResponseEntity.ok(profissionalPlanoSaudeService.listarPorProfissional(idProf));
    }

    @GetMapping("/plano/{idPlanoSaude}")
    @PreAuthorize("hasAuthority('VER_PROFISSIONAL_PLANO_SAUDE')")
    public ResponseEntity<List<ProfissionalPlanoSaude>> listarPorPlano(@PathVariable String idPlanoSaude) {
        return ResponseEntity.ok(profissionalPlanoSaudeService.listarPorPlano(idPlanoSaude));
    }

    @PutMapping("/{idVinculo}")
    @PreAuthorize("hasAuthority('EDITAR_PROFISSIONAL_PLANO_SAUDE')")
    public ResponseEntity<ProfissionalPlanoSaude> atualizar(@PathVariable String idVinculo,
                                                            @RequestBody SaveProfissionalPlanoSaudeDTO dto) {
        return ResponseEntity.ok(profissionalPlanoSaudeService.atualizar(idVinculo, dto));
    }

    @DeleteMapping("/{idVinculo}")
    @PreAuthorize("hasAuthority('DESVINCULAR_PROFISSIONAL_PLANO_SAUDE')")
    public ResponseEntity<Void> desvincular(@PathVariable String idVinculo) {
        profissionalPlanoSaudeService.desvincular(idVinculo);
        return ResponseEntity.noContent().build();
    }
}