package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.Pessoa;
import com.clinica.dailyautism.domain.entity.Profissional;
import com.clinica.dailyautism.domain.entity.Compromisso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProfissionalRepository extends JpaRepository<Profissional, String> {

    Optional<Profissional> findByPessoaProf(Pessoa pessoa);
    Optional<Profissional> findByPessoaProfEmailPessoa(String email);
    boolean existsByPessoaProf(Pessoa pessoa);

    @Query(value = "SELECT * FROM profissional WHERE pessoa_prof = :idPessoa LIMIT 1", nativeQuery = true)
    Optional<Profissional> findByPessoaIdAnyAtivo(@Param("idPessoa") String idPessoa);

    // Agenda da semana do profissional
    @Query("""
        SELECT c FROM Compromisso c
        WHERE c.paciente.idPaciente IN (
            SELECT pp.paciente.idPaciente FROM ProfissionalPaciente pp
            WHERE pp.profissional.idProf = :idProf
        )
        AND c.dataHoraCompromisso BETWEEN :inicio AND :fim
        ORDER BY c.dataHoraCompromisso ASC
    """)
    List<Compromisso> findAgendaSemanaByProfissional(
            @Param("idProf") String idProf,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );
}