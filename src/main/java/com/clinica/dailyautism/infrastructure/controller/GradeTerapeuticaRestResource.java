package com.clinica.dailyautism.infrastructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.GradeTerapeuticaService;
import com.clinica.dailyautism.domain.entity.Compromisso;
import com.clinica.dailyautism.domain.entity.GradeTerapeutica;
import com.clinica.dailyautism.infrastructure.dto.*;
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
@RequestMapping("/grades-terapeuticas")
@RequiredArgsConstructor
public class GradeTerapeuticaRestResource {

    private final GradeTerapeuticaService gradeTerapeuticaService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('CRIAR_COMPROMISSO', 'ADMIN', 'ROLE_ADMIN', 'ROLE_ADMIN_CLINICA', 'ROLE_CLINICA', 'ROLE_FUNCIONARIO_CLINICA', 'ROLE_PROFISSIONAL')")
    public ResponseEntity<GradeTerapeuticaDTO> criarProposta(
            @Valid @RequestBody SaveGradeTerapeuticaDTO saveDTO
    ) {
        GradeTerapeutica grade = gradeTerapeuticaService.criarProposta(saveDTO);
        return ResponseEntity.created(URI.create("/grades-terapeuticas/" + grade.getIdGradeTerapeutica()))
                .body(GradeTerapeuticaDTO.fromEntity(grade));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('VER_COMPROMISSO', 'LISTAR_COMPROMISSO', 'ADMIN', 'ROLE_ADMIN', 'ROLE_ADMIN_CLINICA', 'ROLE_CLINICA', 'ROLE_FUNCIONARIO_CLINICA', 'ROLE_PROFISSIONAL', 'ROLE_RESPONSAVEL')")
    public ResponseEntity<GradeTerapeuticaDTO> carregarGrade(@PathVariable String id) {
        GradeTerapeutica grade = gradeTerapeuticaService.carregarGrade(id);
        return ResponseEntity.ok(GradeTerapeuticaDTO.fromEntity(grade));
    }

    @GetMapping("/paciente/{idPaciente}")
    @PreAuthorize("hasAnyAuthority('VER_COMPROMISSO', 'LISTAR_COMPROMISSO', 'ADMIN', 'ROLE_ADMIN', 'ROLE_ADMIN_CLINICA', 'ROLE_CLINICA', 'ROLE_FUNCIONARIO_CLINICA', 'ROLE_PROFISSIONAL', 'ROLE_RESPONSAVEL')")
    public ResponseEntity<List<GradeTerapeuticaDTO>> listarPorPaciente(@PathVariable String idPaciente) {
        List<GradeTerapeuticaDTO> dtos = gradeTerapeuticaService.listarPorPaciente(idPaciente)
                .stream()
                .map(GradeTerapeuticaDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/clinica/{idClinica}")
    @PreAuthorize("hasAnyAuthority('LISTAR_COMPROMISSO', 'ADMIN', 'ROLE_ADMIN', 'ROLE_ADMIN_CLINICA', 'ROLE_CLINICA', 'ROLE_FUNCIONARIO_CLINICA', 'ROLE_PROFISSIONAL')")
    public ResponseEntity<List<GradeTerapeuticaDTO>> listarPorClinica(@PathVariable String idClinica) {
        List<GradeTerapeuticaDTO> dtos = gradeTerapeuticaService.listarPorClinica(idClinica)
                .stream()
                .map(GradeTerapeuticaDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{id}/enviar")
    @PreAuthorize("hasAnyAuthority('CRIAR_COMPROMISSO', 'EDITAR_COMPROMISSO', 'ADMIN', 'ROLE_ADMIN', 'ROLE_ADMIN_CLINICA', 'ROLE_CLINICA', 'ROLE_FUNCIONARIO_CLINICA', 'ROLE_PROFISSIONAL')")
    public ResponseEntity<GradeTerapeuticaDTO> enviarParaAprovacao(@PathVariable String id) {
        GradeTerapeutica grade = gradeTerapeuticaService.enviarParaAprovacao(id);
        return ResponseEntity.ok(GradeTerapeuticaDTO.fromEntity(grade));
    }

    @PostMapping("/{id}/aprovar")
    @PreAuthorize("hasAnyAuthority('APROVAR_COMPROMISSO', 'CRIAR_COMPROMISSO', 'ADMIN', 'ROLE_ADMIN', 'ROLE_ADMIN_CLINICA', 'ROLE_CLINICA', 'ROLE_FUNCIONARIO_CLINICA', 'ROLE_RESPONSAVEL')")
    public ResponseEntity<List<CompromissoDTO>> aprovarEReplicar(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        String email = currentUser != null ? currentUser.getUsername() : "sistema@clinica.com";
        List<Compromisso> compromissos = gradeTerapeuticaService.aprovarEReplicar(id, email);
        List<CompromissoDTO> dtos = compromissos.stream()
                .map(CompromissoDTO::create)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{id}/sugerir")
    @PreAuthorize("hasAnyAuthority('APROVAR_COMPROMISSO', 'ADMIN', 'ROLE_ADMIN', 'ROLE_RESPONSAVEL')")
    public ResponseEntity<GradeTerapeuticaDTO> sugerirAlteracao(
            @PathVariable String id,
            @Valid @RequestBody SugestaoGradeDTO sugestaoDTO
    ) {
        GradeTerapeutica grade = gradeTerapeuticaService.registrarSugestaoResponsavel(id, sugestaoDTO.getJustificativa());
        return ResponseEntity.ok(GradeTerapeuticaDTO.fromEntity(grade));
    }

    @PostMapping("/{id}/rejeitar")
    @PreAuthorize("hasAnyAuthority('APROVAR_COMPROMISSO', 'ADMIN', 'ROLE_ADMIN', 'ROLE_RESPONSAVEL')")
    public ResponseEntity<GradeTerapeuticaDTO> rejeitar(
            @PathVariable String id,
            @RequestBody(required = false) SugestaoGradeDTO motivoDTO
    ) {
        String motivo = motivoDTO != null ? motivoDTO.getJustificativa() : "Rejeitado pelo responsável";
        GradeTerapeutica grade = gradeTerapeuticaService.rejeitar(id, motivo);
        return ResponseEntity.ok(GradeTerapeuticaDTO.fromEntity(grade));
    }

    @PostMapping("/{id}/alterar")
    @PreAuthorize("hasAnyAuthority('CRIAR_COMPROMISSO', 'EDITAR_COMPROMISSO', 'ADMIN', 'ROLE_ADMIN', 'ROLE_ADMIN_CLINICA', 'ROLE_CLINICA', 'ROLE_FUNCIONARIO_CLINICA', 'ROLE_PROFISSIONAL')")
    public ResponseEntity<GradeTerapeuticaDTO> proporAlteracao(
            @PathVariable String id,
            @Valid @RequestBody ProporAlteracaoGradeDTO alterarDTO
    ) {
        GradeTerapeutica novaVersao = gradeTerapeuticaService.proporAlteracao(id, alterarDTO);
        return ResponseEntity.ok(GradeTerapeuticaDTO.fromEntity(novaVersao));
    }

    @PostMapping("/{id}/cancelar-profissional")
    @PreAuthorize("hasAnyAuthority('EDITAR_COMPROMISSO', 'ADMIN', 'ROLE_ADMIN', 'ROLE_ADMIN_CLINICA', 'ROLE_CLINICA', 'ROLE_FUNCIONARIO_CLINICA')")
    public ResponseEntity<Integer> cancelarProfissional(
            @PathVariable String id,
            @Valid @RequestBody CancelarProfissionalGradeDTO cancelarDTO
    ) {
        int cancelados = gradeTerapeuticaService.cancelarCompromissosFuturosPorProfissional(
                id,
                cancelarDTO.getIdProfissional(),
                cancelarDTO.getDataCorte(),
                cancelarDTO.getMotivo()
        );
        return ResponseEntity.ok(cancelados);
    }

    @GetMapping("/{id}/conflitos")
    @PreAuthorize("hasAnyAuthority('VER_COMPROMISSO', 'ADMIN')")
    public ResponseEntity<List<CompromissoDTO>> listarConflitosPorGrade(@PathVariable String id) {
        List<CompromissoDTO> dtos = gradeTerapeuticaService.listarConflitosPendentes(id)
                .stream()
                .map(CompromissoDTO::create)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/conflitos/paciente/{idPaciente}")
    @PreAuthorize("hasAnyAuthority('VER_COMPROMISSO', 'ADMIN')")
    public ResponseEntity<List<CompromissoDTO>> listarConflitosPorPaciente(@PathVariable String idPaciente) {
        List<CompromissoDTO> dtos = gradeTerapeuticaService.listarConflitosPendentesPorPaciente(idPaciente)
                .stream()
                .map(CompromissoDTO::create)
                .toList();
        return ResponseEntity.ok(dtos);
    }
}
