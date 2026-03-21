package com.clinica.dailyautism.infraestructure.controller;


import com.clinica.dailyautism.domain.aplicationservice.ResponsavelService;
import com.clinica.dailyautism.domain.entity.Responsavel;
import com.clinica.dailyautism.infraestructure.dto.ResponsavelDTO;
import com.clinica.dailyautism.infraestructure.dto.SaveResponsavelDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/responsaveis")
public class ResponsavelRestResource {

    private final ResponsavelService responsavelService;

    @PostMapping
    public ResponseEntity<ResponsavelDTO> createResponsavel(@Valid @RequestBody SaveResponsavelDTO saveResponsavelDTO) {
        Responsavel responsavel = responsavelService.createResponsavel(saveResponsavelDTO);
        return ResponseEntity.created(URI.create("/responsaveis/" + responsavel.getIdResponsavel()))
                .body(ResponsavelDTO.create(responsavel));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponsavelDTO> loadResponsavel(@PathVariable String id) {
        Responsavel responsavel = responsavelService.loadResponsavel(id);
        return ResponseEntity.ok(ResponsavelDTO.create(responsavel));
    }

    @GetMapping
    public ResponseEntity<List<ResponsavelDTO>> listResponsaveis() {
        List<ResponsavelDTO> responsaveis = responsavelService.listResponsaveis()
                .stream()
                .map(ResponsavelDTO::create)
                .toList();
        return ResponseEntity.ok(responsaveis);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResponsavel(@PathVariable String id) {
        responsavelService.deleteResponsavel(id);
        return ResponseEntity.noContent().build();
    }
}