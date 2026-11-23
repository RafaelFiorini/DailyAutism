package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.exception.TooManyRequestsException;
import com.clinica.dailyautism.domain.repository.UserRepository;
import com.clinica.dailyautism.infrastructure.security.JwtService;
import com.clinica.dailyautism.infrastructure.security.LoginRateLimitService;
import com.clinica.dailyautism.infrastructure.security.PasswordSecurityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityPhase1AndGoogleAuthTest {

    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;

    @Test
    @DisplayName("PasswordSecurityUtil gera senha forte de 12 caracteres que passa na validação de complexidade")
    void gerarEValidarSenhaForte() {
        String senha = PasswordSecurityUtil.gerarSenhaTemporariaForte();
        assertNotNull(senha);
        assertEquals(12, senha.length());
        assertDoesNotThrow(() -> PasswordSecurityUtil.validarForcaSenha(senha));

        assertThrows(IllegalArgumentException.class, () -> PasswordSecurityUtil.validarForcaSenha("123456"));
        assertThrows(IllegalArgumentException.class, () -> PasswordSecurityUtil.validarForcaSenha("senhasemnumero!"));
        assertThrows(IllegalArgumentException.class, () -> PasswordSecurityUtil.validarForcaSenha("Senha1234"));
    }

    @Test
    @DisplayName("LoginRateLimitService bloqueia após 5 falhas consecutivas e libera após sucesso")
    void rateLimitBloqueiaELibera() {
        LoginRateLimitService rateLimit = new LoginRateLimitService();
        String email = "usuario@clinica.com";
        String ip = "192.168.1.10";

        for (int i = 0; i < 5; i++) {
            rateLimit.validarBloqueio(email, ip);
            rateLimit.registrarFalha(email, ip);
        }

        assertThrows(TooManyRequestsException.class, () -> rateLimit.validarBloqueio(email, ip));

        rateLimit.registrarSucesso(email);
        assertDoesNotThrow(() -> rateLimit.validarBloqueio(email, ip));
    }

    @Test
    @DisplayName("GoogleAuthService bloqueia e-mail Google que não está cadastrado no DailyAutism")
    void googleLoginBloqueiaEmailNaoCadastrado() {
        GoogleAuthService service = new GoogleAuthService(userRepository, jwtService) {
            @Override
            protected GoogleTokenInfo verificarTokenGoogle(String idTokenString) {
                return new GoogleTokenInfo("sub-novo-123", "desconhecido@gmail.com", true, "Desconhecido");
            }
        };

        when(userRepository.findByGoogleSub("sub-novo-123")).thenReturn(Optional.empty());
        when(userRepository.findByEmailUserIgnoreCase("desconhecido@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmailAnyAtivo("desconhecido@gmail.com")).thenReturn(Optional.empty());

        AccessDeniedException ex = assertThrows(
                AccessDeniedException.class,
                () -> service.autenticarComGoogle("fake-id-token")
        );
        assertTrue(ex.getMessage().contains("Nenhuma conta encontrada"));
        verify(jwtService, never()).gerarToken(any());
    }

    @Test
    @DisplayName("GoogleAuthService vincula googleSub e emite JWT para usuário já cadastrado")
    void googleLoginAutenticaEVinculaUsuarioExistente() {
        GoogleAuthService service = new GoogleAuthService(userRepository, jwtService) {
            @Override
            protected GoogleTokenInfo verificarTokenGoogle(String idTokenString) {
                return new GoogleTokenInfo("sub-alexandra", "alexandra@attenzione.com.br", true, "Alexandra");
            }
        };

        User user = User.builder()
                .idUser("u-1")
                .emailUser("alexandra@attenzione.com.br")
                .nomeUser("Alexandra")
                .build();
        user.setAtivo(true);

        when(userRepository.findByGoogleSub("sub-alexandra")).thenReturn(Optional.empty());
        when(userRepository.findByEmailUserIgnoreCase("alexandra@attenzione.com.br")).thenReturn(Optional.of(user));
        when(jwtService.gerarToken(user)).thenReturn("jwt-valido-123");

        GoogleAuthService.GoogleLoginResult result = service.autenticarComGoogle("fake-id-token");

        assertEquals("jwt-valido-123", result.token());
        assertEquals("alexandra@attenzione.com.br", result.email());
        assertEquals("sub-alexandra", user.getGoogleSub());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("GoogleAuthService bloqueia conta desativada")
    void googleLoginBloqueiaContaInativa() {
        GoogleAuthService service = new GoogleAuthService(userRepository, jwtService) {
            @Override
            protected GoogleTokenInfo verificarTokenGoogle(String idTokenString) {
                return new GoogleTokenInfo("sub-inativo", "inativo@clinica.com", true, "Inativo");
            }
        };

        User inativo = User.builder().idUser("u-2").emailUser("inativo@clinica.com").build();
        inativo.desativar();

        when(userRepository.findByGoogleSub("sub-inativo")).thenReturn(Optional.empty());
        when(userRepository.findByEmailUserIgnoreCase("inativo@clinica.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmailAnyAtivo("inativo@clinica.com")).thenReturn(Optional.of(inativo));

        AccessDeniedException ex = assertThrows(
                AccessDeniedException.class,
                () -> service.autenticarComGoogle("fake-id-token")
        );
        assertTrue(ex.getMessage().contains("desativada"));
    }
}
