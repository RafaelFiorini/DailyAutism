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

    @Query(value = "SELECT * FROM paciente WHERE ativo = false", nativeQuery = true)
    List<Paciente> findAllInactive();

    @Query(value = "SELECT * FROM paciente WHERE id_paciente = :id LIMIT 1", nativeQuery = true)
    Optional<Paciente> findByIdAnyAtivo(@Param("id") String id);

    @Query(value = "SELECT * FROM paciente WHERE pessoa_paciente = :idPessoa LIMIT 1", nativeQuery = true)
    Optional<Paciente> findByPessoaIdAnyAtivo(@Param("idPessoa") String idPessoa);
}