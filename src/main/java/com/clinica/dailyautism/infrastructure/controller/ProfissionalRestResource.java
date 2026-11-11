package com.clinica.dailyautism.infrastructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.ProfissionalService;
import com.clinica.dailyautism.domain.entity.Profissional;
import com.clinica.dailyautism.infrastructure.dto.ProfissionalDTO;
import com.clinica.dailyautism.infrastructure.dto.SaveProfissionalDTO;
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
@RequestMapping("/profissionais")
public class ProfissionalRestResource {

    private final ProfissionalService profissionalService;

    @PostMapping
    @PreAuthorize("hasAuthority('CRIAR_PROFISSIONAL')")
    public ResponseEntity<ProfissionalDTO> createProfissional(@Valid @RequestBody SaveProfissionalDTO saveProfissionalDTO,
                                                              @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        Profissional profissional = profissionalService.createProfissional(saveProfissionalDTO, email);
        return ResponseEntity.created(URI.create("/profissionais/" + profissional.getIdProf()))
                .body(ProfissionalDTO.create(profissional));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VER_PROFISSIONAL')")
    public ResponseEntity<ProfissionalDTO> loadProfissional(@PathVariable String id,
                                                            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        Profissional profissional = profissionalService.loadProfissionalForUser(id, email);
        return ResponseEntity.ok(ProfissionalDTO.create(profissional));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProfissionalDTO> getMe(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        Profissional profissional = profissionalService.findByEmail(currentUser.getUsername());
        return ResponseEntity.ok(ProfissionalDTO.create(profissional));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('LISTAR_PROFISSIONAL')")
    public ResponseEntity<List<ProfissionalDTO>> listProfissionais(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        List<ProfissionalDTO> profissionais = profissionalService.listProfissionaisForUser(email)
                .stream()
                .map(ProfissionalDTO::create)
                .toList();
        return ResponseEntity.ok(profissionais);
    }

    @GetMapping("/inativos")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('EDITAR_PROFISSIONAL')")
    public ResponseEntity<List<ProfissionalDTO>> listProfissionaisInativos(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        List<ProfissionalDTO> profissionais = profissionalService.listProfissionaisInativosForUser(email)
                .stream()
                .map(ProfissionalDTO::create)
                .toList();
        return ResponseEntity.ok(profissionais);
    }

    @PatchMapping("/{id}/reativar")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('EDITAR_PROFISSIONAL')")
    public ResponseEntity<ProfissionalDTO> reativarProfissional(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        return ResponseEntity.ok(ProfissionalDTO.create(profissionalService.reativarProfissional(id, email)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EDITAR_PROFISSIONAL')")
    public ResponseEntity<ProfissionalDTO> updateProfissional(@PathVariable String id,
                                                              @Valid @RequestBody SaveProfissionalDTO saveProfissionalDTO) {
        Profissional profissional = profissionalService.updateProfissional(id, saveProfissionalDTO);
        return ResponseEntity.ok(ProfissionalDTO.create(profissional));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETAR_PROFISSIONAL')")
    public ResponseEntity<Void> deleteProfissional(@PathVariable String id) {
        profissionalService.deleteProfissional(id);
        return ResponseEntity.noContent().build();
    }
}
