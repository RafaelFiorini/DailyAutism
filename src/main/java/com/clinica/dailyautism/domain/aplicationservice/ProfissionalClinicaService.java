// ProfissionalClinicaService
package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.*;
import com.clinica.dailyautism.domain.exception.*;
import com.clinica.dailyautism.domain.repository.*;
import com.clinica.dailyautism.infrastructure.dto.SaveProfissionalClinicaDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ProfissionalClinicaService {

    private final ProfissionalClinicaRepository profissionalClinicaRepository;
    private final ProfissionalRepository profissionalRepository;
    private final ClinicaRepository clinicaRepository;

    @Transactional
    public ProfissionalClinica vincular(SaveProfissionalClinicaDTO dto) {
        Profissional profissional = profissionalRepository.findById(dto.getIdProf())
                .orElseThrow(() -> new ProfissionalNotFoundException(dto.getIdProf()));
        Clinica clinica = clinicaRepository.findById(dto.getIdClinica())
                .orElseThrow(() -> new ClinicaNotFoundException(dto.getIdClinica()));

        ProfissionalClinica vinculo = ProfissionalClinica.builder()
                .profissional(profissional)
                .clinica(clinica)
                .diasAtendimento(dto.getDiasAtendimento())
                .horariosAtendimento(dto.getHorariosAtendimento())
                .build();
        return profissionalClinicaRepository.save(vinculo);
    }

    public List<ProfissionalClinica> listarPorProfissional(String idProf) {
        return profissionalClinicaRepository.findByProfissionalIdProf(idProf);
    }

    public List<ProfissionalClinica> listarPorClinica(String idClinica) {
        return profissionalClinicaRepository.findByClinicaIdClinica(idClinica);
    }

    @Transactional
    public ProfissionalClinica atualizar(String idVinculo, SaveProfissionalClinicaDTO dto) {
        ProfissionalClinica vinculo = profissionalClinicaRepository.findById(idVinculo)
                .orElseThrow(() -> new RuntimeException("Vínculo não encontrado: " + idVinculo));
        vinculo.setDiasAtendimento(dto.getDiasAtendimento());
        vinculo.setHorariosAtendimento(dto.getHorariosAtendimento());
        return profissionalClinicaRepository.save(vinculo);
    }

    @Transactional
    public void desvincular(String idVinculo) {
        ProfissionalClinica vinculo = profissionalClinicaRepository.findById(idVinculo)
                .orElseThrow(() -> new RuntimeException("Vínculo não encontrado: " + idVinculo));
        vinculo.desativar();
        profissionalClinicaRepository.save(vinculo);
    }
}