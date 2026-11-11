package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.GuiaFaturamentoConvenio;
import com.clinica.dailyautism.domain.entity.ProvedorAssinatura;
import com.clinica.dailyautism.domain.entity.security.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InternoAssinaturaProvider implements AssinaturaEletronicaProvider {

    private final PasswordEncoder passwordEncoder;

    @Override
    public ProvedorAssinatura getTipo() {
        return ProvedorAssinatura.INTERNO_DAILYAUTISM;
    }

    @Override
    public ResultadoAssinatura assinar(
            GuiaFaturamentoConvenio guia,
            User assinante,
            String senhaConfirmacao,
            String ipOrigem
    ) {
        if (senhaConfirmacao == null || senhaConfirmacao.isBlank()) {
            throw new IllegalArgumentException("Informe sua senha para confirmar a assinatura eletrônica.");
        }
        if (!passwordEncoder.matches(senhaConfirmacao, assinante.getPasswordUser())) {
            throw new IllegalArgumentException("Senha incorreta para confirmar a assinatura.");
        }

        String nome = (assinante.getPessoa() != null && assinante.getPessoa().getNomePessoa() != null)
                ? assinante.getPessoa().getNomePessoa()
                : assinante.getNomeUser();
        String cpf = (assinante.getPessoa() != null && assinante.getPessoa().getCPFPessoa() != null)
                ? assinante.getPessoa().getCPFPessoa()
                : "-";
        String codigo = "DA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return new ResultadoAssinatura(
                ProvedorAssinatura.INTERNO_DAILYAUTISM,
                assinante.getIdUser(),
                nome,
                cpf,
                LocalDateTime.now(),
                ipOrigem != null ? ipOrigem : "N/A",
                codigo
        );
    }
}
