package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.*;
import com.clinica.dailyautism.domain.exception.ProfissionalNotFoundException;
import com.clinica.dailyautism.domain.repository.LancamentoRepasseRepository;
import com.clinica.dailyautism.domain.repository.ProfissionalClinicaRepository;
import com.clinica.dailyautism.domain.repository.ProfissionalRepository;
import com.clinica.dailyautism.domain.repository.TabelaPrecoClinicaRepository;
import com.clinica.dailyautism.infrastructure.dto.LancamentoRepasseDTO;
import com.clinica.dailyautism.infrastructure.dto.ResumoFinanceiroProfissionalDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LancamentoRepasseService {

    private final LancamentoRepasseRepository lancamentoRepasseRepository;
    private final ProfissionalRepository profissionalRepository;
    private final ProfissionalClinicaRepository profissionalClinicaRepository;
    private final TabelaPrecoClinicaRepository tabelaPrecoClinicaRepository;

    @Transactional
    public LancamentoRepasseProfissional gerarOuAtualizarLancamentoPorCompromisso(Compromisso compromisso) {
        if (compromisso == null || compromisso.getProfissional() == null) {
            return null;
        }

        Profissional prof = compromisso.getProfissional();
        Clinica clinica = compromisso.getClinica();
        Paciente paciente = compromisso.getPaciente();
        LocalDate dataAtendimento = compromisso.getDataHoraCompromisso() != null
                ? compromisso.getDataHoraCompromisso().toLocalDate()
                : LocalDate.now();
        LocalDate dataCompetencia = dataAtendimento.withDayOfMonth(1);

        TipoOrigemLancamento tipoOrigem;
        BigDecimal valorBruto;
        BigDecimal percentual = null;
        BigDecimal valorLiquido;

        if (clinica == null) {
            // Atendimento Particular Direto (Autônomo)
            tipoOrigem = TipoOrigemLancamento.PARTICULAR_DIRETO;
            valorBruto = prof.getValorSessaoParticular() != null
                    ? prof.getValorSessaoParticular()
                    : BigDecimal.ZERO;
            valorLiquido = valorBruto;
        } else {
            // Atendimento vinculado a uma Clínica
            Optional<ProfissionalClinica> pcOpt = profissionalClinicaRepository.findByProfissionalAndClinica(prof, clinica);
            if (pcOpt.isPresent() && pcOpt.get().getTipoRepasse() != null) {
                ProfissionalClinica pc = pcOpt.get();
                if (pc.getTipoRepasse() == TipoRepasseProfissional.VALOR_FIXO) {
                    tipoOrigem = TipoOrigemLancamento.CLINICA_FIXO;
                    valorBruto = pc.getValorRepasse() != null ? pc.getValorRepasse() : BigDecimal.ZERO;
                    valorLiquido = valorBruto;
                } else {
                    tipoOrigem = TipoOrigemLancamento.CLINICA_PERCENTUAL;
                    percentual = pc.getValorRepasse() != null ? pc.getValorRepasse() : BigDecimal.ZERO;

                    // Busca preço base na tabela da clínica para o tipo de compromisso
                    BigDecimal precoBase = BigDecimal.ZERO;
                    if (compromisso.getTipoCompromisso() != null) {
                        List<TabelaPrecoClinica> precos = tabelaPrecoClinicaRepository.findByClinicaIdClinica(clinica.getIdClinica());
                        for (TabelaPrecoClinica tp : precos) {
                            if (tp.getTipoCompromisso() != null &&
                                tp.getTipoCompromisso().getIdTipoCompromisso().equals(compromisso.getTipoCompromisso().getIdTipoCompromisso())) {
                                if (tp.getValorSessao() != null) {
                                    precoBase = tp.getValorSessao();
                                    break;
                                }
                            }
                        }
                    }
                    valorBruto = precoBase;
                    valorLiquido = valorBruto.multiply(percentual)
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                }
            } else {
                tipoOrigem = TipoOrigemLancamento.CLINICA_FIXO;
                valorBruto = BigDecimal.ZERO;
                valorLiquido = BigDecimal.ZERO;
            }
        }

        Optional<LancamentoRepasseProfissional> existente = lancamentoRepasseRepository
                .findByCompromisso_IdCompromisso(compromisso.getIdCompromisso());

        LancamentoRepasseProfissional lancamento;
        if (existente.isPresent()) {
            lancamento = existente.get();
            // Atualiza dados mantendo o status anterior caso já tenha sido pago
            lancamento.setDataAtendimento(dataAtendimento);
            lancamento.setDataCompetencia(dataCompetencia);
            lancamento.setTipoOrigem(tipoOrigem);
            lancamento.setValorBruto(valorBruto);
            lancamento.setPercentualRepasse(percentual);
            lancamento.setValorLiquidoRepasse(valorLiquido);
            if (lancamento.getStatusPagamento() == StatusLancamentoRepasse.CANCELADO) {
                lancamento.setStatusPagamento(StatusLancamentoRepasse.PENDENTE);
            }
        } else {
            lancamento = LancamentoRepasseProfissional.builder()
                    .profissional(prof)
                    .clinica(clinica)
                    .paciente(paciente)
                    .compromisso(compromisso)
                    .dataCompetencia(dataCompetencia)
                    .dataAtendimento(dataAtendimento)
                    .tipoOrigem(tipoOrigem)
                    .valorBruto(valorBruto)
                    .percentualRepasse(percentual)
                    .valorLiquidoRepasse(valorLiquido)
                    .statusPagamento(StatusLancamentoRepasse.PENDENTE)
                    .build();
        }

        return lancamentoRepasseRepository.save(lancamento);
    }

    @Transactional
    public void cancelarLancamentoPorCompromisso(String idCompromisso) {
        if (idCompromisso == null) return;
        lancamentoRepasseRepository.findByCompromisso_IdCompromisso(idCompromisso).ifPresent(l -> {
            l.setStatusPagamento(StatusLancamentoRepasse.CANCELADO);
            lancamentoRepasseRepository.save(l);
        });
    }

    @Transactional
    public LancamentoRepasseProfissional darBaixaLancamento(String idLancamento, LocalDate dataPagamento) {
        LancamentoRepasseProfissional lancamento = lancamentoRepasseRepository.findById(idLancamento)
                .orElseThrow(() -> new RuntimeException("Lançamento financeiro não encontrado: " + idLancamento));

        lancamento.setStatusPagamento(StatusLancamentoRepasse.PAGO);
        lancamento.setDataPagamento(dataPagamento != null ? dataPagamento : LocalDate.now());
        return lancamentoRepasseRepository.save(lancamento);
    }

    @Transactional
    public LancamentoRepasseProfissional reverterBaixaLancamento(String idLancamento) {
        LancamentoRepasseProfissional lancamento = lancamentoRepasseRepository.findById(idLancamento)
                .orElseThrow(() -> new RuntimeException("Lançamento financeiro não encontrado: " + idLancamento));

        lancamento.setStatusPagamento(StatusLancamentoRepasse.PENDENTE);
        lancamento.setDataPagamento(null);
        return lancamentoRepasseRepository.save(lancamento);
    }

    @Transactional
    public int darBaixaLoteClinica(String idProfissional, String idClinica, LocalDate competencia, LocalDate dataPagamento) {
        LocalDate compNormalizada = competencia.withDayOfMonth(1);
        List<LancamentoRepasseProfissional> pendentes;

        if (idClinica == null || idClinica.equalsIgnoreCase("PARTICULAR")) {
            pendentes = lancamentoRepasseRepository.findByProfissional_IdProfAndClinicaIsNullAndDataCompetencia(
                    idProfissional, compNormalizada).stream()
                    .filter(l -> l.getStatusPagamento() == StatusLancamentoRepasse.PENDENTE)
                    .toList();
        } else {
            pendentes = lancamentoRepasseRepository.findByProfissional_IdProfAndClinica_IdClinicaAndDataCompetenciaAndStatusPagamento(
                    idProfissional, idClinica, compNormalizada, StatusLancamentoRepasse.PENDENTE);
        }

        LocalDate data = dataPagamento != null ? dataPagamento : LocalDate.now();
        pendentes.forEach(l -> {
            l.setStatusPagamento(StatusLancamentoRepasse.PAGO);
            l.setDataPagamento(data);
        });

        lancamentoRepasseRepository.saveAll(pendentes);
        return pendentes.size();
    }

    public ResumoFinanceiroProfissionalDTO obterResumoMensal(String idProfissional, LocalDate competencia) {
        LocalDate compNormalizada = competencia.withDayOfMonth(1);
        List<LancamentoRepasseProfissional> lancamentos = lancamentoRepasseRepository
                .findByProfissional_IdProfAndDataCompetencia(idProfissional, compNormalizada)
                .stream()
                .filter(l -> l.getStatusPagamento() != StatusLancamentoRepasse.CANCELADO)
                .toList();

        BigDecimal totalFaturado = BigDecimal.ZERO;
        BigDecimal totalRecebido = BigDecimal.ZERO;
        BigDecimal totalPendente = BigDecimal.ZERO;

        Map<String, ResumoFinanceiroProfissionalDTO.TotalPorOrigemDTO> porOrigemMap = new LinkedHashMap<>();

        for (LancamentoRepasseProfissional l : lancamentos) {
            BigDecimal val = l.getValorLiquidoRepasse() != null ? l.getValorLiquidoRepasse() : BigDecimal.ZERO;
            totalFaturado = totalFaturado.add(val);

            if (l.getStatusPagamento() == StatusLancamentoRepasse.PAGO) {
                totalRecebido = totalRecebido.add(val);
            } else if (l.getStatusPagamento() == StatusLancamentoRepasse.PENDENTE) {
                totalPendente = totalPendente.add(val);
            }

            // Agrupa por clínica ou particular direto
            String keyId = l.getClinica() != null ? l.getClinica().getIdClinica() : "PARTICULAR";
            String keyNome = l.getClinica() != null ? l.getClinica().getNomeClinica() : "Atendimentos Particulares (Direto)";

            ResumoFinanceiroProfissionalDTO.TotalPorOrigemDTO item = porOrigemMap.computeIfAbsent(keyId, k ->
                    ResumoFinanceiroProfissionalDTO.TotalPorOrigemDTO.builder()
                            .idOrigem(k)
                            .nomeOrigem(keyNome)
                            .isParticular(l.getClinica() == null)
                            .total(BigDecimal.ZERO)
                            .totalRecebido(BigDecimal.ZERO)
                            .totalPendente(BigDecimal.ZERO)
                            .quantidadeSessoes(0)
                            .build()
            );

            item.setTotal(item.getTotal().add(val));
            item.setQuantidadeSessoes(item.getQuantidadeSessoes() + 1);
            if (l.getStatusPagamento() == StatusLancamentoRepasse.PAGO) {
                item.setTotalRecebido(item.getTotalRecebido().add(val));
            } else {
                item.setTotalPendente(item.getTotalPendente().add(val));
            }
        }

        Profissional prof = profissionalRepository.findById(idProfissional).orElse(null);
        BigDecimal valorParticularAtual = prof != null ? prof.getValorSessaoParticular() : null;

        return ResumoFinanceiroProfissionalDTO.builder()
                .competencia(compNormalizada.toString().substring(0, 7)) // YYYY-MM
                .totalFaturado(totalFaturado)
                .totalRecebido(totalRecebido)
                .totalPendente(totalPendente)
                .quantidadeTotalSessoes(lancamentos.size())
                .valorSessaoParticularPadrao(valorParticularAtual)
                .totaisPorOrigem(new ArrayList<>(porOrigemMap.values()))
                .build();
    }

    public List<LancamentoRepasseDTO> filtrarLancamentos(
            String idProfissional, LocalDate competencia, String idClinica, StatusLancamentoRepasse status) {
        LocalDate compNormalizada = competencia.withDayOfMonth(1);
        List<LancamentoRepasseProfissional> lista = lancamentoRepasseRepository
                .filtrarLancamentos(idProfissional, compNormalizada, idClinica, status);

        return lista.stream()
                .map(LancamentoRepasseDTO::create)
                .toList();
    }

    @Transactional
    public BigDecimal configurarValorParticular(String idProfissional, BigDecimal valor) {
        Profissional prof = profissionalRepository.findById(idProfissional)
                .orElseThrow(() -> new ProfissionalNotFoundException("Profissional não encontrado: " + idProfissional));
        prof.setValorSessaoParticular(valor);
        profissionalRepository.save(prof);
        return valor;
    }

    @Transactional
    public ProfissionalClinica configurarAcordoRepasse(
            String idClinica, String idProfissional, TipoRepasseProfissional tipo, BigDecimal valor) {
        Profissional prof = profissionalRepository.findById(idProfissional)
                .orElseThrow(() -> new ProfissionalNotFoundException("Profissional não encontrado: " + idProfissional));

        List<ProfissionalClinica> pcs = profissionalClinicaRepository.findByProfissionalIdProf(idProfissional);
        ProfissionalClinica pc = pcs.stream()
                .filter(p -> p.getClinica() != null && p.getClinica().getIdClinica().equals(idClinica))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Vínculo do profissional com a clínica não encontrado."));

        pc.setTipoRepasse(tipo);
        pc.setValorRepasse(valor);
        return profissionalClinicaRepository.save(pc);
    }
}
