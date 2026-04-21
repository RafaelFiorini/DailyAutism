package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.Pessoa;
import com.clinica.dailyautism.domain.entity.security.Perfil;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.exception.EmailAlreadyExistsException;
import com.clinica.dailyautism.domain.exception.PessoaNotFoundException;
import com.clinica.dailyautism.domain.exception.UserNotFoundException;
import com.clinica.dailyautism.domain.repository.PerfilRepository;
import com.clinica.dailyautism.domain.repository.PessoaRepository;
import com.clinica.dailyautism.domain.repository.UserRepository;
import com.clinica.dailyautism.infrastructure.dto.SaveUserDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;


@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PessoaRepository pessoaRepository;
    private final PasswordEncoder passwordEncoder;
    private final PerfilRepository perfilRepository;
    @Transactional
    public User createUser(SaveUserDTO saveUserDTO) {
        if (userRepository.findByEmailUser(saveUserDTO.getEmailUser()).isPresent()) {
            throw new EmailAlreadyExistsException(saveUserDTO.getEmailUser());
        }

        User user = User.builder()
                .nomeUser(saveUserDTO.getNomeUser())
                .emailUser(saveUserDTO.getEmailUser())
                .passwordUser(passwordEncoder.encode(saveUserDTO.getPasswordUser()))
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public User vincularPessoa(String userId, String pessoaId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado: " + userId));

        Pessoa pessoa = pessoaRepository.findById(pessoaId)
                .orElseThrow(() -> new PessoaNotFoundException(pessoaId));

        user.setPessoa(pessoa);
        return userRepository.save(user);
    }

    public void atualizarPerfis(String userId, Set<String> idPerfis) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Set<Perfil> perfis = new HashSet<>(perfilRepository.findAllById(idPerfis));
        user.setPerfis(perfis);
        userRepository.save(user);
    }
    public User findByEmail(String email) {
        return userRepository.findByEmailUser(email)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado: " + email));
    }
}