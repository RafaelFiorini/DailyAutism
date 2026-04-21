package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.Clinica;
import com.clinica.dailyautism.domain.exception.ClinicaNotFoundException;
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

    @Transactional
    public Clinica updateClinica(String clinicaId, SaveClinicaDTO dto) {
        Clinica clinica = loadClinica(clinicaId);
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
}