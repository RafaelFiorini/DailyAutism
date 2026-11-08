package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.Paciente;
import com.clinica.dailyautism.domain.entity.Pessoa;
import com.clinica.dailyautism.domain.entity.Profissional;
import com.clinica.dailyautism.domain.entity.Responsavel;
import com.clinica.dailyautism.domain.entity.security.Perfil;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.exception.EmailAlreadyExistsException;
import com.clinica.dailyautism.domain.repository.*;
import com.clinica.dailyautism.infrastructure.dto.LoginDTO;
import com.clinica.dailyautism.infrastructure.dto.RegisterDTO;
import com.clinica.dailyautism.infrastructure.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PessoaRepository pessoaRepository;
    private final ProfissionalRepository profissionalRepository;
    private final ResponsavelRepository responsavelRepository;
    private final PacienteRepository pacienteRepository;
    private final PerfilRepository perfilRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

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

    @Transactional
    public String register(RegisterDTO dto) {
        String email = dto.getEmail().trim().toLowerCase();

        if (userRepository.findByEmailUser(email).isPresent()) {
            throw new EmailAlreadyExistsException(email);
        }

        String rawCpf = dto.getCpf();
        String cleanCpf = (rawCpf != null) ? rawCpf.replaceAll("\\D", "").trim() : "";
        if (cleanCpf.length() != 11) {
            throw new IllegalArgumentException("CPF deve conter 11 dígitos");
        }

        // Busca ou cria a pessoa
        Pessoa pessoa = pessoaRepository.findByCPFPessoa(cleanCpf)
                .orElseGet(() -> {
                    Pessoa nova = Pessoa.builder()
                            .nomePessoa(dto.getNome().trim())
                            .CPFPessoa(cleanCpf)
                            .emailPessoa(email)
                            .celularPessoa(dto.getCelular() != null ? dto.getCelular().trim() : dto.getTelefone())
                            .telefonePessoa(dto.getTelefone())
                            .RGPessoa(dto.getRg())
                            .datanascPessoa(dto.getDatanasc())
                            .enderecoPessoa(dto.getEndereco())
                            .build();
                    return pessoaRepository.save(nova);
                });

        // Atualiza campos de contato se estavam vazios
        if (pessoa.getEmailPessoa() == null || pessoa.getEmailPessoa().isBlank()) {
            pessoa.setEmailPessoa(email);
            pessoaRepository.save(pessoa);
        }

        String tipoUpper = dto.getTipo().trim().toUpperCase();
        Perfil perfil;

        if ("PROFISSIONAL".equals(tipoUpper)) {
            if (dto.getConselhoProf() == null || dto.getConselhoProf().isBlank()) {
                throw new IllegalArgumentException("Registro no conselho é obrigatório para profissionais");
            }

            if (!profissionalRepository.existsByPessoaProf(pessoa)) {
                Profissional prof = Profissional.builder()
                        .pessoaProf(pessoa)
                        .conselhoProf(dto.getConselhoProf().trim())
                        .build();
                profissionalRepository.save(prof);
            }

            perfil = perfilRepository.findByNome("PROFISSIONAL")
                    .orElseThrow(() -> new RuntimeException("Perfil PROFISSIONAL não encontrado"));

        } else if ("RESPONSAVEL".equals(tipoUpper)) {
            Responsavel resp = responsavelRepository.findByPessoaResponsavel(pessoa)
                    .orElseGet(() -> {
                        Responsavel novo = Responsavel.builder()
                                .pessoaResponsavel(pessoa)
                                .pacientes(new ArrayList<>())
                                .build();
                        return responsavelRepository.save(novo);
                    });

            if (resp.getPacientes() == null) {
                resp.setPacientes(new ArrayList<>());
            }

            // Se também é paciente
            if (Boolean.TRUE.equals(dto.getIsPacienteTambem())) {
                Paciente pacProprio = pacienteRepository.findByPessoaPaciente(pessoa)
                        .orElseGet(() -> {
                            Paciente p = Paciente.builder()
                                    .pessoaPaciente(pessoa)
                                    .isResponsavel(true)
                                    .build();
                            return pacienteRepository.save(p);
                        });
                if (!resp.getPacientes().contains(pacProprio)) {
                    resp.getPacientes().add(pacProprio);
                }
            }

            // Cadastro de pacientes iniciais fornecidos
            if (dto.getPacientes() != null) {
                for (var pDto : dto.getPacientes()) {
                    if (pDto.getNome() != null && !pDto.getNome().isBlank()) {
                        String rawPacCpf = pDto.getCpf();
                        String cleanPacCpf = (rawPacCpf != null) ? rawPacCpf.replaceAll("\\D", "").trim() : "";
                        if (!cleanPacCpf.isBlank()) {
                            Pessoa pessoaPac = pessoaRepository.findByCPFPessoa(cleanPacCpf)
                                    .orElseGet(() -> pessoaRepository.save(Pessoa.builder()
                                            .nomePessoa(pDto.getNome().trim())
                                            .CPFPessoa(cleanPacCpf)
                                            .datanascPessoa(pDto.getDatanasc())
                                            .build()));

                            Paciente pac = pacienteRepository.findByPessoaPaciente(pessoaPac)
                                    .orElseGet(() -> pacienteRepository.save(Paciente.builder()
                                            .pessoaPaciente(pessoaPac)
                                            .apelidoPaciente(pDto.getApelido())
                                            .isResponsavel(false)
                                            .build()));

                            if (!resp.getPacientes().contains(pac)) {
                                resp.getPacientes().add(pac);
                            }
                        }
                    }
                }
            }
            responsavelRepository.save(resp);

            perfil = perfilRepository.findByNome("RESPONSAVEL")
                    .orElseThrow(() -> new RuntimeException("Perfil RESPONSAVEL não encontrado"));
        } else {
            throw new IllegalArgumentException("Tipo inválido. Deve ser RESPONSAVEL ou PROFISSIONAL");
        }

        // Cria o usuário
        User user = User.builder()
                .nomeUser(dto.getNome().trim())
                .emailUser(email)
                .passwordUser(passwordEncoder.encode(dto.getSenha()))
                .pessoa(pessoa)
                .perfis(new HashSet<>(Set.of(perfil)))
                .build();

        user = userRepository.save(user);

        return jwtService.gerarToken(user);
    }
}
