package com.clinica.dailyautism.infrastructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.TipoArquivoService;
import com.clinica.dailyautism.domain.entity.TipoArquivo;
import com.clinica.dailyautism.infrastructure.dto.SaveTipoArquivoDTO;
import com.clinica.dailyautism.infrastructure.dto.TipoArquivoDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/tipos-arquivo")
public class TipoArquivoRestResource {

    private final TipoArquivoService tipoArquivoService;

    @PostMapping
    @PreAuthorize("hasAuthority('CRIAR_TIPOARQUIVO')")
    public ResponseEntity<TipoArquivoDTO> createTipoArquivo(@Valid @RequestBody SaveTipoArquivoDTO saveTipoArquivoDTO) {
        TipoArquivo tipoArquivo = tipoArquivoService.createTipoArquivo(saveTipoArquivoDTO);
        return ResponseEntity.created(URI.create("/tipos-arquivo/" + tipoArquivo.getIdTipoArquivo()))
                .body(TipoArquivoDTO.create(tipoArquivo));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VER_TIPOARQUIVO')")
    public ResponseEntity<TipoArquivoDTO> loadTipoArquivo(@PathVariable String id) {
        TipoArquivo tipoArquivo = tipoArquivoService.loadTipoArquivo(id);
        return ResponseEntity.ok(TipoArquivoDTO.create(tipoArquivo));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('LISTAR_TIPOARQUIVO')")
    public ResponseEntity<List<TipoArquivoDTO>> listTiposArquivo() {
        List<TipoArquivoDTO> tipos = tipoArquivoService.listTiposArquivo()
                .stream()
                .map(TipoArquivoDTO::create)
                .toList();
        return ResponseEntity.ok(tipos);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EDITAR_TIPOARQUIVO')")
    public ResponseEntity<TipoArquivoDTO> updateTipoArquivo(@PathVariable String id,
                                                            @Valid @RequestBody SaveTipoArquivoDTO saveTipoArquivoDTO) {
        TipoArquivo tipoArquivo = tipoArquivoService.updateTipoArquivo(id, saveTipoArquivoDTO);
        return ResponseEntity.ok(TipoArquivoDTO.create(tipoArquivo));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETAR_TIPOARQUIVO')")
    public ResponseEntity<Void> deleteTipoArquivo(@PathVariable String id) {
        tipoArquivoService.deleteTipoArquivo(id);
        return ResponseEntity.noContent().build();
    }
}