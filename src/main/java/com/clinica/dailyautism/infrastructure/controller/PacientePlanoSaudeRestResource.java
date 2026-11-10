package com.clinica.dailyautism.infrastructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.PacientePlanoSaudeService;
import com.clinica.dailyautism.domain.entity.PacientePlanoSaude;
import com.clinica.dailyautism.infrastructure.dto.PacientePlanoSaudeDTO;
import com.clinica.dailyautism.infrastructure.dto.SavePacientePlanoSaudeDTO;
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
    public ResponseEntity<PacientePlanoSaudeDTO> vincular(@RequestBody SavePacientePlanoSaudeDTO dto) {
        PacientePlanoSaude saved = pacientePlanoSaudeService.vincular(dto);
        return ResponseEntity.ok(PacientePlanoSaudeDTO.create(saved));
    }

    @GetMapping("/paciente/{idPaciente}")
    @PreAuthorize("hasAuthority('VER_PACIENTE_PLANO_SAUDE')")
    public ResponseEntity<List<PacientePlanoSaudeDTO>> listarPorPaciente(@PathVariable String idPaciente) {
        List<PacientePlanoSaudeDTO> list = pacientePlanoSaudeService.listarPorPaciente(idPaciente)
                .stream()
                .map(PacientePlanoSaudeDTO::create)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/plano/{idPlanoSaude}")
    @PreAuthorize("hasAuthority('VER_PACIENTE_PLANO_SAUDE')")
    public ResponseEntity<List<PacientePlanoSaudeDTO>> listarPorPlano(@PathVariable String idPlanoSaude) {
        List<PacientePlanoSaudeDTO> list = pacientePlanoSaudeService.listarPorPlano(idPlanoSaude)
                .stream()
                .map(PacientePlanoSaudeDTO::create)
                .toList();
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{idVinculo}")
    @PreAuthorize("hasAuthority('EDITAR_PACIENTE_PLANO_SAUDE')")
    public ResponseEntity<PacientePlanoSaudeDTO> atualizar(@PathVariable String idVinculo,
                                                           @RequestBody SavePacientePlanoSaudeDTO dto) {
        PacientePlanoSaude updated = pacientePlanoSaudeService.atualizar(idVinculo, dto);
        return ResponseEntity.ok(PacientePlanoSaudeDTO.create(updated));
    }

    @DeleteMapping("/{idVinculo}")
    @PreAuthorize("hasAuthority('DESVINCULAR_PACIENTE_PLANO_SAUDE')")
    public ResponseEntity<Void> desvincular(@PathVariable String idVinculo) {
        pacientePlanoSaudeService.desvincular(idVinculo);
        return ResponseEntity.noContent().build();
    }
}
