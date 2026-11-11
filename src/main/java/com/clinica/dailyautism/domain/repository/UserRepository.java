package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.security.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmailUser(String emailUser);

    @Query(value = "SELECT * FROM user WHERE ativo = false ORDER BY nome_user ASC", nativeQuery = true)
    List<User> findAllInactive();

    @Query(value = "SELECT * FROM user WHERE id_user = :id LIMIT 1", nativeQuery = true)
    Optional<User> findByIdAnyAtivo(@Param("id") String id);

    @Query(value = "SELECT * FROM user WHERE LOWER(email_user) = LOWER(:email) LIMIT 1", nativeQuery = true)
    Optional<User> findByEmailAnyAtivo(@Param("email") String email);

    @Query(value = "SELECT * FROM user WHERE id_pessoa = :idPessoa LIMIT 1", nativeQuery = true)
    Optional<User> findByPessoaIdAnyAtivo(@Param("idPessoa") String idPessoa);
}
