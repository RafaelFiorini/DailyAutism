// ClinicaPlanoSaudeRestResource
package com.clinica.dailyautism.infraestructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.ClinicaPlanoSaudeService;
import com.clinica.dailyautism.domain.entity.ClinicaPlanoSaude;
import com.clinica.dailyautism.infraestructure.dto.SaveClinicaPlanoSaudeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/clinica-plano-saude")
@RequiredArgsConstructor
public class ClinicaPlanoSaudeRestResource {

    private final ClinicaPlanoSaudeService clinicaPlanoSaudeService;

    @PostMapping
    public ResponseEntity<ClinicaPlanoSaude> vincular(@RequestBody SaveClinicaPlanoSaudeDTO dto) {
        return ResponseEntity.ok(clinicaPlanoSaudeService.vincular(dto));
    }

    @GetMapping("/clinica/{idClinica}")
    public ResponseEntity<List<ClinicaPlanoSaude>> listarPorClinica(@PathVariable String idClinica) {
        return ResponseEntity.ok(clinicaPlanoSaudeService.listarPorClinica(idClinica));
    }

    @GetMapping("/plano/{idPlanoSaude}")
    public ResponseEntity<List<ClinicaPlanoSaude>> listarPorPlano(@PathVariable String idPlanoSaude) {
        return ResponseEntity.ok(clinicaPlanoSaudeService.listarPorPlano(idPlanoSaude));
    }

    @DeleteMapping("/{idVinculo}")
    public ResponseEntity<Void> desvincular(@PathVariable String idVinculo) {
        clinicaPlanoSaudeService.desvincular(idVinculo);
        return ResponseEntity.noContent().build();
    }
}