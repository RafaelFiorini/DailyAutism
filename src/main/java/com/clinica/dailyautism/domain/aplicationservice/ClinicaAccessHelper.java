package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.*;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.exception.ClinicaNotFoundException;
import com.clinica.dailyautism.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Helper compartilhado para resolução de clínica e validação de acesso multi-clínica.
 */
@Component
@RequiredArgsConstructor
public class ClinicaAccessHelper {

    private final UserRepository userRepository;
    private final FuncionarioClinicaRepository funcionarioClinicaRepository;
    private final ClinicaRepository clinicaRepository;
    private final ResponsavelRepository responsavelRepository;
    private final ProfissionalClinicaRepository profissionalClinicaRepository;

    public User loadUserOrNull(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return userRepository.findByEmailUser(email).orElse(null);
    }

    public boolean isAdmin(User user) {
        return user != null && user.getPerfis() != null && user.getPerfis().stream()
                .anyMatch(p -> "ADMIN".equalsIgnoreCase(p.getNome()));
    }

    public boolean isStaffClinica(User user) {
        return user != null && user.getPerfis() != null && user.getPerfis().stream()
                .anyMatch(p -> "ADMIN_CLINICA".equalsIgnoreCase(p.getNome())
                        || "FUNCIONARIO_CLINICA".equalsIgnoreCase(p.getNome())
                        || "CLINICA".equalsIgnoreCase(p.getNome()));
    }

    public boolean isResponsavel(User user) {
        return user != null && user.getPerfis() != null && user.getPerfis().stream()
                .anyMatch(p -> "RESPONSAVEL".equalsIgnoreCase(p.getNome()));
    }

    public List<String> listarIdsClinicasDoUsuario(User user) {
        if (user == null || user.getPessoa() == null) {
            return List.of();
        }
        return funcionarioClinicaRepository.findByPessoa(user.getPessoa()).stream()
                .filter(v -> v.getAtivo() == null || v.getAtivo())
                .map(FuncionarioClinica::getClinica)
                .filter(Objects::nonNull)
                .map(Clinica::getIdClinica)
                .distinct()
                .toList();
    }

    /**
     * Valida que o usuário autenticado tem permissão de operar sobre a clínica solicitada.
     * Se idClinica vier vazio para um funcionário de clínica única, resolve automaticamente sua clínica.
     */
    public Clinica validarEAcessarClinica(String idClinica, String emailUsuario) {
        User user = loadUserOrNull(emailUsuario);
        if (isAdmin(user)) {
            if (idClinica == null || idClinica.isBlank()) {
                return clinicaRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new ClinicaNotFoundException("Nenhuma clínica cadastrada."));
            }
            return clinicaRepository.findById(idClinica.trim())
                    .orElseThrow(() -> new ClinicaNotFoundException(idClinica));
        }

        List<String> permitidas = listarIdsClinicasDoUsuario(user);
        if (permitidas.isEmpty()) {
            throw new AccessDeniedException("Usuário não está vinculado a nenhuma clínica.");
        }

        String idAlvo = (idClinica == null || idClinica.isBlank()) ? permitidas.get(0) : idClinica.trim();
        if (!permitidas.contains(idAlvo)) {
            throw new AccessDeniedException("Usuário sem permissão para acessar esta clínica.");
        }

        return clinicaRepository.findById(idAlvo)
                .orElseThrow(() -> new ClinicaNotFoundException(idAlvo));
    }

    public List<Clinica> listarClinicasAcessiveis(String emailUsuario) {
        User user = loadUserOrNull(emailUsuario);
        if (user == null || isAdmin(user)) {
            return clinicaRepository.findAll();
        }
        List<String> ids = listarIdsClinicasDoUsuario(user);
        if (ids.isEmpty()) {
            return List.of();
        }
        return clinicaRepository.findAllById(ids);
    }

    public List<String> listarIdsPacientesDoResponsavel(User user) {
        if (user == null || user.getPessoa() == null) {
            return List.of();
        }
        return responsavelRepository.findByPessoaResponsavel(user.getPessoa())
                .map(r -> r.getPacientes() == null ? List.<String>of() : r.getPacientes().stream()
                        .filter(p -> p.getAtivo() == null || p.getAtivo())
                        .map(Paciente::getIdPaciente)
                        .toList())
                .orElse(List.of());
    }

    /**
     * Resolve a clínica de um compromisso priorizando o vínculo explícito e usando fallback seguro.
     */
    public Clinica resolverClinicaDoCompromisso(Compromisso compromisso) {
        if (compromisso == null) {
            return null;
        }
        if (compromisso.getClinica() != null) {
            return compromisso.getClinica();
        }
        Paciente paciente = compromisso.getPaciente();
        Profissional profissional = compromisso.getProfissional();

        Set<String> clinicasProf = new HashSet<>();
        List<ProfissionalClinica> vinculosProf = (profissional != null && profissional.getIdProf() != null)
                ? profissionalClinicaRepository.findByProfissionalIdProf(profissional.getIdProf())
                : List.of();
        vinculosProf.stream()
                .filter(pc -> pc.getAtivo() == null || pc.getAtivo())
                .map(ProfissionalClinica::getClinica)
                .filter(Objects::nonNull)
                .forEach(c -> clinicasProf.add(c.getIdClinica()));

        if (paciente != null && paciente.getClinicas() != null) {
            List<Clinica> clinicasPaciente = paciente.getClinicas().stream()
                    .filter(pc -> pc.getAtivo() == null || pc.getAtivo())
                    .map(PacienteClinica::getClinica)
                    .filter(Objects::nonNull)
                    .toList();

            if (!clinicasProf.isEmpty()) {
                for (Clinica c : clinicasPaciente) {
                    if (clinicasProf.contains(c.getIdClinica())) {
                        return c;
                    }
                }
            }
            if (!clinicasPaciente.isEmpty()) {
                return clinicasPaciente.get(0);
            }
        }

        return vinculosProf.stream()
                .filter(pc -> pc.getAtivo() == null || pc.getAtivo())
                .map(ProfissionalClinica::getClinica)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}
