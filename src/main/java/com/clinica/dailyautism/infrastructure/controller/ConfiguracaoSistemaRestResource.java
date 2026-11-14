package com.clinica.dailyautism.infrastructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.ConfiguracaoSistemaService;
import com.clinica.dailyautism.infrastructure.dto.TravaBaixaFuturaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sistema/configuracoes")
@RequiredArgsConstructor
public class ConfiguracaoSistemaRestResource {

    private final ConfiguracaoSistemaService configuracaoSistemaService;

    @GetMapping("/trava-baixa-futura")
    public ResponseEntity<TravaBaixaFuturaDTO> getTravaBaixaFutura() {
        boolean ativa = configuracaoSistemaService.isTravaBaixaSessoesFuturasAtiva();
        return ResponseEntity.ok(TravaBaixaFuturaDTO.builder()
                .ativa(ativa)
                .mensagem(ativa
                        ? "Trava ATIVA: Bloqueia baixa de sessões antes do dia do atendimento."
                        : "Trava DESATIVADA: Permite baixa de sessões futuras para testes.")
                .build());
    }

    @PatchMapping("/trava-baixa-futura")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<TravaBaixaFuturaDTO> setTravaBaixaFutura(@RequestBody TravaBaixaFuturaDTO dto) {
        boolean novoStatus = configuracaoSistemaService.setTravaBaixaSessoesFuturas(dto.isAtiva());
        return ResponseEntity.ok(TravaBaixaFuturaDTO.builder()
                .ativa(novoStatus)
                .mensagem(novoStatus
                        ? "Trava ATIVADA com sucesso pelo Administrador."
                        : "Trava DESATIVADA com sucesso pelo Administrador (modo de testes ativo).")
                .build());
    }
}
