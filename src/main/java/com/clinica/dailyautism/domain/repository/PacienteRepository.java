package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.Paciente;
import com.clinica.dailyautism.domain.entity.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, String> {

    Optional<Paciente> findByPessoaPaciente(Pessoa pessoa);
    Optional<Paciente> findByPessoaPacienteCPFPessoa(String cpf);
}