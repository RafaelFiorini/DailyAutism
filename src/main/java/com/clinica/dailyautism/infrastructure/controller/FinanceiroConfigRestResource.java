package com.clinica.dailyautism.infrastructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.ClinicaAccessHelper;
import com.clinica.dailyautism.domain.aplicationservice.ClinicaPlanoSaudeService;
import com.clinica.dailyautism.domain.aplicationservice.PacoteParticularService;
import com.clinica.dailyautism.domain.aplicationservice.TabelaPrecoService;
import com.clinica.dailyautism.domain.entity.Clinica;
import com.clinica.dailyautism.domain.entity.ClinicaPlanoSaude;
import com.clinica.dailyautism.domain.entity.PacoteParticular;
import com.clinica.dailyautism.domain.entity.TabelaPrecoClinica;
import com.clinica.dailyautism.infrastructure.dto.*;
import com.clinica.dailyautism.infrastructure.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/financeiro")
@RequiredArgsConstructor
public class FinanceiroConfigRestResource {

    private final TabelaPrecoService tabelaPrecoService;
    private final PacoteParticularService pacoteParticularService;
    private final ClinicaPlanoSaudeService clinicaPlanoSaudeService;
    private final ClinicaAccessHelper clinicaAccessHelper;

    // =================== TABELA DE PREÇOS ===================

    @GetMapping("/precos")
    @PreAuthorize("hasAuthority('VER_FINANCEIRO')")
    public ResponseEntity<List<TabelaPrecoDTO>> listarPrecos(
            @RequestParam(required = false) String clinicaId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        return ResponseEntity.ok(
                tabelaPrecoService.listarPorClinica(clinicaId, email).stream()
                        .map(TabelaPrecoDTO::create)
                        .toList()
        );
    }

    @PostMapping("/precos")
    @PreAuthorize("hasAuthority('GERENCIAR_PRECOS_CLINICA')")
    public ResponseEntity<TabelaPrecoDTO> salvarPreco(
            @Valid @RequestBody SaveTabelaPrecoDTO dto,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        TabelaPrecoClinica saved = tabelaPrecoService.salvarOuAtualizar(dto, email);
        return ResponseEntity.ok(TabelaPrecoDTO.create(saved));
    }

    @DeleteMapping("/precos/{id}")
    @PreAuthorize("hasAuthority('GERENCIAR_PRECOS_CLINICA')")
    public ResponseEntity<Void> excluirPreco(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        tabelaPrecoService.excluir(id, email);
        return ResponseEntity.noContent().build();
    }

    // =================== PACOTES PARTICULARES ===================

    @GetMapping("/pacotes")
    @PreAuthorize("hasAuthority('VER_FINANCEIRO')")
    public ResponseEntity<List<PacoteParticularDTO>> listarPacotes(
            @RequestParam(required = false) String clinicaId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        return ResponseEntity.ok(
                pacoteParticularService.listarPorClinica(clinicaId, email).stream()
                        .map(PacoteParticularDTO::create)
                        .toList()
        );
    }

    @PostMapping("/pacotes")
    @PreAuthorize("hasAuthority('GERENCIAR_PRECOS_CLINICA')")
    public ResponseEntity<PacoteParticularDTO> criarPacote(
            @Valid @RequestBody SavePacoteParticularDTO dto,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        PacoteParticular saved = pacoteParticularService.salvar(null, dto, email);
        return ResponseEntity.ok(PacoteParticularDTO.create(saved));
    }

    @PutMapping("/pacotes/{id}")
    @PreAuthorize("hasAuthority('GERENCIAR_PRECOS_CLINICA')")
    public ResponseEntity<PacoteParticularDTO> atualizarPacote(
            @PathVariable String id,
            @Valid @RequestBody SavePacoteParticularDTO dto,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        PacoteParticular saved = pacoteParticularService.salvar(id, dto, email);
        return ResponseEntity.ok(PacoteParticularDTO.create(saved));
    }

    @DeleteMapping("/pacotes/{id}")
    @PreAuthorize("hasAuthority('GERENCIAR_PRECOS_CLINICA')")
    public ResponseEntity<Void> excluirPacote(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        pacoteParticularService.excluir(id, email);
        return ResponseEntity.noContent().build();
    }

    // =================== PRAZOS DE CONVÊNIOS POR CLÍNICA ===================

    @GetMapping("/convenios-prazos")
    @PreAuthorize("hasAuthority('VER_FINANCEIRO')")
    public ResponseEntity<List<ClinicaPlanoSaudeDTO>> listarPrazosConvenios(
            @RequestParam(required = false) String clinicaId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        List<Clinica> clinicas = (clinicaId != null && !clinicaId.isBlank())
                ? List.of(clinicaAccessHelper.validarEAcessarClinica(clinicaId, email))
                : clinicaAccessHelper.listarClinicasAcessiveis(email);

        List<ClinicaPlanoSaudeDTO> result = clinicas.stream()
                .flatMap(c -> clinicaPlanoSaudeService.listarPorClinica(c.getIdClinica()).stream())
                .map(ClinicaPlanoSaudeDTO::create)
                .toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/convenios-prazos")
    @PreAuthorize("hasAuthority('GERENCIAR_PRECOS_CLINICA')")
    public ResponseEntity<ClinicaPlanoSaudeDTO> salvarPrazoConvenio(
            @RequestBody SaveClinicaPlanoSaudeDTO dto,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        Clinica clinica = clinicaAccessHelper.validarEAcessarClinica(dto.getIdClinica(), email);
        dto.setIdClinica(clinica.getIdClinica());
        ClinicaPlanoSaude saved = clinicaPlanoSaudeService.vincular(dto);
        return ResponseEntity.ok(ClinicaPlanoSaudeDTO.create(saved));
    }
}
