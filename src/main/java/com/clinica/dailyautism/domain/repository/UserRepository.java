package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.security.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
