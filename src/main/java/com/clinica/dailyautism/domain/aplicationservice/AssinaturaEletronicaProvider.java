package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.GuiaFaturamentoConvenio;
import com.clinica.dailyautism.domain.entity.ProvedorAssinatura;
import com.clinica.dailyautism.domain.entity.security.User;

import java.time.LocalDateTime;

/**
 * Contrato desacoplado de provedor de assinatura eletrônica (Interno hoje, gov.br futuramente).
 */
public interface AssinaturaEletronicaProvider {

    ProvedorAssinatura getTipo();

    ResultadoAssinatura assinar(GuiaFaturamentoConvenio guia, User assinante, String senhaConfirmacao, String ipOrigem);

    record ResultadoAssinatura(
            ProvedorAssinatura provedor,
            String userId,
            String nomeAssinante,
            String cpfAssinante,
            LocalDateTime dataHoraAssinatura,
            String ipAssinatura,
            String codigoVerificador
    ) {}
}
