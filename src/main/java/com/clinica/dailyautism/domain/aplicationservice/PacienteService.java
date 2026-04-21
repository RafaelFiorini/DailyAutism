package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.Paciente;
import com.clinica.dailyautism.domain.entity.Pessoa;
import com.clinica.dailyautism.domain.exception.PacienteNotFoundException;
import com.clinica.dailyautism.domain.exception.PessoaNotFoundException;
import com.clinica.dailyautism.domain.repository.PacienteRepository;
import com.clinica.dailyautism.domain.repository.PessoaRepository;
import com.clinica.dailyautism.infrastructure.dto.SavePacienteDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final PessoaRepository pessoaRepository;

    @Transactional
    public Paciente createPaciente(SavePacienteDTO savePacienteDTO) {
        Pessoa pessoa = pessoaRepository.findById(savePacienteDTO.getIdPessoa())
                .orElseThrow(() -> new PessoaNotFoundException(savePacienteDTO.getIdPessoa()));

        Paciente paciente = Paciente.builder()
                .pessoaPaciente(pessoa)
                .apelidoPaciente(savePacienteDTO.getApelidoPaciente())
                .isResponsavel(savePacienteDTO.isResponsavel())
                // planoPaciente e vencimentoLiberacaoPlanoPaciente removidos
                .build();

        return pacienteRepository.save(paciente);
    }

    public Paciente loadPaciente(String pacienteId) {
        return pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new PacienteNotFoundException(pacienteId));
        // corrigido: estava usando PessoaNotFoundException por engano!
    }

    public List<Paciente> listPacientes() {
        return pacienteRepository.findAll();
    }

    @Transactional
    public Paciente updatePaciente(String pacienteId, SavePacienteDTO savePacienteDTO) {
        Paciente paciente = loadPaciente(pacienteId);
        paciente.setApelidoPaciente(savePacienteDTO.getApelidoPaciente());
        paciente.setResponsavel(savePacienteDTO.isResponsavel());
        // planoPaciente e vencimentoLiberacaoPlanoPaciente removidos
        return pacienteRepository.save(paciente);
    }

    @Transactional
    public void deletePaciente(String pacienteId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new PacienteNotFoundException(pacienteId));
        paciente.desativar();
        pacienteRepository.save(paciente);
    }
}