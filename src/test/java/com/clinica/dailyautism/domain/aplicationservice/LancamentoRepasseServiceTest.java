package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.*;
import com.clinica.dailyautism.domain.repository.LancamentoRepasseRepository;
import com.clinica.dailyautism.domain.repository.ProfissionalClinicaRepository;
import com.clinica.dailyautism.domain.repository.ProfissionalRepository;
import com.clinica.dailyautism.domain.repository.TabelaPrecoClinicaRepository;
import com.clinica.dailyautism.infrastructure.dto.ResumoFinanceiroProfissionalDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LancamentoRepasseServiceTest {

    @Mock private LancamentoRepasseRepository lancamentoRepasseRepository;
    @Mock private ProfissionalRepository profissionalRepository;
    @Mock private ProfissionalClinicaRepository profissionalClinicaRepository;
    @Mock private TabelaPrecoClinicaRepository tabelaPrecoClinicaRepository;

    @InjectMocks
    private LancamentoRepasseService lancamentoRepasseService;

    private Profissional profissional;
    private Clinica clinica;
    private Paciente paciente;

    @BeforeEach
    void setUp() {
        profissional = Profissional.builder()
                .idProf("prof-1")
                .valorSessaoParticular(new BigDecimal("150.00"))
                .build();

        clinica = Clinica.builder()
                .idClinica("cli-1")
                .nomeClinica("Clínica ABC")
                .build();

        paciente = Paciente.builder()
                .idPaciente("pac-1")
                .build();
    }

    @Test
    void deveGerarLancamentoParticularDiretoQuandoNaoHouverClinica() {
        Compromisso compromisso = Compromisso.builder()
                .idCompromisso("cmp-1")
                .profissional(profissional)
                .paciente(paciente)
                .clinica(null)
                .dataHoraCompromisso(LocalDateTime.of(2026, 11, 10, 14, 0))
                .status(StatusCompromisso.REALIZADO)
                .build();

        when(lancamentoRepasseRepository.findByCompromisso_IdCompromisso("cmp-1")).thenReturn(Optional.empty());
        when(lancamentoRepasseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LancamentoRepasseProfissional lancamento = lancamentoRepasseService.gerarOuAtualizarLancamentoPorCompromisso(compromisso);

        assertNotNull(lancamento);
        assertEquals(TipoOrigemLancamento.PARTICULAR_DIRETO, lancamento.getTipoOrigem());
        assertEquals(new BigDecimal("150.00"), lancamento.getValorLiquidoRepasse());
        assertEquals(StatusLancamentoRepasse.PENDENTE, lancamento.getStatusPagamento());
        assertNull(lancamento.getClinica());
    }

    @Test
    void deveGerarLancamentoComRepasseFixoDaClinica() {
        ProfissionalClinica pc = ProfissionalClinica.builder()
                .profissional(profissional)
                .clinica(clinica)
                .tipoRepasse(TipoRepasseProfissional.VALOR_FIXO)
                .valorRepasse(new BigDecimal("80.00"))
                .build();

        Compromisso compromisso = Compromisso.builder()
                .idCompromisso("cmp-2")
                .profissional(profissional)
                .paciente(paciente)
                .clinica(clinica)
                .dataHoraCompromisso(LocalDateTime.of(2026, 11, 12, 10, 0))
                .status(StatusCompromisso.REALIZADO)
                .build();

        when(profissionalClinicaRepository.findByProfissionalAndClinica(profissional, clinica))
                .thenReturn(Optional.of(pc));
        when(lancamentoRepasseRepository.findByCompromisso_IdCompromisso("cmp-2")).thenReturn(Optional.empty());
        when(lancamentoRepasseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LancamentoRepasseProfissional lancamento = lancamentoRepasseService.gerarOuAtualizarLancamentoPorCompromisso(compromisso);

        assertNotNull(lancamento);
        assertEquals(TipoOrigemLancamento.CLINICA_FIXO, lancamento.getTipoOrigem());
        assertEquals(new BigDecimal("80.00"), lancamento.getValorLiquidoRepasse());
        assertEquals(clinica, lancamento.getClinica());
    }

    @Test
    void deveGerarLancamentoComRepassePercentualSobreTabela() {
        TipoCompromisso tipo = TipoCompromisso.builder().idTipoCompromisso("tipo-fono").build();

        ProfissionalClinica pc = ProfissionalClinica.builder()
                .profissional(profissional)
                .clinica(clinica)
                .tipoRepasse(TipoRepasseProfissional.PERCENTUAL)
                .valorRepasse(new BigDecimal("60.00")) // 60%
                .build();

        TabelaPrecoClinica tp = TabelaPrecoClinica.builder()
                .clinica(clinica)
                .tipoCompromisso(tipo)
                .valorSessao(new BigDecimal("200.00"))
                .build();

        Compromisso compromisso = Compromisso.builder()
                .idCompromisso("cmp-3")
                .profissional(profissional)
                .paciente(paciente)
                .clinica(clinica)
                .tipoCompromisso(tipo)
                .dataHoraCompromisso(LocalDateTime.of(2026, 11, 15, 9, 0))
                .status(StatusCompromisso.REALIZADO)
                .build();

        when(profissionalClinicaRepository.findByProfissionalAndClinica(profissional, clinica))
                .thenReturn(Optional.of(pc));
        when(tabelaPrecoClinicaRepository.findByClinicaIdClinica("cli-1")).thenReturn(List.of(tp));
        when(lancamentoRepasseRepository.findByCompromisso_IdCompromisso("cmp-3")).thenReturn(Optional.empty());
        when(lancamentoRepasseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LancamentoRepasseProfissional lancamento = lancamentoRepasseService.gerarOuAtualizarLancamentoPorCompromisso(compromisso);

        assertNotNull(lancamento);
        assertEquals(TipoOrigemLancamento.CLINICA_PERCENTUAL, lancamento.getTipoOrigem());
        assertEquals(new BigDecimal("200.00"), lancamento.getValorBruto());
        assertEquals(new BigDecimal("60.00"), lancamento.getPercentualRepasse());
        // 60% de 200 = 120.00
        assertEquals(new BigDecimal("120.00"), lancamento.getValorLiquidoRepasse());
    }

    @Test
    void deveDarBaixaIndividualEmLancamento() {
        LancamentoRepasseProfissional l = LancamentoRepasseProfissional.builder()
                .idLancamento("lan-1")
                .statusPagamento(StatusLancamentoRepasse.PENDENTE)
                .build();

        when(lancamentoRepasseRepository.findById("lan-1")).thenReturn(Optional.of(l));
        when(lancamentoRepasseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LocalDate data = LocalDate.of(2026, 11, 16);
        LancamentoRepasseProfissional baixado = lancamentoRepasseService.darBaixaLancamento("lan-1", data);

        assertEquals(StatusLancamentoRepasse.PAGO, baixado.getStatusPagamento());
        assertEquals(data, baixado.getDataPagamento());
    }
}
