package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.GuiaFaturamentoConvenio;
import com.clinica.dailyautism.domain.entity.StatusGuiaFaturamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuiaFaturamentoConvenioRepository extends JpaRepository<GuiaFaturamentoConvenio, String> {

    List<GuiaFaturamentoConvenio> findByClinicaIdClinicaOrderByCompetenciaFimDesc(String idClinica);

    List<GuiaFaturamentoConvenio> findByPacienteIdPacienteInAndStatusInOrderByCompetenciaFimDesc(
            List<String> idsPacientes,
            List<StatusGuiaFaturamento> statuses
    );

    List<GuiaFaturamentoConvenio> findByPacienteIdPacienteInOrderByCompetenciaFimDesc(List<String> idsPacientes);

    long countByClinicaIdClinica(String idClinica);

    List<GuiaFaturamentoConvenio> findByClinicaIdClinicaAndPlanoSaudeIdPlanoSaudeAndStatus(
            String idClinica,
            String idPlanoSaude,
            StatusGuiaFaturamento status
    );

    List<GuiaFaturamentoConvenio> findByClinicaIdClinicaAndStatus(
            String idClinica,
            StatusGuiaFaturamento status
    );
}
