package com.clinica.dailyautism.infrastructure.security;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utilitário centralizado para geração de senhas temporárias fortes
 * e validação de complexidade mínima (Fase 1 de Segurança).
 */
public final class PasswordSecurityUtil {

    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijkmnopqrstuvwxyz";
    private static final String DIGITS = "23456789";
    private static final String SYMBOLS = "@#$%&*!?";
    private static final String ALL = UPPER + LOWER + DIGITS + SYMBOLS;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordSecurityUtil() {}

    /**
     * Gera uma senha aleatória criptograficamente segura de 12 caracteres
     * contendo pelo menos 1 maiúscula, 1 minúscula, 1 número e 1 símbolo.
     */
    public static String gerarSenhaTemporariaForte() {
        List<Character> chars = new ArrayList<>(12);
        chars.add(UPPER.charAt(RANDOM.nextInt(UPPER.length())));
        chars.add(LOWER.charAt(RANDOM.nextInt(LOWER.length())));
        chars.add(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        chars.add(SYMBOLS.charAt(RANDOM.nextInt(SYMBOLS.length())));

        for (int i = 4; i < 12; i++) {
            chars.add(ALL.charAt(RANDOM.nextInt(ALL.length())));
        }

        Collections.shuffle(chars, RANDOM);
        StringBuilder sb = new StringBuilder(12);
        for (char c : chars) {
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Valida política mínima de força de senha:
     * - Mínimo 8 caracteres
     * - Pelo menos 1 letra maiúscula, 1 minúscula, 1 número e 1 caractere especial
     */
    public static void validarForcaSenha(String senha) {
        if (senha == null || senha.length() < 8) {
            throw new IllegalArgumentException("A nova senha deve ter pelo menos 8 caracteres.");
        }
        boolean temMaiuscula = senha.chars().anyMatch(Character::isUpperCase);
        boolean temMinuscula = senha.chars().anyMatch(Character::isLowerCase);
        boolean temDigito = senha.chars().anyMatch(Character::isDigit);
        boolean temSimbolo = senha.chars().anyMatch(c -> !Character.isLetterOrDigit(c));

        if (!temMaiuscula || !temMinuscula || !temDigito || !temSimbolo) {
            throw new IllegalArgumentException(
                    "A nova senha deve conter pelo menos uma letra maiúscula, uma minúscula, um número e um símbolo (ex: @, #, !, $)."
            );
        }
    }
}
