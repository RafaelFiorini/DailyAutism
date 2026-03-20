package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.repository.UserRepository;
import com.clinica.dailyautism.infraestructure.dto.LoginDTO;
import com.clinica.dailyautism.infraestructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public String login(LoginDTO loginDTO) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getEmailUser(),
                        loginDTO.getPasswordUser()
                )
        );

        User user = userRepository.findByEmailUser(loginDTO.getEmailUser())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return jwtService.gerarToken(user);
    }
}