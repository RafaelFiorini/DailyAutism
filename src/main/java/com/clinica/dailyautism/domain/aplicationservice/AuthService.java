package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.Paciente;
import com.clinica.dailyautism.domain.entity.Pessoa;
import com.clinica.dailyautism.domain.entity.Profissional;
import com.clinica.dailyautism.domain.entity.Responsavel;
import com.clinica.dailyautism.domain.entity.security.Perfil;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.exception.EmailAlreadyExistsException;
import com.clinica.dailyautism.domain.repository.*;
import com.clinica.dailyautism.infrastructure.dto.AtualizarCadastroDTO;
import com.clinica.dailyautism.infrastructure.dto.DadosUsuarioRecuperacaoDTO;
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

        userRepository.findByEmailAnyAtivo(email).ifPresent(existente -> {
            if (!Boolean.TRUE.equals(existente.getAtivo())) {
                throw new IllegalArgumentException("Este e-mail pertence a uma conta desativada. Entre em contato com a clínica ou administrador para reativá-la.");
            }
            throw new EmailAlreadyExistsException(email);
        });

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

    public DadosUsuarioRecuperacaoDTO buscarDadosParaAtualizacao(String email, String cpf) {
        if (email == null || email.isBlank() || cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("E-mail e CPF são obrigatórios para identificação.");
        }

        String emailNorm = email.trim().toLowerCase();
        String cleanCpf = cpf.replaceAll("\\D", "").trim();

        User user = userRepository.findByEmailAnyAtivo(emailNorm)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado para o e-mail informado."));

        if (!Boolean.TRUE.equals(user.getAtivo())) {
            throw new IllegalArgumentException("Esta conta está inativa. Entre em contato com a clínica ou suporte.");
        }

        Pessoa pessoa = user.getPessoa();
        if (pessoa == null || pessoa.getCPFPessoa() == null || !pessoa.getCPFPessoa().equals(cleanCpf)) {
            throw new IllegalArgumentException("O CPF informado não corresponde ao titular deste e-mail.");
        }

        return DadosUsuarioRecuperacaoDTO.builder()
                .nome(pessoa.getNomePessoa())
                .email(emailNorm)
                .cpf(pessoa.getCPFPessoa())
                .telefone(pessoa.getTelefonePessoa())
                .celular(pessoa.getCelularPessoa())
                .endereco(pessoa.getEnderecoPessoa())
                .build();
    }

    @Transactional
    public void atualizarCadastro(AtualizarCadastroDTO dto) {
        if (dto.getEmail() == null || dto.getEmail().isBlank() || dto.getCpf() == null || dto.getCpf().isBlank()) {
            throw new IllegalArgumentException("E-mail e CPF são obrigatórios.");
        }

        String emailNorm = dto.getEmail().trim().toLowerCase();
        String cleanCpf = dto.getCpf().replaceAll("\\D", "").trim();

        User user = userRepository.findByEmailAnyAtivo(emailNorm)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        if (!Boolean.TRUE.equals(user.getAtivo())) {
            throw new IllegalArgumentException("Esta conta está inativa.");
        }

        Pessoa pessoa = user.getPessoa();
        if (pessoa == null || pessoa.getCPFPessoa() == null || !pessoa.getCPFPessoa().equals(cleanCpf)) {
            throw new IllegalArgumentException("O CPF informado não confere com o cadastro deste e-mail.");
        }

        // Se informou nova senha
        if (dto.getNovaSenha() != null && !dto.getNovaSenha().isBlank()) {
            if (dto.getNovaSenha().trim().length() < 6) {
                throw new IllegalArgumentException("A nova senha deve ter no mínimo 6 caracteres.");
            }
            user.setPasswordUser(passwordEncoder.encode(dto.getNovaSenha().trim()));
            user.setTrocarSenhaNoProximoLogin(false);
            userRepository.save(user);
        }

        // Atualiza contatos na Pessoa (e-mail e CPF continuam inalterados)
        if (dto.getTelefone() != null) {
            pessoa.setTelefonePessoa(dto.getTelefone().trim());
        }
        if (dto.getCelular() != null) {
            pessoa.setCelularPessoa(dto.getCelular().trim());
        }
        if (dto.getEndereco() != null) {
            pessoa.setEnderecoPessoa(dto.getEndereco().trim());
        }

        pessoaRepository.save(pessoa);
    }

    @Transactional
    public void alterarSenha(String emailUsuario, com.clinica.dailyautism.infrastructure.dto.AlterarSenhaDTO dto) {
        if (emailUsuario == null || emailUsuario.isBlank()) {
            throw new IllegalArgumentException("Usuário não autenticado.");
        }
        User user = userRepository.findByEmailUserIgnoreCase(emailUsuario.trim())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        if (!passwordEncoder.matches(dto.getSenhaAtual(), user.getPasswordUser())) {
            throw new IllegalArgumentException("A senha atual informada está incorreta.");
        }

        com.clinica.dailyautism.infrastructure.security.PasswordSecurityUtil.validarForcaSenha(dto.getNovaSenha());

        if (passwordEncoder.matches(dto.getNovaSenha(), user.getPasswordUser())) {
            throw new IllegalArgumentException("A nova senha deve ser diferente da senha atual.");
        }

        user.setPasswordUser(passwordEncoder.encode(dto.getNovaSenha()));
        user.setTrocarSenhaNoProximoLogin(false);
        userRepository.save(user);
    }
}
