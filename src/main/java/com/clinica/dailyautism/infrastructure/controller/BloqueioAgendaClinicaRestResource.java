package com.clinica.dailyautism.infrastructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.BloqueioAgendaClinicaService;
import com.clinica.dailyautism.infrastructure.dto.BloqueioAgendaClinicaDTO;
import com.clinica.dailyautism.infrastructure.dto.SaveBloqueioAgendaClinicaDTO;
import com.clinica.dailyautism.infrastructure.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/bloqueios-agenda")
@RequiredArgsConstructor
public class BloqueioAgendaClinicaRestResource {

    private final BloqueioAgendaClinicaService bloqueioService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BloqueioAgendaClinicaDTO>> listar(
            @RequestParam(required = false) String idClinica,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        return ResponseEntity.ok(bloqueioService.listarPorUsuario(email, idClinica));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMIN_CLINICA', 'CLINICA', 'FUNCIONARIO_CLINICA')")
    public ResponseEntity<BloqueioAgendaClinicaDTO> criar(
            @Valid @RequestBody SaveBloqueioAgendaClinicaDTO dto,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        BloqueioAgendaClinicaDTO criado = bloqueioService.criarBloqueio(dto, email);
        return ResponseEntity.created(URI.create("/bloqueios-agenda/" + criado.getIdBloqueio()))
                .body(criado);
    }

    @PostMapping("/importar-lote")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMIN_CLINICA', 'CLINICA', 'FUNCIONARIO_CLINICA')")
    public ResponseEntity<List<BloqueioAgendaClinicaDTO>> importarEmLote(
            @Valid @RequestBody List<SaveBloqueioAgendaClinicaDTO> itens,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        return ResponseEntity.ok(bloqueioService.importarEmLote(itens, email));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMIN_CLINICA', 'CLINICA', 'FUNCIONARIO_CLINICA')")
    public ResponseEntity<Void> remover(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        bloqueioService.removerBloqueio(id, email);
        return ResponseEntity.noContent().build();
    }
}
