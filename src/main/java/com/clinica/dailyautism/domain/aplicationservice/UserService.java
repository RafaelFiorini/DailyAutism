package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.repository.UserRepository;
import com.clinica.dailyautism.infraestructure.dto.SaveUserDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User createUser(SaveUserDTO saveUserDTO){
        User user = User
                .builder()
                .nomeUser(saveUserDTO.getNomeUser())
                .emailUser(saveUserDTO.getEmailUser())
                .passwordUser(saveUserDTO.getPasswordUser())
                .build();

        userRepository.save(user);

        return user;
    }
}
