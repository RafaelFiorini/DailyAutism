// PacienteClinicaRestResource
package com.clinica.dailyautism.infraestructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.PacienteClinicaService;
import com.clinica.dailyautism.domain.entity.PacienteClinica;
import com.clinica.dailyautism.infraestructure.dto.SavePacienteClinicaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/paciente-clinica")
@RequiredArgsConstructor
public class PacienteClinicaRestResource {

    private final PacienteClinicaService pacienteClinicaService;

    @PostMapping
    public ResponseEntity<PacienteClinica> vincular(@RequestBody SavePacienteClinicaDTO dto) {
        return ResponseEntity.ok(pacienteClinicaService.vincular(dto));
    }

    @GetMapping("/paciente/{idPaciente}")
    public ResponseEntity<List<PacienteClinica>> listarPorPaciente(@PathVariable String idPaciente) {
        return ResponseEntity.ok(pacienteClinicaService.listarPorPaciente(idPaciente));
    }

    @GetMapping("/clinica/{idClinica}")
    public ResponseEntity<List<PacienteClinica>> listarPorClinica(@PathVariable String idClinica) {
        return ResponseEntity.ok(pacienteClinicaService.listarPorClinica(idClinica));
    }

    @DeleteMapping("/{idVinculo}")
    public ResponseEntity<Void> desvincular(@PathVariable String idVinculo) {
        pacienteClinicaService.desvincular(idVinculo);
        return ResponseEntity.noContent().build();
    }
}