package com.clinica.dailyautism.infrastructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.ClinicaService;
import com.clinica.dailyautism.domain.entity.Clinica;
import com.clinica.dailyautism.infrastructure.dto.ClinicaDTO;
import com.clinica.dailyautism.infrastructure.dto.SaveClinicaDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/clinicas")
public class ClinicaRestResource {

    private final ClinicaService clinicaService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('CRIAR_CLINICA')")
    public ResponseEntity<ClinicaDTO> createClinica(@Valid @RequestBody SaveClinicaDTO dto) {
        Clinica clinica = clinicaService.createClinica(dto);
        return ResponseEntity.created(URI.create("/clinicas/" + clinica.getIdClinica()))
                .body(ClinicaDTO.create(clinica));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('VER_CLINICA')")
    public ResponseEntity<ClinicaDTO> loadClinica(@PathVariable String id) {
        return ResponseEntity.ok(ClinicaDTO.create(clinicaService.loadClinica(id)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('LISTAR_CLINICA')")
    public ResponseEntity<List<ClinicaDTO>> listClinicas() {
        List<ClinicaDTO> clinicas = clinicaService.listClinicas()
                .stream()
                .map(ClinicaDTO::create)
                .toList();
        return ResponseEntity.ok(clinicas);
    }

    @GetMapping("/inativos")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<ClinicaDTO>> listClinicasInativas() {
        List<ClinicaDTO> clinicas = clinicaService.listClinicasInativas()
                .stream()
                .map(ClinicaDTO::create)
                .toList();
        return ResponseEntity.ok(clinicas);
    }

    @PatchMapping("/{id}/reativar")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ClinicaDTO> reativarClinica(@PathVariable String id) {
        return ResponseEntity.ok(ClinicaDTO.create(clinicaService.reativarClinica(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('EDITAR_CLINICA')")
    public ResponseEntity<ClinicaDTO> updateClinica(@PathVariable String id,
                                                    @Valid @RequestBody SaveClinicaDTO dto) {
        return ResponseEntity.ok(ClinicaDTO.create(clinicaService.updateClinica(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('DELETAR_CLINICA')")
    public ResponseEntity<Void> deleteClinica(@PathVariable String id) {
        clinicaService.deleteClinica(id);
        return ResponseEntity.noContent().build();
    }
}
