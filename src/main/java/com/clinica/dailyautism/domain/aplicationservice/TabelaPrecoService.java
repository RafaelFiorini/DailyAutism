package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.*;
import com.clinica.dailyautism.domain.exception.PlanoSaudeNotFoundException;
import com.clinica.dailyautism.domain.exception.TipoCompromissoNotFoundException;
import com.clinica.dailyautism.domain.repository.*;
import com.clinica.dailyautism.infrastructure.dto.SaveTabelaPrecoDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TabelaPrecoService {

    private final TabelaPrecoClinicaRepository tabelaPrecoRepository;
    private final TipoCompromissoRepository tipoCompromissoRepository;
    private final PlanoSaudeRepository planoSaudeRepository;
    private final ClinicaAccessHelper clinicaAccessHelper;

    @Transactional
    public TabelaPrecoClinica salvarOuAtualizar(SaveTabelaPrecoDTO dto, String emailUsuario) {
        Clinica clinica = clinicaAccessHelper.validarEAcessarClinica(dto.getIdClinica(), emailUsuario);
        TipoCompromisso tipo = tipoCompromissoRepository.findById(dto.getIdTipoCompromisso().trim())
                .orElseThrow(() -> new TipoCompromissoNotFoundException(
                        "Especialidade não encontrada: " + dto.getIdTipoCompromisso()));

        final PlanoSaude planoAlvo = (dto.getIdPlanoSaude() != null && !dto.getIdPlanoSaude().isBlank())
                ? planoSaudeRepository.findById(dto.getIdPlanoSaude().trim())
                        .orElseThrow(() -> new PlanoSaudeNotFoundException(dto.getIdPlanoSaude()))
                : null;

        Optional<TabelaPrecoClinica> existente = (planoAlvo == null)
                ? tabelaPrecoRepository.findByClinicaIdClinicaAndTipoCompromissoIdTipoCompromissoAndPlanoSaudeIsNull(
                        clinica.getIdClinica(), tipo.getIdTipoCompromisso())
                : tabelaPrecoRepository.findByClinicaIdClinicaAndTipoCompromissoIdTipoCompromissoAndPlanoSaudeIdPlanoSaude(
                        clinica.getIdClinica(), tipo.getIdTipoCompromisso(), planoAlvo.getIdPlanoSaude());

        TabelaPrecoClinica registro = existente.orElseGet(() -> TabelaPrecoClinica.builder()
                .clinica(clinica)
                .tipoCompromisso(tipo)
                .planoSaude(planoAlvo)
                .build());

        registro.setValorSessao(dto.getValorSessao());
        return tabelaPrecoRepository.save(registro);
    }

    public List<TabelaPrecoClinica> listarPorClinica(String idClinica, String emailUsuario) {
        if (idClinica != null && !idClinica.isBlank()) {
            Clinica clinica = clinicaAccessHelper.validarEAcessarClinica(idClinica, emailUsuario);
            return tabelaPrecoRepository.findByClinicaIdClinica(clinica.getIdClinica());
        }
        return clinicaAccessHelper.listarClinicasAcessiveis(emailUsuario).stream()
                .flatMap(c -> tabelaPrecoRepository.findByClinicaIdClinica(c.getIdClinica()).stream())
                .toList();
    }

    @Transactional
    public void excluir(String idTabelaPreco, String emailUsuario) {
        TabelaPrecoClinica preco = tabelaPrecoRepository.findById(idTabelaPreco)
                .orElseThrow(() -> new IllegalArgumentException("Preço não encontrado: " + idTabelaPreco));
        clinicaAccessHelper.validarEAcessarClinica(
                preco.getClinica() != null ? preco.getClinica().getIdClinica() : null,
                emailUsuario
        );
        preco.desativar();
        tabelaPrecoRepository.save(preco);
    }

    public BigDecimal resolverValorParticular(String idClinica, String idTipoCompromisso) {
        if (idClinica == null || idTipoCompromisso == null) {
            return BigDecimal.ZERO;
        }
        return tabelaPrecoRepository
                .findByClinicaIdClinicaAndTipoCompromissoIdTipoCompromissoAndPlanoSaudeIsNull(idClinica, idTipoCompromisso)
                .map(TabelaPrecoClinica::getValorSessao)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal resolverValorConvenio(String idClinica, String idTipoCompromisso, String idPlanoSaude) {
        if (idClinica == null || idTipoCompromisso == null || idPlanoSaude == null) {
            return BigDecimal.ZERO;
        }
        return tabelaPrecoRepository
                .findByClinicaIdClinicaAndTipoCompromissoIdTipoCompromissoAndPlanoSaudeIdPlanoSaude(
                        idClinica, idTipoCompromisso, idPlanoSaude)
                .map(TabelaPrecoClinica::getValorSessao)
                .orElse(BigDecimal.ZERO);
    }
}
