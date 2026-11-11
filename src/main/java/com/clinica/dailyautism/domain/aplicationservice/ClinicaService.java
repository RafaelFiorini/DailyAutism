package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.Clinica;
import com.clinica.dailyautism.domain.exception.ClinicaNotFoundException;
import com.clinica.dailyautism.domain.exception.InactiveResourceConflictException;
import com.clinica.dailyautism.domain.repository.ClinicaRepository;
import com.clinica.dailyautism.infrastructure.dto.SaveClinicaDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ClinicaService {

    private final ClinicaRepository clinicaRepository;

    @Transactional
    public Clinica createClinica(SaveClinicaDTO dto) {
        validarCnpjDisponivel(dto.getCnpjClinica(), null);
        Clinica clinica = Clinica.builder()
                .nomeClinica(dto.getNomeClinica())
                .cnpjClinica(dto.getCnpjClinica())
                .enderecoClinica(dto.getEnderecoClinica())
                .telefoneClinica(dto.getTelefoneClinica())
                .whatsappClinica(dto.getWhatsappClinica())
                .emailClinica(dto.getEmailClinica())
                .build();
        return clinicaRepository.save(clinica);
    }

    public Clinica loadClinica(String clinicaId) {
        return clinicaRepository.findById(clinicaId)
                .orElseThrow(() -> new ClinicaNotFoundException(clinicaId));
    }

    public List<Clinica> listClinicas() {
        return clinicaRepository.findAll();
    }

    public List<Clinica> listClinicasInativas() {
        return clinicaRepository.findAllInactive();
    }

    @Transactional
    public Clinica updateClinica(String clinicaId, SaveClinicaDTO dto) {
        Clinica clinica = loadClinica(clinicaId);
        validarCnpjDisponivel(dto.getCnpjClinica(), clinica.getIdClinica());
        clinica.setNomeClinica(dto.getNomeClinica());
        clinica.setCnpjClinica(dto.getCnpjClinica());
        clinica.setEnderecoClinica(dto.getEnderecoClinica());
        clinica.setTelefoneClinica(dto.getTelefoneClinica());
        clinica.setWhatsappClinica(dto.getWhatsappClinica());
        clinica.setEmailClinica(dto.getEmailClinica());
        return clinicaRepository.save(clinica);
    }

    @Transactional
    public void deleteClinica(String clinicaId) {
        Clinica clinica = clinicaRepository.findById(clinicaId)
                .orElseThrow(() -> new ClinicaNotFoundException(clinicaId));
        clinica.desativar();
        clinicaRepository.save(clinica);
    }

    @Transactional
    public Clinica reativarClinica(String clinicaId) {
        Clinica clinica = clinicaRepository.findByIdAnyAtivo(clinicaId)
                .orElseThrow(() -> new ClinicaNotFoundException(clinicaId));
        clinica.setAtivo(true);
        return clinicaRepository.save(clinica);
    }

    private void validarCnpjDisponivel(String cnpj, String ignoreClinicaId) {
        if (cnpj == null || cnpj.isBlank()) {
            return;
        }
        String cnpjLimpo = cnpj.replaceAll("\\D", "").trim();
        if (cnpjLimpo.isBlank()) {
            return;
        }
        clinicaRepository.findByCnpjAnyAtivo(cnpjLimpo).ifPresent(existente -> {
            if (ignoreClinicaId != null && ignoreClinicaId.equals(existente.getIdClinica())) {
                return;
            }
            if (!Boolean.TRUE.equals(existente.getAtivo())) {
                throw new InactiveResourceConflictException(
                        "CLINICA",
                        existente.getIdClinica(),
                        existente.getNomeClinica(),
                        "Já existe uma clínica desativada com este CNPJ (" + existente.getNomeClinica() + "). Reative o cadastro existente."
                );
            }
            throw new IllegalArgumentException("Já existe uma clínica ativa cadastrada com este CNPJ.");
        });
    }
}