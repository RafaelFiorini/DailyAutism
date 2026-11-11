package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.GuiaFaturamentoConvenio;
import com.clinica.dailyautism.domain.entity.GuiaFaturamentoItem;
import com.clinica.dailyautism.domain.entity.StatusGuiaFaturamento;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

/**
 * Gera o documento PDF A4 da guia de faturamento de convênio (prévia e versão assinada).
 */
@Service
public class GuiaPdfService {

    private static final DateTimeFormatter DATE_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat BRL = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));
    private static final Path STORAGE_DIR = Paths.get("storage", "guias");

    public byte[] gerarPdf(GuiaFaturamentoConvenio guia) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font tituloFont = new Font(Font.HELVETICA, 14, Font.BOLD, new Color(30, 41, 59));
            Font subTituloFont = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(55, 65, 81));
            Font normalFont = new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(31, 41, 55));
            Font smallMuted = new Font(Font.HELVETICA, 8, Font.NORMAL, new Color(100, 116, 139));
            Font thFont = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);

            String nomeClinica = guia.getClinica() != null ? guia.getClinica().getNomeClinica() : "Clínica";
            Paragraph pClinica = new Paragraph(nomeClinica, tituloFont);
            doc.add(pClinica);

            if (guia.getClinica() != null) {
                String cnpj = guia.getClinica().getCnpjClinica() != null ? "CNPJ: " + guia.getClinica().getCnpjClinica() : "";
                String tel = guia.getClinica().getTelefoneClinica() != null ? " | Tel: " + guia.getClinica().getTelefoneClinica() : "";
                String end = guia.getClinica().getEnderecoClinica() != null ? " | " + guia.getClinica().getEnderecoClinica() : "";
                Paragraph infoCli = new Paragraph((cnpj + tel + end).trim(), smallMuted);
                infoCli.setSpacingAfter(8f);
                doc.add(infoCli);
            }

            Paragraph tituloDoc = new Paragraph(
                    "GUIA DE FATURAMENTO DE CONVÊNIO — DEMONSTRATIVO DE SESSÕES",
                    subTituloFont
            );
            tituloDoc.setSpacingAfter(10f);
            doc.add(tituloDoc);

            // Quadro de identificação
            PdfPTable meta = new PdfPTable(2);
            meta.setWidthPercentage(100);
            meta.setSpacingAfter(12f);

            String nomePac = (guia.getPaciente() != null && guia.getPaciente().getPessoaPaciente() != null)
                    ? guia.getPaciente().getPessoaPaciente().getNomePessoa()
                    : "-";
            String cpfPac = (guia.getPaciente() != null && guia.getPaciente().getPessoaPaciente() != null
                    && guia.getPaciente().getPessoaPaciente().getCPFPessoa() != null)
                    ? guia.getPaciente().getPessoaPaciente().getCPFPessoa()
                    : "-";
            String nomePlano = guia.getPlanoSaude() != null ? guia.getPlanoSaude().getNomePlano() : "-";
            String nomeResp = (guia.getResponsavel() != null && guia.getResponsavel().getPessoaResponsavel() != null)
                    ? guia.getResponsavel().getPessoaResponsavel().getNomePessoa()
                    : "-";

            addMetaCell(meta, "Nº Lote / Guia: " + safe(guia.getNumeroLoteGuia()), normalFont);
            addMetaCell(meta, "Status: " + safe(guia.getStatus() != null ? guia.getStatus().name() : "-"), normalFont);
            addMetaCell(meta, "Convênio: " + nomePlano, normalFont);
            addMetaCell(meta, "Competência: " + formatDate(guia.getCompetenciaInicio()) + " a " + formatDate(guia.getCompetenciaFim()), normalFont);
            addMetaCell(meta, "Paciente: " + nomePac + " (CPF: " + cpfPac + ")", normalFont);
            addMetaCell(meta, "Carteirinha: " + safe(guia.getNumeroCarteirinha()) + " | Guia Op.: " + safe(guia.getNumeroGuiaOperadora()), normalFont);
            addMetaCell(meta, "Responsável Legal: " + nomeResp, normalFont);
            addMetaCell(meta, "Qtd. Atendimentos: " + (guia.getItens() != null ? guia.getItens().size() : 0), normalFont);
            doc.add(meta);

            // Tabela de atendimentos
            PdfPTable table = new PdfPTable(new float[]{0.8f, 2.2f, 2.8f, 3.0f, 1.6f});
            table.setWidthPercentage(100);
            table.setSpacingAfter(10f);

            String[] headers = {"#", "Data / Hora", "Especialidade", "Profissional / Conselho", "Valor"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, thFont));
                cell.setBackgroundColor(new Color(79, 70, 229));
                cell.setPadding(6f);
                table.addCell(cell);
            }

            List<GuiaFaturamentoItem> itensOrdenados = guia.getItens() == null
                    ? List.of()
                    : guia.getItens().stream()
                            .filter(i -> i.getAtivo() == null || i.getAtivo())
                            .sorted(Comparator.comparing(GuiaFaturamentoItem::getDataHoraAtendimento))
                            .toList();

            int idx = 1;
            for (GuiaFaturamentoItem item : itensOrdenados) {
                addBodyCell(table, String.valueOf(idx++), normalFont);
                addBodyCell(table, item.getDataHoraAtendimento() != null ? item.getDataHoraAtendimento().format(DATETIME_BR) : "-", normalFont);
                addBodyCell(table, safe(item.getTipoCompromissoNome()), normalFont);
                String profTexto = safe(item.getProfissionalNome());
                if (item.getConselhoProfissional() != null && !item.getConselhoProfissional().isBlank()) {
                    profTexto += " (" + item.getConselhoProfissional() + ")";
                }
                addBodyCell(table, profTexto, normalFont);
                addBodyCell(table, formatBrl(item.getValorUnitarioConvenio()), normalFont);
            }
            doc.add(table);

            Paragraph pTotal = new Paragraph(
                    "Valor Total da Guia: " + formatBrl(guia.getValorTotal()),
                    new Font(Font.HELVETICA, 11, Font.BOLD, new Color(15, 23, 42))
            );
            pTotal.setAlignment(Element.ALIGN_RIGHT);
            pTotal.setSpacingAfter(16f);
            doc.add(pTotal);

            // Bloco de Assinatura Eletrônica
            PdfPTable sigBox = new PdfPTable(1);
            sigBox.setWidthPercentage(100);
            PdfPCell sigCell = new PdfPCell();
            sigCell.setPadding(10f);
            sigCell.setBorderColor(new Color(148, 163, 184));

            boolean assinada = guia.getStatus() == StatusGuiaFaturamento.ASSINADA
                    || guia.getStatus() == StatusGuiaFaturamento.ENVIADA_CONVENIO
                    || guia.getStatus() == StatusGuiaFaturamento.PAGA;

            if (assinada && guia.getDataHoraAssinatura() != null) {
                sigCell.setBackgroundColor(new Color(240, 253, 244));
                sigCell.addElement(new Phrase("DOCUMENTO ASSINADO ELETRONICAMENTE", new Font(Font.HELVETICA, 9, Font.BOLD, new Color(21, 128, 61))));
                sigCell.addElement(new Phrase("Assinante: " + safe(guia.getAssinadoPorNome()) + " | CPF: " + safe(guia.getAssinadoPorCpf()), normalFont));
                sigCell.addElement(new Phrase(
                        "Data/Hora: " + guia.getDataHoraAssinatura().format(DATETIME_BR)
                                + " | IP: " + safe(guia.getIpAssinatura())
                                + " | Provedor: " + safe(guia.getProvedorAssinatura() != null ? guia.getProvedorAssinatura().name() : "INTERNO_DAILYAUTISM"),
                        normalFont
                ));
                sigCell.addElement(new Phrase(
                        "Código Verificador: " + safe(guia.getCodigoVerificador())
                                + " | Hash Original SHA-256: " + safe(guia.getHashPdfOriginal()),
                        smallMuted
                ));
            } else {
                sigCell.setBackgroundColor(new Color(248, 250, 252));
                sigCell.addElement(new Phrase("CONFERÊNCIA E ASSINATURA DO RESPONSÁVEL", subTituloFont));
                sigCell.addElement(new Phrase(
                        "Status atual: " + (guia.getStatus() != null ? guia.getStatus().name() : "RASCUNHO")
                                + " — Aguardando confirmação eletrônica do responsável legal.",
                        smallMuted
                ));
            }
            sigBox.addCell(sigCell);
            doc.add(sigBox);

            doc.close();
            return baos.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Erro ao gerar PDF da guia de faturamento: " + ex.getMessage(), ex);
        }
    }

    public String calcularSha256(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(bytes));
        } catch (Exception ex) {
            throw new IllegalStateException("Erro ao calcular SHA-256 do PDF", ex);
        }
    }

    public String salvarEmDisco(String nomeArquivo, byte[] bytes) {
        try {
            Files.createDirectories(STORAGE_DIR);
            Path destino = STORAGE_DIR.resolve(nomeArquivo);
            Files.write(destino, bytes);
            return destino.toString();
        } catch (IOException ex) {
            throw new IllegalStateException("Erro ao salvar PDF em disco: " + ex.getMessage(), ex);
        }
    }

    public byte[] carregarOuRegerar(GuiaFaturamentoConvenio guia) {
        if (guia.getCaminhoPdf() != null && !guia.getCaminhoPdf().isBlank()) {
            Path p = Paths.get(guia.getCaminhoPdf());
            if (Files.exists(p)) {
                try {
                    return Files.readAllBytes(p);
                } catch (IOException ignored) {
                    // Fallback para regeneração determinística abaixo
                }
            }
        }
        return gerarPdf(guia);
    }

    private void addMetaCell(PdfPTable table, String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setPadding(5f);
        c.setBorderColor(new Color(226, 232, 240));
        table.addCell(c);
    }

    private void addBodyCell(PdfPTable table, String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setPadding(5f);
        c.setBorderColor(new Color(226, 232, 240));
        table.addCell(c);
    }

    private String formatDate(java.time.LocalDate d) {
        return d != null ? d.format(DATE_BR) : "-";
    }

    private String formatBrl(BigDecimal v) {
        return BRL.format(v != null ? v : BigDecimal.ZERO);
    }

    private String safe(String s) {
        return (s != null && !s.isBlank()) ? s : "-";
    }
}
