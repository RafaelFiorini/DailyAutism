package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.*;
import com.clinica.dailyautism.domain.exception.TipoCompromissoNotFoundException;
import com.clinica.dailyautism.domain.repository.*;
import com.clinica.dailyautism.infrastructure.dto.SavePacoteParticularDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PacoteParticularService {

    private final PacoteParticularRepository pacoteRepository;
    private final TipoCompromissoRepository tipoCompromissoRepository;
    private final TabelaPrecoService tabelaPrecoService;
    private final ClinicaAccessHelper clinicaAccessHelper;

    @Transactional
    public PacoteParticular salvar(String idPacote, SavePacoteParticularDTO dto, String emailUsuario) {
        Clinica clinica = clinicaAccessHelper.validarEAcessarClinica(dto.getIdClinica(), emailUsuario);

        PacoteParticular pacote;
        if (idPacote != null && !idPacote.isBlank()) {
            pacote = pacoteRepository.findById(idPacote.trim())
                    .orElseThrow(() -> new IllegalArgumentException("Pacote não encontrado: " + idPacote));
            clinicaAccessHelper.validarEAcessarClinica(
                    pacote.getClinica() != null ? pacote.getClinica().getIdClinica() : null,
                    emailUsuario
            );
            pacote.getItens().clear();
        } else {
            pacote = PacoteParticular.builder()
                    .clinica(clinica)
                    .itens(new ArrayList<>())
                    .build();
        }

        pacote.setClinica(clinica);
        pacote.setNomePacote(dto.getNomePacote().trim());
        pacote.setDescricao(dto.getDescricao() != null ? dto.getDescricao().trim() : null);
        pacote.setValorTotalPacote(dto.getValorTotalPacote());
        pacote.setValidadeMesesSugerida(dto.getValidadeMesesSugerida());

        BigDecimal valorSemDesconto = BigDecimal.ZERO;
        for (SavePacoteParticularDTO.SavePacoteItemDTO itemDto : dto.getItens()) {
            TipoCompromisso tipo = tipoCompromissoRepository.findById(itemDto.getIdTipoCompromisso().trim())
                    .orElseThrow(() -> new TipoCompromissoNotFoundException(
                            "Especialidade não encontrada: " + itemDto.getIdTipoCompromisso()));

            BigDecimal unitario = tabelaPrecoService.resolverValorParticular(
                    clinica.getIdClinica(), tipo.getIdTipoCompromisso());
            valorSemDesconto = valorSemDesconto.add(
                    unitario.multiply(BigDecimal.valueOf(itemDto.getQuantidadeSessoes()))
            );

            PacoteParticularItem item = PacoteParticularItem.builder()
                    .pacote(pacote)
                    .tipoCompromisso(tipo)
                    .quantidadeSessoes(itemDto.getQuantidadeSessoes())
                    .build();
            pacote.getItens().add(item);
        }

        pacote.setValorSemDescontoCalculado(valorSemDesconto);
        return pacoteRepository.save(pacote);
    }

    public List<PacoteParticular> listarPorClinica(String idClinica, String emailUsuario) {
        if (idClinica != null && !idClinica.isBlank()) {
            Clinica clinica = clinicaAccessHelper.validarEAcessarClinica(idClinica, emailUsuario);
            return pacoteRepository.findByClinicaIdClinicaOrderByNomePacoteAsc(clinica.getIdClinica());
        }
        return clinicaAccessHelper.listarClinicasAcessiveis(emailUsuario).stream()
                .flatMap(c -> pacoteRepository.findByClinicaIdClinicaOrderByNomePacoteAsc(c.getIdClinica()).stream())
                .toList();
    }

    @Transactional
    public void excluir(String idPacote, String emailUsuario) {
        PacoteParticular pacote = pacoteRepository.findById(idPacote)
                .orElseThrow(() -> new IllegalArgumentException("Pacote não encontrado: " + idPacote));
        clinicaAccessHelper.validarEAcessarClinica(
                pacote.getClinica() != null ? pacote.getClinica().getIdClinica() : null,
                emailUsuario
        );
        pacote.desativar();
        pacoteRepository.save(pacote);
    }
}
