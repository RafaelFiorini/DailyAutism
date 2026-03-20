package com.clinica.dailyautism.infraestructure.controller;


import com.clinica.dailyautism.domain.aplicationservice.PessoaService;
import com.clinica.dailyautism.domain.entity.Pessoa;
import com.clinica.dailyautism.infraestructure.dto.PessoaDTO;
import com.clinica.dailyautism.infraestructure.dto.SavePessoaDTO;

import com.clinica.dailyautism.infraestructure.dto.UpdatePessoaDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/pessoas")
public class PessoaRestResource {

    private final PessoaService pessoaService;

    @PostMapping
    public ResponseEntity<PessoaDTO> createPessoa(@Valid @RequestBody SavePessoaDTO savePessoaDTO) {
        Pessoa pessoa = pessoaService.createPessoa(savePessoaDTO);
        return ResponseEntity.created(URI.create("/pessoas/" + pessoa.getIdPessoa()))
                .body(PessoaDTO.create(pessoa));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PessoaDTO> loadPessoa(@PathVariable String id) {
        Pessoa pessoa = pessoaService.loadPessoa(id);
        return ResponseEntity.ok(PessoaDTO.create(pessoa));
    }

    @GetMapping
    public ResponseEntity<List<PessoaDTO>> listPessoas() {
        List<PessoaDTO> pessoas = pessoaService.listPessoas()
                .stream()
                .map(PessoaDTO::create)
                .toList();
        return ResponseEntity.ok(pessoas);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PessoaDTO> updatePessoa(@PathVariable String id,
                                                  @Valid @RequestBody UpdatePessoaDTO updatePessoaDTO) {
        Pessoa pessoa = pessoaService.updatePessoa(id, updatePessoaDTO);
        return ResponseEntity.ok(PessoaDTO.create(pessoa));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePessoa(@PathVariable String id) {
        pessoaService.deletePessoa(id);
        return ResponseEntity.noContent().build();
    }
}