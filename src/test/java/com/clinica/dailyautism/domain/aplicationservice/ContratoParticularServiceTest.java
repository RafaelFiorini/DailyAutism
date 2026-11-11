package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.*;
import com.clinica.dailyautism.domain.repository.*;
import com.clinica.dailyautism.infrastructure.dto.SaveContratoParticularDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContratoParticularServiceTest {

    @Mock
    private ContratoParticularRepository contratoRepository;
    @Mock
    private MovimentacaoContratoParticularRepository movimentacaoRepository;
    @Mock
    private PacienteRepository pacienteRepository;
    @Mock
    private ResponsavelRepository responsavelRepository;
    @Mock
    private PacoteParticularRepository pacoteRepository;
    @Mock
    private TipoCompromissoRepository tipoCompromissoRepository;
    @Mock
    private TabelaPrecoService tabelaPrecoService;
    @Mock
    private ClinicaAccessHelper clinicaAccessHelper;

    @InjectMocks
    private ContratoParticularService contratoService;

    @Test
    void deveMultiplicarComboMensalPorMesesECongelarValorUnitario() {
        Clinica clinica = Clinica.builder().idClinica("cli-1").nomeClinica("Clinica A").build();
        Paciente paciente = Paciente.builder().idPaciente("pac-1").build();
        TipoCompromisso to = TipoCompromisso.builder().idTipoCompromisso("tp-to").nomeTipo("TO").build();
        TipoCompromisso psico = TipoCompromisso.builder().idTipoCompromisso("tp-psi").nomeTipo("Psicologia").build();

        PacoteParticular pacote = PacoteParticular.builder()
                .idPacote("pkg-1")
                .clinica(clinica)
                .nomePacote("Combo Mensal 4 TO + 2 Psico")
                .valorTotalPacote(new BigDecimal("500.00"))
                .validadeMesesSugerida(1)
                .itens(List.of(
                        PacoteParticularItem.builder().tipoCompromisso(to).quantidadeSessoes(4).build(),
                        PacoteParticularItem.builder().tipoCompromisso(psico).quantidadeSessoes(2).build()
                ))
                .build();

        when(clinicaAccessHelper.validarEAcessarClinica("cli-1", "admin@clinica.com")).thenReturn(clinica);
        when(pacienteRepository.findById("pac-1")).thenReturn(Optional.of(paciente));
        when(pacoteRepository.findById("pkg-1")).thenReturn(Optional.of(pacote));
        when(tabelaPrecoService.resolverValorParticular("cli-1", "tp-to")).thenReturn(new BigDecimal("100.00"));
        when(tabelaPrecoService.resolverValorParticular("cli-1", "tp-psi")).thenReturn(new BigDecimal("120.00"));
        when(contratoRepository.save(any(ContratoParticular.class))).thenAnswer(inv -> inv.getArgument(0));

        SaveContratoParticularDTO dto = new SaveContratoParticularDTO();
        dto.setIdClinica("cli-1");
        dto.setIdPaciente("pac-1");
        dto.setIdPacote("pkg-1");
        dto.setMultiplicadorMeses(2);
        dto.setModalidade(ModalidadeContratoParticular.POR_PERIODO_MENSAL);
        dto.setDataInicio(LocalDate.of(2026, 11, 10));
        dto.setPagoNaContratacao(true);

        ContratoParticular salvo = contratoService.criarContrato(dto, "admin@clinica.com");

        assertEquals(new BigDecimal("1000.00"), salvo.getValorCobrado());
        assertEquals(StatusPagamentoContrato.PAGO, salvo.getStatusPagamento());
        assertEquals(LocalDate.of(2027, 1, 10), salvo.getDataFimPrevista());
        assertEquals(2, salvo.getItens().size());
        assertEquals(8, salvo.getItens().get(0).getQuantidadeContratada());
        assertEquals(4, salvo.getItens().get(1).getQuantidadeContratada());
        assertEquals(new BigDecimal("100.00"), salvo.getItens().get(0).getValorUnitarioReferencia());
    }

    @Test
    void deveAbaterApenasEspecialidadeCorrespondenteESerIdempotente() {
        Clinica clinica = Clinica.builder().idClinica("cli-1").build();
        Paciente paciente = Paciente.builder().idPaciente("pac-1").build();
        TipoCompromisso to = TipoCompromisso.builder().idTipoCompromisso("tp-to").nomeTipo("TO").build();
        TipoCompromisso psico = TipoCompromisso.builder().idTipoCompromisso("tp-psi").nomeTipo("Psicologia").build();

        ContratoParticular contrato = ContratoParticular.builder()
                .idContratoParticular("ctr-1")
                .clinica(clinica)
                .paciente(paciente)
                .statusContrato(StatusContratoParticular.ATIVO)
                .itens(new ArrayList<>())
                .build();
        ContratoParticularItem itemTo = ContratoParticularItem.builder()
                .idContratoItem("it-1")
                .contrato(contrato)
                .tipoCompromisso(to)
                .quantidadeContratada(1)
                .quantidadeConsumida(0)
                .build();
        ContratoParticularItem itemPsi = ContratoParticularItem.builder()
                .idContratoItem("it-2")
                .contrato(contrato)
                .tipoCompromisso(psico)
                .quantidadeContratada(2)
                .quantidadeConsumida(0)
                .build();
        contrato.getItens().add(itemTo);
        contrato.getItens().add(itemPsi);

        Compromisso compTo = Compromisso.builder()
                .idCompromisso("cmp-1")
                .clinica(clinica)
                .paciente(paciente)
                .tipoCompromisso(to)
                .status(StatusCompromisso.REALIZADO)
                .build();

        when(clinicaAccessHelper.resolverClinicaDoCompromisso(compTo)).thenReturn(clinica);
        when(movimentacaoRepository.findByCompromissoIdCompromissoOrderByDataMovimentacaoDesc("cmp-1"))
                .thenReturn(List.of());
        when(contratoRepository.findByPacienteIdPacienteAndClinicaIdClinicaAndStatusContratoOrderByDataInicioAsc(
                "pac-1", "cli-1", StatusContratoParticular.ATIVO))
                .thenReturn(List.of(contrato));

        contratoService.processarAlteracaoStatusCompromisso(compTo, StatusCompromisso.AGENDADO, StatusCompromisso.REALIZADO);

        assertEquals(1, itemTo.getQuantidadeConsumida());
        assertEquals(0, itemPsi.getQuantidadeConsumida());
        assertEquals(StatusContratoParticular.ATIVO, contrato.getStatusContrato());
        verify(movimentacaoRepository, times(1)).save(any(MovimentacaoContratoParticular.class));

        // Simula reprocessamento idempotente com abatimento já existente
        MovimentacaoContratoParticular movExistente = MovimentacaoContratoParticular.builder()
                .tipoMovimentacao(TipoMovimentacaoContrato.ABATIMENTO)
                .quantidade(1)
                .contratoItem(itemTo)
                .build();
        when(movimentacaoRepository.findByCompromissoIdCompromissoOrderByDataMovimentacaoDesc("cmp-1"))
                .thenReturn(List.of(movExistente));

        contratoService.processarAlteracaoStatusCompromisso(compTo, StatusCompromisso.AGENDADO, StatusCompromisso.REALIZADO);
        assertEquals(1, itemTo.getQuantidadeConsumida());
        verify(movimentacaoRepository, times(1)).save(any(MovimentacaoContratoParticular.class));
    }

    @Test
    void deveEstornarEReabrirContratoQuandoRevertidoDeRealizado() {
        Clinica clinica = Clinica.builder().idClinica("cli-1").build();
        Paciente paciente = Paciente.builder().idPaciente("pac-1").build();
        TipoCompromisso to = TipoCompromisso.builder().idTipoCompromisso("tp-to").nomeTipo("TO").build();

        ContratoParticular contrato = ContratoParticular.builder()
                .idContratoParticular("ctr-1")
                .clinica(clinica)
                .paciente(paciente)
                .statusContrato(StatusContratoParticular.CONCLUIDO)
                .itens(new ArrayList<>())
                .build();
        ContratoParticularItem itemTo = ContratoParticularItem.builder()
                .idContratoItem("it-1")
                .contrato(contrato)
                .tipoCompromisso(to)
                .quantidadeContratada(1)
                .quantidadeConsumida(1)
                .build();
        contrato.getItens().add(itemTo);

        Compromisso compTo = Compromisso.builder()
                .idCompromisso("cmp-1")
                .clinica(clinica)
                .paciente(paciente)
                .tipoCompromisso(to)
                .status(StatusCompromisso.CANCELADO)
                .build();

        MovimentacaoContratoParticular movAbat = MovimentacaoContratoParticular.builder()
                .tipoMovimentacao(TipoMovimentacaoContrato.ABATIMENTO)
                .quantidade(1)
                .contratoItem(itemTo)
                .build();

        when(movimentacaoRepository.findByCompromissoIdCompromissoOrderByDataMovimentacaoDesc("cmp-1"))
                .thenReturn(List.of(movAbat));

        contratoService.processarAlteracaoStatusCompromisso(compTo, StatusCompromisso.REALIZADO, StatusCompromisso.CANCELADO);

        assertEquals(0, itemTo.getQuantidadeConsumida());
        assertEquals(StatusContratoParticular.ATIVO, contrato.getStatusContrato());
        verify(movimentacaoRepository).save(argThat(m -> m.getTipoMovimentacao() == TipoMovimentacaoContrato.ESTORNO));
    }
}
