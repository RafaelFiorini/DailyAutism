package com.clinica.dailyautism.infrastructure.controller;

import com.clinica.dailyautism.domain.aplicationservice.AuditoriaService;
import com.clinica.dailyautism.domain.aplicationservice.AuthService;
import com.clinica.dailyautism.domain.aplicationservice.GoogleAuthService;
import com.clinica.dailyautism.infrastructure.dto.*;
import com.clinica.dailyautism.infrastructure.security.LoginRateLimitService;
import com.clinica.dailyautism.infrastructure.security.UserDetailsImpl;
import com.clinica.dailyautism.infrastructure.util.HttpRequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthRestResource {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;
    private final LoginRateLimitService loginRateLimitService;
    private final AuditoriaService auditoriaService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        String ip = HttpRequestUtil.extrairIpOrigem(request);
        String ua = HttpRequestUtil.extrairUserAgent(request);

        loginRateLimitService.validarBloqueio(loginDTO.getEmailUser(), ip);

        try {
            String token = authService.login(loginDTO);
            loginRateLimitService.registrarSucesso(loginDTO.getEmailUser());
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
            loginRateLimitService.registrarFalha(loginDTO.getEmailUser(), ip);
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

    @PostMapping("/google")
    public ResponseEntity<AuthResponseDTO> loginGoogle(
            @Valid @RequestBody GoogleLoginDTO googleLoginDTO,
            HttpServletRequest request
    ) {
        String ip = HttpRequestUtil.extrairIpOrigem(request);
        String ua = HttpRequestUtil.extrairUserAgent(request);

        loginRateLimitService.validarBloqueio(null, ip);

        try {
            GoogleAuthService.GoogleLoginResult result = googleAuthService.autenticarComGoogle(googleLoginDTO.getIdToken());
            loginRateLimitService.registrarSucesso(result.email());
            auditoriaService.registrarAutenticacaoAsync(
                    result.email(),
                    null,
                    true,
                    "Login via Google OAuth2",
                    ip,
                    ua,
                    null
            );
            return ResponseEntity.ok(new AuthResponseDTO(result.token()));
        } catch (Exception e) {
            loginRateLimitService.registrarFalha(null, ip);
            auditoriaService.registrarAutenticacaoAsync(
                    "google-oauth",
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

    @PostMapping("/alterar-senha")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> alterarSenha(
            @Valid @RequestBody AlterarSenhaDTO dto,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        String email = currentUser != null ? currentUser.getUsername() : null;
        authService.alterarSenha(email, dto);
        return ResponseEntity.noContent().build();
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
