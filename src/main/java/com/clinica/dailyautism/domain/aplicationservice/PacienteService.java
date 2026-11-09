package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.*;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.exception.PacienteNotFoundException;
import com.clinica.dailyautism.domain.exception.PessoaNotFoundException;
import com.clinica.dailyautism.domain.repository.*;
import com.clinica.dailyautism.infrastructure.dto.SavePacienteCompletoDTO;
import com.clinica.dailyautism.infrastructure.dto.SavePacienteDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final PessoaRepository pessoaRepository;
    private final UserRepository userRepository;
    private final ResponsavelRepository responsavelRepository;
    private final PlanoSaudeRepository planoSaudeRepository;
    private final ClinicaRepository clinicaRepository;
    private final PacientePlanoSaudeRepository pacientePlanoSaudeRepository;
    private final PacienteClinicaRepository pacienteClinicaRepository;
    private final CompromissoRepository compromissoRepository;
    private final ProfissionalPacienteRepository profissionalPacienteRepository;
    private final FuncionarioClinicaRepository funcionarioClinicaRepository;

    @Transactional
    public Paciente createPaciente(SavePacienteDTO savePacienteDTO) {
        return createPaciente(savePacienteDTO, null);
    }

    @Transactional
    public Paciente createPaciente(SavePacienteDTO savePacienteDTO, String emailDoUsuario) {
        Pessoa pessoa;
        String idPessoa = savePacienteDTO.getIdPessoa();
        String rawCpf = savePacienteDTO.getCPFPessoa();
        String cpf = (rawCpf != null) ? rawCpf.replaceAll("\\D", "").trim() : "";

        if (idPessoa != null && !idPessoa.isBlank()) {
            pessoa = pessoaRepository.findById(idPessoa.trim())
                    .orElseThrow(() -> new PessoaNotFoundException(idPessoa));
        } else if (!cpf.isBlank()) {
            final String cpfLimpo = cpf;
            pessoa = pessoaRepository.findByCPFPessoa(cpfLimpo)
                    .orElseGet(() -> {
                        Pessoa nova = Pessoa.builder()
                                .nomePessoa(savePacienteDTO.getNomePessoa() != null ? savePacienteDTO.getNomePessoa().trim() : "")
                                .CPFPessoa(cpfLimpo)
                                .datanascPessoa(savePacienteDTO.getDatanascPessoa())
                                .RGPessoa(savePacienteDTO.getRGPessoa())
                                .enderecoPessoa(savePacienteDTO.getEnderecoPessoa())
                                .telefonePessoa(savePacienteDTO.getTelefonePessoa())
                                .celularPessoa(savePacienteDTO.getCelularPessoa())
                                .emailPessoa(savePacienteDTO.getEmailPessoa())
                                .build();
                        return pessoaRepository.save(nova);
                    });
        } else {
            throw new IllegalArgumentException("Informe a pessoa existente ou os dados pessoais com CPF");
        }

        // Descobrir a clínica alvo
        String idClinicaAlvo = savePacienteDTO.getIdClinica();
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
            clinicaAlvo = clinicaRepository.findById(idClinicaAlvo.trim()).orElse(null);
        }

        var pacExistenteOpt = pacienteRepository.findByPessoaPaciente(pessoa);
        if (pacExistenteOpt.isPresent()) {
            Paciente pacExistente = pacExistenteOpt.get();
            if (clinicaAlvo != null) {
                if (pacienteClinicaRepository.existsByPacienteAndClinica(pacExistente, clinicaAlvo)) {
                    throw new IllegalArgumentException("Este paciente já está cadastrado nesta clínica.");
                }
                PacienteClinica pc = PacienteClinica.builder()
                        .paciente(pacExistente)
                        .clinica(clinicaAlvo)
                        .dataInicioAtendimento(LocalDate.now())
                        .build();
                pacienteClinicaRepository.save(pc);

                // Plano de saúde opcional
                if (savePacienteDTO.getIdPlanoSaude() != null && !savePacienteDTO.getIdPlanoSaude().isBlank()) {
                    PlanoSaude plano = planoSaudeRepository.findById(savePacienteDTO.getIdPlanoSaude().trim()).orElse(null);
                    if (plano != null) {
                        PacientePlanoSaude pps = PacientePlanoSaude.builder()
                                .paciente(pacExistente)
                                .planoSaude(plano)
                                .numeroCarteirinha(savePacienteDTO.getNumeroCarteirinha())
                                .build();
                        pacientePlanoSaudeRepository.save(pps);
                    }
                }
                return pacExistente;
            } else {
                throw new IllegalArgumentException("Esta pessoa já está cadastrada como paciente.");
            }
        }

        boolean isResp = Boolean.TRUE.equals(savePacienteDTO.getIsResponsavel());

        Paciente paciente = Paciente.builder()
                .pessoaPaciente(pessoa)
                .apelidoPaciente(savePacienteDTO.getApelidoPaciente())
                .isResponsavel(isResp)
                .planos(new ArrayList<>())
                .clinicas(new ArrayList<>())
                .build();

        paciente = pacienteRepository.save(paciente);
        final Paciente finalPaciente = paciente;

        // Se marcado como isResponsavel, garante o registro em Responsavel
        if (isResp) {
            if (!responsavelRepository.existsByPessoaResponsavel(pessoa)) {
                Responsavel resp = Responsavel.builder()
                        .pessoaResponsavel(pessoa)
                        .pacientes(new ArrayList<>(List.of(paciente)))
                        .build();
                responsavelRepository.save(resp);
            } else {
                Responsavel resp = responsavelRepository.findByPessoaResponsavel(pessoa).orElse(null);
                if (resp != null && (resp.getPacientes() == null || !resp.getPacientes().contains(paciente))) {
                    if (resp.getPacientes() == null) {
                        resp.setPacientes(new ArrayList<>());
                    }
                    resp.getPacientes().add(paciente);
                    responsavelRepository.save(resp);
                }
            }
        }

        // Se o usuário logado for RESPONSAVEL, vincula automaticamente o paciente a ele
        if (emailDoUsuario != null && !emailDoUsuario.isBlank()) {
            userRepository.findByEmailUser(emailDoUsuario).ifPresent(u -> {
                boolean userIsResp = u.getPerfis() != null && u.getPerfis().stream()
                        .anyMatch(p -> "RESPONSAVEL".equalsIgnoreCase(p.getNome()));
                if (userIsResp && u.getPessoa() != null) {
                    Responsavel respLogado = responsavelRepository.findByPessoaResponsavel(u.getPessoa()).orElse(null);
                    if (respLogado != null) {
                        if (respLogado.getPacientes() == null) {
                            respLogado.setPacientes(new ArrayList<>());
                        }
                        if (!respLogado.getPacientes().contains(finalPaciente)) {
                            respLogado.getPacientes().add(finalPaciente);
                            responsavelRepository.save(respLogado);
                        }
                    }
                }
            });
        }

        // Vínculo opcional inicial com Plano de Saúde
        if (savePacienteDTO.getIdPlanoSaude() != null && !savePacienteDTO.getIdPlanoSaude().isBlank()) {
            PlanoSaude plano = planoSaudeRepository.findById(savePacienteDTO.getIdPlanoSaude().trim()).orElse(null);
            if (plano != null) {
                PacientePlanoSaude pps = PacientePlanoSaude.builder()
                        .paciente(paciente)
                        .planoSaude(plano)
                        .numeroCarteirinha(savePacienteDTO.getNumeroCarteirinha())
                        .build();
                pacientePlanoSaudeRepository.save(pps);
            }
        }

        // Vínculo com Clínica
        if (clinicaAlvo != null) {
            PacienteClinica pc = PacienteClinica.builder()
                    .paciente(paciente)
                    .clinica(clinicaAlvo)
                    .dataInicioAtendimento(LocalDate.now())
                    .build();
            pacienteClinicaRepository.save(pc);
        }

        return paciente;
    }

    public Paciente loadPaciente(String pacienteId) {
        return pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new PacienteNotFoundException(pacienteId));
    }

    public Paciente loadPacienteForUser(String pacienteId, String email) {
        Paciente paciente = loadPaciente(pacienteId);
        if (email == null || email.isBlank()) {
            return paciente;
        }

        User user = userRepository.findByEmailUser(email).orElse(null);
        if (user == null) {
            return paciente;
        }

        boolean isAdmin = user.getPerfis() != null && user.getPerfis().stream()
                .anyMatch(p -> "ADMIN".equalsIgnoreCase(p.getNome()));
        if (isAdmin) {
            return paciente;
        }

        boolean isResponsavel = user.getPerfis() != null && user.getPerfis().stream()
                .anyMatch(p -> "RESPONSAVEL".equalsIgnoreCase(p.getNome()));
        if (isResponsavel) {
            if (user.getPessoa() != null) {
                Responsavel resp = responsavelRepository.findByPessoaResponsavel(user.getPessoa()).orElse(null);
                if (resp != null && resp.getPacientes() != null) {
                    boolean autorizado = resp.getPacientes().stream()
                            .anyMatch(p -> p.getIdPaciente().equals(pacienteId));
                    if (autorizado) {
                        return paciente;
                    }
                }
            }
            throw new AccessDeniedException("Você não tem permissão para visualizar este paciente.");
        }

        boolean isProfissional = user.getPerfis() != null && user.getPerfis().stream()
                .anyMatch(p -> "PROFISSIONAL".equalsIgnoreCase(p.getNome()));
        if (isProfissional) {
            boolean temCompromisso = compromissoRepository.findByCriadoPorIdUser(user.getIdUser()).stream()
                    .anyMatch(c -> c.getPaciente() != null && c.getPaciente().getIdPaciente().equals(pacienteId));
            boolean temVinculo = false;
            if (user.getPessoa() != null && user.getPessoa().getProfissional() != null) {
                String idProf = user.getPessoa().getProfissional().getIdProf();
                temVinculo = profissionalPacienteRepository.existsByProfissionalIdProfAndPacienteIdPaciente(idProf, pacienteId);
            }
            if (temCompromisso || temVinculo) {
                return paciente;
            }
            throw new AccessDeniedException("Você não tem permissão para visualizar este paciente.");
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
                boolean pertence = paciente.getClinicas() != null && paciente.getClinicas().stream()
                        .anyMatch(pc -> pc.getClinica() != null && clinicaIds.contains(pc.getClinica().getIdClinica()));
                if (pertence) {
                    return paciente;
                }
            }
            throw new AccessDeniedException("Você não tem permissão para visualizar pacientes de outra clínica.");
        }

        return paciente;
    }

    public List<Paciente> listPacientes() {
        return pacienteRepository.findAll();
    }

    public List<Paciente> listPacientesForUser(String email) {
        if (email == null || email.isBlank()) {
            return listPacientes();
        }

        User user = userRepository.findByEmailUser(email).orElse(null);
        if (user == null) {
            return listPacientes();
        }

        boolean isAdmin = user.getPerfis() != null && user.getPerfis().stream()
                .anyMatch(p -> "ADMIN".equalsIgnoreCase(p.getNome()));
        if (isAdmin) {
            return listPacientes();
        }

        boolean isResponsavel = user.getPerfis() != null && user.getPerfis().stream()
                .anyMatch(p -> "RESPONSAVEL".equalsIgnoreCase(p.getNome()));
        if (isResponsavel) {
            if (user.getPessoa() == null) {
                return List.of();
            }
            Responsavel resp = responsavelRepository.findByPessoaResponsavel(user.getPessoa()).orElse(null);
            if (resp == null || resp.getPacientes() == null) {
                return List.of();
            }
            return resp.getPacientes().stream()
                    .filter(p -> p.getAtivo() == null || p.getAtivo())
                    .toList();
        }

        boolean isProfissional = user.getPerfis() != null && user.getPerfis().stream()
                .anyMatch(p -> "PROFISSIONAL".equalsIgnoreCase(p.getNome()));
        if (isProfissional) {
            Set<String> pacienteIds = new HashSet<>();

            // 1. Pacientes dos compromissos agendados pelo profissional
            List<Compromisso> compromissos = compromissoRepository.findByCriadoPorIdUser(user.getIdUser());
            if (compromissos != null) {
                for (Compromisso c : compromissos) {
                    if (c.getPaciente() != null && (c.getAtivo() == null || c.getAtivo())) {
                        pacienteIds.add(c.getPaciente().getIdPaciente());
                    }
                }
            }

            // 2. Pacientes vinculados diretamente via ProfissionalPaciente
            if (user.getPessoa() != null && user.getPessoa().getProfissional() != null) {
                String idProf = user.getPessoa().getProfissional().getIdProf();
                List<ProfissionalPaciente> vinculos = profissionalPacienteRepository.findByProfissionalIdProf(idProf);
                if (vinculos != null) {
                    for (ProfissionalPaciente vp : vinculos) {
                        if (vp.getPaciente() != null && (vp.getAtivo() == null || vp.getAtivo())) {
                            pacienteIds.add(vp.getPaciente().getIdPaciente());
                        }
                    }
                }
            }

            if (pacienteIds.isEmpty()) {
                return List.of();
            }
            return pacienteRepository.findAllById(pacienteIds).stream()
                    .filter(p -> p.getAtivo() == null || p.getAtivo())
                    .toList();
        }

        boolean isClinica = user.getPerfis() != null && user.getPerfis().stream()
                .anyMatch(p -> "CLINICA".equalsIgnoreCase(p.getNome()) || "FUNCIONARIO_CLINICA".equalsIgnoreCase(p.getNome()) || "ADMIN_CLINICA".equalsIgnoreCase(p.getNome()));
        if (isClinica) {
            if (user.getPessoa() != null) {
                var vinculos = funcionarioClinicaRepository.findByPessoa(user.getPessoa());
                if (!vinculos.isEmpty()) {
                    List<String> clinicaIds = vinculos.stream()
                            .filter(v -> v.getClinica() != null)
                            .map(v -> v.getClinica().getIdClinica())
                            .toList();
                    if (!clinicaIds.isEmpty()) {
                        return pacienteRepository.findByClinicaIds(clinicaIds).stream()
                                .filter(p -> p.getAtivo() == null || p.getAtivo())
                                .toList();
                    }
                }
            }
            return List.of();
        }

        return listPacientes();
    }

    @Transactional
    public Paciente updatePaciente(String pacienteId, SavePacienteDTO savePacienteDTO) {
        return updatePaciente(pacienteId, savePacienteDTO, null);
    }

    @Transactional
    public Paciente updatePaciente(String pacienteId, SavePacienteDTO savePacienteDTO, String emailDoUsuario) {
        // Valida se o usuário tem permissão para acessar e atualizar este paciente
        Paciente paciente = (emailDoUsuario != null && !emailDoUsuario.isBlank())
                ? loadPacienteForUser(pacienteId, emailDoUsuario)
                : loadPaciente(pacienteId);

        paciente.setApelidoPaciente(savePacienteDTO.getApelidoPaciente());
        boolean isResp = Boolean.TRUE.equals(savePacienteDTO.getIsResponsavel());
        paciente.setResponsavel(isResp);

        Pessoa pessoa = paciente.getPessoaPaciente();
        if (pessoa != null) {
            if (savePacienteDTO.getNomePessoa() != null && !savePacienteDTO.getNomePessoa().isBlank()) {
                pessoa.setNomePessoa(savePacienteDTO.getNomePessoa().trim());
            }
            if (savePacienteDTO.getDatanascPessoa() != null) {
                pessoa.setDatanascPessoa(savePacienteDTO.getDatanascPessoa());
            }
            if (savePacienteDTO.getRGPessoa() != null) {
                pessoa.setRGPessoa(savePacienteDTO.getRGPessoa().trim());
            }
            if (savePacienteDTO.getEnderecoPessoa() != null) {
                pessoa.setEnderecoPessoa(savePacienteDTO.getEnderecoPessoa().trim());
            }
            if (savePacienteDTO.getTelefonePessoa() != null) {
                pessoa.setTelefonePessoa(savePacienteDTO.getTelefonePessoa().trim());
            }
            if (savePacienteDTO.getCelularPessoa() != null) {
                pessoa.setCelularPessoa(savePacienteDTO.getCelularPessoa().trim());
            }
            if (savePacienteDTO.getEmailPessoa() != null) {
                pessoa.setEmailPessoa(savePacienteDTO.getEmailPessoa().trim());
            }
            pessoaRepository.save(pessoa);

            if (isResp) {
                if (!responsavelRepository.existsByPessoaResponsavel(pessoa)) {
                    Responsavel resp = Responsavel.builder()
                            .pessoaResponsavel(pessoa)
                            .pacientes(new ArrayList<>(List.of(paciente)))
                            .build();
                    responsavelRepository.save(resp);
                } else {
                    Responsavel resp = responsavelRepository.findByPessoaResponsavel(pessoa).orElse(null);
                    if (resp != null && (resp.getPacientes() == null || !resp.getPacientes().contains(paciente))) {
                        if (resp.getPacientes() == null) {
                            resp.setPacientes(new ArrayList<>());
                        }
                        resp.getPacientes().add(paciente);
                        responsavelRepository.save(resp);
                    }
                }
            }
        }

        // Vínculo opcional com Plano de Saúde se idPlanoSaude informado
        if (savePacienteDTO.getIdPlanoSaude() != null && !savePacienteDTO.getIdPlanoSaude().isBlank()) {
            PlanoSaude plano = planoSaudeRepository.findById(savePacienteDTO.getIdPlanoSaude().trim()).orElse(null);
            if (plano != null) {
                boolean jaTem = paciente.getPlanos() != null && paciente.getPlanos().stream()
                        .anyMatch(p -> p.getPlanoSaude() != null && p.getPlanoSaude().getIdPlanoSaude().equals(plano.getIdPlanoSaude()));
                if (!jaTem) {
                    PacientePlanoSaude pps = PacientePlanoSaude.builder()
                            .paciente(paciente)
                            .planoSaude(plano)
                            .numeroCarteirinha(savePacienteDTO.getNumeroCarteirinha())
                            .build();
                    pacientePlanoSaudeRepository.save(pps);
                }
            }
        }

        // Vínculo opcional com Clínica se idClinica informado
        if (savePacienteDTO.getIdClinica() != null && !savePacienteDTO.getIdClinica().isBlank()) {
            Clinica clinica = clinicaRepository.findById(savePacienteDTO.getIdClinica().trim()).orElse(null);
            if (clinica != null) {
                boolean jaTem = paciente.getClinicas() != null && paciente.getClinicas().stream()
                        .anyMatch(c -> c.getClinica() != null && c.getClinica().getIdClinica().equals(clinica.getIdClinica()));
                if (!jaTem) {
                    PacienteClinica pc = PacienteClinica.builder()
                            .paciente(paciente)
                            .clinica(clinica)
                            .dataInicioAtendimento(LocalDate.now())
                            .build();
                    pacienteClinicaRepository.save(pc);
                }
            }
        }

        return pacienteRepository.save(paciente);
    }

    @Transactional
    public void deletePaciente(String pacienteId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new PacienteNotFoundException(pacienteId));
        paciente.desativar();
        pacienteRepository.save(paciente);
    }

    @Transactional
    public Paciente createPacienteCompleto(
            SavePacienteCompletoDTO dto,
            String emailDoToken) {

        Pessoa pessoa = pessoaRepository.findByCPFPessoa(dto.getCPFPessoa())
                .orElseGet(() -> {
                    Pessoa nova = Pessoa.builder()
                            .nomePessoa(dto.getNomePessoa())
                            .CPFPessoa(dto.getCPFPessoa())
                            .datanascPessoa(dto.getDatanascPessoa())
                            .RGPessoa(dto.getRGPessoa())
                            .enderecoPessoa(dto.getEnderecoPessoa())
                            .telefonePessoa(dto.getTelefonePessoa())
                            .celularPessoa(dto.getCelularPessoa())
                            .emailPessoa(dto.getEmailPessoa())
                            .build();
                    return pessoaRepository.save(nova);
                });

        Paciente paciente = Paciente.builder()
                .pessoaPaciente(pessoa)
                .apelidoPaciente(dto.getApelidoPaciente())
                .isResponsavel(dto.isResponsavel())
                .build();

        paciente = pacienteRepository.save(paciente);

        // SEMPRE vincula ao responsável do usuário logado
        User user = userRepository.findByEmailUser(emailDoToken)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Responsavel responsavel = responsavelRepository
                .findByPessoaResponsavel(user.getPessoa())
                .orElseThrow(() -> new RuntimeException("Responsável não encontrado"));

        responsavel.getPacientes().add(paciente);
        responsavelRepository.save(responsavel);

        return paciente;
    }
}
