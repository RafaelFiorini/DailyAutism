package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.*;
import com.clinica.dailyautism.domain.entity.security.Perfil;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.repository.*;
import com.clinica.dailyautism.infrastructure.dto.AssinarGuiaDTO;
import com.clinica.dailyautism.infrastructure.dto.EnviarGuiaConvenioDTO;
import com.clinica.dailyautism.infrastructure.dto.GerarGuiaFaturamentoDTO;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FaturamentoConvenioServiceTest {

    @Mock
    private GuiaFaturamentoConvenioRepository guiaRepository;
    @Mock
    private GuiaFaturamentoItemRepository guiaItemRepository;
    @Mock
    private CompromissoRepository compromissoRepository;
    @Mock
    private PacienteRepository pacienteRepository;
    @Mock
    private PlanoSaudeRepository planoSaudeRepository;
    @Mock
    private ResponsavelRepository responsavelRepository;
    @Mock
    private PacientePlanoSaudeRepository pacientePlanoSaudeRepository;
    @Mock
    private ClinicaPlanoSaudeRepository clinicaPlanoSaudeRepository;
    @Mock
    private TabelaPrecoService tabelaPrecoService;
    @Mock
    private ClinicaAccessHelper clinicaAccessHelper;
    @Mock
    private GuiaPdfService guiaPdfService;
    @Mock
    private AssinaturaEletronicaProvider assinaturaProvider;

    @InjectMocks
    private FaturamentoConvenioService faturamentoService;

    @Test
    void deveGerarGuiaComValoresCongeladosENaoDuplicarAtendimentos() {
        Clinica clinica = Clinica.builder().idClinica("cli-1").nomeClinica("Attenzione").build();
        Paciente paciente = Paciente.builder().idPaciente("pac-1").build();
        PlanoSaude plano = PlanoSaude.builder().idPlanoSaude("pl-1").nomePlano("Unimed").build();
        TipoCompromisso to = TipoCompromisso.builder().idTipoCompromisso("tp-to").nomeTipo("TO").build();
        Compromisso c1 = Compromisso.builder()
                .idCompromisso("cmp-1")
                .clinica(clinica)
                .paciente(paciente)
                .tipoCompromisso(to)
                .status(StatusCompromisso.REALIZADO)
                .dataHoraCompromisso(LocalDateTime.of(2026, 11, 5, 10, 0))
                .build();
        Compromisso c2JaFaturado = Compromisso.builder()
                .idCompromisso("cmp-2")
                .clinica(clinica)
                .paciente(paciente)
                .tipoCompromisso(to)
                .status(StatusCompromisso.REALIZADO)
                .dataHoraCompromisso(LocalDateTime.of(2026, 11, 12, 10, 0))
                .build();

        when(clinicaAccessHelper.validarEAcessarClinica("cli-1", "gestor@cli.com")).thenReturn(clinica);
        when(pacienteRepository.findById("pac-1")).thenReturn(Optional.of(paciente));
        when(planoSaudeRepository.findById("pl-1")).thenReturn(Optional.of(plano));
        when(compromissoRepository.findByPacienteIdPacienteAndStatusAndDataHoraCompromissoBetweenOrderByDataHoraCompromissoAsc(
                eq("pac-1"), eq(StatusCompromisso.REALIZADO), any(), any()))
                .thenReturn(List.of(c1, c2JaFaturado));
        when(clinicaAccessHelper.resolverClinicaDoCompromisso(any())).thenReturn(clinica);
        when(guiaItemRepository.findCompromissoIdsJaFaturados(anyList(), eq(StatusGuiaFaturamento.CANCELADA)))
                .thenReturn(List.of("cmp-2"));
        when(tabelaPrecoService.resolverValorConvenio("cli-1", "tp-to", "pl-1"))
                .thenReturn(new BigDecimal("70.00"));
        when(guiaPdfService.gerarPdf(any())).thenReturn("%PDF-fake".getBytes());
        when(guiaPdfService.calcularSha256(any())).thenReturn("hash-orig");
        when(guiaPdfService.salvarEmDisco(anyString(), any())).thenReturn("storage/guias/lote.pdf");
        when(guiaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GerarGuiaFaturamentoDTO dto = new GerarGuiaFaturamentoDTO(
                "cli-1", "pac-1", "pl-1", null,
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30),
                null, null, true
        );

        GuiaFaturamentoConvenio guia = faturamentoService.gerarGuia(dto, "gestor@cli.com");

        assertEquals(StatusGuiaFaturamento.AGUARDANDO_ASSINATURA, guia.getStatus());
        assertEquals(1, guia.getItens().size());
        assertEquals("cmp-1", guia.getItens().get(0).getCompromisso().getIdCompromisso());
        assertEquals(new BigDecimal("70.00"), guia.getValorTotal());
        assertEquals("hash-orig", guia.getHashPdfOriginal());
    }

    @Test
    void deveBloquearEnvioAntesDaAssinaturaECalcularPrevisaoAposAssinar() {
        Clinica clinica = Clinica.builder().idClinica("cli-1").nomeClinica("Attenzione").build();
        Paciente paciente = Paciente.builder().idPaciente("pac-1").build();
        PlanoSaude plano = PlanoSaude.builder().idPlanoSaude("pl-1").nomePlano("Unimed").build();

        GuiaFaturamentoConvenio guia = GuiaFaturamentoConvenio.builder()
                .idGuiaFaturamento("guia-1")
                .clinica(clinica)
                .paciente(paciente)
                .planoSaude(plano)
                .numeroLoteGuia("FAT-202611-0001")
                .status(StatusGuiaFaturamento.AGUARDANDO_ASSINATURA)
                .build();

        when(guiaRepository.findById("guia-1")).thenReturn(Optional.of(guia));
        when(clinicaAccessHelper.validarEAcessarClinica("cli-1", "gestor@cli.com")).thenReturn(clinica);

        assertThrows(IllegalStateException.class,
                () -> faturamentoService.marcarEnviadaAoConvenio("guia-1", new EnviarGuiaConvenioDTO(LocalDate.of(2026, 12, 2)), "gestor@cli.com"));

        User respUser = User.builder()
                .idUser("u-resp")
                .nomeUser("Carla")
                .perfis(Set.of(Perfil.builder().nome("RESPONSAVEL").build()))
                .build();
        when(clinicaAccessHelper.loadUserOrNull("carla@teste.com")).thenReturn(respUser);
        when(clinicaAccessHelper.isAdmin(respUser)).thenReturn(false);
        when(clinicaAccessHelper.listarIdsPacientesDoResponsavel(respUser)).thenReturn(List.of("pac-1"));
        when(assinaturaProvider.assinar(eq(guia), eq(respUser), eq("123456"), eq("10.0.0.1")))
                .thenReturn(new AssinaturaEletronicaProvider.ResultadoAssinatura(
                        ProvedorAssinatura.INTERNO_DAILYAUTISM, "u-resp", "Carla", "41100011101",
                        LocalDateTime.of(2026, 12, 1, 14, 0), "10.0.0.1", "DA-9999"
                ));
        when(guiaPdfService.gerarPdf(guia)).thenReturn("%PDF-signed".getBytes());
        when(guiaPdfService.calcularSha256(any())).thenReturn("hash-assinado");
        when(guiaPdfService.salvarEmDisco(anyString(), any())).thenReturn("storage/guias/signed.pdf");
        when(guiaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GuiaFaturamentoConvenio assinada = faturamentoService.assinarGuia(
                "guia-1", new AssinarGuiaDTO("123456"), "10.0.0.1", "carla@teste.com");
        assertEquals(StatusGuiaFaturamento.ASSINADA, assinada.getStatus());
        assertEquals("DA-9999", assinada.getCodigoVerificador());

        ClinicaPlanoSaude cps = ClinicaPlanoSaude.builder()
                .clinica(clinica).planoSaude(plano).prazoPagamentoDias(45).build();
        when(clinicaPlanoSaudeRepository.findByClinicaIdClinicaAndPlanoSaudeIdPlanoSaude("cli-1", "pl-1"))
                .thenReturn(Optional.of(cps));

        GuiaFaturamentoConvenio enviada = faturamentoService.marcarEnviadaAoConvenio(
                "guia-1", new EnviarGuiaConvenioDTO(LocalDate.of(2026, 12, 2)), "gestor@cli.com");
        assertEquals(StatusGuiaFaturamento.ENVIADA_CONVENIO, enviada.getStatus());
        assertEquals(LocalDate.of(2027, 1, 16), enviada.getDataPrevisaoPagamento());
    }

    @Test
    void deveEnviarLoteAoConvenioComSucesso() {
        Clinica clinica = Clinica.builder().idClinica("cli-1").nomeClinica("Attenzione").build();
        PlanoSaude plano = PlanoSaude.builder().idPlanoSaude("pl-1").nomePlano("Unimed").build();

        GuiaFaturamentoConvenio g1 = GuiaFaturamentoConvenio.builder()
                .idGuiaFaturamento("g-1")
                .clinica(clinica)
                .planoSaude(plano)
                .status(StatusGuiaFaturamento.ASSINADA)
                .valorTotal(new BigDecimal("150.00"))
                .build();

        GuiaFaturamentoConvenio g2 = GuiaFaturamentoConvenio.builder()
                .idGuiaFaturamento("g-2")
                .clinica(clinica)
                .planoSaude(plano)
                .status(StatusGuiaFaturamento.ASSINADA)
                .valorTotal(new BigDecimal("200.00"))
                .build();

        when(clinicaAccessHelper.validarEAcessarClinica("cli-1", "admin@teste.com")).thenReturn(clinica);
        when(planoSaudeRepository.findById("pl-1")).thenReturn(Optional.of(plano));
        when(guiaRepository.findByClinicaIdClinicaAndPlanoSaudeIdPlanoSaudeAndStatus("cli-1", "pl-1", StatusGuiaFaturamento.ASSINADA))
                .thenReturn(List.of(g1, g2));

        ClinicaPlanoSaude cps = ClinicaPlanoSaude.builder()
                .clinica(clinica).planoSaude(plano).prazoPagamentoDias(30).build();
        when(clinicaPlanoSaudeRepository.findByClinicaIdClinicaAndPlanoSaudeIdPlanoSaude("cli-1", "pl-1"))
                .thenReturn(Optional.of(cps));

        com.clinica.dailyautism.infrastructure.dto.EnviarLoteGuiasDTO dto =
                com.clinica.dailyautism.infrastructure.dto.EnviarLoteGuiasDTO.builder()
                        .idClinica("cli-1")
                        .idPlanoSaude("pl-1")
                        .dataEnvio(LocalDate.of(2026, 11, 20))
                        .build();

        com.clinica.dailyautism.infrastructure.dto.ResultadoLoteGuiasDTO resultado =
                faturamentoService.enviarLoteAoConvenio(dto, "admin@teste.com");

        assertNotNull(resultado);
        assertEquals(2, resultado.getQuantidadeGuias());
        assertEquals(new BigDecimal("350.00"), resultado.getValorTotal());
        assertEquals(LocalDate.of(2026, 12, 20), resultado.getDataPrevisaoPagamento());
        assertEquals(StatusGuiaFaturamento.ENVIADA_CONVENIO, g1.getStatus());
        assertEquals(StatusGuiaFaturamento.ENVIADA_CONVENIO, g2.getStatus());
        verify(guiaRepository).saveAll(any());
    }
}
