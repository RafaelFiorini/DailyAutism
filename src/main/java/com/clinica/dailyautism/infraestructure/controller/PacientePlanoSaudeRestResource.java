// PacientePlanoSaudeRestResource
package com.clinica.dailyautism.infraestructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.PacientePlanoSaudeService;
import com.clinica.dailyautism.domain.entity.PacientePlanoSaude;
import com.clinica.dailyautism.infraestructure.dto.SavePacientePlanoSaudeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/paciente-plano-saude")
@RequiredArgsConstructor
public class PacientePlanoSaudeRestResource {

    private final PacientePlanoSaudeService pacientePlanoSaudeService;

    @PostMapping
    public ResponseEntity<PacientePlanoSaude> vincular(@RequestBody SavePacientePlanoSaudeDTO dto) {
        return ResponseEntity.ok(pacientePlanoSaudeService.vincular(dto));
    }

    @GetMapping("/paciente/{idPaciente}")
    public ResponseEntity<List<PacientePlanoSaude>> listarPorPaciente(@PathVariable String idPaciente) {
        return ResponseEntity.ok(pacientePlanoSaudeService.listarPorPaciente(idPaciente));
    }

    @GetMapping("/plano/{idPlanoSaude}")
    public ResponseEntity<List<PacientePlanoSaude>> listarPorPlano(@PathVariable String idPlanoSaude) {
        return ResponseEntity.ok(pacientePlanoSaudeService.listarPorPlano(idPlanoSaude));
    }

    @PutMapping("/{idVinculo}")
    public ResponseEntity<PacientePlanoSaude> atualizar(@PathVariable String idVinculo,
                                                        @RequestBody SavePacientePlanoSaudeDTO dto) {
        return ResponseEntity.ok(pacientePlanoSaudeService.atualizar(idVinculo, dto));
    }

    @DeleteMapping("/{idVinculo}")
    public ResponseEntity<Void> desvincular(@PathVariable String idVinculo) {
        pacientePlanoSaudeService.desvincular(idVinculo);
        return ResponseEntity.noContent().build();
    }
}