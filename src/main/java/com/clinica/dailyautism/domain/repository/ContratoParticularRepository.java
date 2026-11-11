package com.clinica.dailyautism.domain.repository;

import com.clinica.dailyautism.domain.entity.ContratoParticular;
import com.clinica.dailyautism.domain.entity.StatusContratoParticular;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContratoParticularRepository extends JpaRepository<ContratoParticular, String> {

    List<ContratoParticular> findByClinicaIdClinicaOrderByDataInicioDesc(String idClinica);

    List<ContratoParticular> findByPacienteIdPacienteOrderByDataInicioDesc(String idPaciente);

    List<ContratoParticular> findByPacienteIdPacienteInOrderByDataInicioDesc(List<String> idsPacientes);

    List<ContratoParticular> findByPacienteIdPacienteAndClinicaIdClinicaAndStatusContratoOrderByDataInicioAsc(
            String idPaciente, String idClinica, StatusContratoParticular statusContrato
    );
}
