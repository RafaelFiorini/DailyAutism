package com.clinica.dailyautism.infrastructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.AuditoriaService;
import com.clinica.dailyautism.infrastructure.dto.AuditoriaLogDTO;
import com.clinica.dailyautism.infrastructure.dto.FiltroAuditoriaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auditoria")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_ADMIN')")
public class AuditoriaRestResource {

    private final AuditoriaService auditoriaService;

    @GetMapping
    public ResponseEntity<Page<AuditoriaLogDTO>> listarLogs(
            @ModelAttribute FiltroAuditoriaDTO filtro,
            @PageableDefault(size = 20, sort = "dataHora", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AuditoriaLogDTO> resultado = auditoriaService.buscarComFiltros(filtro, pageable);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditoriaLogDTO> buscarPorId(@PathVariable String id) {
        AuditoriaLogDTO dto = auditoriaService.buscarPorId(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportarCsv(@ModelAttribute FiltroAuditoriaDTO filtro) {
        byte[] csv = auditoriaService.exportarCsv(filtro);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"relatorio_auditoria_dailyautism.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }
}
