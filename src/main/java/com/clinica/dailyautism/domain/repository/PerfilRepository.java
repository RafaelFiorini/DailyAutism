package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.security.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerfilRepository extends JpaRepository<Perfil,String>{
    Optional<Perfil> findByNome(String nome);
}
