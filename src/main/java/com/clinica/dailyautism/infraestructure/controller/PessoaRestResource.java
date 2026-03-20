package com.clinica.dailyautism.infraestructure.controller;


import com.clinica.dailyautism.domain.aplicationservice.PessoaService;
import com.clinica.dailyautism.domain.entity.Pessoa;
import com.clinica.dailyautism.infraestructure.dto.PessoaDTO;
import com.clinica.dailyautism.infraestructure.dto.SavePessoaDataDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;


@RequiredArgsConstructor
@RestController
@RequestMapping("/pessoas")
public class PessoaRestResource {

    private final PessoaService pessoaService;

    @PostMapping
    public ResponseEntity<PessoaDTO> createPessoa(@Valid @RequestBody SavePessoaDataDTO savePessoaDTO){
        Pessoa pessoa = pessoaService.createPessoa(savePessoaDTO);
        return ResponseEntity.created(URI.create("/pessoas/" + pessoa.getIdPessoa()))
                .body(PessoaDTO.create(pessoa));
    }


}
