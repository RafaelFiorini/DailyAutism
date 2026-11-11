package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.*;
import com.clinica.dailyautism.domain.repository.*;
import com.clinica.dailyautism.infrastructure.dto.SavePacoteParticularDTO;
import com.clinica.dailyautism.infrastructure.dto.SaveTabelaPrecoDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TabelaPrecoServiceTest {

    @Mock
    private TabelaPrecoClinicaRepository tabelaPrecoRepository;
    @Mock
    private TipoCompromissoRepository tipoCompromissoRepository;
    @Mock
    private PlanoSaudeRepository planoSaudeRepository;
    @Mock
    private ClinicaAccessHelper clinicaAccessHelper;

    @InjectMocks
    private TabelaPrecoService tabelaPrecoService;

    @Test
    void deveAtualizarPrecoParticularExistenteSemDuplicar() {
        Clinica clinica = Clinica.builder().idClinica("cli-1").nomeClinica("Clinica A").build();
        TipoCompromisso tipo = TipoCompromisso.builder().idTipoCompromisso("tp-1").nomeTipo("Terapia Ocupacional").build();
        TabelaPrecoClinica existente = TabelaPrecoClinica.builder()
                .idTabelaPreco("prec-1")
                .clinica(clinica)
                .tipoCompromisso(tipo)
                .planoSaude(null)
                .valorSessao(new BigDecimal("100.00"))
                .build();

        when(clinicaAccessHelper.validarEAcessarClinica("cli-1", "admin@clinica.com")).thenReturn(clinica);
        when(tipoCompromissoRepository.findById("tp-1")).thenReturn(Optional.of(tipo));
        when(tabelaPrecoRepository
                .findByClinicaIdClinicaAndTipoCompromissoIdTipoCompromissoAndPlanoSaudeIsNull("cli-1", "tp-1"))
                .thenReturn(Optional.of(existente));
        when(tabelaPrecoRepository.save(any(TabelaPrecoClinica.class))).thenAnswer(inv -> inv.getArgument(0));

        SaveTabelaPrecoDTO dto = new SaveTabelaPrecoDTO("cli-1", "tp-1", null, new BigDecimal("120.00"));
        TabelaPrecoClinica salvo = tabelaPrecoService.salvarOuAtualizar(dto, "admin@clinica.com");

        assertEquals("prec-1", salvo.getIdTabelaPreco());
        assertNull(salvo.getPlanoSaude());
        assertEquals(new BigDecimal("120.00"), salvo.getValorSessao());
    }

    @Test
    void deveSalvarValorDiferenciadoParaConvenio() {
        Clinica clinica = Clinica.builder().idClinica("cli-1").nomeClinica("Clinica A").build();
        TipoCompromisso tipo = TipoCompromisso.builder().idTipoCompromisso("tp-1").nomeTipo("Fonoaudiologia").build();
        PlanoSaude unimed = PlanoSaude.builder().idPlanoSaude("pl-1").nomePlano("Unimed").build();

        when(clinicaAccessHelper.validarEAcessarClinica("cli-1", "admin@clinica.com")).thenReturn(clinica);
        when(tipoCompromissoRepository.findById("tp-1")).thenReturn(Optional.of(tipo));
        when(planoSaudeRepository.findById("pl-1")).thenReturn(Optional.of(unimed));
        when(tabelaPrecoRepository
                .findByClinicaIdClinicaAndTipoCompromissoIdTipoCompromissoAndPlanoSaudeIdPlanoSaude("cli-1", "tp-1", "pl-1"))
                .thenReturn(Optional.empty());
        when(tabelaPrecoRepository.save(any(TabelaPrecoClinica.class))).thenAnswer(inv -> inv.getArgument(0));

        SaveTabelaPrecoDTO dto = new SaveTabelaPrecoDTO("cli-1", "tp-1", "pl-1", new BigDecimal("70.00"));
        TabelaPrecoClinica salvo = tabelaPrecoService.salvarOuAtualizar(dto, "admin@clinica.com");

        assertNotNull(salvo.getPlanoSaude());
        assertEquals("Unimed", salvo.getPlanoSaude().getNomePlano());
        assertEquals(new BigDecimal("70.00"), salvo.getValorSessao());
    }

    @Test
    void deveNegarAcessoQuandoUsuarioNaoPertenceAClinica() {
        when(clinicaAccessHelper.validarEAcessarClinica("cli-2", "staff@cli1.com"))
                .thenThrow(new AccessDeniedException("Usuário sem permissão"));

        SaveTabelaPrecoDTO dto = new SaveTabelaPrecoDTO("cli-2", "tp-1", null, new BigDecimal("100.00"));
        assertThrows(AccessDeniedException.class,
                () -> tabelaPrecoService.salvarOuAtualizar(dto, "staff@cli1.com"));
        verify(tabelaPrecoRepository, never()).save(any());
    }
}
