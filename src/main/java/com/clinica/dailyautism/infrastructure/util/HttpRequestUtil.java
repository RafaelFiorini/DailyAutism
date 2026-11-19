package com.clinica.dailyautism.infrastructure.util;

import jakarta.servlet.http.HttpServletRequest;

public class HttpRequestUtil {

    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    public static String extrairIpOrigem(HttpServletRequest request) {
        if (request == null) {
            return "UNKNOWN";
        }

        for (String header : IP_HEADERS) {
            String value = request.getHeader(header);
            if (value != null && !value.isBlank() && !"unknown".equalsIgnoreCase(value.trim())) {
                // X-Forwarded-For pode conter múltiplos IPs (cliente, proxy1, proxy2...). Pega o primeiro.
                if (value.contains(",")) {
                    value = value.split(",")[0].trim();
                }
                if ("0:0:0:0:0:0:0:1".equals(value)) {
                    return "127.0.0.1";
                }
                return value;
            }
        }

        String remoteAddr = request.getRemoteAddr();
        if ("0:0:0:0:0:0:0:1".equals(remoteAddr)) {
            return "127.0.0.1";
        }
        return remoteAddr != null ? remoteAddr : "UNKNOWN";
    }

    public static String extrairUserAgent(HttpServletRequest request) {
        if (request == null) {
            return "UNKNOWN";
        }
        String ua = request.getHeader("User-Agent");
        if (ua == null || ua.isBlank()) {
            return "UNKNOWN";
        }
        // Trunca para até 490 caracteres para caber na coluna de 500 caracteres
        if (ua.length() > 490) {
            return ua.substring(0, 490);
        }
        return ua;
    }
}
