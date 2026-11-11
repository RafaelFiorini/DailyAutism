package com.clinica.dailyautism.infrastructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.FaturamentoConvenioService;
import com.clinica.dailyautism.domain.entity.GuiaFaturamentoConvenio;
import com.clinica.dailyautism.domain.entity.StatusGuiaFaturamento;
import com.clinica.dailyautism.infrastructure.dto.*;
import com.clinica.dailyautism.infrastructure.security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/financeiro/guias-convenio")
@RequiredArgsConstructor
public class FaturamentoConvenioRestResource {

    private final FaturamentoConvenioService faturamentoService;

    @GetMapping("/sessoes-elegiveis")
    @PreAuthorize("hasAuthority('GERENCIAR_FATURAMENTO_CONVENIO')")
    public ResponseEntity<List<SessaoElegivelDTO>> listarSessoesElegiveis(
            @RequestParam(required = false) String clinicaId,
            @RequestParam String pacienteId,
            @RequestParam String planoSaudeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        return ResponseEntity.ok(
                faturamentoService.listarSessoesElegiveis(clinicaId, pacienteId, planoSaudeId, inicio, fim, email)
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VER_FINANCEIRO')")
    public ResponseEntity<List<GuiaFaturamentoDTO>> listarGuias(
            @RequestParam(required = false) String clinicaId,
            @RequestParam(required = false) StatusGuiaFaturamento status,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        return ResponseEntity.ok(
                faturamentoService.listarGuias(clinicaId, status, email).stream()
                        .map(GuiaFaturamentoDTO::create)
                        .toList()
        );
    }

    @PostMapping
    @PreAuthorize("hasAuthority('GERENCIAR_FATURAMENTO_CONVENIO')")
    public ResponseEntity<GuiaFaturamentoDTO> gerarGuia(
            @Valid @RequestBody GerarGuiaFaturamentoDTO dto,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        GuiaFaturamentoConvenio saved = faturamentoService.gerarGuia(dto, email);
        return ResponseEntity.ok(GuiaFaturamentoDTO.create(saved));
    }

    @PatchMapping("/{id}/liberar-assinatura")
    @PreAuthorize("hasAuthority('GERENCIAR_FATURAMENTO_CONVENIO')")
    public ResponseEntity<GuiaFaturamentoDTO> liberarParaAssinatura(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        return ResponseEntity.ok(GuiaFaturamentoDTO.create(faturamentoService.liberarParaAssinatura(id, email)));
    }

    @PatchMapping("/{id}/assinar")
    @PreAuthorize("hasAuthority('ASSINAR_GUIA_CONVENIO')")
    public ResponseEntity<GuiaFaturamentoDTO> assinarGuia(
            @PathVariable String id,
            @Valid @RequestBody AssinarGuiaDTO dto,
            HttpServletRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        return ResponseEntity.ok(GuiaFaturamentoDTO.create(faturamentoService.assinarGuia(id, dto, ip, email)));
    }

    @PatchMapping("/{id}/enviar")
    @PreAuthorize("hasAuthority('GERENCIAR_FATURAMENTO_CONVENIO')")
    public ResponseEntity<GuiaFaturamentoDTO> marcarEnviada(
            @PathVariable String id,
            @RequestBody(required = false) EnviarGuiaConvenioDTO dto,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        return ResponseEntity.ok(GuiaFaturamentoDTO.create(faturamentoService.marcarEnviadaAoConvenio(id, dto, email)));
    }

    @PatchMapping("/{id}/pagar")
    @PreAuthorize("hasAuthority('GERENCIAR_FATURAMENTO_CONVENIO')")
    public ResponseEntity<GuiaFaturamentoDTO> registrarPagamento(
            @PathVariable String id,
            @RequestBody(required = false) RegistrarPagamentoGuiaDTO dto,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        return ResponseEntity.ok(GuiaFaturamentoDTO.create(faturamentoService.registrarPagamento(id, dto, email)));
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('GERENCIAR_FATURAMENTO_CONVENIO')")
    public ResponseEntity<GuiaFaturamentoDTO> cancelarGuia(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        return ResponseEntity.ok(GuiaFaturamentoDTO.create(faturamentoService.cancelarGuia(id, email)));
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAuthority('VER_FINANCEIRO')")
    public ResponseEntity<byte[]> baixarPdf(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        GuiaFaturamentoConvenio guia = faturamentoService.loadGuiaComAcesso(id, email);
        byte[] bytes = faturamentoService.baixarPdf(id, email);
        String filename = (guia.getNumeroLoteGuia() != null ? guia.getNumeroLoteGuia() : "guia") + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }
}
