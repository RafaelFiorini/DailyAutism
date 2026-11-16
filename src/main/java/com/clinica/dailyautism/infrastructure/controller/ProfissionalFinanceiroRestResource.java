package com.clinica.dailyautism.infrastructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.LancamentoRepasseService;
import com.clinica.dailyautism.domain.aplicationservice.ProfissionalService;
import com.clinica.dailyautism.domain.entity.Profissional;
import com.clinica.dailyautism.domain.entity.StatusLancamentoRepasse;
import com.clinica.dailyautism.infrastructure.dto.*;
import com.clinica.dailyautism.infrastructure.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/financeiro/profissional")
@RequiredArgsConstructor
public class ProfissionalFinanceiroRestResource {

    private final LancamentoRepasseService lancamentoRepasseService;
    private final ProfissionalService profissionalService;

    private Profissional obterProfissionalLogado(UserDetailsImpl currentUser) {
        if (currentUser == null) {
            throw new RuntimeException("Usuário não autenticado");
        }
        return profissionalService.findByEmail(currentUser.getUsername());
    }

    private LocalDate parseMesAno(String mesAno) {
        if (mesAno == null || mesAno.isBlank()) {
            return LocalDate.now().withDayOfMonth(1);
        }
        try {
            return YearMonth.parse(mesAno.trim()).atDay(1);
        } catch (Exception e) {
            return LocalDate.now().withDayOfMonth(1);
        }
    }

    @GetMapping("/resumo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResumoFinanceiroProfissionalDTO> obterResumoMensal(
            @RequestParam(required = false) String mesAno,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        Profissional prof = obterProfissionalLogado(currentUser);
        LocalDate competencia = parseMesAno(mesAno);
        ResumoFinanceiroProfissionalDTO resumo = lancamentoRepasseService.obterResumoMensal(prof.getIdProf(), competencia);
        return ResponseEntity.ok(resumo);
    }

    @GetMapping("/lancamentos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LancamentoRepasseDTO>> listarLancamentos(
            @RequestParam(required = false) String mesAno,
            @RequestParam(required = false) String idClinica,
            @RequestParam(required = false) StatusLancamentoRepasse status,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        Profissional prof = obterProfissionalLogado(currentUser);
        LocalDate competencia = parseMesAno(mesAno);
        List<LancamentoRepasseDTO> lista = lancamentoRepasseService.filtrarLancamentos(
                prof.getIdProf(), competencia, idClinica, status);
        return ResponseEntity.ok(lista);
    }

    @PostMapping("/lancamentos/{id}/baixar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LancamentoRepasseDTO> darBaixaLancamento(
            @PathVariable String id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataPagamento,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        // Valida se o profissional tem acesso
        obterProfissionalLogado(currentUser);
        var atualizado = lancamentoRepasseService.darBaixaLancamento(id, dataPagamento);
        return ResponseEntity.ok(LancamentoRepasseDTO.create(atualizado));
    }

    @PostMapping("/lancamentos/{id}/reverter")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LancamentoRepasseDTO> reverterBaixaLancamento(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        obterProfissionalLogado(currentUser);
        var atualizado = lancamentoRepasseService.reverterBaixaLancamento(id);
        return ResponseEntity.ok(LancamentoRepasseDTO.create(atualizado));
    }

    @PostMapping("/clinicas/{idClinica}/baixar-mes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Integer> darBaixaLoteClinica(
            @PathVariable String idClinica,
            @RequestParam(required = false) String mesAno,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataPagamento,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        Profissional prof = obterProfissionalLogado(currentUser);
        LocalDate competencia = parseMesAno(mesAno);
        int totalBaixados = lancamentoRepasseService.darBaixaLoteClinica(
                prof.getIdProf(), idClinica, competencia, dataPagamento);
        return ResponseEntity.ok(totalBaixados);
    }

    @PutMapping("/meu-valor-particular")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BigDecimal> configurarValorParticular(
            @Valid @RequestBody ConfigurarValorParticularDTO dto,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        Profissional prof = obterProfissionalLogado(currentUser);
        BigDecimal salvo = lancamentoRepasseService.configurarValorParticular(prof.getIdProf(), dto.getValorSessao());
        return ResponseEntity.ok(salvo);
    }

    @PutMapping("/clinicas/{idClinica}/acordo-repasse")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> configurarAcordoRepasse(
            @PathVariable String idClinica,
            @Valid @RequestBody ConfigurarAcordoRepasseDTO dto,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        Profissional prof = obterProfissionalLogado(currentUser);
        lancamentoRepasseService.configurarAcordoRepasse(
                idClinica, prof.getIdProf(), dto.getTipoRepasse(), dto.getValorRepasse());
        return ResponseEntity.ok().build();
    }
}
