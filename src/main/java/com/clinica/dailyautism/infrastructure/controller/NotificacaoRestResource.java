package com.clinica.dailyautism.infrastructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.NotificacaoService;
import com.clinica.dailyautism.infrastructure.dto.NotificacaoDTO;
import com.clinica.dailyautism.infrastructure.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notificacoes")
@RequiredArgsConstructor
public class NotificacaoRestResource {

    private final NotificacaoService notificacaoService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificacaoDTO>> listar(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(notificacaoService.listarPorUsuario(currentUser.getUsername()));
    }

    @GetMapping("/nao-lidas/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Long>> contarNaoLidas(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        long count = notificacaoService.contarNaoLidas(currentUser.getUsername());
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PatchMapping("/{id}/lida")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificacaoDTO> marcarComoLida(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        return ResponseEntity.ok(notificacaoService.marcarComoLida(id, currentUser.getUsername()));
    }

    @PatchMapping("/marcar-todas-lidas")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Integer>> marcarTodasComoLidas(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        int atualizadas = notificacaoService.marcarTodasComoLidas(currentUser.getUsername());
        return ResponseEntity.ok(Map.of("atualizadas", atualizadas));
    }
}
