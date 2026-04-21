package com.clinica.dailyautism.infrastructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.CompromissoService;
import com.clinica.dailyautism.infrastructure.dto.AgendaItemDTO;
import com.clinica.dailyautism.infrastructure.dto.AgendaResponsavelDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/agenda")
public class AgendaRestResource {

    private final CompromissoService compromissoService;

    @GetMapping("/profissional/{profissionalId}")
    @PreAuthorize("hasAuthority('VER_AGENDA')")
    public ResponseEntity<List<AgendaItemDTO>> agendaProfissional(
            @PathVariable String profissionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime de,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ate) {
        return ResponseEntity.ok(compromissoService.agendaProfissional(profissionalId, de, ate));
    }

    @GetMapping("/responsavel/{responsavelId}")
    @PreAuthorize("hasAuthority('VER_AGENDA')")
    public ResponseEntity<AgendaResponsavelDTO> agendaResponsavel(
            @PathVariable String responsavelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime de,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ate) {
        return ResponseEntity.ok(compromissoService.agendaResponsavel(responsavelId, de, ate));
    }
}