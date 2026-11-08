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
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @Transactional
    public Paciente createPaciente(SavePacienteDTO savePacienteDTO) {
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

        if (pacienteRepository.findByPessoaPaciente(pessoa).isPresent()) {
            throw new IllegalArgumentException("Esta pessoa já está cadastrada como paciente.");
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

        // Vínculo opcional inicial com Plano de Saúde se idPlanoSaude informado
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

        // Vínculo opcional inicial com Clínica se idClinica informado
        if (savePacienteDTO.getIdClinica() != null && !savePacienteDTO.getIdClinica().isBlank()) {
            Clinica clinica = clinicaRepository.findById(savePacienteDTO.getIdClinica().trim()).orElse(null);
            if (clinica != null) {
                PacienteClinica pc = PacienteClinica.builder()
                        .paciente(paciente)
                        .clinica(clinica)
                        .dataInicioAtendimento(LocalDate.now())
                        .build();
                pacienteClinicaRepository.save(pc);
            }
        }

        return paciente;
    }

    public Paciente loadPaciente(String pacienteId) {
        return pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new PacienteNotFoundException(pacienteId));
    }

    public List<Paciente> listPacientes() {
        return pacienteRepository.findAll();
    }

    @Transactional
    public Paciente updatePaciente(String pacienteId, SavePacienteDTO savePacienteDTO) {
        Paciente paciente = loadPaciente(pacienteId);
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
