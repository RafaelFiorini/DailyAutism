package com.clinica.dailyautism.infrastructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.ContratoParticularService;
import com.clinica.dailyautism.domain.entity.ContratoParticular;
import com.clinica.dailyautism.infrastructure.dto.ContratoParticularDTO;
import com.clinica.dailyautism.infrastructure.dto.RegistrarPagamentoContratoDTO;
import com.clinica.dailyautism.infrastructure.dto.SaveContratoParticularDTO;
import com.clinica.dailyautism.infrastructure.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/financeiro/contratos-particulares")
@RequiredArgsConstructor
public class ContratoParticularRestResource {

    private final ContratoParticularService contratoParticularService;

    @GetMapping
    @PreAuthorize("hasAuthority('VER_FINANCEIRO')")
    public ResponseEntity<List<ContratoParticularDTO>> listar(
            @RequestParam(required = false) String clinicaId,
            @RequestParam(required = false) String pacienteId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        return ResponseEntity.ok(
                contratoParticularService.listarParaUsuario(clinicaId, pacienteId, email).stream()
                        .map(ContratoParticularDTO::create)
                        .toList()
        );
    }

    @PostMapping
    @PreAuthorize("hasAuthority('GERENCIAR_CONTRATO_PARTICULAR')")
    public ResponseEntity<ContratoParticularDTO> criar(
            @Valid @RequestBody SaveContratoParticularDTO dto,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        ContratoParticular saved = contratoParticularService.criarContrato(dto, email);
        return ResponseEntity.ok(ContratoParticularDTO.create(saved));
    }

    @PatchMapping("/{id}/pagar")
    @PreAuthorize("hasAuthority('GERENCIAR_CONTRATO_PARTICULAR')")
    public ResponseEntity<ContratoParticularDTO> registrarPagamento(
            @PathVariable String id,
            @RequestBody(required = false) RegistrarPagamentoContratoDTO dto,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        ContratoParticular saved = contratoParticularService.registrarPagamento(id, dto, email);
        return ResponseEntity.ok(ContratoParticularDTO.create(saved));
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('GERENCIAR_CONTRATO_PARTICULAR')")
    public ResponseEntity<ContratoParticularDTO> cancelar(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        ContratoParticular saved = contratoParticularService.cancelarContrato(id, email);
        return ResponseEntity.ok(ContratoParticularDTO.create(saved));
    }
}
