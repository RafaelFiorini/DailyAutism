// PacienteClinicaRestResource
package com.clinica.dailyautism.infrastructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.PacienteClinicaService;
import com.clinica.dailyautism.domain.entity.PacienteClinica;
import com.clinica.dailyautism.infrastructure.dto.SavePacienteClinicaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/paciente-clinica")
@RequiredArgsConstructor
public class PacienteClinicaRestResource {

    private final PacienteClinicaService pacienteClinicaService;

    @PostMapping
    @PreAuthorize("hasAuthority('VINCULAR_PACIENTE_CLINICA')")
    public ResponseEntity<PacienteClinica> vincular(@RequestBody SavePacienteClinicaDTO dto) {
        return ResponseEntity.ok(pacienteClinicaService.vincular(dto));
    }

    @GetMapping("/paciente/{idPaciente}")
    @PreAuthorize("hasAuthority('VER_PACIENTE_CLINICA')")
    public ResponseEntity<List<PacienteClinica>> listarPorPaciente(@PathVariable String idPaciente) {
        return ResponseEntity.ok(pacienteClinicaService.listarPorPaciente(idPaciente));
    }

    @GetMapping("/clinica/{idClinica}")
    @PreAuthorize("hasAuthority('VER_PACIENTE_CLINICA')")
    public ResponseEntity<List<PacienteClinica>> listarPorClinica(@PathVariable String idClinica) {
        return ResponseEntity.ok(pacienteClinicaService.listarPorClinica(idClinica));
    }

    @DeleteMapping("/{idVinculo}")
    @PreAuthorize("hasAuthority('DESVINCULAR_PACIENTE_CLINICA')")
    public ResponseEntity<Void> desvincular(@PathVariable String idVinculo) {
        pacienteClinicaService.desvincular(idVinculo);
        return ResponseEntity.noContent().build();
    }
}