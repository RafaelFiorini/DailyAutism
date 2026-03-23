package com.clinica.dailyautism.domain.aplicationservice;

import com.clinica.dailyautism.domain.entity.Compromisso;
import com.clinica.dailyautism.domain.entity.Paciente;
import com.clinica.dailyautism.domain.entity.Periodicidade;
import com.clinica.dailyautism.domain.entity.TipoCompromisso;
import com.clinica.dailyautism.domain.entity.security.User;
import com.clinica.dailyautism.domain.exception.CompromissoNotFoundException;
import com.clinica.dailyautism.domain.exception.PacienteNotFoundException;
import com.clinica.dailyautism.domain.exception.PeriodicidadeNotFoundException;
import com.clinica.dailyautism.domain.exception.TipoCompromissoNotFoundException;
import com.clinica.dailyautism.domain.repository.CompromissoRepository;
import com.clinica.dailyautism.domain.repository.PacienteRepository;
import com.clinica.dailyautism.domain.repository.PeriodicidadeRepository;
import com.clinica.dailyautism.domain.repository.TipoCompromissoRepository;
import com.clinica.dailyautism.domain.repository.UserRepository;
import com.clinica.dailyautism.infraestructure.dto.SaveCompromissoDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CompromissoService {

    private final CompromissoRepository compromissoRepository;
    private final PacienteRepository pacienteRepository;
    private final TipoCompromissoRepository tipoCompromissoRepository;
    private final PeriodicidadeRepository periodicidadeRepository;
    private final UserRepository userRepository;

    @Transactional
    public Compromisso createCompromisso(SaveCompromissoDTO saveCompromissoDTO) {
        Paciente paciente = pacienteRepository.findById(saveCompromissoDTO.getIdPaciente())
                .orElseThrow(() -> new PacienteNotFoundException(saveCompromissoDTO.getIdPaciente()));

        TipoCompromisso tipoCompromisso = tipoCompromissoRepository.findById(saveCompromissoDTO.getIdTipoCompromisso())
                .orElseThrow(() -> new TipoCompromissoNotFoundException("Tipo de compromisso não encontrado: " + saveCompromissoDTO.getIdTipoCompromisso()));

        Periodicidade periodicidade = periodicidadeRepository.findById(saveCompromissoDTO.getIdPeriodicidade())
                .orElseThrow(() -> new PeriodicidadeNotFoundException("Periodicidade não encontrada: " + saveCompromissoDTO.getIdPeriodicidade()));

        User user = userRepository.findById(saveCompromissoDTO.getIdUser())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + saveCompromissoDTO.getIdUser()));

        Compromisso compromisso = Compromisso.builder()
                .tituloCompromisso(saveCompromissoDTO.getTituloCompromisso())
                .descricaoCompromisso(saveCompromissoDTO.getDescricaoCompromisso())
                .dataHoraCompromisso(saveCompromissoDTO.getDataHoraCompromisso())
                .localCompromisso(saveCompromissoDTO.getLocalCompromisso())
                .aprovado(false) // sempre começa como não aprovado
                .paciente(paciente)
                .tipoCompromisso(tipoCompromisso)
                .periodicidade(periodicidade)
                .criadoPor(user)
                .build();

        return compromissoRepository.save(compromisso);
    }

    public Compromisso loadCompromisso(String compromissoId) {
        return compromissoRepository.findById(compromissoId)
                .orElseThrow(() -> new CompromissoNotFoundException("Compromisso não encontrado: " + compromissoId));
    }

    public List<Compromisso> listCompromissosByPaciente(String pacienteId) {
        return compromissoRepository.findByPacienteIdPacienteOrderByDataHoraCompromissoAsc(pacienteId);
    }

    @Transactional
    public Compromisso updateCompromisso(String compromissoId, SaveCompromissoDTO saveCompromissoDTO) {
        Compromisso compromisso = loadCompromisso(compromissoId);
        compromisso.setTituloCompromisso(saveCompromissoDTO.getTituloCompromisso());
        compromisso.setDescricaoCompromisso(saveCompromissoDTO.getDescricaoCompromisso());
        compromisso.setDataHoraCompromisso(saveCompromissoDTO.getDataHoraCompromisso());
        compromisso.setLocalCompromisso(saveCompromissoDTO.getLocalCompromisso());
        return compromissoRepository.save(compromisso);
    }

    @Transactional
    public Compromisso aprovarCompromisso(String compromissoId) {
        Compromisso compromisso = loadCompromisso(compromissoId);
        compromisso.setAprovado(true);
        return compromissoRepository.save(compromisso);
    }

    @Transactional
    public void deleteCompromisso(String compromissoId) {
        Compromisso compromisso = compromissoRepository.findById(compromissoId)
                .orElseThrow(() -> new CompromissoNotFoundException("Compromisso não encontrado: " + compromissoId));
        compromisso.desativar();
        compromissoRepository.save(compromisso);
    }
}