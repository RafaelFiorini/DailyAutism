package com.clinica.dailyautism.infrastructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.AuditoriaService;
import com.clinica.dailyautism.domain.aplicationservice.AuthService;
import com.clinica.dailyautism.infrastructure.dto.*;
import com.clinica.dailyautism.infrastructure.util.HttpRequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthRestResource {

    private final AuthService authService;
    private final AuditoriaService auditoriaService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        String ip = HttpRequestUtil.extrairIpOrigem(request);
        String ua = HttpRequestUtil.extrairUserAgent(request);
        try {
            String token = authService.login(loginDTO);
            auditoriaService.registrarAutenticacaoAsync(
                    loginDTO.getEmailUser(),
                    null,
                    true,
                    null,
                    ip,
                    ua,
                    null
            );
            return ResponseEntity.ok(new AuthResponseDTO(token));
        } catch (Exception e) {
            auditoriaService.registrarAutenticacaoAsync(
                    loginDTO.getEmailUser(),
                    null,
                    false,
                    e.getMessage(),
                    ip,
                    ua,
                    null
            );
            throw e;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        String token = authService.register(registerDTO);
        return ResponseEntity.ok(new AuthResponseDTO(token));
    }

    @PostMapping("/consultar-cadastro")
    public ResponseEntity<DadosUsuarioRecuperacaoDTO> consultarCadastro(@Valid @RequestBody ConsultaCadastroDTO dto) {
        DadosUsuarioRecuperacaoDTO dados = authService.buscarDadosParaAtualizacao(dto.getEmail(), dto.getCpf());
        return ResponseEntity.ok(dados);
    }

    @PostMapping("/atualizar-cadastro")
    public ResponseEntity<Void> atualizarCadastro(@Valid @RequestBody AtualizarCadastroDTO dto) {
        authService.atualizarCadastro(dto);
        return ResponseEntity.ok().build();
    }
}
