package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.repository.UserRepository;
import com.clinica.dailyautism.infrastructure.security.JwtService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Autenticação unificada via Google ID Token (usada tanto pelo Frontend Web GIS
 * quanto pelo App Android DApp via Credential Manager).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Value("${app.security.google.web-client-id:}")
    private String googleWebClientId;

    @Value("${app.security.google.allowed-client-ids:}")
    private String googleAllowedClientIds;

    public record GoogleTokenInfo(String sub, String email, boolean emailVerified, String nome) {}

    public record GoogleLoginResult(String token, String email) {}

    @Transactional
    public GoogleLoginResult autenticarComGoogle(String idTokenString) {
        if (idTokenString == null || idTokenString.isBlank()) {
            throw new IllegalArgumentException("Token do Google inválido ou ausente.");
        }

        GoogleTokenInfo tokenInfo = verificarTokenGoogle(idTokenString);
        if (!tokenInfo.emailVerified() || tokenInfo.email() == null || tokenInfo.email().isBlank()) {
            throw new AccessDeniedException("A conta Google informada não possui e-mail verificado.");
        }

        String emailNormalizado = tokenInfo.email().trim().toLowerCase();

        // 1. Procura primeiro pelo identificador imutável do Google (sub), depois pelo e-mail cadastrado
        Optional<User> userOpt = (tokenInfo.sub() != null && !tokenInfo.sub().isBlank())
                ? userRepository.findByGoogleSub(tokenInfo.sub())
                : Optional.empty();

        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmailUserIgnoreCase(emailNormalizado);
        }

        if (userOpt.isEmpty()) {
            // Verifica se existe conta desativada com este e-mail para dar mensagem precisa
            Optional<User> inativoOpt = userRepository.findByEmailAnyAtivo(emailNormalizado);
            if (inativoOpt.isPresent() && !Boolean.TRUE.equals(inativoOpt.get().getAtivo())) {
                throw new AccessDeniedException(
                        "Esta conta está desativada no DailyAutism. Entre em contato com a clínica para reativá-la."
                );
            }
            throw new AccessDeniedException(
                    "Nenhuma conta encontrada para o e-mail Google (" + emailNormalizado + "). " +
                    "Solicite o cadastro à sua clínica ou utilize o cadastro de primeiro acesso."
            );
        }

        User user = userOpt.get();

        // Vincula automaticamente o googleSub na primeira vez que entra via Google
        if (tokenInfo.sub() != null && !tokenInfo.sub().isBlank() &&
                (user.getGoogleSub() == null || user.getGoogleSub().isBlank())) {
            user.setGoogleSub(tokenInfo.sub());
            userRepository.save(user);
        }

        String jwt = jwtService.gerarToken(user);
        return new GoogleLoginResult(jwt, user.getEmailUser());
    }

    protected GoogleTokenInfo verificarTokenGoogle(String idTokenString) {
        List<String> audiences = listarClientIdsPermitidos();
        if (audiences.isEmpty()) {
            throw new IllegalStateException(
                    "Login com Google ainda não configurado no servidor (defina GOOGLE_OAUTH_WEB_CLIENT_ID)."
            );
        }

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance()
            )
                    .setAudience(audiences)
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new AccessDeniedException("Token do Google inválido, expirado ou não autorizado para este aplicativo.");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String sub = payload.getSubject();
            String email = payload.getEmail();
            boolean emailVerified = Boolean.TRUE.equals(payload.getEmailVerified());
            String nome = (String) payload.get("name");

            return new GoogleTokenInfo(sub, email, emailVerified, nome);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Falha ao verificar Google ID Token: {}", e.getMessage());
            throw new AccessDeniedException("Não foi possível validar o token de autenticação do Google.");
        }
    }

    private List<String> listarClientIdsPermitidos() {
        List<String> ids = new ArrayList<>();
        if (googleWebClientId != null && !googleWebClientId.isBlank()) {
            ids.add(googleWebClientId.trim());
        }
        if (googleAllowedClientIds != null && !googleAllowedClientIds.isBlank()) {
            Arrays.stream(googleAllowedClientIds.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank() && !ids.contains(s))
                    .forEach(ids::add);
        }
        return ids;
    }
}
