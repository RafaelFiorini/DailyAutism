package com.clinica.dailyautism.infrastructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.ProfissionalPacienteService;
import com.clinica.dailyautism.domain.entity.ProfissionalPaciente;
import com.clinica.dailyautism.infrastructure.dto.SaveProfissionalPacienteDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/profissional-paciente")
@RequiredArgsConstructor
public class ProfissionalPacienteRestResource {

    private final ProfissionalPacienteService profissionalPacienteService;

    @PostMapping
    @PreAuthorize("hasAuthority('VINCULAR_PROFISSIONAL_PACIENTE')")
    public ResponseEntity<ProfissionalPaciente> vincular(@RequestBody SaveProfissionalPacienteDTO dto) {
        return ResponseEntity.ok(profissionalPacienteService.vincular(dto));
    }

    @GetMapping("/profissional/{idProf}")
    @PreAuthorize("hasAuthority('VER_PROFISSIONAL_PACIENTE')")
    public ResponseEntity<List<ProfissionalPaciente>> listarPorProfissional(@PathVariable String idProf) {
        return ResponseEntity.ok(profissionalPacienteService.listarPorProfissional(idProf));
    }

    @GetMapping("/paciente/{idPaciente}")
    @PreAuthorize("hasAuthority('VER_PROFISSIONAL_PACIENTE')")
    public ResponseEntity<List<ProfissionalPaciente>> listarPorPaciente(@PathVariable String idPaciente) {
        return ResponseEntity.ok(profissionalPacienteService.listarPorPaciente(idPaciente));
    }

    @PatchMapping("/{idVinculo}/anamnese")
    @PreAuthorize("hasAuthority('EDITAR_ANAMNESE')")
    public ResponseEntity<ProfissionalPaciente> atualizarAnamnese(
            @PathVariable String idVinculo,
            @RequestParam boolean anamnese) {
        return ResponseEntity.ok(profissionalPacienteService.atualizarAnamnese(idVinculo, anamnese));
    }

    @DeleteMapping("/{idVinculo}")
    @PreAuthorize("hasAuthority('DESVINCULAR_PROFISSIONAL_PACIENTE')")
    public ResponseEntity<Void> desvincular(@PathVariable String idVinculo) {
        profissionalPacienteService.desvincular(idVinculo);
        return ResponseEntity.noContent().build();
    }
}