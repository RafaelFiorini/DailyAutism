package com.clinica.dailyautism.infraestructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.ClinicaService;
import com.clinica.dailyautism.domain.entity.Clinica;
import com.clinica.dailyautism.infraestructure.dto.ClinicaDTO;
import com.clinica.dailyautism.infraestructure.dto.SaveClinicaDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/clinicas")
public class ClinicaRestResource {

    private final ClinicaService clinicaService;

    @PostMapping
    public ResponseEntity<ClinicaDTO> createClinica(@Valid @RequestBody SaveClinicaDTO dto) {
        Clinica clinica = clinicaService.createClinica(dto);
        return ResponseEntity.created(URI.create("/clinicas/" + clinica.getIdClinica()))
                .body(ClinicaDTO.create(clinica));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClinicaDTO> loadClinica(@PathVariable String id) {
        return ResponseEntity.ok(ClinicaDTO.create(clinicaService.loadClinica(id)));
    }

    @GetMapping
    public ResponseEntity<List<ClinicaDTO>> listClinicas() {
        List<ClinicaDTO> clinicas = clinicaService.listClinicas()
                .stream()
                .map(ClinicaDTO::create)
                .toList();
        return ResponseEntity.ok(clinicas);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClinicaDTO> updateClinica(@PathVariable String id,
                                                    @Valid @RequestBody SaveClinicaDTO dto) {
        return ResponseEntity.ok(ClinicaDTO.create(clinicaService.updateClinica(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClinica(@PathVariable String id) {
        clinicaService.deleteClinica(id);
        return ResponseEntity.noContent().build();
    }
}