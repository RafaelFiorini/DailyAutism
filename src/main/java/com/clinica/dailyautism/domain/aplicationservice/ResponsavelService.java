package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.Paciente;
import com.clinica.dailyautism.domain.entity.Pessoa;
import com.clinica.dailyautism.domain.entity.Responsavel;
import com.clinica.dailyautism.domain.entity.security.Perfil;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.exception.PessoaNotFoundException;
import com.clinica.dailyautism.domain.exception.ResponsavelNotFoundException;
import com.clinica.dailyautism.domain.repository.PacienteRepository;
import com.clinica.dailyautism.domain.repository.PerfilRepository;
import com.clinica.dailyautism.domain.repository.PessoaRepository;
import com.clinica.dailyautism.domain.repository.ResponsavelRepository;
import com.clinica.dailyautism.domain.repository.UserRepository;
import com.clinica.dailyautism.infrastructure.dto.SaveResponsavelDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class ResponsavelService {

    private final ResponsavelRepository responsavelRepository;
    private final PessoaRepository pessoaRepository;
    private final PacienteRepository pacienteRepository;
    private final UserRepository userRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Responsavel createResponsavel(SaveResponsavelDTO saveResponsavelDTO) {
        Pessoa pessoa;
        String idPessoa = saveResponsavelDTO.getIdPessoa();
        String rawCpf = saveResponsavelDTO.getCPFPessoa();
        String cpf = (rawCpf != null) ? rawCpf.replaceAll("\\D", "").trim() : "";

        if (idPessoa != null && !idPessoa.isBlank()) {
            pessoa = pessoaRepository.findById(idPessoa.trim())
                    .orElseThrow(() -> new PessoaNotFoundException(idPessoa));
        } else if (!cpf.isBlank()) {
            final String cpfLimpo = cpf;
            pessoa = pessoaRepository.findByCPFPessoa(cpfLimpo)
                    .orElseGet(() -> {
                        Pessoa nova = Pessoa.builder()
                                .nomePessoa(saveResponsavelDTO.getNomePessoa() != null ? saveResponsavelDTO.getNomePessoa().trim() : "")
                                .CPFPessoa(cpfLimpo)
                                .datanascPessoa(saveResponsavelDTO.getDatanascPessoa())
                                .RGPessoa(saveResponsavelDTO.getRGPessoa())
                                .enderecoPessoa(saveResponsavelDTO.getEnderecoPessoa())
                                .telefonePessoa(saveResponsavelDTO.getTelefonePessoa())
                                .celularPessoa(saveResponsavelDTO.getCelularPessoa())
                                .emailPessoa(saveResponsavelDTO.getEmailPessoa())
                                .build();
                        return pessoaRepository.save(nova);
                    });
        } else {
            throw new IllegalArgumentException("Informe a pessoa existente ou os dados pessoais com CPF");
        }

        if (responsavelRepository.existsByPessoaResponsavel(pessoa)) {
            throw new IllegalArgumentException("Esta pessoa já está cadastrada como responsável.");
        }

        Responsavel responsavel = Responsavel.builder()
                .pessoaResponsavel(pessoa)
                .pacientes(new ArrayList<>())
                .build();

        // Se o responsável também é paciente
        if (Boolean.TRUE.equals(saveResponsavelDTO.getIsPacienteTambem())) {
            Paciente pacProprio = pacienteRepository.findByPessoaPaciente(pessoa)
                    .orElseGet(() -> {
                        Paciente novoPac = Paciente.builder()
                                .pessoaPaciente(pessoa)
                                .isResponsavel(true)
                                .build();
                        return pacienteRepository.save(novoPac);
                    });
            responsavel.getPacientes().add(pacProprio);
        }

        // Vínculo com pacientes existentes selecionados
        if (saveResponsavelDTO.getPacienteIds() != null && !saveResponsavelDTO.getPacienteIds().isEmpty()) {
            for (String idPac : saveResponsavelDTO.getPacienteIds()) {
                if (idPac != null && !idPac.isBlank()) {
                    Paciente pac = pacienteRepository.findById(idPac.trim()).orElse(null);
                    if (pac != null && !responsavel.getPacientes().contains(pac)) {
                        responsavel.getPacientes().add(pac);
                    }
                }
            }
        }

        Responsavel saved = responsavelRepository.save(responsavel);

        // Auto-criação do usuário para o responsável
        criarOuAtualizarUsuarioResponsavel(pessoa, saveResponsavelDTO.getEmailPessoa());

        return saved;
    }

    public Responsavel loadResponsavel(String responsavelId) {
        return responsavelRepository.findById(responsavelId)
                .orElseThrow(() -> new ResponsavelNotFoundException("Responsável não encontrado: " + responsavelId));
    }

    public List<Responsavel> listResponsaveis() {
        return responsavelRepository.findAll();
    }

    @Transactional
    public Responsavel updateResponsavel(String responsavelId, SaveResponsavelDTO saveResponsavelDTO) {
        Responsavel responsavel = loadResponsavel(responsavelId);
        Pessoa pessoa = responsavel.getPessoaResponsavel();

        if (pessoa != null) {
            if (saveResponsavelDTO.getNomePessoa() != null && !saveResponsavelDTO.getNomePessoa().isBlank()) {
                pessoa.setNomePessoa(saveResponsavelDTO.getNomePessoa().trim());
            }
            if (saveResponsavelDTO.getEmailPessoa() != null) {
                pessoa.setEmailPessoa(saveResponsavelDTO.getEmailPessoa().trim());
            }
            if (saveResponsavelDTO.getCelularPessoa() != null) {
                pessoa.setCelularPessoa(saveResponsavelDTO.getCelularPessoa().trim());
            }
            if (saveResponsavelDTO.getTelefonePessoa() != null) {
                pessoa.setTelefonePessoa(saveResponsavelDTO.getTelefonePessoa().trim());
            }
            if (saveResponsavelDTO.getEnderecoPessoa() != null) {
                pessoa.setEnderecoPessoa(saveResponsavelDTO.getEnderecoPessoa().trim());
            }
            if (saveResponsavelDTO.getRGPessoa() != null) {
                pessoa.setRGPessoa(saveResponsavelDTO.getRGPessoa().trim());
            }
            if (saveResponsavelDTO.getDatanascPessoa() != null) {
                pessoa.setDatanascPessoa(saveResponsavelDTO.getDatanascPessoa());
            }
            pessoaRepository.save(pessoa);

            // Mantém usuário sincronizado
            criarOuAtualizarUsuarioResponsavel(pessoa, saveResponsavelDTO.getEmailPessoa());
        }

        // Se marcou que também é paciente
        if (Boolean.TRUE.equals(saveResponsavelDTO.getIsPacienteTambem())) {
            Paciente pacProprio = pacienteRepository.findByPessoaPaciente(pessoa)
                    .orElseGet(() -> {
                        Paciente novoPac = Paciente.builder()
                                .pessoaPaciente(pessoa)
                                .isResponsavel(true)
                                .build();
                        return pacienteRepository.save(novoPac);
                    });
            if (!responsavel.getPacientes().contains(pacProprio)) {
                responsavel.getPacientes().add(pacProprio);
            }
        }

        // Atualizar lista de pacientes vinculados
        if (saveResponsavelDTO.getPacienteIds() != null) {
            List<Paciente> novosPacientes = new ArrayList<>();
            // Se o responsável também é paciente, garante que ele continua na lista
            if (Boolean.TRUE.equals(saveResponsavelDTO.getIsPacienteTambem())) {
                pacienteRepository.findByPessoaPaciente(pessoa).ifPresent(novosPacientes::add);
            }
            for (String idPac : saveResponsavelDTO.getPacienteIds()) {
                if (idPac != null && !idPac.isBlank()) {
                    Paciente pac = pacienteRepository.findById(idPac.trim()).orElse(null);
                    if (pac != null && !novosPacientes.contains(pac)) {
                        novosPacientes.add(pac);
                    }
                }
            }
            responsavel.setPacientes(novosPacientes);
        }

        return responsavelRepository.save(responsavel);
    }

    @Transactional
    public void deleteResponsavel(String responsavelId) {
        Responsavel responsavel = responsavelRepository.findById(responsavelId)
                .orElseThrow(() -> new ResponsavelNotFoundException(responsavelId));
        responsavel.desativar();
        responsavelRepository.save(responsavel);
    }

    public Responsavel findByEmail(String email) {
        return responsavelRepository.findByPessoaResponsavelEmailPessoa(email)
                .orElseThrow(() -> new ResponsavelNotFoundException("Responsável não encontrado para o e-mail: " + email));
    }

    private void criarOuAtualizarUsuarioResponsavel(Pessoa pessoa, String emailInformado) {
        String email = (emailInformado != null && !emailInformado.isBlank())
                ? emailInformado.trim().toLowerCase()
                : (pessoa.getEmailPessoa() != null ? pessoa.getEmailPessoa().trim().toLowerCase() : null);

        if (email == null || email.isBlank()) {
            return;
        }

        Perfil perfilResp = perfilRepository.findByNome("RESPONSAVEL").orElse(null);
        Set<Perfil> perfis = new HashSet<>();
        if (perfilResp != null) perfis.add(perfilResp);

        userRepository.findByEmailUser(email).ifPresentOrElse(
                userExistente -> {
                    userExistente.setPessoa(pessoa);
                    if (perfilResp != null) {
                        if (userExistente.getPerfis() == null) {
                            userExistente.setPerfis(new HashSet<>());
                        }
                        userExistente.getPerfis().add(perfilResp);
                    }
                    userRepository.save(userExistente);
                },
                () -> {
                    String rawCpf = pessoa.getCPFPessoa();
                    String senhaPadrao = (rawCpf != null && !rawCpf.isBlank())
                            ? rawCpf.replaceAll("\\D", "")
                            : "Daily@123";
                    User novoUser = User.builder()
                            .nomeUser(pessoa.getNomePessoa() != null && !pessoa.getNomePessoa().isBlank() ? pessoa.getNomePessoa() : "Responsável")
                            .emailUser(email)
                            .passwordUser(passwordEncoder.encode(senhaPadrao))
                            .pessoa(pessoa)
                            .perfis(perfis)
                            .build();
                    userRepository.save(novoUser);
                }
        );
    }
}
