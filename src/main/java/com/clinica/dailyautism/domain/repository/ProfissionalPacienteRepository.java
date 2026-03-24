package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.ProfissionalPaciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProfissionalPacienteRepository extends JpaRepository<ProfissionalPaciente, String> {

    // Todos os pacientes de um profissional
    List<ProfissionalPaciente> findByProfissionalIdProf(String idProf);

    // Todos os profissionais de um paciente
    List<ProfissionalPaciente> findByPacienteIdPaciente(String idPaciente);

    // Vínculo específico entre profissional e paciente
    boolean existsByProfissionalIdProfAndPacienteIdPaciente(String idProf, String idPaciente);
}