package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.*;
import com.clinica.dailyautism.domain.entity.security.Perfil;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.exception.ClinicaNotFoundException;
import com.clinica.dailyautism.domain.exception.PessoaNotFoundException;
import com.clinica.dailyautism.domain.exception.ResponsavelNotFoundException;
import com.clinica.dailyautism.domain.repository.*;
import com.clinica.dailyautism.infrastructure.dto.SaveResponsavelDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@RequiredArgsConstructor
@Service
public class ResponsavelService {

    private final ResponsavelRepository responsavelRepository;
    private final PessoaRepository pessoaRepository;
    private final PacienteRepository pacienteRepository;
    private final UserRepository userRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;
    private final ResponsavelClinicaRepository responsavelClinicaRepository;
    private final ClinicaRepository clinicaRepository;
    private final FuncionarioClinicaRepository funcionarioClinicaRepository;

    @Transactional
    public Responsavel createResponsavel(SaveResponsavelDTO saveResponsavelDTO) {
        return createResponsavel(saveResponsavelDTO, null);
    }

    @Transactional
    public Responsavel createResponsavel(SaveResponsavelDTO saveResponsavelDTO, String emailDoUsuario) {
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

        // Descobrir a clínica alvo
        String idClinicaAlvo = saveResponsavelDTO.getIdClinica();
        if ((idClinicaAlvo == null || idClinicaAlvo.isBlank()) && emailDoUsuario != null) {
            User userLogado = userRepository.findByEmailUser(emailDoUsuario).orElse(null);
            if (userLogado != null && userLogado.getPessoa() != null) {
                var vinculos = funcionarioClinicaRepository.findByPessoa(userLogado.getPessoa());
                if (!vinculos.isEmpty() && vinculos.get(0).getClinica() != null) {
                    idClinicaAlvo = vinculos.get(0).getClinica().getIdClinica();
                }
            }
        }

        Clinica clinicaAlvo = null;
        if (idClinicaAlvo != null && !idClinicaAlvo.isBlank()) {
            final String idParaBuscar = idClinicaAlvo.trim();
            clinicaAlvo = clinicaRepository.findById(idParaBuscar)
                    .orElseThrow(() -> new ClinicaNotFoundException(idParaBuscar));
        }

        Optional<Responsavel> respExistenteOpt = responsavelRepository.findByPessoaResponsavel(pessoa);

        if (respExistenteOpt.isPresent()) {
            Responsavel respExistente = respExistenteOpt.get();
            if (clinicaAlvo != null) {
                if (responsavelClinicaRepository.existsByResponsavelAndClinica(respExistente, clinicaAlvo)) {
                    throw new IllegalArgumentException("Este responsável já está vinculado a esta clínica.");
                }
                ResponsavelClinica rc = ResponsavelClinica.builder()
                        .responsavel(respExistente)
                        .clinica(clinicaAlvo)
                        .build();
                responsavelClinicaRepository.save(rc);

                // Vincula pacientes adicionais se passados
                if (saveResponsavelDTO.getPacienteIds() != null) {
                    for (String idPac : saveResponsavelDTO.getPacienteIds()) {
                        if (idPac != null && !idPac.isBlank()) {
                            pacienteRepository.findById(idPac.trim()).ifPresent(pac -> {
                                if (!respExistente.getPacientes().contains(pac)) {
                                    respExistente.getPacientes().add(pac);
                                }
                            });
                        }
                    }
                    responsavelRepository.save(respExistente);
                }

                criarOuAtualizarUsuarioResponsavel(pessoa, saveResponsavelDTO.getEmailPessoa());
                return respExistente;
            } else {
                throw new IllegalArgumentException("Esta pessoa já está cadastrada como responsável.");
            }
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

        if (clinicaAlvo != null) {
            ResponsavelClinica rc = ResponsavelClinica.builder()
                    .responsavel(saved)
                    .clinica(clinicaAlvo)
                    .build();
            responsavelClinicaRepository.save(rc);
        }

        // Auto-criação do usuário para o responsável
        criarOuAtualizarUsuarioResponsavel(pessoa, saveResponsavelDTO.getEmailPessoa());

        return saved;
    }

    public Responsavel loadResponsavel(String responsavelId) {
        return responsavelRepository.findById(responsavelId)
                .orElseThrow(() -> new ResponsavelNotFoundException("Responsável não encontrado: " + responsavelId));
    }

    public Responsavel loadResponsavelForUser(String responsavelId, String email) {
        Responsavel responsavel = loadResponsavel(responsavelId);
        if (email == null || email.isBlank()) {
            return responsavel;
        }

        User user = userRepository.findByEmailUser(email).orElse(null);
        if (user == null) {
            return responsavel;
        }

        boolean isAdmin = user.getPerfis() != null && user.getPerfis().stream()
                .anyMatch(p -> "ADMIN".equalsIgnoreCase(p.getNome()));
        if (isAdmin) {
            return responsavel;
        }

        boolean isClinica = user.getPerfis() != null && user.getPerfis().stream()
                .anyMatch(p -> "CLINICA".equalsIgnoreCase(p.getNome()) || "FUNCIONARIO_CLINICA".equalsIgnoreCase(p.getNome()) || "ADMIN_CLINICA".equalsIgnoreCase(p.getNome()));
        if (isClinica) {
            if (user.getPessoa() != null) {
                var vinculosFunc = funcionarioClinicaRepository.findByPessoa(user.getPessoa());
                List<String> clinicaIds = vinculosFunc.stream()
                        .filter(v -> v.getClinica() != null)
                        .map(v -> v.getClinica().getIdClinica())
                        .toList();

                // 1. Vinculado diretamente à clínica
                List<ResponsavelClinica> vinculosResp = responsavelClinicaRepository.findByResponsavelIdResponsavel(responsavelId);
                boolean vinculadoDireto = vinculosResp.stream()
                        .anyMatch(rc -> rc.getClinica() != null && clinicaIds.contains(rc.getClinica().getIdClinica()));
                if (vinculadoDireto) {
                    return responsavel;
                }

                // 2. Ou algum paciente dele está na clínica
                if (responsavel.getPacientes() != null) {
                    for (Paciente pac : responsavel.getPacientes()) {
                        if (pac.getClinicas() != null) {
                            boolean pacienteNaClinica = pac.getClinicas().stream()
                                    .anyMatch(pc -> pc.getClinica() != null && clinicaIds.contains(pc.getClinica().getIdClinica()));
                            if (pacienteNaClinica) {
                                return responsavel;
                            }
                        }
                    }
                }
            }
            throw new AccessDeniedException("Você não tem permissão para visualizar responsáveis de outra clínica.");
        }

        return responsavel;
    }

    public List<Responsavel> listResponsaveis() {
        return responsavelRepository.findAll();
    }

    public List<Responsavel> listResponsaveisForUser(String email) {
        if (email == null || email.isBlank()) {
            return listResponsaveis();
        }

        User user = userRepository.findByEmailUser(email).orElse(null);
        if (user == null) {
            return listResponsaveis();
        }

        boolean isAdmin = user.getPerfis() != null && user.getPerfis().stream()
                .anyMatch(p -> "ADMIN".equalsIgnoreCase(p.getNome()));
        if (isAdmin) {
            return listResponsaveis();
        }

        boolean isClinica = user.getPerfis() != null && user.getPerfis().stream()
                .anyMatch(p -> "CLINICA".equalsIgnoreCase(p.getNome()) || "FUNCIONARIO_CLINICA".equalsIgnoreCase(p.getNome()) || "ADMIN_CLINICA".equalsIgnoreCase(p.getNome()));
        if (isClinica) {
            if (user.getPessoa() == null) {
                return List.of();
            }
            var vinculosFunc = funcionarioClinicaRepository.findByPessoa(user.getPessoa());
            List<String> clinicaIds = vinculosFunc.stream()
                    .filter(v -> v.getClinica() != null)
                    .map(v -> v.getClinica().getIdClinica())
                    .toList();
            if (clinicaIds.isEmpty()) {
                return List.of();
            }

            Map<String, Responsavel> resultado = new LinkedHashMap<>();
            for (Responsavel r : responsavelClinicaRepository.findResponsaveisByClinicaIds(clinicaIds)) {
                if (r != null && r.getIdResponsavel() != null) {
                    resultado.put(r.getIdResponsavel(), r);
                }
            }

            // Também inclui responsáveis cujos dependentes atendem na clínica
            List<Paciente> pacientesDaClinica = pacienteRepository.findByClinicaIds(clinicaIds);
            for (Paciente pac : pacientesDaClinica) {
                if (pac.getResponsaveis() != null) {
                    for (Responsavel r : pac.getResponsaveis()) {
                        if (r != null && r.getIdResponsavel() != null) {
                            resultado.put(r.getIdResponsavel(), r);
                        }
                    }
                }
            }

            return new ArrayList<>(resultado.values());
        }

        boolean isResponsavel = user.getPerfis() != null && user.getPerfis().stream()
                .anyMatch(p -> "RESPONSAVEL".equalsIgnoreCase(p.getNome()));
        if (isResponsavel && user.getPessoa() != null) {
            return responsavelRepository.findByPessoaResponsavel(user.getPessoa())
                    .map(List::of)
                    .orElse(List.of());
        }

        return listResponsaveis();
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

        // Vínculo com clínica na edição se informado
        if (saveResponsavelDTO.getIdClinica() != null && !saveResponsavelDTO.getIdClinica().isBlank()) {
            Clinica clinica = clinicaRepository.findById(saveResponsavelDTO.getIdClinica().trim()).orElse(null);
            if (clinica != null && !responsavelClinicaRepository.existsByResponsavelAndClinica(responsavel, clinica)) {
                ResponsavelClinica rc = ResponsavelClinica.builder()
                        .responsavel(responsavel)
                        .clinica(clinica)
                        .build();
                responsavelClinicaRepository.save(rc);
            }
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
