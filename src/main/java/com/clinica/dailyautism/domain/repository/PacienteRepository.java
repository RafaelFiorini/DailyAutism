package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.Paciente;
import com.clinica.dailyautism.domain.entity.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, String> {

    Optional<Paciente> findByPessoaPaciente(Pessoa pessoa);
    Optional<Paciente> findByPessoaPacienteCPFPessoa(String cpf);

    @Query("SELECT DISTINCT p FROM Paciente p JOIN p.clinicas pc WHERE pc.clinica.idClinica IN :clinicaIds")
    List<Paciente> findByClinicaIds(@Param("clinicaIds") List<String> clinicaIds);
}