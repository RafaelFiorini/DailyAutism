package com.clinica.dailyautism.infraestructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.ArquivoService;
import com.clinica.dailyautism.domain.entity.Arquivo;
import com.clinica.dailyautism.infraestructure.dto.ArquivoDTO;
import com.clinica.dailyautism.infraestructure.dto.SaveArquivoDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/arquivos")
public class ArquivoRestResource {

    private final ArquivoService arquivoService;

    @PostMapping
    public ResponseEntity<ArquivoDTO> createArquivo(@Valid @RequestBody SaveArquivoDTO saveArquivoDTO) {
        Arquivo arquivo = arquivoService.createArquivo(saveArquivoDTO);
        return ResponseEntity.created(URI.create("/arquivos/" + arquivo.getIdArquivo()))
                .body(ArquivoDTO.create(arquivo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArquivoDTO> loadArquivo(@PathVariable String id) {
        Arquivo arquivo = arquivoService.loadArquivo(id);
        return ResponseEntity.ok(ArquivoDTO.create(arquivo));
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<ArquivoDTO>> listArquivosByPaciente(@PathVariable String pacienteId) {
        List<ArquivoDTO> arquivos = arquivoService.listArquivosByPaciente(pacienteId)
                .stream()
                .map(ArquivoDTO::create)
                .toList();
        return ResponseEntity.ok(arquivos);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<String> downloadArquivo(@PathVariable String id) {
        Arquivo arquivo = arquivoService.loadArquivoComBase64(id);
        return ResponseEntity.ok(arquivo.getArquivoBase64());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArquivo(@PathVariable String id) {
        arquivoService.deleteArquivo(id);
        return ResponseEntity.noContent().build();
    }
}