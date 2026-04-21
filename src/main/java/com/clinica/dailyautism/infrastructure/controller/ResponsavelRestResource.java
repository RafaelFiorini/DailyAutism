package com.clinica.dailyautism.infrastructure.controller;


import com.clinica.dailyautism.domain.aplicationservice.ResponsavelService;
import com.clinica.dailyautism.domain.entity.Responsavel;
import com.clinica.dailyautism.infrastructure.dto.ResponsavelDTO;
import com.clinica.dailyautism.infrastructure.dto.SaveResponsavelDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/responsaveis")
public class ResponsavelRestResource {

    private final ResponsavelService responsavelService;

    @PostMapping
    @PreAuthorize("hasAuthority('CRIAR_RESPONSAVEL')")
    public ResponseEntity<ResponsavelDTO> createResponsavel(@Valid @RequestBody SaveResponsavelDTO saveResponsavelDTO) {
        Responsavel responsavel = responsavelService.createResponsavel(saveResponsavelDTO);
        return ResponseEntity.created(URI.create("/responsaveis/" + responsavel.getIdResponsavel()))
                .body(ResponsavelDTO.create(responsavel));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VER_RESPONSAVEL')")
    public ResponseEntity<ResponsavelDTO> loadResponsavel(@PathVariable String id) {
        Responsavel responsavel = responsavelService.loadResponsavel(id);
        return ResponseEntity.ok(ResponsavelDTO.create(responsavel));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('LISTAR_RESPONSAVEL')")
    public ResponseEntity<List<ResponsavelDTO>> listResponsaveis() {
        List<ResponsavelDTO> responsaveis = responsavelService.listResponsaveis()
                .stream()
                .map(ResponsavelDTO::create)
                .toList();
        return ResponseEntity.ok(responsaveis);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETAR_RESPONSAVEL')")
    public ResponseEntity<Void> deleteResponsavel(@PathVariable String id) {
        responsavelService.deleteResponsavel(id);
        return ResponseEntity.noContent().build();
    }
}