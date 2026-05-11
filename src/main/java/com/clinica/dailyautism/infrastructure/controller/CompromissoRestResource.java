package com.clinica.dailyautism.infrastructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.CompromissoService;
import com.clinica.dailyautism.domain.entity.Compromisso;
import com.clinica.dailyautism.infrastructure.dto.AprovarCompromissoDTO;
import com.clinica.dailyautism.infrastructure.dto.CompromissoDTO;
import com.clinica.dailyautism.infrastructure.dto.SaveCompromissoDTO;
import com.clinica.dailyautism.infrastructure.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/compromissos")
public class CompromissoRestResource {

    private final CompromissoService compromissoService;

    @PostMapping
    @PreAuthorize("hasAuthority('CRIAR_COMPROMISSO')")
    public ResponseEntity<CompromissoDTO> createCompromisso(
            @Valid @RequestBody SaveCompromissoDTO saveCompromissoDTO,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        Compromisso compromisso = compromissoService.createCompromisso(saveCompromissoDTO, currentUser.getUsername());
        return ResponseEntity.created(URI.create("/compromissos/" + compromisso.getIdCompromisso()))
                .body(CompromissoDTO.create(compromisso));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VER_COMPROMISSO')")
    public ResponseEntity<CompromissoDTO> loadCompromisso(@PathVariable String id) {
        Compromisso compromisso = compromissoService.loadCompromisso(id);
        return ResponseEntity.ok(CompromissoDTO.create(compromisso));
    }

    @GetMapping("/paciente/{pacienteId}")
    @PreAuthorize("hasAuthority('LISTAR_COMPROMISSO')")
    public ResponseEntity<List<CompromissoDTO>> listCompromissosByPaciente(@PathVariable String pacienteId) {
        List<CompromissoDTO> compromissos = compromissoService.listCompromissosByPaciente(pacienteId)
                .stream()
                .map(CompromissoDTO::create)
                .toList();
        return ResponseEntity.ok(compromissos);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EDITAR_COMPROMISSO')")
    public ResponseEntity<CompromissoDTO> updateCompromisso(@PathVariable String id,
                                                            @Valid @RequestBody SaveCompromissoDTO saveCompromissoDTO) {
        Compromisso compromisso = compromissoService.updateCompromisso(id, saveCompromissoDTO);
        return ResponseEntity.ok(CompromissoDTO.create(compromisso));
    }

//    @PatchMapping("/{id}/aprovar")
//    @PreAuthorize("hasAuthority('APROVAR_COMPROMISSO')")
//    public ResponseEntity<CompromissoDTO> aprovarCompromisso(@PathVariable String id) {
//        Compromisso compromisso = compromissoService.aprovarCompromisso(id);
//        return ResponseEntity.ok(CompromissoDTO.create(compromisso));
//    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETAR_COMPROMISSO')")
    public ResponseEntity<Void> deleteCompromisso(@PathVariable String id) {
        compromissoService.deleteCompromisso(id);
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/{id}/rejeitar")
    @PreAuthorize("hasAuthority('APROVAR_COMPROMISSO')")
    public ResponseEntity<CompromissoDTO> rejeitarCompromisso(@PathVariable String id) {
        Compromisso compromisso = compromissoService.rejeitarCompromisso(id);
        return ResponseEntity.ok(CompromissoDTO.create(compromisso));
    }
    @PatchMapping("/{id}/aprovar")
    @PreAuthorize("hasAuthority('APROVAR_COMPROMISSO')")
    public ResponseEntity<List<CompromissoDTO>> aprovarCompromisso(
            @PathVariable String id,
            @RequestBody(required = false) AprovarCompromissoDTO dto) {
        List<Compromisso> compromissos = compromissoService.aprovarCompromisso(
                id, dto != null ? dto : new AprovarCompromissoDTO(null));
        return ResponseEntity.ok(compromissos.stream()
                .map(CompromissoDTO::create)
                .toList());
    }

}