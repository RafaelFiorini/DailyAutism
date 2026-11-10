package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.*;
import com.clinica.dailyautism.domain.exception.*;
import com.clinica.dailyautism.domain.repository.*;
import com.clinica.dailyautism.infrastructure.dto.SavePacientePlanoSaudeDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PacientePlanoSaudeService {

    private final PacientePlanoSaudeRepository pacientePlanoSaudeRepository;
    private final PacienteRepository pacienteRepository;
    private final PlanoSaudeRepository planoSaudeRepository;
    private final PeriodicidadeRepository periodicidadeRepository;

    @Transactional
    public PacientePlanoSaude vincular(SavePacientePlanoSaudeDTO dto) {
        Paciente paciente = pacienteRepository.findById(dto.getIdPaciente())
                .orElseThrow(() -> new PacienteNotFoundException(dto.getIdPaciente()));
        PlanoSaude plano = planoSaudeRepository.findById(dto.getIdPlanoSaude())
                .orElseThrow(() -> new PlanoSaudeNotFoundException(dto.getIdPlanoSaude()));

        LocalDate dataInicio = dto.getDataInicioLiberacao() != null ? dto.getDataInicioLiberacao() : LocalDate.now();
        TipoLiberacaoGuia tipo = dto.getTipoLiberacao() != null ? dto.getTipoLiberacao() : TipoLiberacaoGuia.PERIODO;
        LocalDate vencimentoCalculado = resolverVencimentoGuia(
                tipo,
                dataInicio,
                dto.getVencimentoLiberacao(),
                dto.getQuantidadeSessoesLiberadas(),
                dto.getIdPeriodicidadeReferencia()
        );

        PacientePlanoSaude vinculo = PacientePlanoSaude.builder()
                .paciente(paciente)
                .planoSaude(plano)
                .numeroCarteirinha(dto.getNumeroCarteirinha())
                .validadeCarteirinha(dto.getValidadeCarteirinha())
                .numeroGuia(dto.getNumeroGuia())
                .tipoLiberacao(tipo)
                .dataInicioLiberacao(dataInicio)
                .vencimentoLiberacao(vencimentoCalculado)
                .quantidadeSessoesLiberadas(dto.getQuantidadeSessoesLiberadas())
                .build();
        return pacientePlanoSaudeRepository.save(vinculo);
    }

    public List<PacientePlanoSaude> listarPorPaciente(String idPaciente) {
        return pacientePlanoSaudeRepository.findByPacienteIdPaciente(idPaciente);
    }

    public List<PacientePlanoSaude> listarPorPlano(String idPlanoSaude) {
        return pacientePlanoSaudeRepository.findByPlanoSaudeIdPlanoSaude(idPlanoSaude);
    }

    @Transactional
    public PacientePlanoSaude atualizar(String idVinculo, SavePacientePlanoSaudeDTO dto) {
        PacientePlanoSaude vinculo = pacientePlanoSaudeRepository.findById(idVinculo)
                .orElseThrow(() -> new RuntimeException("Vínculo não encontrado: " + idVinculo));

        if (dto.getIdPlanoSaude() != null && !dto.getIdPlanoSaude().isBlank()) {
            PlanoSaude plano = planoSaudeRepository.findById(dto.getIdPlanoSaude().trim())
                    .orElseThrow(() -> new PlanoSaudeNotFoundException(dto.getIdPlanoSaude()));
            vinculo.setPlanoSaude(plano);
        }

        LocalDate dataInicio = dto.getDataInicioLiberacao() != null
                ? dto.getDataInicioLiberacao()
                : (vinculo.getDataInicioLiberacao() != null ? vinculo.getDataInicioLiberacao() : LocalDate.now());
        TipoLiberacaoGuia tipo = dto.getTipoLiberacao() != null
                ? dto.getTipoLiberacao()
                : (vinculo.getTipoLiberacao() != null ? vinculo.getTipoLiberacao() : TipoLiberacaoGuia.PERIODO);

        LocalDate vencimentoCalculado = resolverVencimentoGuia(
                tipo,
                dataInicio,
                dto.getVencimentoLiberacao(),
                dto.getQuantidadeSessoesLiberadas(),
                dto.getIdPeriodicidadeReferencia()
        );

        vinculo.setNumeroCarteirinha(dto.getNumeroCarteirinha());
        vinculo.setValidadeCarteirinha(dto.getValidadeCarteirinha());
        vinculo.setNumeroGuia(dto.getNumeroGuia());
        vinculo.setTipoLiberacao(tipo);
        vinculo.setDataInicioLiberacao(dataInicio);
        vinculo.setVencimentoLiberacao(vencimentoCalculado);
        vinculo.setQuantidadeSessoesLiberadas(dto.getQuantidadeSessoesLiberadas());
        return pacientePlanoSaudeRepository.save(vinculo);
    }

    @Transactional
    public void desvincular(String idVinculo) {
        PacientePlanoSaude vinculo = pacientePlanoSaudeRepository.findById(idVinculo)
                .orElseThrow(() -> new RuntimeException("Vínculo não encontrado: " + idVinculo));
        vinculo.desativar();
        pacientePlanoSaudeRepository.save(vinculo);
    }

    public LocalDate resolverVencimentoGuia(
            TipoLiberacaoGuia tipo,
            LocalDate dataInicio,
            LocalDate vencimentoInformado,
            Integer quantidadeSessoes,
            String idPeriodicidadeRef
    ) {
        if (tipo == TipoLiberacaoGuia.QUANTIDADE && vencimentoInformado == null
                && quantidadeSessoes != null && quantidadeSessoes > 0
                && idPeriodicidadeRef != null && !idPeriodicidadeRef.isBlank()) {
            Periodicidade per = periodicidadeRepository.findById(idPeriodicidadeRef.trim()).orElse(null);
            if (per != null && per.getDiasIntervalo() != null && per.getDiasIntervalo() > 0) {
                long diasTotais = (long) Math.max(0, quantidadeSessoes - 1) * per.getDiasIntervalo();
                return dataInicio.plusDays(diasTotais);
            }
        }
        return vencimentoInformado;
    }
}
