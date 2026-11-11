package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.Clinica;
import com.clinica.dailyautism.domain.entity.Pessoa;
import com.clinica.dailyautism.domain.entity.Profissional;
import com.clinica.dailyautism.domain.entity.ProfissionalClinica;
import com.clinica.dailyautism.domain.entity.security.Perfil;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.exception.ClinicaNotFoundException;
import com.clinica.dailyautism.domain.exception.InactiveResourceConflictException;
import com.clinica.dailyautism.domain.exception.PessoaNotFoundException;
import com.clinica.dailyautism.domain.exception.ProfissionalNotFoundException;
import com.clinica.dailyautism.domain.repository.*;
import com.clinica.dailyautism.infrastructure.dto.SaveProfissionalDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class ProfissionalService {

    private final ProfissionalRepository profissionalRepository;
    private final PessoaRepository pessoaRepository;
    private final UserRepository userRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfissionalClinicaRepository profissionalClinicaRepository;
    private final ClinicaRepository clinicaRepository;
    private final FuncionarioClinicaRepository funcionarioClinicaRepository;

    @Transactional
    public Profissional createProfissional(SaveProfissionalDTO saveProfissionalDTO) {
        return createProfissional(saveProfissionalDTO, null);
    }

    @Transactional
    public Profissional createProfissional(SaveProfissionalDTO saveProfissionalDTO, String emailDoUsuario) {
        Pessoa pessoa;
        String idPessoa = saveProfissionalDTO.getIdPessoa();
        String rawCpf = saveProfissionalDTO.getCPFPessoa();
        String cpf = (rawCpf != null) ? rawCpf.replaceAll("\\D", "").trim() : "";

        if (idPessoa != null && !idPessoa.isBlank()) {
            pessoa = pessoaRepository.findById(idPessoa.trim())
                    .orElseThrow(() -> new PessoaNotFoundException(idPessoa));
        } else if (!cpf.isBlank()) {
            final String cpfLimpo = cpf;
            pessoa = pessoaRepository.findByCPFPessoa(cpfLimpo)
                    .orElseGet(() -> {
                        Pessoa nova = Pessoa.builder()
                                .nomePessoa(saveProfissionalDTO.getNomePessoa() != null ? saveProfissionalDTO.getNomePessoa().trim() : "")
                                .CPFPessoa(cpfLimpo)
                                .datanascPessoa(saveProfissionalDTO.getDatanascPessoa())
                                .RGPessoa(saveProfissionalDTO.getRGPessoa())
                                .enderecoPessoa(saveProfissionalDTO.getEnderecoPessoa())
                                .telefonePessoa(saveProfissionalDTO.getTelefonePessoa())
                                .celularPessoa(saveProfissionalDTO.getCelularPessoa())
                                .emailPessoa(saveProfissionalDTO.getEmailPessoa())
                                .build();
                        return pessoaRepository.save(nova);
                    });
        } else {
            throw new IllegalArgumentException("Informe a pessoa existente ou os dados pessoais com CPF");
        }

        // Descobrir a clínica alvo (se passada explicitamente ou pelo funcionário logado)
        String idClinicaAlvo = saveProfissionalDTO.getIdClinica();
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

        profissionalRepository.findByPessoaIdAnyAtivo(pessoa.getIdPessoa()).ifPresent(profInativo -> {
            if (!Boolean.TRUE.equals(profInativo.getAtivo())) {
                throw new InactiveResourceConflictException(
                        "PROFISSIONAL",
                        profInativo.getIdProf(),
                        pessoa.getNomePessoa(),
                        "Já existe um profissional desativado com este CPF (" + pessoa.getNomePessoa() + "). Reative o cadastro existente."
                );
            }
        });

        Optional<Profissional> profExistenteOpt = profissionalRepository.findByPessoaProf(pessoa);

        if (profExistenteOpt.isPresent()) {
            Profissional profExistente = profExistenteOpt.get();
            if (clinicaAlvo != null) {
                if (profissionalClinicaRepository.existsByProfissionalAndClinica(profExistente, clinicaAlvo)) {
                    throw new IllegalArgumentException("Este profissional já está vinculado a esta clínica.");
                }
                ProfissionalClinica pc = ProfissionalClinica.builder()
                        .profissional(profExistente)
                        .clinica(clinicaAlvo)
                        .diasAtendimento(saveProfissionalDTO.getDiasAtendimento())
                        .horariosAtendimento(saveProfissionalDTO.getHorariosAtendimento())
                        .build();
                profissionalClinicaRepository.save(pc);
                criarOuAtualizarUsuarioProfissional(pessoa, saveProfissionalDTO.getEmailPessoa());
                return profExistente;
            } else {
                throw new IllegalArgumentException("Esta pessoa já está cadastrada como profissional.");
            }
        }

        Profissional profissional = Profissional.builder()
                .pessoaProf(pessoa)
                .conselhoProf(saveProfissionalDTO.getConselhoProf())
                .build();

        Profissional saved = profissionalRepository.save(profissional);

        if (clinicaAlvo != null) {
            ProfissionalClinica pc = ProfissionalClinica.builder()
                    .profissional(saved)
                    .clinica(clinicaAlvo)
                    .diasAtendimento(saveProfissionalDTO.getDiasAtendimento())
                    .horariosAtendimento(saveProfissionalDTO.getHorariosAtendimento())
                    .build();
            profissionalClinicaRepository.save(pc);
        }

        // Auto-criação do usuário para o profissional
        criarOuAtualizarUsuarioProfissional(pessoa, saveProfissionalDTO.getEmailPessoa());

        return saved;
    }

    public Profissional loadProfissional(String profissionalId) {
        return profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new ProfissionalNotFoundException("Profissional não encontrado: " + profissionalId));
    }

    public Profissional loadProfissionalForUser(String profissionalId, String email) {
        Profissional profissional = loadProfissional(profissionalId);
        if (email == null || email.isBlank()) {
            return profissional;
        }

        User user = userRepository.findByEmailUser(email).orElse(null);
        if (user == null) {
            return profissional;
        }

        boolean isAdmin = user.getPerfis() != null && user.getPerfis().stream()
                .anyMatch(p -> "ADMIN".equalsIgnoreCase(p.getNome()));
        if (isAdmin) {
            return profissional;
        }

        boolean isClinica = user.getPerfis() != null && user.getPerfis().stream()
                .anyMatch(p -> "CLINICA".equalsIgnoreCase(p.getNome()) || "FUNCIONARIO_CLINICA".equalsIgnoreCase(p.getNome()) || "ADMIN_CLINICA".equalsIgnoreCase(p.getNome()));
        if (isClinica) {
            if (user.getPessoa() != null) {
                var vinculos = funcionarioClinicaRepository.findByPessoa(user.getPessoa());
                List<String> clinicaIds = vinculos.stream()
                        .filter(v -> v.getClinica() != null)
                        .map(v -> v.getClinica().getIdClinica())
                        .toList();
                List<ProfissionalClinica> vinculosProf = profissionalClinicaRepository.findByProfissionalIdProf(profissionalId);
                boolean pertence = vinculosProf.stream()
                        .anyMatch(vp -> vp.getClinica() != null && clinicaIds.contains(vp.getClinica().getIdClinica()));
                if (pertence) {
                    return profissional;
                }
            }
            throw new AccessDeniedException("Você não tem permissão para visualizar profissionais de outra clínica.");
        }

        return profissional;
    }

    public List<Profissional> listProfissionais() {
        return profissionalRepository.findAll();
    }

    public List<Profissional> listProfissionaisForUser(String email) {
        if (email == null || email.isBlank()) {
            return listProfissionais();
        }

        User user = userRepository.findByEmailUser(email).orElse(null);
        if (user == null) {
            return listProfissionais();
        }

        boolean isAdmin = user.getPerfis() != null && user.getPerfis().stream()
                .anyMatch(p -> "ADMIN".equalsIgnoreCase(p.getNome()));
        if (isAdmin) {
            return listProfissionais();
        }

        boolean isClinica = user.getPerfis() != null && user.getPerfis().stream()
                .anyMatch(p -> "CLINICA".equalsIgnoreCase(p.getNome()) || "FUNCIONARIO_CLINICA".equalsIgnoreCase(p.getNome()) || "ADMIN_CLINICA".equalsIgnoreCase(p.getNome()));
        if (isClinica) {
            if (user.getPessoa() == null) {
                return List.of();
            }
            var vinculos = funcionarioClinicaRepository.findByPessoa(user.getPessoa());
            List<String> clinicaIds = vinculos.stream()
                    .filter(v -> v.getClinica() != null)
                    .map(v -> v.getClinica().getIdClinica())
                    .toList();
            if (clinicaIds.isEmpty()) {
                return List.of();
            }
            return profissionalClinicaRepository.findProfissionaisByClinicaIds(clinicaIds);
        }

        boolean isProfissional = user.getPerfis() != null && user.getPerfis().stream()
                .anyMatch(p -> "PROFISSIONAL".equalsIgnoreCase(p.getNome()));
        if (isProfissional && user.getPessoa() != null) {
            return profissionalRepository.findByPessoaProf(user.getPessoa())
                    .map(List::of)
                    .orElse(List.of());
        }

        return listProfissionais();
    }

    @Transactional
    public Profissional updateProfissional(String profissionalId, SaveProfissionalDTO saveProfissionalDTO) {
        Profissional profissional = loadProfissional(profissionalId);
        if (saveProfissionalDTO.getConselhoProf() != null && !saveProfissionalDTO.getConselhoProf().isBlank()) {
            profissional.setConselhoProf(saveProfissionalDTO.getConselhoProf().trim());
        }

        Pessoa pessoa = profissional.getPessoaProf();
        if (pessoa != null) {
            if (saveProfissionalDTO.getNomePessoa() != null && !saveProfissionalDTO.getNomePessoa().isBlank()) {
                pessoa.setNomePessoa(saveProfissionalDTO.getNomePessoa().trim());
            }
            if (saveProfissionalDTO.getEmailPessoa() != null) {
                pessoa.setEmailPessoa(saveProfissionalDTO.getEmailPessoa().trim());
            }
            if (saveProfissionalDTO.getCelularPessoa() != null) {
                pessoa.setCelularPessoa(saveProfissionalDTO.getCelularPessoa().trim());
            }
            if (saveProfissionalDTO.getTelefonePessoa() != null) {
                pessoa.setTelefonePessoa(saveProfissionalDTO.getTelefonePessoa().trim());
            }
            if (saveProfissionalDTO.getEnderecoPessoa() != null) {
                pessoa.setEnderecoPessoa(saveProfissionalDTO.getEnderecoPessoa().trim());
            }
            if (saveProfissionalDTO.getRGPessoa() != null) {
                pessoa.setRGPessoa(saveProfissionalDTO.getRGPessoa().trim());
            }
            if (saveProfissionalDTO.getDatanascPessoa() != null) {
                pessoa.setDatanascPessoa(saveProfissionalDTO.getDatanascPessoa());
            }
            pessoaRepository.save(pessoa);

            // Mantém usuário sincronizado
            criarOuAtualizarUsuarioProfissional(pessoa, saveProfissionalDTO.getEmailPessoa());
        }

        // Se informou clínica para vincular na edição
        if (saveProfissionalDTO.getIdClinica() != null && !saveProfissionalDTO.getIdClinica().isBlank()) {
            Clinica clinica = clinicaRepository.findById(saveProfissionalDTO.getIdClinica().trim()).orElse(null);
            if (clinica != null && !profissionalClinicaRepository.existsByProfissionalAndClinica(profissional, clinica)) {
                ProfissionalClinica pc = ProfissionalClinica.builder()
                        .profissional(profissional)
                        .clinica(clinica)
                        .diasAtendimento(saveProfissionalDTO.getDiasAtendimento())
                        .horariosAtendimento(saveProfissionalDTO.getHorariosAtendimento())
                        .build();
                profissionalClinicaRepository.save(pc);
            }
        }

        return profissionalRepository.save(profissional);
    }

    @Transactional
    public void deleteProfissional(String profissionalId) {
        Profissional profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new ProfissionalNotFoundException(profissionalId));
        profissional.desativar();
        profissionalRepository.save(profissional);
    }

    public List<Profissional> listProfissionaisInativosForUser(String email) {
        User user = (email != null && !email.isBlank())
                ? userRepository.findByEmailUser(email).orElse(null)
                : null;
        if (user == null) {
            return List.of();
        }
        boolean isAdmin = user.getPerfis() != null && user.getPerfis().stream()
                .anyMatch(p -> "ADMIN".equalsIgnoreCase(p.getNome()));
        List<Profissional> inativos = profissionalRepository.findAllInactive();
        if (isAdmin) {
            return inativos;
        }
        boolean isClinica = user.getPerfis() != null && user.getPerfis().stream()
                .anyMatch(p -> "CLINICA".equalsIgnoreCase(p.getNome())
                        || "FUNCIONARIO_CLINICA".equalsIgnoreCase(p.getNome())
                        || "ADMIN_CLINICA".equalsIgnoreCase(p.getNome()));
        if (!isClinica || user.getPessoa() == null) {
            throw new AccessDeniedException("Você não tem permissão para listar profissionais inativos.");
        }
        Set<String> clinicaIds = funcionarioClinicaRepository.findByPessoa(user.getPessoa()).stream()
                .map(v -> v.getClinica() != null ? v.getClinica().getIdClinica() : null)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
        if (clinicaIds.isEmpty()) {
            return List.of();
        }
        return inativos.stream()
                .filter(p -> profissionalClinicaRepository.findByProfissionalIdProf(p.getIdProf()).stream()
                        .anyMatch(pc -> pc.getClinica() != null && clinicaIds.contains(pc.getClinica().getIdClinica())))
                .toList();
    }

    @Transactional
    public Profissional reativarProfissional(String profissionalId, String email) {
        Profissional profissional = profissionalRepository.findByIdAnyAtivo(profissionalId)
                .orElseThrow(() -> new ProfissionalNotFoundException("Profissional não encontrado: " + profissionalId));
        if (email != null && !email.isBlank()) {
            User user = userRepository.findByEmailUser(email).orElse(null);
            boolean isAdmin = user != null && user.getPerfis() != null && user.getPerfis().stream()
                    .anyMatch(p -> "ADMIN".equalsIgnoreCase(p.getNome()));
            if (!isAdmin) {
                boolean isClinica = user != null && user.getPerfis() != null && user.getPerfis().stream()
                        .anyMatch(p -> "CLINICA".equalsIgnoreCase(p.getNome())
                                || "FUNCIONARIO_CLINICA".equalsIgnoreCase(p.getNome())
                                || "ADMIN_CLINICA".equalsIgnoreCase(p.getNome()));
                if (!isClinica || user.getPessoa() == null) {
                    throw new AccessDeniedException("Você não tem permissão para reativar profissionais.");
                }
                Set<String> clinicaIds = funcionarioClinicaRepository.findByPessoa(user.getPessoa()).stream()
                        .map(v -> v.getClinica() != null ? v.getClinica().getIdClinica() : null)
                        .filter(java.util.Objects::nonNull)
                        .collect(java.util.stream.Collectors.toSet());
                boolean pertence = profissionalClinicaRepository.findByProfissionalIdProf(profissional.getIdProf()).stream()
                        .anyMatch(pc -> pc.getClinica() != null && clinicaIds.contains(pc.getClinica().getIdClinica()));
                if (!pertence) {
                    throw new AccessDeniedException("Você não tem permissão para reativar este profissional.");
                }
            }
        }
        profissional.setAtivo(true);
        return profissionalRepository.save(profissional);
    }

    public Profissional findByEmail(String email) {
        return profissionalRepository.findByPessoaProfEmailPessoa(email)
                .orElseThrow(() -> new ProfissionalNotFoundException("Profissional não encontrado para o e-mail: " + email));
    }

    private void criarOuAtualizarUsuarioProfissional(Pessoa pessoa, String emailInformado) {
        String email = (emailInformado != null && !emailInformado.isBlank())
                ? emailInformado.trim().toLowerCase()
                : (pessoa.getEmailPessoa() != null ? pessoa.getEmailPessoa().trim().toLowerCase() : null);

        if (email == null || email.isBlank()) {
            return;
        }

        Perfil perfilProf = perfilRepository.findByNome("PROFISSIONAL").orElse(null);
        Set<Perfil> perfis = new HashSet<>();
        if (perfilProf != null) perfis.add(perfilProf);

        userRepository.findByEmailAnyAtivo(email)
                .or(() -> userRepository.findByPessoaIdAnyAtivo(pessoa.getIdPessoa()))
                .ifPresentOrElse(
                        userExistente -> {
                            if (!Boolean.TRUE.equals(userExistente.getAtivo())) {
                                return;
                            }
                            userExistente.setPessoa(pessoa);
                            if (perfilProf != null) {
                                if (userExistente.getPerfis() == null) {
                                    userExistente.setPerfis(new HashSet<>());
                                }
                                userExistente.getPerfis().add(perfilProf);
                            }
                            userRepository.save(userExistente);
                        },
                        () -> {
                            String rawCpf = pessoa.getCPFPessoa();
                            String senhaPadrao = (rawCpf != null && !rawCpf.isBlank())
                                    ? rawCpf.replaceAll("\\D", "")
                                    : "Daily@123";
                            User novoUser = User.builder()
                                    .nomeUser(pessoa.getNomePessoa() != null && !pessoa.getNomePessoa().isBlank() ? pessoa.getNomePessoa() : "Profissional")
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
