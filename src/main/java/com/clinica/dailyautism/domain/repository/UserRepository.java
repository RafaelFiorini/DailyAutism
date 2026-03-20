package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.security.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmailUser(String emailUser);
}
