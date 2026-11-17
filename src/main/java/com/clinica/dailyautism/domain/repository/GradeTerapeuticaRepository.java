package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.GradeTerapeutica;
import com.clinica.dailyautism.domain.entity.StatusGradeTerapeutica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GradeTerapeuticaRepository extends JpaRepository<GradeTerapeutica, String> {

    List<GradeTerapeutica> findByPacienteIdPacienteOrderByNumeroVersaoDesc(String idPaciente);

    List<GradeTerapeutica> findByPacienteIdPacienteAndStatus(String idPaciente, StatusGradeTerapeutica status);

    Optional<GradeTerapeutica> findFirstByPacienteIdPacienteAndStatusOrderByNumeroVersaoDesc(
            String idPaciente,
            StatusGradeTerapeutica status
    );

    List<GradeTerapeutica> findByClinicaIdClinicaOrderByDataEnvioDesc(String idClinica);

    List<GradeTerapeutica> findByClinicaIdClinicaAndStatusOrderByDataEnvioDesc(String idClinica, StatusGradeTerapeutica status);

    List<GradeTerapeutica> findByPacienteIdPacienteInAndStatusOrderByDataEnvioDesc(
            List<String> pacienteIds,
            StatusGradeTerapeutica status
    );

    List<GradeTerapeutica> findByPacienteIdPacienteInOrderByDataEnvioDesc(List<String> pacienteIds);

    @Query("SELECT g FROM GradeTerapeutica g LEFT JOIN FETCH g.itens WHERE g.idGradeTerapeutica = :id")
    Optional<GradeTerapeutica> findByIdWithItens(@Param("id") String id);
}
