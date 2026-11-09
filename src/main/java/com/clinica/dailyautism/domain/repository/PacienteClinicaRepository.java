package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.Clinica;
import com.clinica.dailyautism.domain.entity.Paciente;
import com.clinica.dailyautism.domain.entity.PacienteClinica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PacienteClinicaRepository extends JpaRepository<PacienteClinica, String> {

    List<PacienteClinica> findByPacienteIdPaciente(String idPaciente);

    List<PacienteClinica> findByClinicaIdClinica(String idClinica);

    Optional<PacienteClinica> findByPacienteAndClinica(Paciente paciente, Clinica clinica);

    boolean existsByPacienteAndClinica(Paciente paciente, Clinica clinica);
}
