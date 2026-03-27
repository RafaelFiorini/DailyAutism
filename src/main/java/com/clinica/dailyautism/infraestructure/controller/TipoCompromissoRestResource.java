package com.clinica.dailyautism.infraestructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.TipoCompromissoService;
import com.clinica.dailyautism.domain.entity.TipoCompromisso;
import com.clinica.dailyautism.infraestructure.dto.SaveTipoCompromissoDTO;
import com.clinica.dailyautism.infraestructure.dto.TipoCompromissoDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/tipos-compromisso")
public class TipoCompromissoRestResource {

    private final TipoCompromissoService tipoCompromissoService;

    @PostMapping
    @PreAuthorize("hasAuthority('CRIAR_TIPOCOMPROMISSO')")
    public ResponseEntity<TipoCompromissoDTO> createTipoCompromisso(@Valid @RequestBody SaveTipoCompromissoDTO saveTipoCompromissoDTO) {
        TipoCompromisso tipoCompromisso = tipoCompromissoService.createTipoCompromisso(saveTipoCompromissoDTO);
        return ResponseEntity.created(URI.create("/tipos-compromisso/" + tipoCompromisso.getIdTipoCompromisso()))
                .body(TipoCompromissoDTO.create(tipoCompromisso));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VER_TIPOCOMPROMISSO')")
    public ResponseEntity<TipoCompromissoDTO> loadTipoCompromisso(@PathVariable String id) {
        TipoCompromisso tipoCompromisso = tipoCompromissoService.loadTipoCompromisso(id);
        return ResponseEntity.ok(TipoCompromissoDTO.create(tipoCompromisso));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('LISTAR_TIPOCOMPROMISSO')")
    public ResponseEntity<List<TipoCompromissoDTO>> listTiposCompromisso() {
        List<TipoCompromissoDTO> tipos = tipoCompromissoService.listTiposCompromisso()
                .stream()
                .map(TipoCompromissoDTO::create)
                .toList();
        return ResponseEntity.ok(tipos);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EDITAR_TIPOCOMPROMISSO')")
    public ResponseEntity<TipoCompromissoDTO> updateTipoCompromisso(@PathVariable String id,
                                                                    @Valid @RequestBody SaveTipoCompromissoDTO saveTipoCompromissoDTO) {
        TipoCompromisso tipoCompromisso = tipoCompromissoService.updateTipoCompromisso(id, saveTipoCompromissoDTO);
        return ResponseEntity.ok(TipoCompromissoDTO.create(tipoCompromisso));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETAR_TIPOCOMPROMISSO')")
    public ResponseEntity<Void> deleteTipoCompromisso(@PathVariable String id) {
        tipoCompromissoService.deleteTipoCompromisso(id);
        return ResponseEntity.noContent().build();
    }
}