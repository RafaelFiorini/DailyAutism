package com.clinica.dailyautism.infrastructure.dto;

import com.clinica.dailyautism.domain.entity.TipoLiberacaoGuia;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavePacientePlanoSaudeDTO {
    private String idPaciente;
    private String idPlanoSaude;
    private String numeroCarteirinha;
    private LocalDate validadeCarteirinha;
    private String numeroGuia;
    private TipoLiberacaoGuia tipoLiberacao;
    private LocalDate dataInicioLiberacao;
    private LocalDate vencimentoLiberacao;
    private Integer quantidadeSessoesLiberadas;
    private String idPeriodicidadeReferencia;
}
