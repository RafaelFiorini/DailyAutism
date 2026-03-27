// ClinicaPlanoSaudeRestResource
package com.clinica.dailyautism.infraestructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.ClinicaPlanoSaudeService;
import com.clinica.dailyautism.domain.entity.ClinicaPlanoSaude;
import com.clinica.dailyautism.infraestructure.dto.SaveClinicaPlanoSaudeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/clinica-plano-saude")
@RequiredArgsConstructor
public class ClinicaPlanoSaudeRestResource {

    private final ClinicaPlanoSaudeService clinicaPlanoSaudeService;

    @PostMapping
    @PreAuthorize("hasAuthority('VINCULAR_PLANO_SAUDE')")
    public ResponseEntity<ClinicaPlanoSaude> vincular(@RequestBody SaveClinicaPlanoSaudeDTO dto) {
        return ResponseEntity.ok(clinicaPlanoSaudeService.vincular(dto));
    }

    @GetMapping("/clinica/{idClinica}")
    @PreAuthorize("hasAuthority('VER_CLINICA_PLANO_SAUDE')")
    public ResponseEntity<List<ClinicaPlanoSaude>> listarPorClinica(@PathVariable String idClinica) {
        return ResponseEntity.ok(clinicaPlanoSaudeService.listarPorClinica(idClinica));
    }

    @GetMapping("/plano/{idPlanoSaude}")
    @PreAuthorize("hasAuthority('VER_CLINICA_PLANO_SAUDE')")
    public ResponseEntity<List<ClinicaPlanoSaude>> listarPorPlano(@PathVariable String idPlanoSaude) {
        return ResponseEntity.ok(clinicaPlanoSaudeService.listarPorPlano(idPlanoSaude));
    }

    @DeleteMapping("/{idVinculo}")
    @PreAuthorize("hasAuthority('DESVINCULAR_PLANO_SAUDE')")
    public ResponseEntity<Void> desvincular(@PathVariable String idVinculo) {
        clinicaPlanoSaudeService.desvincular(idVinculo);
        return ResponseEntity.noContent().build();
    }
}