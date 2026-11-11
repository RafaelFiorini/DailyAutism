package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GuiaPdfServiceTest {

    private final GuiaPdfService pdfService = new GuiaPdfService();

    @Test
    void deveGerarPdfValidoComHashesDistintosEntreRascunhoEAssinada() {
        Clinica clinica = Clinica.builder()
                .idClinica("cli-1")
                .nomeClinica("Attenzione Clínica Integrada")
                .cnpjClinica("12345678000190")
                .build();
        Pessoa pessoaPac = Pessoa.builder().nomePessoa("Lorenzo Lima").CPFPessoa("51100011103").build();
        Paciente paciente = Paciente.builder().idPaciente("pac-1").pessoaPaciente(pessoaPac).build();
        PlanoSaude plano = PlanoSaude.builder().idPlanoSaude("pl-1").nomePlano("Unimed Curitiba").build();

        GuiaFaturamentoItem item = GuiaFaturamentoItem.builder()
                .dataHoraAtendimento(LocalDateTime.of(2026, 11, 10, 14, 0))
                .tipoCompromissoNome("Terapia Ocupacional")
                .profissionalNome("Dra. Camila Ribeiro")
                .conselhoProfissional("CRP 08/21450")
                .valorUnitarioConvenio(new BigDecimal("70.00"))
                .build();

        GuiaFaturamentoConvenio guia = GuiaFaturamentoConvenio.builder()
                .clinica(clinica)
                .paciente(paciente)
                .planoSaude(plano)
                .numeroLoteGuia("FAT-202611-0001")
                .numeroCarteirinha("UC-511003")
                .competenciaInicio(LocalDate.of(2026, 11, 1))
                .competenciaFim(LocalDate.of(2026, 11, 30))
                .valorTotal(new BigDecimal("70.00"))
                .status(StatusGuiaFaturamento.AGUARDANDO_ASSINATURA)
                .itens(List.of(item))
                .build();

        byte[] pdfOriginal = pdfService.gerarPdf(guia);
        assertTrue(pdfOriginal.length > 500);
        assertEquals("%PDF-", new String(pdfOriginal, 0, 5));
        String hashOriginal = pdfService.calcularSha256(pdfOriginal);

        guia.setStatus(StatusGuiaFaturamento.ASSINADA);
        guia.setProvedorAssinatura(ProvedorAssinatura.INTERNO_DAILYAUTISM);
        guia.setAssinadoPorNome("Fernando Souza Lima");
        guia.setAssinadoPorCpf("41100011102");
        guia.setDataHoraAssinatura(LocalDateTime.of(2026, 11, 30, 16, 15));
        guia.setIpAssinatura("127.0.0.1");
        guia.setCodigoVerificador("DA-1234ABCD");
        guia.setHashPdfOriginal(hashOriginal);

        byte[] pdfAssinado = pdfService.gerarPdf(guia);
        assertEquals("%PDF-", new String(pdfAssinado, 0, 5));
        String hashAssinado = pdfService.calcularSha256(pdfAssinado);
        assertNotEquals(hashOriginal, hashAssinado);
    }
}
