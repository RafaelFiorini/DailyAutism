package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.Pessoa;
import com.clinica.dailyautism.domain.entity.Profissional;
import com.clinica.dailyautism.domain.entity.Responsavel;
import com.clinica.dailyautism.domain.entity.security.Perfil;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.exception.EmailAlreadyExistsException;
import com.clinica.dailyautism.domain.exception.PessoaNotFoundException;
import com.clinica.dailyautism.domain.exception.UserNotFoundException;
import com.clinica.dailyautism.domain.repository.PerfilRepository;
import com.clinica.dailyautism.domain.repository.PessoaRepository;
import com.clinica.dailyautism.domain.repository.ProfissionalRepository;
import com.clinica.dailyautism.domain.repository.ResponsavelRepository;
import com.clinica.dailyautism.domain.repository.UserRepository;
import com.clinica.dailyautism.infrastructure.dto.SaveUserDTO;
import com.clinica.dailyautism.infrastructure.dto.SaveUserFullDTO;
import com.clinica.dailyautism.infrastructure.dto.UserDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PessoaRepository pessoaRepository;
    private final PasswordEncoder passwordEncoder;
    private final PerfilRepository perfilRepository;
    private final ResponsavelRepository responsavelRepository;
    private final ProfissionalRepository profissionalRepository;

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

    public List<UserDTO> listUsers() {
        return userRepository.findAll().stream()
                .map(UserDTO::create)
                .toList();
    }

    public UserDTO getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado: " + id));
        return UserDTO.create(user);
    }

    @Transactional
    public UserDTO createFullUser(SaveUserFullDTO dto) {
        String email = dto.getEmail() != null ? dto.getEmail().trim().toLowerCase() : "";
        if (email.isBlank()) {
            throw new IllegalArgumentException("E-mail é obrigatório");
        }
        if (userRepository.findByEmailUser(email).isPresent()) {
            throw new EmailAlreadyExistsException(email);
        }

        String rawCpf = dto.getCpf();
        String cpfLimpo = rawCpf != null ? rawCpf.replaceAll("\\D", "").trim() : "";
        if (cpfLimpo.length() != 11) {
            throw new IllegalArgumentException("CPF inválido: deve conter 11 dígitos");
        }

        if (Boolean.TRUE.equals(dto.getIsProfissional()) && (dto.getConselhoProf() == null || dto.getConselhoProf().isBlank())) {
            throw new IllegalArgumentException("Informe o registro no conselho profissional");
        }

        // 1. Pessoa
        Pessoa pessoa = pessoaRepository.findByCPFPessoa(cpfLimpo).orElse(null);
        if (pessoa == null) {
            pessoa = Pessoa.builder()
                    .nomePessoa(dto.getNome() != null ? dto.getNome().trim() : "")
                    .CPFPessoa(cpfLimpo)
                    .emailPessoa(email)
                    .telefonePessoa(dto.getTelefone())
                    .celularPessoa(dto.getCelular())
                    .RGPessoa(dto.getRg())
                    .datanascPessoa(dto.getDatanasc())
                    .enderecoPessoa(dto.getEndereco())
                    .build();
        } else {
            if (dto.getNome() != null && !dto.getNome().isBlank()) {
                pessoa.setNomePessoa(dto.getNome().trim());
            }
            pessoa.setEmailPessoa(email);
            if (dto.getTelefone() != null) pessoa.setTelefonePessoa(dto.getTelefone());
            if (dto.getCelular() != null) pessoa.setCelularPessoa(dto.getCelular());
            if (dto.getRg() != null) pessoa.setRGPessoa(dto.getRg());
            if (dto.getDatanasc() != null) pessoa.setDatanascPessoa(dto.getDatanasc());
            if (dto.getEndereco() != null) pessoa.setEnderecoPessoa(dto.getEndereco());
        }
        pessoa = pessoaRepository.save(pessoa);

        // 2. Senha
        String rawSenha = dto.getSenha();
        if (rawSenha == null || rawSenha.isBlank()) {
            rawSenha = "Daily@" + (cpfLimpo.length() >= 4 ? cpfLimpo.substring(0, 4) : "123");
        }

        // 3. User
        User user = User.builder()
                .nomeUser(dto.getNome() != null ? dto.getNome().trim() : "")
                .emailUser(email)
                .passwordUser(passwordEncoder.encode(rawSenha))
                .pessoa(pessoa)
                .build();

        // 4. Perfis
        Set<Perfil> perfis = new HashSet<>();
        if (Boolean.TRUE.equals(dto.getIsAdmin())) {
            perfilRepository.findByNome("ADMIN").ifPresent(perfis::add);
        }
        if (Boolean.TRUE.equals(dto.getIsResponsavel())) {
            perfilRepository.findByNome("RESPONSAVEL").ifPresent(perfis::add);
        }
        if (Boolean.TRUE.equals(dto.getIsProfissional())) {
            perfilRepository.findByNome("PROFISSIONAL").ifPresent(perfis::add);
        }
        if (perfis.isEmpty()) {
            perfilRepository.findByNome("RESPONSAVEL").ifPresent(perfis::add);
        }
        user.setPerfis(perfis);
        user = userRepository.save(user);

        // 5. Responsável
        if (Boolean.TRUE.equals(dto.getIsResponsavel())) {
            if (!responsavelRepository.existsByPessoaResponsavel(pessoa)) {
                Responsavel resp = Responsavel.builder()
                        .pessoaResponsavel(pessoa)
                        .build();
                responsavelRepository.save(resp);
            }
        }

        // 6. Profissional
        if (Boolean.TRUE.equals(dto.getIsProfissional())) {
            var optProf = profissionalRepository.findByPessoaProf(pessoa);
            if (optProf.isPresent()) {
                Profissional prof = optProf.get();
                if (dto.getConselhoProf() != null && !dto.getConselhoProf().isBlank()) {
                    prof.setConselhoProf(dto.getConselhoProf().trim());
                    profissionalRepository.save(prof);
                }
            } else {
                Profissional prof = Profissional.builder()
                        .pessoaProf(pessoa)
                        .conselhoProf(dto.getConselhoProf() != null ? dto.getConselhoProf().trim() : "Não informado")
                        .build();
                profissionalRepository.save(prof);
            }
        }

        return UserDTO.create(user);
    }

    @Transactional
    public UserDTO updateFullUser(String idUser, SaveUserFullDTO dto) {
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado: " + idUser));

        String email = dto.getEmail() != null ? dto.getEmail().trim().toLowerCase() : "";
        if (email.isBlank()) {
            throw new IllegalArgumentException("E-mail é obrigatório");
        }
        if (!user.getEmailUser().equalsIgnoreCase(email)) {
            if (userRepository.findByEmailUser(email).isPresent()) {
                throw new EmailAlreadyExistsException(email);
            }
            user.setEmailUser(email);
        }

        if (dto.getNome() != null && !dto.getNome().isBlank()) {
            user.setNomeUser(dto.getNome().trim());
        }

        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            user.setPasswordUser(passwordEncoder.encode(dto.getSenha()));
        }

        String rawCpf = dto.getCpf();
        String cpfLimpo = rawCpf != null ? rawCpf.replaceAll("\\D", "").trim() : "";
        if (cpfLimpo.length() != 11) {
            throw new IllegalArgumentException("CPF inválido: deve conter 11 dígitos");
        }

        if (Boolean.TRUE.equals(dto.getIsProfissional()) && (dto.getConselhoProf() == null || dto.getConselhoProf().isBlank())) {
            throw new IllegalArgumentException("Informe o registro no conselho profissional");
        }

        // Pessoa
        Pessoa pessoa = user.getPessoa();
        if (pessoa == null) {
            pessoa = pessoaRepository.findByCPFPessoa(cpfLimpo).orElse(null);
            if (pessoa == null) {
                pessoa = Pessoa.builder()
                        .CPFPessoa(cpfLimpo)
                        .build();
            }
        }
        pessoa.setNomePessoa(user.getNomeUser());
        pessoa.setCPFPessoa(cpfLimpo);
        pessoa.setEmailPessoa(email);
        if (dto.getTelefone() != null) pessoa.setTelefonePessoa(dto.getTelefone());
        if (dto.getCelular() != null) pessoa.setCelularPessoa(dto.getCelular());
        if (dto.getRg() != null) pessoa.setRGPessoa(dto.getRg());
        if (dto.getDatanasc() != null) pessoa.setDatanascPessoa(dto.getDatanasc());
        if (dto.getEndereco() != null) pessoa.setEnderecoPessoa(dto.getEndereco());
        pessoa = pessoaRepository.save(pessoa);
        user.setPessoa(pessoa);

        // Perfis
        Set<Perfil> perfis = new HashSet<>();
        if (Boolean.TRUE.equals(dto.getIsAdmin())) {
            perfilRepository.findByNome("ADMIN").ifPresent(perfis::add);
        }
        if (Boolean.TRUE.equals(dto.getIsResponsavel())) {
            perfilRepository.findByNome("RESPONSAVEL").ifPresent(perfis::add);
        }
        if (Boolean.TRUE.equals(dto.getIsProfissional())) {
            perfilRepository.findByNome("PROFISSIONAL").ifPresent(perfis::add);
        }
        if (perfis.isEmpty()) {
            perfilRepository.findByNome("RESPONSAVEL").ifPresent(perfis::add);
        }
        user.setPerfis(perfis);
        user = userRepository.save(user);

        // Responsável
        if (Boolean.TRUE.equals(dto.getIsResponsavel())) {
            if (!responsavelRepository.existsByPessoaResponsavel(pessoa)) {
                Responsavel resp = Responsavel.builder()
                        .pessoaResponsavel(pessoa)
                        .build();
                responsavelRepository.save(resp);
            }
        }

        // Profissional
        if (Boolean.TRUE.equals(dto.getIsProfissional())) {
            var optProf = profissionalRepository.findByPessoaProf(pessoa);
            if (optProf.isPresent()) {
                Profissional prof = optProf.get();
                if (dto.getConselhoProf() != null && !dto.getConselhoProf().isBlank()) {
                    prof.setConselhoProf(dto.getConselhoProf().trim());
                    profissionalRepository.save(prof);
                }
            } else {
                Profissional prof = Profissional.builder()
                        .pessoaProf(pessoa)
                        .conselhoProf(dto.getConselhoProf() != null ? dto.getConselhoProf().trim() : "Não informado")
                        .build();
                profissionalRepository.save(prof);
            }
        }

        return UserDTO.create(user);
    }

    @Transactional
    public void deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado: " + id));
        user.desativar();
        userRepository.save(user);
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
