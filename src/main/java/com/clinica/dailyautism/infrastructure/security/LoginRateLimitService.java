package com.clinica.dailyautism.infrastructure.security;

import com.clinica.dailyautism.domain.exception.TooManyRequestsException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proteção contra força bruta e credential stuffing nas rotas de autenticação.
 * - Máximo de 5 falhas consecutivas por e-mail → bloqueio de 15 minutos.
 * - Máximo de 20 tentativas falhas por IP em 15 minutos → bloqueio temporário.
 */
@Service
public class LoginRateLimitService {

    private static final int MAX_EMAIL_FAILURES = 5;
    private static final int MAX_IP_FAILURES = 20;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private static final class AttemptState {
        int failures;
        Instant windowStart;
        Instant blockedUntil;

        AttemptState(Instant now) {
            this.failures = 0;
            this.windowStart = now;
            this.blockedUntil = null;
        }
    }

    private final Map<String, AttemptState> emailAttempts = new ConcurrentHashMap<>();
    private final Map<String, AttemptState> ipAttempts = new ConcurrentHashMap<>();

    public void validarBloqueio(String email, String ip) {
        Instant now = Instant.now();

        if (ip != null && !ip.isBlank()) {
            AttemptState ipState = ipAttempts.get(ip.trim());
            if (ipState != null && ipState.blockedUntil != null && now.isBefore(ipState.blockedUntil)) {
                long minutos = Math.max(1, Duration.between(now, ipState.blockedUntil).toMinutes() + 1);
                throw new TooManyRequestsException(
                        "Muitas tentativas de acesso a partir deste endereço. Tente novamente em " + minutos + " minuto(s)."
                );
            }
        }

        if (email != null && !email.isBlank()) {
            String key = email.trim().toLowerCase();
            AttemptState emailState = emailAttempts.get(key);
            if (emailState != null && emailState.blockedUntil != null && now.isBefore(emailState.blockedUntil)) {
                long minutos = Math.max(1, Duration.between(now, emailState.blockedUntil).toMinutes() + 1);
                throw new TooManyRequestsException(
                        "Conta temporariamente bloqueada por excesso de tentativas inválidas. Tente novamente em " + minutos + " minuto(s)."
                );
            }
        }
    }

    public void registrarFalha(String email, String ip) {
        Instant now = Instant.now();

        if (ip != null && !ip.isBlank()) {
            registrarEmMapa(ipAttempts, ip.trim(), MAX_IP_FAILURES, now);
        }
        if (email != null && !email.isBlank()) {
            registrarEmMapa(emailAttempts, email.trim().toLowerCase(), MAX_EMAIL_FAILURES, now);
        }
    }

    public void registrarSucesso(String email) {
        if (email != null && !email.isBlank()) {
            emailAttempts.remove(email.trim().toLowerCase());
        }
    }

    private void registrarEmMapa(Map<String, AttemptState> map, String key, int maxFailures, Instant now) {
        map.compute(key, (k, existing) -> {
            if (existing == null || Duration.between(existing.windowStart, now).compareTo(LOCK_DURATION) > 0) {
                AttemptState fresh = new AttemptState(now);
                fresh.failures = 1;
                return fresh;
            }
            existing.failures++;
            if (existing.failures >= maxFailures) {
                existing.blockedUntil = now.plus(LOCK_DURATION);
            }
            return existing;
        });
    }
}
