package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.Clinica;
import com.clinica.dailyautism.domain.entity.FuncionarioClinica;
import com.clinica.dailyautism.domain.entity.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FuncionarioClinicaRepository extends JpaRepository<FuncionarioClinica, String> {

    List<FuncionarioClinica> findByPessoaIdPessoa(String idPessoa);

    List<FuncionarioClinica> findByPessoa(Pessoa pessoa);

    List<FuncionarioClinica> findByClinicaIdClinica(String idClinica);

    Optional<FuncionarioClinica> findByPessoaAndClinica(Pessoa pessoa, Clinica clinica);

    boolean existsByPessoaAndClinica(Pessoa pessoa, Clinica clinica);

    void deleteByPessoa(Pessoa pessoa);
}
