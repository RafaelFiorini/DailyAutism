package com.clinica.dailyautism.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DadosUsuarioRecuperacaoDTO {
    private String nome;
    private String email;
    private String cpf;
    private String telefone;
    private String celular;
    private String endereco;
}
