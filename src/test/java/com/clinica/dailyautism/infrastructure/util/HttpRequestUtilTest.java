package com.clinica.dailyautism.infrastructure.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HttpRequestUtilTest {

    @Mock
    private HttpServletRequest request;

    @Test
    @DisplayName("Deve extrair o primeiro IP de múltiplos proxies no header X-Forwarded-For")
    void deveExtrairPrimeiroIpDoXForwardedFor() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("200.150.10.5, 10.0.0.1, 192.168.1.1");

        String ip = HttpRequestUtil.extrairIpOrigem(request);
        assertEquals("200.150.10.5", ip);
    }

    @Test
    @DisplayName("Deve converter loopback IPv6 para 127.0.0.1")
    void deveConverterIpv6LocalParaIpv4() {
        when(request.getHeader(anyString())).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("0:0:0:0:0:0:0:1");

        String ip = HttpRequestUtil.extrairIpOrigem(request);
        assertEquals("127.0.0.1", ip);
    }

    @Test
    @DisplayName("Deve truncar User-Agent que exceda o limite de caracteres com segurança")
    void deveTruncarUserAgentLongo() {
        String uaGigante = "Mozilla/5.0 ".repeat(60); // Mais de 700 caracteres
        when(request.getHeader("User-Agent")).thenReturn(uaGigante);

        String ua = HttpRequestUtil.extrairUserAgent(request);
        assertTrue(ua.length() <= 490);
        assertTrue(ua.startsWith("Mozilla/5.0"));
    }
}
