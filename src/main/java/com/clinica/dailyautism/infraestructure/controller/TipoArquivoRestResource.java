package com.clinica.dailyautism.infraestructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.TipoArquivoService;
import com.clinica.dailyautism.domain.entity.TipoArquivo;
import com.clinica.dailyautism.infraestructure.dto.SaveTipoArquivoDTO;
import com.clinica.dailyautism.infraestructure.dto.TipoArquivoDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/tipos-arquivo")
public class TipoArquivoRestResource {

    private final TipoArquivoService tipoArquivoService;

    @PostMapping
    public ResponseEntity<TipoArquivoDTO> createTipoArquivo(@Valid @RequestBody SaveTipoArquivoDTO saveTipoArquivoDTO) {
        TipoArquivo tipoArquivo = tipoArquivoService.createTipoArquivo(saveTipoArquivoDTO);
        return ResponseEntity.created(URI.create("/tipos-arquivo/" + tipoArquivo.getIdTipoArquivo()))
                .body(TipoArquivoDTO.create(tipoArquivo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoArquivoDTO> loadTipoArquivo(@PathVariable String id) {
        TipoArquivo tipoArquivo = tipoArquivoService.loadTipoArquivo(id);
        return ResponseEntity.ok(TipoArquivoDTO.create(tipoArquivo));
    }

    @GetMapping
    public ResponseEntity<List<TipoArquivoDTO>> listTiposArquivo() {
        List<TipoArquivoDTO> tipos = tipoArquivoService.listTiposArquivo()
                .stream()
                .map(TipoArquivoDTO::create)
                .toList();
        return ResponseEntity.ok(tipos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoArquivoDTO> updateTipoArquivo(@PathVariable String id,
                                                            @Valid @RequestBody SaveTipoArquivoDTO saveTipoArquivoDTO) {
        TipoArquivo tipoArquivo = tipoArquivoService.updateTipoArquivo(id, saveTipoArquivoDTO);
        return ResponseEntity.ok(TipoArquivoDTO.create(tipoArquivo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTipoArquivo(@PathVariable String id) {
        tipoArquivoService.deleteTipoArquivo(id);
        return ResponseEntity.noContent().build();
    }
}