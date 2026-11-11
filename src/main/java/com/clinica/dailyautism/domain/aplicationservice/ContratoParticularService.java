package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.*;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.exception.PacienteNotFoundException;
import com.clinica.dailyautism.domain.exception.TipoCompromissoNotFoundException;
import com.clinica.dailyautism.domain.repository.*;
import com.clinica.dailyautism.infrastructure.dto.RegistrarPagamentoContratoDTO;
import com.clinica.dailyautism.infrastructure.dto.SaveContratoParticularDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContratoParticularService {

    private final ContratoParticularRepository contratoRepository;
    private final MovimentacaoContratoParticularRepository movimentacaoRepository;
    private final PacienteRepository pacienteRepository;
    private final ResponsavelRepository responsavelRepository;
    private final PacoteParticularRepository pacoteRepository;
    private final TipoCompromissoRepository tipoCompromissoRepository;
    private final TabelaPrecoService tabelaPrecoService;
    private final ClinicaAccessHelper clinicaAccessHelper;

    @Transactional
    public ContratoParticular criarContrato(SaveContratoParticularDTO dto, String emailUsuario) {
        Clinica clinica = clinicaAccessHelper.validarEAcessarClinica(dto.getIdClinica(), emailUsuario);
        Paciente paciente = pacienteRepository.findById(dto.getIdPaciente().trim())
                .orElseThrow(() -> new PacienteNotFoundException(dto.getIdPaciente()));

        Responsavel responsavel = resolverResponsavel(dto.getIdResponsavel(), paciente);

        PacoteParticular pacoteOrigem = null;
        if (dto.getIdPacote() != null && !dto.getIdPacote().isBlank()) {
            pacoteOrigem = pacoteRepository.findById(dto.getIdPacote().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Pacote não encontrado: " + dto.getIdPacote()));
        }

        int multiplicador = (dto.getMultiplicadorMeses() != null && dto.getMultiplicadorMeses() > 0)
                ? dto.getMultiplicadorMeses()
                : 1;

        ModalidadeContratoParticular modalidade = dto.getModalidade() != null
                ? dto.getModalidade()
                : ModalidadeContratoParticular.POR_SESSOES;

        LocalDate dataInicio = dto.getDataInicio() != null ? dto.getDataInicio() : LocalDate.now();
        LocalDate dataFimPrevista = dto.getDataFimPrevista();
        if (dataFimPrevista == null && pacoteOrigem != null && pacoteOrigem.getValidadeMesesSugerida() != null) {
            dataFimPrevista = dataInicio.plusMonths((long) pacoteOrigem.getValidadeMesesSugerida() * multiplicador);
        } else if (dataFimPrevista == null && modalidade == ModalidadeContratoParticular.POR_PERIODO_MENSAL) {
            dataFimPrevista = dataInicio.plusMonths(multiplicador);
        }

        String descricao = (dto.getDescricao() != null && !dto.getDescricao().isBlank())
                ? dto.getDescricao().trim()
                : (pacoteOrigem != null
                        ? (multiplicador > 1 ? pacoteOrigem.getNomePacote() + " (" + multiplicador + "x)" : pacoteOrigem.getNomePacote())
                        : "Contrato Particular");

        ContratoParticular contrato = ContratoParticular.builder()
                .clinica(clinica)
                .paciente(paciente)
                .responsavel(responsavel)
                .pacoteOrigem(pacoteOrigem)
                .descricao(descricao)
                .modalidade(modalidade)
                .dataInicio(dataInicio)
                .dataFimPrevista(dataFimPrevista)
                .valorCobrado(BigDecimal.ZERO)
                .statusContrato(StatusContratoParticular.ATIVO)
                .itens(new ArrayList<>())
                .build();

        BigDecimal somaTabela = BigDecimal.ZERO;
        if (dto.getItens() != null && !dto.getItens().isEmpty()) {
            for (SaveContratoParticularDTO.SaveContratoItemDTO itemDto : dto.getItens()) {
                TipoCompromisso tipo = tipoCompromissoRepository.findById(itemDto.getIdTipoCompromisso().trim())
                        .orElseThrow(() -> new TipoCompromissoNotFoundException(
                                "Especialidade não encontrada: " + itemDto.getIdTipoCompromisso()));
                int qtdFinal = itemDto.getQuantidadeContratada() * multiplicador;
                BigDecimal unitario = tabelaPrecoService.resolverValorParticular(clinica.getIdClinica(), tipo.getIdTipoCompromisso());
                somaTabela = somaTabela.add(unitario.multiply(BigDecimal.valueOf(qtdFinal)));
                contrato.getItens().add(ContratoParticularItem.builder()
                        .contrato(contrato)
                        .tipoCompromisso(tipo)
                        .quantidadeContratada(qtdFinal)
                        .quantidadeConsumida(0)
                        .valorUnitarioReferencia(unitario)
                        .build());
            }
        } else if (pacoteOrigem != null && pacoteOrigem.getItens() != null && !pacoteOrigem.getItens().isEmpty()) {
            for (PacoteParticularItem itemPac : pacoteOrigem.getItens()) {
                if (itemPac.getAtivo() != null && !itemPac.getAtivo()) continue;
                int qtdFinal = itemPac.getQuantidadeSessoes() * multiplicador;
                BigDecimal unitario = tabelaPrecoService.resolverValorParticular(
                        clinica.getIdClinica(), itemPac.getTipoCompromisso().getIdTipoCompromisso());
                somaTabela = somaTabela.add(unitario.multiply(BigDecimal.valueOf(qtdFinal)));
                contrato.getItens().add(ContratoParticularItem.builder()
                        .contrato(contrato)
                        .tipoCompromisso(itemPac.getTipoCompromisso())
                        .quantidadeContratada(qtdFinal)
                        .quantidadeConsumida(0)
                        .valorUnitarioReferencia(unitario)
                        .build());
            }
        } else {
            throw new IllegalArgumentException("Informe os itens por especialidade ou selecione um pacote válido.");
        }

        BigDecimal valorFinal;
        if (dto.getValorCobrado() != null && dto.getValorCobrado().compareTo(BigDecimal.ZERO) > 0) {
            valorFinal = dto.getValorCobrado();
        } else if (pacoteOrigem != null && pacoteOrigem.getValorTotalPacote() != null) {
            valorFinal = pacoteOrigem.getValorTotalPacote().multiply(BigDecimal.valueOf(multiplicador));
        } else {
            valorFinal = somaTabela;
        }
        contrato.setValorCobrado(valorFinal);

        if (Boolean.TRUE.equals(dto.getPagoNaContratacao())) {
            contrato.setStatusPagamento(StatusPagamentoContrato.PAGO);
            contrato.setDataPagamento(dto.getDataPagamento() != null ? dto.getDataPagamento() : LocalDate.now());
            contrato.setFormaPagamento(dto.getFormaPagamento());
        } else {
            contrato.setStatusPagamento(StatusPagamentoContrato.PENDENTE);
        }

        return contratoRepository.save(contrato);
    }

    @Transactional
    public ContratoParticular registrarPagamento(String idContrato, RegistrarPagamentoContratoDTO dto, String emailUsuario) {
        ContratoParticular contrato = carregarValidandoClinica(idContrato, emailUsuario);
        if (contrato.getStatusContrato() == StatusContratoParticular.CANCELADO) {
            throw new IllegalStateException("Não é possível registrar pagamento em um contrato cancelado.");
        }
        contrato.setStatusPagamento(StatusPagamentoContrato.PAGO);
        contrato.setDataPagamento(dto != null && dto.getDataPagamento() != null ? dto.getDataPagamento() : LocalDate.now());
        contrato.setFormaPagamento(dto != null ? dto.getFormaPagamento() : null);
        return contratoRepository.save(contrato);
    }

    @Transactional
    public ContratoParticular cancelarContrato(String idContrato, String emailUsuario) {
        ContratoParticular contrato = carregarValidandoClinica(idContrato, emailUsuario);
        contrato.setStatusContrato(StatusContratoParticular.CANCELADO);
        contrato.setStatusPagamento(StatusPagamentoContrato.CANCELADO);
        return contratoRepository.save(contrato);
    }

    public List<ContratoParticular> listarParaUsuario(String clinicaId, String pacienteId, String emailUsuario) {
        User user = clinicaAccessHelper.loadUserOrNull(emailUsuario);
        if (user != null && clinicaAccessHelper.isResponsavel(user)
                && !clinicaAccessHelper.isAdmin(user) && !clinicaAccessHelper.isStaffClinica(user)) {
            List<String> idsPacientes = clinicaAccessHelper.listarIdsPacientesDoResponsavel(user);
            if (idsPacientes.isEmpty()) {
                return List.of();
            }
            List<ContratoParticular> contratos = contratoRepository.findByPacienteIdPacienteInOrderByDataInicioDesc(idsPacientes);
            if (pacienteId != null && !pacienteId.isBlank()) {
                return contratos.stream()
                        .filter(c -> c.getPaciente() != null && pacienteId.equals(c.getPaciente().getIdPaciente()))
                        .toList();
            }
            return contratos;
        }

        List<ContratoParticular> base;
        if (clinicaId != null && !clinicaId.isBlank()) {
            Clinica clinica = clinicaAccessHelper.validarEAcessarClinica(clinicaId, emailUsuario);
            base = contratoRepository.findByClinicaIdClinicaOrderByDataInicioDesc(clinica.getIdClinica());
        } else {
            base = clinicaAccessHelper.listarClinicasAcessiveis(emailUsuario).stream()
                    .flatMap(c -> contratoRepository.findByClinicaIdClinicaOrderByDataInicioDesc(c.getIdClinica()).stream())
                    .toList();
        }

        if (pacienteId != null && !pacienteId.isBlank()) {
            return base.stream()
                    .filter(c -> c.getPaciente() != null && pacienteId.equals(c.getPaciente().getIdPaciente()))
                    .toList();
        }
        return base;
    }

    /**
     * Abate automaticamente uma sessão quando um compromisso passa para REALIZADO,
     * ou estorna quando um compromisso REALIZADO é revertido.
     * Nunca bloqueia a marcação clínica de REALIZADO caso não haja contrato ativo com saldo.
     */
    @Transactional
    public void processarAlteracaoStatusCompromisso(
            Compromisso compromisso,
            StatusCompromisso statusAnterior,
            StatusCompromisso novoStatus
    ) {
        if (compromisso == null || novoStatus == null) {
            return;
        }

        if (novoStatus == StatusCompromisso.REALIZADO) {
            abaterSessaoSeDisponivel(compromisso);
        } else if (statusAnterior == StatusCompromisso.REALIZADO) {
            estornarSessaoSeAbatida(compromisso);
        }
    }

    private void abaterSessaoSeDisponivel(Compromisso compromisso) {
        if (saldoLiquidoMovimentado(compromisso.getIdCompromisso()) > 0) {
            return; // Idempotente: já existe abatimento ativo para este compromisso
        }
        if (compromisso.getPaciente() == null || compromisso.getTipoCompromisso() == null) {
            return;
        }
        Clinica clinica = clinicaAccessHelper.resolverClinicaDoCompromisso(compromisso);
        if (clinica == null) {
            return;
        }

        List<ContratoParticular> contratosAtivos = contratoRepository
                .findByPacienteIdPacienteAndClinicaIdClinicaAndStatusContratoOrderByDataInicioAsc(
                        compromisso.getPaciente().getIdPaciente(),
                        clinica.getIdClinica(),
                        StatusContratoParticular.ATIVO
                );

        String idTipo = compromisso.getTipoCompromisso().getIdTipoCompromisso();
        for (ContratoParticular contrato : contratosAtivos) {
            if (contrato.getItens() == null) continue;
            for (ContratoParticularItem item : contrato.getItens()) {
                if ((item.getAtivo() == null || item.getAtivo())
                        && item.getTipoCompromisso() != null
                        && idTipo.equals(item.getTipoCompromisso().getIdTipoCompromisso())
                        && item.getQuantidadeRestante() > 0) {

                    int consumidaAtual = item.getQuantidadeConsumida() != null ? item.getQuantidadeConsumida() : 0;
                    item.setQuantidadeConsumida(consumidaAtual + 1);

                    MovimentacaoContratoParticular mov = MovimentacaoContratoParticular.builder()
                            .contratoItem(item)
                            .compromisso(compromisso)
                            .tipoMovimentacao(TipoMovimentacaoContrato.ABATIMENTO)
                            .quantidade(1)
                            .dataMovimentacao(LocalDateTime.now())
                            .observacao("Abatimento automático por atendimento realizado")
                            .build();
                    movimentacaoRepository.save(mov);

                    boolean todosConsumidos = contrato.getItens().stream()
                            .filter(i -> i.getAtivo() == null || i.getAtivo())
                            .allMatch(i -> i.getQuantidadeRestante() == 0);
                    if (todosConsumidos) {
                        contrato.setStatusContrato(StatusContratoParticular.CONCLUIDO);
                    }
                    contratoRepository.save(contrato);
                    return;
                }
            }
        }
    }

    private void estornarSessaoSeAbatida(Compromisso compromisso) {
        if (saldoLiquidoMovimentado(compromisso.getIdCompromisso()) <= 0) {
            return;
        }
        var optUltimoAbatimento = movimentacaoRepository
                .findByCompromissoIdCompromissoOrderByDataMovimentacaoDesc(compromisso.getIdCompromisso())
                .stream()
                .filter(m -> m.getTipoMovimentacao() == TipoMovimentacaoContrato.ABATIMENTO)
                .findFirst();

        if (optUltimoAbatimento.isEmpty()) {
            return;
        }

        ContratoParticularItem item = optUltimoAbatimento.get().getContratoItem();
        if (item == null) {
            return;
        }

        int consumidaAtual = item.getQuantidadeConsumida() != null ? item.getQuantidadeConsumida() : 0;
        item.setQuantidadeConsumida(Math.max(0, consumidaAtual - 1));

        MovimentacaoContratoParticular estorno = MovimentacaoContratoParticular.builder()
                .contratoItem(item)
                .compromisso(compromisso)
                .tipoMovimentacao(TipoMovimentacaoContrato.ESTORNO)
                .quantidade(1)
                .dataMovimentacao(LocalDateTime.now())
                .observacao("Estorno automático por alteração de status do atendimento")
                .build();
        movimentacaoRepository.save(estorno);

        ContratoParticular contrato = item.getContrato();
        if (contrato != null) {
            if (contrato.getStatusContrato() == StatusContratoParticular.CONCLUIDO) {
                contrato.setStatusContrato(StatusContratoParticular.ATIVO);
            }
            contratoRepository.save(contrato);
        }
    }

    private int saldoLiquidoMovimentado(String idCompromisso) {
        if (idCompromisso == null) return 0;
        List<MovimentacaoContratoParticular> movs = movimentacaoRepository
                .findByCompromissoIdCompromissoOrderByDataMovimentacaoDesc(idCompromisso);
        int saldo = 0;
        for (MovimentacaoContratoParticular m : movs) {
            int qtd = m.getQuantidade() != null ? m.getQuantidade() : 0;
            if (m.getTipoMovimentacao() == TipoMovimentacaoContrato.ABATIMENTO) {
                saldo += qtd;
            } else if (m.getTipoMovimentacao() == TipoMovimentacaoContrato.ESTORNO) {
                saldo -= qtd;
            }
        }
        return saldo;
    }

    private ContratoParticular carregarValidandoClinica(String idContrato, String emailUsuario) {
        ContratoParticular contrato = contratoRepository.findById(idContrato)
                .orElseThrow(() -> new IllegalArgumentException("Contrato particular não encontrado: " + idContrato));
        String idClinica = contrato.getClinica() != null ? contrato.getClinica().getIdClinica() : null;
        try {
            clinicaAccessHelper.validarEAcessarClinica(idClinica, emailUsuario);
        } catch (AccessDeniedException ex) {
            throw ex;
        }
        return contrato;
    }

    private Responsavel resolverResponsavel(String idResponsavel, Paciente paciente) {
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
