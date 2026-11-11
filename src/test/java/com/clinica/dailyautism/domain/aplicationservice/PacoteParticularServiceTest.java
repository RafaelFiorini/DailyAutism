package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.Clinica;
import com.clinica.dailyautism.domain.entity.PacoteParticular;
import com.clinica.dailyautism.domain.entity.PacoteParticularItem;
import com.clinica.dailyautism.domain.entity.TipoCompromisso;
import com.clinica.dailyautism.domain.repository.PacoteParticularRepository;
import com.clinica.dailyautism.domain.repository.TipoCompromissoRepository;
import com.clinica.dailyautism.infrastructure.dto.PacoteParticularDTO;
import com.clinica.dailyautism.infrastructure.dto.SavePacoteParticularDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PacoteParticularServiceTest {

    @Mock
    private PacoteParticularRepository pacoteRepository;
    @Mock
    private TipoCompromissoRepository tipoCompromissoRepository;
    @Mock
    private TabelaPrecoService tabelaPrecoService;
    @Mock
    private ClinicaAccessHelper clinicaAccessHelper;

    @InjectMocks
    private PacoteParticularService pacoteParticularService;

    @Test
    void deveInativarItensAntigosSemRemoverFisicamenteAoEditarPacote() {
        Clinica clinica = Clinica.builder().idClinica("clinica-1").nomeClinica("Clínica Teste").build();
        TipoCompromisso tipoAntigo = TipoCompromisso.builder().idTipoCompromisso("tipo-to").nomeTipo("Terapia Ocupacional").build();
        TipoCompromisso tipoNovo = TipoCompromisso.builder().idTipoCompromisso("tipo-psico").nomeTipo("Psicologia").build();

        PacoteParticularItem itemAntigo = PacoteParticularItem.builder()
                .idPacoteItem("item-antigo-1")
                .tipoCompromisso(tipoAntigo)
                .quantidadeSessoes(4)
                .build();

        PacoteParticular pacoteExistente = PacoteParticular.builder()
                .idPacote("pacote-1")
                .clinica(clinica)
                .nomePacote("Pacote Original")
                .valorTotalPacote(new BigDecimal("400.00"))
                .itens(new ArrayList<>(List.of(itemAntigo)))
                .build();
        itemAntigo.setPacote(pacoteExistente);

        when(clinicaAccessHelper.validarEAcessarClinica("clinica-1", "admin@clinica.com")).thenReturn(clinica);
        when(pacoteRepository.findById("pacote-1")).thenReturn(Optional.of(pacoteExistente));
        when(tipoCompromissoRepository.findById("tipo-psico")).thenReturn(Optional.of(tipoNovo));
        when(tabelaPrecoService.resolverValorParticular("clinica-1", "tipo-psico")).thenReturn(new BigDecimal("100.00"));
        when(pacoteRepository.save(any(PacoteParticular.class))).thenAnswer(inv -> inv.getArgument(0));

        SavePacoteParticularDTO dto = new SavePacoteParticularDTO(
                "clinica-1",
                "Pacote Atualizado",
                "Descrição atualizada",
                new BigDecimal("550.00"),
                2,
                List.of(new SavePacoteParticularDTO.SavePacoteItemDTO("tipo-psico", 6))
        );

        PacoteParticular salvo = pacoteParticularService.salvar("pacote-1", dto, "admin@clinica.com");

        assertEquals(2, salvo.getItens().size(), "Item antigo deve permanecer preservado na coleção com ativo=false");
        assertFalse(itemAntigo.getAtivo(), "Item antigo deve ser marcado como ativo=false");
        assertTrue(salvo.getItens().get(1).getAtivo(), "Novo item deve estar ativo=true");

        PacoteParticularDTO responseDto = PacoteParticularDTO.create(salvo);
        assertEquals(1, responseDto.getItens().size(), "DTO deve expor apenas itens ativos");
        assertEquals("tipo-psico", responseDto.getItens().get(0).getIdTipoCompromisso());
        assertEquals(6, responseDto.getTotalSessoes());
    }

    @Test
    void deveInativarPacoteEItensAoExcluir() {
        Clinica clinica = Clinica.builder().idClinica("clinica-1").nomeClinica("Clínica Teste").build();
        PacoteParticularItem item = PacoteParticularItem.builder().idPacoteItem("item-1").quantidadeSessoes(8).build();
        PacoteParticular pacote = PacoteParticular.builder()
                .idPacote("pacote-1")
                .clinica(clinica)
                .nomePacote("Pacote Mensal")
                .valorTotalPacote(new BigDecimal("700.00"))
                .itens(new ArrayList<>(List.of(item)))
                .build();

        when(pacoteRepository.findById("pacote-1")).thenReturn(Optional.of(pacote));
        when(clinicaAccessHelper.validarEAcessarClinica("clinica-1", "admin@clinica.com")).thenReturn(clinica);

        pacoteParticularService.excluir("pacote-1", "admin@clinica.com");

        assertFalse(pacote.getAtivo());
        assertFalse(item.getAtivo());
    }
}
