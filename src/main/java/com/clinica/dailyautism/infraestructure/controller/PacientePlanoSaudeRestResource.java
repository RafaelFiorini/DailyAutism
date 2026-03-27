// PacientePlanoSaudeRestResource
package com.clinica.dailyautism.infraestructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.PacientePlanoSaudeService;
import com.clinica.dailyautism.domain.entity.PacientePlanoSaude;
import com.clinica.dailyautism.infraestructure.dto.SavePacientePlanoSaudeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/paciente-plano-saude")
@RequiredArgsConstructor
public class PacientePlanoSaudeRestResource {

    private final PacientePlanoSaudeService pacientePlanoSaudeService;

    @PostMapping
    @PreAuthorize("hasAuthority('VINCULAR_PACIENTE_PLANO_SAUDE')")
    public ResponseEntity<PacientePlanoSaude> vincular(@RequestBody SavePacientePlanoSaudeDTO dto) {
        return ResponseEntity.ok(pacientePlanoSaudeService.vincular(dto));
    }

    @GetMapping("/paciente/{idPaciente}")
    @PreAuthorize("hasAuthority('VER_PACIENTE_PLANO_SAUDE')")
    public ResponseEntity<List<PacientePlanoSaude>> listarPorPaciente(@PathVariable String idPaciente) {
        return ResponseEntity.ok(pacientePlanoSaudeService.listarPorPaciente(idPaciente));
    }

    @GetMapping("/plano/{idPlanoSaude}")
    @PreAuthorize("hasAuthority('VER_PACIENTE_PLANO_SAUDE')")
    public ResponseEntity<List<PacientePlanoSaude>> listarPorPlano(@PathVariable String idPlanoSaude) {
        return ResponseEntity.ok(pacientePlanoSaudeService.listarPorPlano(idPlanoSaude));
    }

    @PutMapping("/{idVinculo}")
    @PreAuthorize("hasAuthority('EDITAR_PACIENTE_PLANO_SAUDE')")
    public ResponseEntity<PacientePlanoSaude> atualizar(@PathVariable String idVinculo,
                                                        @RequestBody SavePacientePlanoSaudeDTO dto) {
        return ResponseEntity.ok(pacientePlanoSaudeService.atualizar(idVinculo, dto));
    }

    @DeleteMapping("/{idVinculo}")
    @PreAuthorize("hasAuthority('DESVINCULAR_PACIENTE_PLANO_SAUDE')")
    public ResponseEntity<Void> desvincular(@PathVariable String idVinculo) {
        pacientePlanoSaudeService.desvincular(idVinculo);
        return ResponseEntity.noContent().build();
    }
}