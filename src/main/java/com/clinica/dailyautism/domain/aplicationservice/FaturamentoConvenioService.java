package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.*;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.exception.PacienteNotFoundException;
import com.clinica.dailyautism.domain.exception.PlanoSaudeNotFoundException;
import com.clinica.dailyautism.domain.repository.*;
import com.clinica.dailyautism.infrastructure.dto.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FaturamentoConvenioService {

    private static final DateTimeFormatter LOTE_YYYYMM = DateTimeFormatter.ofPattern("yyyyMM");

    private final GuiaFaturamentoConvenioRepository guiaRepository;
    private final GuiaFaturamentoItemRepository guiaItemRepository;
    private final CompromissoRepository compromissoRepository;
    private final PacienteRepository pacienteRepository;
    private final PlanoSaudeRepository planoSaudeRepository;
    private final ResponsavelRepository responsavelRepository;
    private final PacientePlanoSaudeRepository pacientePlanoSaudeRepository;
    private final ClinicaPlanoSaudeRepository clinicaPlanoSaudeRepository;
    private final TabelaPrecoService tabelaPrecoService;
    private final ClinicaAccessHelper clinicaAccessHelper;
    private final GuiaPdfService guiaPdfService;
    private final AssinaturaEletronicaProvider assinaturaProvider;

    public List<SessaoElegivelDTO> listarSessoesElegiveis(
            String clinicaId,
            String pacienteId,
            String planoSaudeId,
            LocalDate inicio,
            LocalDate fim,
            String emailUsuario
    ) {
        Clinica clinica = clinicaAccessHelper.validarEAcessarClinica(clinicaId, emailUsuario);
        List<Compromisso> compromissos = buscarCompromissosElegiveis(clinica.getIdClinica(), pacienteId, inicio, fim);

        return compromissos.stream()
                .map(c -> {
                    String idTipo = c.getTipoCompromisso() != null ? c.getTipoCompromisso().getIdTipoCompromisso() : null;
                    BigDecimal valor = tabelaPrecoService.resolverValorConvenio(clinica.getIdClinica(), idTipo, planoSaudeId);
                    String nomeProf = (c.getProfissional() != null && c.getProfissional().getPessoaProf() != null)
                            ? c.getProfissional().getPessoaProf().getNomePessoa()
                            : "-";
                    String conselho = c.getProfissional() != null ? c.getProfissional().getConselhoProf() : null;
                    return SessaoElegivelDTO.builder()
                            .idCompromisso(c.getIdCompromisso())
                            .tituloCompromisso(c.getTituloCompromisso())
                            .dataHoraAtendimento(c.getDataHoraCompromisso())
                            .idTipoCompromisso(idTipo)
                            .nomeTipoCompromisso(c.getTipoCompromisso() != null ? c.getTipoCompromisso().getNomeTipo() : "-")
                            .nomeProfissional(nomeProf)
                            .conselhoProfissional(conselho)
                            .valorUnitarioConvenio(valor)
                            .build();
                })
                .toList();
    }

    @Transactional
    public GuiaFaturamentoConvenio gerarGuia(GerarGuiaFaturamentoDTO dto, String emailUsuario) {
        if (dto.getCompetenciaFim().isBefore(dto.getCompetenciaInicio())) {
            throw new IllegalArgumentException("A data final da competência deve ser igual ou posterior à data inicial.");
        }

        Clinica clinica = clinicaAccessHelper.validarEAcessarClinica(dto.getIdClinica(), emailUsuario);
        Paciente paciente = pacienteRepository.findById(dto.getIdPaciente().trim())
                .orElseThrow(() -> new PacienteNotFoundException(dto.getIdPaciente()));
        PlanoSaude plano = planoSaudeRepository.findById(dto.getIdPlanoSaude().trim())
                .orElseThrow(() -> new PlanoSaudeNotFoundException(dto.getIdPlanoSaude()));

        List<Compromisso> elegiveis = buscarCompromissosElegiveis(
                clinica.getIdClinica(), paciente.getIdPaciente(), dto.getCompetenciaInicio(), dto.getCompetenciaFim());

        List<Compromisso> selecionados;
        if (dto.getIdsCompromissos() != null && !dto.getIdsCompromissos().isEmpty()) {
            Set<String> idsSolicitados = new HashSet<>(dto.getIdsCompromissos());
            List<String> jaFaturados = guiaItemRepository.findCompromissoIdsJaFaturados(
                    new ArrayList<>(idsSolicitados), StatusGuiaFaturamento.CANCELADA);
            if (!jaFaturados.isEmpty()) {
                throw new IllegalStateException("Um ou mais atendimentos selecionados já pertencem a outra guia ativa.");
            }
            selecionados = elegiveis.stream()
                    .filter(c -> idsSolicitados.contains(c.getIdCompromisso()))
                    .toList();
        } else {
            selecionados = elegiveis;
        }

        if (selecionados.isEmpty()) {
            throw new IllegalArgumentException("Nenhum atendimento realizado elegível encontrado para este período.");
        }

        Responsavel responsavel = resolverResponsavelGuia(dto.getIdResponsavel(), paciente);
        PacientePlanoSaude vinculoPlano = pacientePlanoSaudeRepository.findByPacienteIdPaciente(paciente.getIdPaciente()).stream()
                .filter(p -> p.getAtivo() == null || p.getAtivo())
                .filter(p -> p.getPlanoSaude() != null && plano.getIdPlanoSaude().equals(p.getPlanoSaude().getIdPlanoSaude()))
                .findFirst()
                .orElse(null);

        long seq = guiaRepository.countByClinicaIdClinica(clinica.getIdClinica()) + 1;
        String numeroLote = String.format("FAT-%s-%04d", dto.getCompetenciaFim().format(LOTE_YYYYMM), seq);

        StatusGuiaFaturamento statusInicial = Boolean.TRUE.equals(dto.getLiberarParaAssinatura())
                ? StatusGuiaFaturamento.AGUARDANDO_ASSINATURA
                : StatusGuiaFaturamento.RASCUNHO;

        GuiaFaturamentoConvenio guia = GuiaFaturamentoConvenio.builder()
                .clinica(clinica)
                .paciente(paciente)
                .planoSaude(plano)
                .responsavel(responsavel)
                .numeroLoteGuia(numeroLote)
                .numeroCarteirinha(vinculoPlano != null ? vinculoPlano.getNumeroCarteirinha() : null)
                .numeroGuiaOperadora(vinculoPlano != null ? vinculoPlano.getNumeroGuia() : null)
                .competenciaInicio(dto.getCompetenciaInicio())
                .competenciaFim(dto.getCompetenciaFim())
                .valorTotal(BigDecimal.ZERO)
                .status(statusInicial)
                .observacoes(dto.getObservacoes())
                .itens(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (Compromisso comp : selecionados) {
            String idTipo = comp.getTipoCompromisso() != null ? comp.getTipoCompromisso().getIdTipoCompromisso() : null;
            BigDecimal valor = tabelaPrecoService.resolverValorConvenio(
                    clinica.getIdClinica(), idTipo, plano.getIdPlanoSaude());
            total = total.add(valor);

            String nomeProf = (comp.getProfissional() != null && comp.getProfissional().getPessoaProf() != null)
                    ? comp.getProfissional().getPessoaProf().getNomePessoa()
                    : "-";
            String conselho = comp.getProfissional() != null ? comp.getProfissional().getConselhoProf() : null;

            guia.getItens().add(GuiaFaturamentoItem.builder()
                    .guia(guia)
                    .compromisso(comp)
                    .dataHoraAtendimento(comp.getDataHoraCompromisso())
                    .tipoCompromissoNome(comp.getTipoCompromisso() != null ? comp.getTipoCompromisso().getNomeTipo() : comp.getTituloCompromisso())
                    .profissionalNome(nomeProf)
                    .conselhoProfissional(conselho)
                    .valorUnitarioConvenio(valor)
                    .build());
        }
        guia.setValorTotal(total);

        byte[] pdfBytes = guiaPdfService.gerarPdf(guia);
        guia.setHashPdfOriginal(guiaPdfService.calcularSha256(pdfBytes));
        guia.setCaminhoPdf(guiaPdfService.salvarEmDisco(numeroLote + ".pdf", pdfBytes));

        return guiaRepository.save(guia);
    }

    @Transactional
    public GuiaFaturamentoConvenio liberarParaAssinatura(String idGuia, String emailUsuario) {
        GuiaFaturamentoConvenio guia = carregarGuiaComAcessoClinica(idGuia, emailUsuario);
        if (guia.getStatus() != StatusGuiaFaturamento.RASCUNHO) {
            throw new IllegalStateException("Apenas guias em RASCUNHO podem ser liberadas para assinatura.");
        }
        guia.setStatus(StatusGuiaFaturamento.AGUARDANDO_ASSINATURA);
        return guiaRepository.save(guia);
    }

    @Transactional
    public GuiaFaturamentoConvenio assinarGuia(
            String idGuia,
            AssinarGuiaDTO dto,
            String ipOrigem,
            String emailUsuario
    ) {
        GuiaFaturamentoConvenio guia = guiaRepository.findById(idGuia)
                .orElseThrow(() -> new IllegalArgumentException("Guia de faturamento não encontrada: " + idGuia));

        if (guia.getStatus() != StatusGuiaFaturamento.AGUARDANDO_ASSINATURA) {
            throw new IllegalStateException("Esta guia não está aguardando assinatura.");
        }

        User user = clinicaAccessHelper.loadUserOrNull(emailUsuario);
        if (user == null) {
            throw new AccessDeniedException("Usuário autenticado não encontrado.");
        }

        if (!clinicaAccessHelper.isAdmin(user)) {
            boolean autorizado = false;
            String pacienteId = guia.getPaciente() != null ? guia.getPaciente().getIdPaciente() : null;
            if (pacienteId != null) {
                List<String> meusPacientes = clinicaAccessHelper.listarIdsPacientesDoResponsavel(user);
                if (meusPacientes.contains(pacienteId)) {
                    autorizado = true;
                } else if (guia.getPaciente().isResponsavel()
                        && guia.getPaciente().getPessoaPaciente() != null
                        && user.getPessoa() != null
                        && guia.getPaciente().getPessoaPaciente().getIdPessoa().equals(user.getPessoa().getIdPessoa())) {
                    autorizado = true;
                }
            }
            if (!autorizado) {
                throw new AccessDeniedException("Apenas o responsável vinculado ao paciente pode assinar esta guia.");
            }
        }

        AssinaturaEletronicaProvider.ResultadoAssinatura resultado = assinaturaProvider.assinar(
                guia, user, dto.getSenhaConfirmacao(), ipOrigem);

        guia.setStatus(StatusGuiaFaturamento.ASSINADA);
        guia.setProvedorAssinatura(resultado.provedor());
        guia.setAssinadoPorUserId(resultado.userId());
        guia.setAssinadoPorNome(resultado.nomeAssinante());
        guia.setAssinadoPorCpf(resultado.cpfAssinante());
        guia.setDataHoraAssinatura(resultado.dataHoraAssinatura());
        guia.setIpAssinatura(resultado.ipAssinatura());
        guia.setCodigoVerificador(resultado.codigoVerificador());

        byte[] pdfAssinado = guiaPdfService.gerarPdf(guia);
        guia.setHashPdfAssinado(guiaPdfService.calcularSha256(pdfAssinado));
        guia.setCaminhoPdf(guiaPdfService.salvarEmDisco(guia.getNumeroLoteGuia() + "-assinada.pdf", pdfAssinado));

        return guiaRepository.save(guia);
    }

    @Transactional
    public GuiaFaturamentoConvenio marcarEnviadaAoConvenio(
            String idGuia,
            EnviarGuiaConvenioDTO dto,
            String emailUsuario
    ) {
        GuiaFaturamentoConvenio guia = carregarGuiaComAcessoClinica(idGuia, emailUsuario);
        if (guia.getStatus() != StatusGuiaFaturamento.ASSINADA) {
            throw new IllegalStateException("A guia precisa estar ASSINADA pelo responsável antes do envio ao convênio.");
        }

        LocalDate dataEnvio = (dto != null && dto.getDataEnvio() != null) ? dto.getDataEnvio() : LocalDate.now();
        int prazoDias = clinicaPlanoSaudeRepository
                .findByClinicaIdClinicaAndPlanoSaudeIdPlanoSaude(
                        guia.getClinica().getIdClinica(),
                        guia.getPlanoSaude().getIdPlanoSaude())
                .map(ClinicaPlanoSaude::getPrazoPagamentoDias)
                .orElse(30);

        guia.setStatus(StatusGuiaFaturamento.ENVIADA_CONVENIO);
        guia.setDataEnvioConvenio(dataEnvio);
        guia.setDataPrevisaoPagamento(dataEnvio.plusDays(prazoDias));
        return guiaRepository.save(guia);
    }

    @Transactional
    public GuiaFaturamentoConvenio registrarPagamento(
            String idGuia,
            RegistrarPagamentoGuiaDTO dto,
            String emailUsuario
    ) {
        GuiaFaturamentoConvenio guia = carregarGuiaComAcessoClinica(idGuia, emailUsuario);
        if (guia.getStatus() != StatusGuiaFaturamento.ENVIADA_CONVENIO
                && guia.getStatus() != StatusGuiaFaturamento.ASSINADA) {
            throw new IllegalStateException("Apenas guias enviadas ou assinadas podem ser baixadas como pagas.");
        }
        guia.setStatus(StatusGuiaFaturamento.PAGA);
        guia.setDataPagamento(dto != null && dto.getDataPagamento() != null ? dto.getDataPagamento() : LocalDate.now());
        return guiaRepository.save(guia);
    }

    @Transactional
    public GuiaFaturamentoConvenio cancelarGuia(String idGuia, String emailUsuario) {
        GuiaFaturamentoConvenio guia = carregarGuiaComAcessoClinica(idGuia, emailUsuario);
        if (guia.getStatus() == StatusGuiaFaturamento.PAGA) {
            throw new IllegalStateException("Não é possível cancelar uma guia já recebida/paga.");
        }
        guia.setStatus(StatusGuiaFaturamento.CANCELADA);
        return guiaRepository.save(guia);
    }

    public List<GuiaFaturamentoConvenio> listarGuias(
            String clinicaId,
            StatusGuiaFaturamento status,
            String emailUsuario
    ) {
        User user = clinicaAccessHelper.loadUserOrNull(emailUsuario);
        List<GuiaFaturamentoConvenio> base;

        if (user != null && clinicaAccessHelper.isResponsavel(user)
                && !clinicaAccessHelper.isAdmin(user) && !clinicaAccessHelper.isStaffClinica(user)) {
            List<String> idsPacientes = clinicaAccessHelper.listarIdsPacientesDoResponsavel(user);
            if (idsPacientes.isEmpty()) {
                return List.of();
            }
            base = guiaRepository.findByPacienteIdPacienteInOrderByCompetenciaFimDesc(idsPacientes).stream()
                    .filter(g -> g.getStatus() != StatusGuiaFaturamento.RASCUNHO)
                    .toList();
        } else if (clinicaId != null && !clinicaId.isBlank()) {
            Clinica clinica = clinicaAccessHelper.validarEAcessarClinica(clinicaId, emailUsuario);
            base = guiaRepository.findByClinicaIdClinicaOrderByCompetenciaFimDesc(clinica.getIdClinica());
        } else {
            base = clinicaAccessHelper.listarClinicasAcessiveis(emailUsuario).stream()
                    .flatMap(c -> guiaRepository.findByClinicaIdClinicaOrderByCompetenciaFimDesc(c.getIdClinica()).stream())
                    .toList();
        }

        if (status != null) {
            return base.stream().filter(g -> g.getStatus() == status).toList();
        }
        return base;
    }

    public byte[] baixarPdf(String idGuia, String emailUsuario) {
        GuiaFaturamentoConvenio guia = guiaRepository.findById(idGuia)
                .orElseThrow(() -> new IllegalArgumentException("Guia de faturamento não encontrada: " + idGuia));
        validarAcessoLeituraGuia(guia, emailUsuario);
        return guiaPdfService.carregarOuRegerar(guia);
    }

    public GuiaFaturamentoConvenio loadGuiaComAcesso(String idGuia, String emailUsuario) {
        GuiaFaturamentoConvenio guia = guiaRepository.findById(idGuia)
                .orElseThrow(() -> new IllegalArgumentException("Guia de faturamento não encontrada: " + idGuia));
        validarAcessoLeituraGuia(guia, emailUsuario);
        return guia;
    }

    private List<Compromisso> buscarCompromissosElegiveis(
            String idClinica,
            String idPaciente,
            LocalDate inicio,
            LocalDate fim
    ) {
        List<Compromisso> realizados = compromissoRepository
                .findByPacienteIdPacienteAndStatusAndDataHoraCompromissoBetweenOrderByDataHoraCompromissoAsc(
                        idPaciente.trim(),
                        StatusCompromisso.REALIZADO,
                        inicio.atStartOfDay(),
                        fim.atTime(LocalTime.MAX)
                );

        List<Compromisso> daClinica = realizados.stream()
                .filter(c -> {
                    Clinica clinicaComp = clinicaAccessHelper.resolverClinicaDoCompromisso(c);
                    return clinicaComp != null && idClinica.equals(clinicaComp.getIdClinica());
                })
                .toList();

        if (daClinica.isEmpty()) {
            return List.of();
        }

        List<String> ids = daClinica.stream().map(Compromisso::getIdCompromisso).toList();
        Set<String> jaFaturados = new HashSet<>(
                guiaItemRepository.findCompromissoIdsJaFaturados(ids, StatusGuiaFaturamento.CANCELADA));

        return daClinica.stream()
                .filter(c -> !jaFaturados.contains(c.getIdCompromisso()))
                .toList();
    }

    private GuiaFaturamentoConvenio carregarGuiaComAcessoClinica(String idGuia, String emailUsuario) {
        GuiaFaturamentoConvenio guia = guiaRepository.findById(idGuia)
                .orElseThrow(() -> new IllegalArgumentException("Guia de faturamento não encontrada: " + idGuia));
        clinicaAccessHelper.validarEAcessarClinica(
                guia.getClinica() != null ? guia.getClinica().getIdClinica() : null,
                emailUsuario
        );
        return guia;
    }

    private void validarAcessoLeituraGuia(GuiaFaturamentoConvenio guia, String emailUsuario) {
        User user = clinicaAccessHelper.loadUserOrNull(emailUsuario);
        if (user == null || clinicaAccessHelper.isAdmin(user)) {
            return;
        }
        if (clinicaAccessHelper.isStaffClinica(user)) {
            clinicaAccessHelper.validarEAcessarClinica(
                    guia.getClinica() != null ? guia.getClinica().getIdClinica() : null,
                    emailUsuario
            );
            return;
        }
        if (clinicaAccessHelper.isResponsavel(user) && guia.getPaciente() != null) {
            List<String> ids = clinicaAccessHelper.listarIdsPacientesDoResponsavel(user);
            if (ids.contains(guia.getPaciente().getIdPaciente())
                    && guia.getStatus() != StatusGuiaFaturamento.RASCUNHO) {
                return;
            }
        }
        throw new AccessDeniedException("Usuário sem permissão para acessar esta guia.");
    }

    private Responsavel resolverResponsavelGuia(String idResponsavel, Paciente paciente) {
        if (idResponsavel != null && !idResponsavel.isBlank()) {
            return responsavelRepository.findById(idResponsavel.trim()).orElse(null);
        }
        if (paciente.getResponsaveis() != null) {
            return paciente.getResponsaveis().stream()
                    .filter(r -> r.getAtivo() == null || r.getAtivo())
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}
