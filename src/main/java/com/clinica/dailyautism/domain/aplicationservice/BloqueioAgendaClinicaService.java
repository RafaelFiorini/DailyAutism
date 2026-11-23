package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.*;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.repository.BloqueioAgendaClinicaRepository;
import com.clinica.dailyautism.domain.repository.CompromissoRepository;
import com.clinica.dailyautism.infrastructure.dto.BloqueioAgendaClinicaDTO;
import com.clinica.dailyautism.infrastructure.dto.SaveBloqueioAgendaClinicaDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BloqueioAgendaClinicaService {

    private final BloqueioAgendaClinicaRepository bloqueioRepository;
    private final CompromissoRepository compromissoRepository;
    private final ClinicaAccessHelper clinicaAccessHelper;
    private final ContratoParticularService contratoParticularService;
    private final LancamentoRepasseService lancamentoRepasseService;
    private final AuditoriaService auditoriaService;

    public List<BloqueioAgendaClinicaDTO> listarPorUsuario(String emailUsuario, String idClinicaFiltro) {
        User user = clinicaAccessHelper.loadUserOrNull(emailUsuario);
        if (user == null || clinicaAccessHelper.isAdmin(user)) {
            if (idClinicaFiltro != null && !idClinicaFiltro.isBlank()) {
                return bloqueioRepository.findByClinicaIdClinicaOrderByDataInicioAsc(idClinicaFiltro.trim())
                        .stream()
                        .map(BloqueioAgendaClinicaDTO::create)
                        .toList();
            }
            return bloqueioRepository.findAllByOrderByDataInicioAsc()
                    .stream()
                    .map(BloqueioAgendaClinicaDTO::create)
                    .toList();
        }

        List<String> clinicaIds = clinicaAccessHelper.listarIdsClinicasDoUsuario(user);
        if (clinicaIds.isEmpty()) {
            return List.of();
        }

        if (idClinicaFiltro != null && !idClinicaFiltro.isBlank()) {
            if (!clinicaIds.contains(idClinicaFiltro.trim())) {
                throw new AccessDeniedException("Usuário sem permissão para visualizar feriados/recessos desta clínica.");
            }
            return bloqueioRepository.findByClinicaIdClinicaOrderByDataInicioAsc(idClinicaFiltro.trim())
                    .stream()
                    .map(BloqueioAgendaClinicaDTO::create)
                    .toList();
        }

        return bloqueioRepository.findByClinicaIdClinicaInOrderByDataInicioAsc(clinicaIds)
                .stream()
                .map(BloqueioAgendaClinicaDTO::create)
                .toList();
    }

    @Transactional
    public BloqueioAgendaClinicaDTO criarBloqueio(SaveBloqueioAgendaClinicaDTO dto, String emailUsuario) {
        if (dto.getDataInicio() == null || dto.getDataFim() == null) {
            throw new IllegalArgumentException("Informe as datas de início e término do bloqueio.");
        }
        if (dto.getDataFim().isBefore(dto.getDataInicio())) {
            throw new IllegalArgumentException("A data de término não pode ser anterior à data de início.");
        }
        if ((dto.getHoraInicio() != null && dto.getHoraFim() == null) ||
                (dto.getHoraInicio() == null && dto.getHoraFim() != null)) {
            throw new IllegalArgumentException("Informe tanto a hora de início quanto a hora de término, ou deixe ambas em branco para o dia inteiro.");
        }
        if (dto.getHoraInicio() != null && !dto.getHoraFim().isAfter(dto.getHoraInicio())) {
            throw new IllegalArgumentException("A hora de término deve ser posterior à hora de início.");
        }

        Clinica clinica = clinicaAccessHelper.validarEAcessarClinica(dto.getIdClinica(), emailUsuario);
        User user = clinicaAccessHelper.loadUserOrNull(emailUsuario);

        BloqueioAgendaClinica bloqueio = BloqueioAgendaClinica.builder()
                .clinica(clinica)
                .titulo(dto.getTitulo().trim())
                .tipo(dto.getTipo() != null ? dto.getTipo() : TipoBloqueioAgenda.FERIADO)
                .dataInicio(dto.getDataInicio())
                .dataFim(dto.getDataFim())
                .horaInicio(dto.getHoraInicio())
                .horaFim(dto.getHoraFim())
                .observacao(dto.getObservacao() != null && !dto.getObservacao().isBlank() ? dto.getObservacao().trim() : null)
                .criadoPor(user)
                .build();
        if (bloqueio.getAtivo() == null) {
            bloqueio.setAtivo(true);
        }

        BloqueioAgendaClinica saved = bloqueioRepository.save(bloqueio);

        int suspensos = 0;
        if (!Boolean.FALSE.equals(dto.getSuspenderAgendamentosExistentes())) {
            suspensos = suspenderCompromissosNoPeriodo(saved);
        }

        if (user != null) {
            auditoriaService.registrarAcaoAsync(
                    user.getIdUser(),
                    user.getNomeUser(),
                    user.getEmailUser(),
                    clinicaAccessHelper.isAdmin(user) ? "ADMIN" : "ADMIN_CLINICA",
                    clinica.getIdClinica(),
                    CategoriaAuditoria.GRADE_TERAPEUTICA,
                    AcaoAuditoria.CRIACAO,
                    "BloqueioAgendaClinica",
                    saved.getIdBloqueio(),
                    "Criou bloqueio de agenda '" + saved.getTitulo() + "' (" + saved.getDataInicio() + " a " + saved.getDataFim() + ") com " + suspensos + " agendamento(s) suspenso(s)",
                    null,
                    null
            );
        }

        return BloqueioAgendaClinicaDTO.create(saved, suspensos);
    }

    @Transactional
    public List<BloqueioAgendaClinicaDTO> importarEmLote(List<SaveBloqueioAgendaClinicaDTO> itens, String emailUsuario) {
        if (itens == null || itens.isEmpty()) {
            return List.of();
        }
        List<BloqueioAgendaClinicaDTO> resultados = new ArrayList<>();
        for (SaveBloqueioAgendaClinicaDTO item : itens) {
            resultados.add(criarBloqueio(item, emailUsuario));
        }
        return resultados;
    }

    @Transactional
    public void removerBloqueio(String idBloqueio, String emailUsuario) {
        BloqueioAgendaClinica bloqueio = bloqueioRepository.findById(idBloqueio)
                .orElseThrow(() -> new IllegalArgumentException("Bloqueio de agenda não encontrado: " + idBloqueio));

        if (bloqueio.getClinica() != null) {
            clinicaAccessHelper.validarEAcessarClinica(bloqueio.getClinica().getIdClinica(), emailUsuario);
        }

        bloqueio.desativar();
        bloqueioRepository.save(bloqueio);

        User user = clinicaAccessHelper.loadUserOrNull(emailUsuario);
        if (user != null) {
            auditoriaService.registrarAcaoAsync(
                    user.getIdUser(),
                    user.getNomeUser(),
                    user.getEmailUser(),
                    clinicaAccessHelper.isAdmin(user) ? "ADMIN" : "ADMIN_CLINICA",
                    bloqueio.getClinica() != null ? bloqueio.getClinica().getIdClinica() : null,
                    CategoriaAuditoria.GRADE_TERAPEUTICA,
                    AcaoAuditoria.EXCLUSAO,
                    "BloqueioAgendaClinica",
                    bloqueio.getIdBloqueio(),
                    "Removeu bloqueio de agenda '" + bloqueio.getTitulo() + "'",
                    null,
                    null
            );
        }
    }

    /**
     * Suspende (cancela) todos os compromissos futuros/pendentes da clínica que colidem com o bloqueio,
     * preservando integralmente o saldo de sessões de guias e contratos particulares.
     */
    private int suspenderCompromissosNoPeriodo(BloqueioAgendaClinica bloqueio) {
        if (bloqueio.getClinica() == null || bloqueio.getClinica().getIdClinica() == null) {
            return 0;
        }
        LocalDateTime inicioDia = bloqueio.getDataInicio().atStartOfDay();
        LocalDateTime fimDia = bloqueio.getDataFim().atTime(LocalTime.MAX);

        List<Compromisso> candidatos = compromissoRepository
                .findByClinicaIdClinicaAndDataHoraCompromissoBetweenOrderByDataHoraCompromissoAsc(
                        bloqueio.getClinica().getIdClinica(),
                        inicioDia,
                        fimDia
                );

        int suspensos = 0;
        String motivo = "Suspenso automaticamente (" + bloqueio.getTitulo() + ") — saldo de sessões preservado";

        for (Compromisso c : candidatos) {
            StatusCompromisso statusAnterior = c.getStatus();
            boolean elegivel = statusAnterior == StatusCompromisso.AGENDADO
                    || statusAnterior == StatusCompromisso.AGUARDANDO_APROVACAO
                    || statusAnterior == StatusCompromisso.PENDENTE_REAGENDAMENTO;

            if (elegivel && bloqueio.conflitaCom(c.getDataHoraCompromisso())) {
                c.setStatus(StatusCompromisso.CANCELADO);
                c.setDescricaoCompromisso(
                        c.getDescricaoCompromisso() == null || c.getDescricaoCompromisso().isBlank()
                                ? motivo
                                : c.getDescricaoCompromisso() + " | " + motivo
                );
                compromissoRepository.save(c);
                contratoParticularService.processarAlteracaoStatusCompromisso(c, statusAnterior, StatusCompromisso.CANCELADO);
                lancamentoRepasseService.cancelarLancamentoPorCompromisso(c.getIdCompromisso());
                suspensos++;
            }
        }

        return suspensos;
    }

    public List<BloqueioAgendaClinica> buscarBloqueiosAtivosNoPeriodo(String idClinica, LocalDate dataInicio, LocalDate dataFim) {
        if (idClinica == null || idClinica.isBlank() || dataInicio == null || dataFim == null) {
            return List.of();
        }
        return bloqueioRepository.findConflitantesPorPeriodo(idClinica, dataInicio, dataFim);
    }

    public Optional<BloqueioAgendaClinica> buscarBloqueioConflitante(String idClinica, LocalDateTime dataHora) {
        if (idClinica == null || idClinica.isBlank() || dataHora == null) {
            return Optional.empty();
        }
        LocalDate data = dataHora.toLocalDate();
        return bloqueioRepository.findConflitantesPorPeriodo(idClinica, data, data)
                .stream()
                .filter(b -> b.conflitaCom(dataHora))
                .findFirst();
    }
}
