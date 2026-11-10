package com.clinica.dailyautism.infrastructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.ArquivoService;
import com.clinica.dailyautism.domain.entity.Arquivo;
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

@RequiredArgsConstructor
@RestController
@RequestMapping("/arquivos")
public class ArquivoRestResource {

    private final ArquivoService arquivoService;

    @PostMapping
    @PreAuthorize("hasAuthority('CRIAR_ARQUIVO')")
    public ResponseEntity<ArquivoDTO> createArquivo(
            @Valid @RequestBody SaveArquivoDTO saveArquivoDTO,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        Arquivo arquivo = arquivoService.createArquivo(saveArquivoDTO, email);
        return ResponseEntity.created(URI.create("/arquivos/" + arquivo.getIdArquivo()))
                .body(ArquivoDTO.create(arquivo));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('LISTAR_ARQUIVO')")
    public ResponseEntity<List<ArquivoDTO>> listArquivos(
            @RequestParam(required = false) String pacienteId,
            @RequestParam(required = false) String idTipoArquivo,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        List<ArquivoDTO> arquivos = arquivoService.listArquivosForUser(email, pacienteId, idTipoArquivo)
                .stream()
                .map(ArquivoDTO::create)
                .toList();
        return ResponseEntity.ok(arquivos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VER_ARQUIVO')")
    public ResponseEntity<ArquivoDTO> loadArquivo(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        Arquivo arquivo = arquivoService.loadArquivo(id, email);
        return ResponseEntity.ok(ArquivoDTO.create(arquivo));
    }

    @GetMapping("/paciente/{pacienteId}")
    @PreAuthorize("hasAuthority('LISTAR_ARQUIVO')")
    public ResponseEntity<List<ArquivoDTO>> listArquivosByPaciente(
            @PathVariable String pacienteId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        List<ArquivoDTO> arquivos = arquivoService.listArquivosByPacienteForUser(pacienteId, email)
                .stream()
                .map(ArquivoDTO::create)
                .toList();
        return ResponseEntity.ok(arquivos);
    }

    @GetMapping("/opcoes-compartilhamento/{pacienteId}")
    @PreAuthorize("hasAuthority('LISTAR_ARQUIVO') or hasAuthority('CRIAR_ARQUIVO')")
    public ResponseEntity<OpcoesCompartilhamentoDTO> opcoesCompartilhamento(
            @PathVariable String pacienteId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        return ResponseEntity.ok(arquivoService.opcoesCompartilhamento(pacienteId, email));
    }

    @PatchMapping("/{id}/compartilhar")
    @PreAuthorize("hasAuthority('CRIAR_ARQUIVO') or hasAuthority('VER_ARQUIVO')")
    public ResponseEntity<ArquivoDTO> compartilharArquivo(
            @PathVariable String id,
            @RequestBody CompartilharArquivoDTO dto,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        Arquivo arquivo = arquivoService.compartilharArquivo(id, dto, email);
        return ResponseEntity.ok(ArquivoDTO.create(arquivo));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAuthority('BAIXAR_ARQUIVO')")
    public ResponseEntity<ArquivoDownloadDTO> downloadArquivo(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        Arquivo arquivo = arquivoService.loadArquivoComBase64(id, email);
        return ResponseEntity.ok(ArquivoDownloadDTO.create(arquivo));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETAR_ARQUIVO')")
    public ResponseEntity<Void> deleteArquivo(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        arquivoService.deleteArquivo(id, email);
        return ResponseEntity.noContent().build();
    }
}
