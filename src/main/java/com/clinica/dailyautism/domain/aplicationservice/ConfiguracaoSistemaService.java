package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.ConfiguracaoSistema;
import com.clinica.dailyautism.domain.repository.ConfiguracaoSistemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfiguracaoSistemaService {

    public static final String CHAVE_TRAVA_BAIXA_FUTURA = "TRAVA_BAIXA_SESSOES_FUTURAS";

    private final ConfiguracaoSistemaRepository repository;

    /**
     * Retorna se a trava para impedir baixa de sessões futuras está ativa.
     * Padrão de segurança: ativa (true) se não configurado.
     */
    public boolean isTravaBaixaSessoesFuturasAtiva() {
        return repository.findById(CHAVE_TRAVA_BAIXA_FUTURA)
                .map(cfg -> !"false".equalsIgnoreCase(cfg.getValor().trim()))
                .orElse(true);
    }

    @Transactional
    public boolean setTravaBaixaSessoesFuturas(boolean ativa) {
        ConfiguracaoSistema cfg = repository.findById(CHAVE_TRAVA_BAIXA_FUTURA)
                .orElseGet(() -> ConfiguracaoSistema.builder()
                        .chave(CHAVE_TRAVA_BAIXA_FUTURA)
                        .descricao("Impede marcar sessões futuras como Realizado ou Falta antes da data do atendimento")
                        .build());

        cfg.setValor(String.valueOf(ativa));
        repository.save(cfg);
        return ativa;
    }
}
