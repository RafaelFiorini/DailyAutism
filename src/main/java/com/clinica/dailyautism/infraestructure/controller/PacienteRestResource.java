package com.clinica.dailyautism.infraestructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.PacienteService;
import com.clinica.dailyautism.domain.entity.Paciente;
import com.clinica.dailyautism.infraestructure.dto.PacienteDTO;
import com.clinica.dailyautism.infraestructure.dto.SavePacienteDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/pacientes")
public class PacienteRestResource {

    private final PacienteService pacienteService;

    @PostMapping
    @PreAuthorize("hasAuthority('CRIAR_PACIENTE')")
    public ResponseEntity<PacienteDTO> createPaciente(@Valid @RequestBody SavePacienteDTO savePacienteDTO) {
        Paciente paciente = pacienteService.createPaciente(savePacienteDTO);
        return ResponseEntity.created(URI.create("/pacientes/" + paciente.getIdPaciente()))
                .body(PacienteDTO.create(paciente));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VER_PACIENTE')")
    public ResponseEntity<PacienteDTO> loadPaciente(@PathVariable String id) {
        Paciente paciente = pacienteService.loadPaciente(id);
        return ResponseEntity.ok(PacienteDTO.create(paciente));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('LISTAR_PACIENTE')")
    public ResponseEntity<List<PacienteDTO>> listPacientes() {
        List<PacienteDTO> pacientes = pacienteService.listPacientes()
                .stream()
                .map(PacienteDTO::create)
                .toList();
        return ResponseEntity.ok(pacientes);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EDITAR_PACIENTE')")
    public ResponseEntity<PacienteDTO> updatePaciente(@PathVariable String id,
                                                      @Valid @RequestBody SavePacienteDTO savePacienteDTO) {
        Paciente paciente = pacienteService.updatePaciente(id, savePacienteDTO);
        return ResponseEntity.ok(PacienteDTO.create(paciente));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETAR_PACIENTE')")
    public ResponseEntity<Void> deletePaciente(@PathVariable String id) {
        pacienteService.deletePaciente(id);
        return ResponseEntity.noContent().build();
    }
}