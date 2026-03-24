// ProfissionalClinicaRestResource
package com.clinica.dailyautism.infraestructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.ProfissionalClinicaService;
import com.clinica.dailyautism.domain.entity.ProfissionalClinica;
import com.clinica.dailyautism.infraestructure.dto.SaveProfissionalClinicaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/profissional-clinica")
@RequiredArgsConstructor
public class ProfissionalClinicaRestResource {

    private final ProfissionalClinicaService profissionalClinicaService;

    @PostMapping
    public ResponseEntity<ProfissionalClinica> vincular(@RequestBody SaveProfissionalClinicaDTO dto) {
        return ResponseEntity.ok(profissionalClinicaService.vincular(dto));
    }

    @GetMapping("/profissional/{idProf}")
    public ResponseEntity<List<ProfissionalClinica>> listarPorProfissional(@PathVariable String idProf) {
        return ResponseEntity.ok(profissionalClinicaService.listarPorProfissional(idProf));
    }

    @GetMapping("/clinica/{idClinica}")
    public ResponseEntity<List<ProfissionalClinica>> listarPorClinica(@PathVariable String idClinica) {
        return ResponseEntity.ok(profissionalClinicaService.listarPorClinica(idClinica));
    }

    @PutMapping("/{idVinculo}")
    public ResponseEntity<ProfissionalClinica> atualizar(@PathVariable String idVinculo,
                                                         @RequestBody SaveProfissionalClinicaDTO dto) {
        return ResponseEntity.ok(profissionalClinicaService.atualizar(idVinculo, dto));
    }

    @DeleteMapping("/{idVinculo}")
    public ResponseEntity<Void> desvincular(@PathVariable String idVinculo) {
        profissionalClinicaService.desvincular(idVinculo);
        return ResponseEntity.noContent().build();
    }
}