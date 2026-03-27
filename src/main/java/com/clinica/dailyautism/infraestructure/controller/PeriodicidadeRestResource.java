package com.clinica.dailyautism.infraestructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.PeriodicidadeService;
import com.clinica.dailyautism.domain.entity.Periodicidade;
import com.clinica.dailyautism.infraestructure.dto.SavePeriodicidadeDTO;
import com.clinica.dailyautism.infraestructure.dto.PeriodicidadeDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/periodicidades")
public class PeriodicidadeRestResource {

    private final PeriodicidadeService periodicidadeService;

    @PostMapping
    @PreAuthorize("hasAuthority('CRIAR_PERIODICIDADE')")
    public ResponseEntity<PeriodicidadeDTO> createPeriodicidade(@Valid @RequestBody SavePeriodicidadeDTO savePeriodicidadeDTO) {
        Periodicidade periodicidade = periodicidadeService.createPeriodicidade(savePeriodicidadeDTO);
        return ResponseEntity.created(URI.create("/periodicidades/" + periodicidade.getIdPeriodicidade()))
                .body(PeriodicidadeDTO.create(periodicidade));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VER_PERIODICIDADE')")
    public ResponseEntity<PeriodicidadeDTO> loadPeriodicidade(@PathVariable String id) {
        Periodicidade periodicidade = periodicidadeService.loadPeriodicidade(id);
        return ResponseEntity.ok(PeriodicidadeDTO.create(periodicidade));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('LISTAR_PERIODICIDADE')")
    public ResponseEntity<List<PeriodicidadeDTO>> listPeriodicidades() {
        List<PeriodicidadeDTO> periodicidades = periodicidadeService.listPeriodicidades()
                .stream()
                .map(PeriodicidadeDTO::create)
                .toList();
        return ResponseEntity.ok(periodicidades);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EDITAR_PERIODICIDADE')")
    public ResponseEntity<PeriodicidadeDTO> updatePeriodicidade(@PathVariable String id,
                                                                @Valid @RequestBody SavePeriodicidadeDTO savePeriodicidadeDTO) {
        Periodicidade periodicidade = periodicidadeService.updatePeriodicidade(id, savePeriodicidadeDTO);
        return ResponseEntity.ok(PeriodicidadeDTO.create(periodicidade));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETAR_PERIODICIDADE')")
    public ResponseEntity<Void> deletePeriodicidade(@PathVariable String id) {
        periodicidadeService.deletePeriodicidade(id);
        return ResponseEntity.noContent().build();
    }
}