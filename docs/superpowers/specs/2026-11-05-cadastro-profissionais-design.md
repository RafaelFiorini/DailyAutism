# Design Doc: Cadastro e Gestão de Profissionais (Painel Administrativo)

**Data:** 05/11/2026  
**Status:** Proposto  
**Autor:** Antigravity (Google DeepMind) & Rafael Fiorini  

---

## 1. Contexto e Motivação

O sistema DailyAutism gerencia atendimentos clínicos a pacientes no espectro autista. O painel administrativo já possui autenticação JWT para administradores e rotas protegidas com layout de navegação. 

O próximo passo essencial é a **gestão de profissionais de saúde (médicos, psicólogos, terapeutas ocupacionais, fonoaudiólogos, etc.)**, permitindo que o administrador cadastre novos profissionais e consulte a equipe ativa na clínica.

No modelo de domínio do DailyAutism, **a `Pessoa` é o centro da identidade civil**: um indivíduo pode ser tanto um profissional de saúde quanto o responsável financeiro/familiar de um paciente. Portanto, o cadastro de profissional não pode gerar registros duplicados de pessoas para o mesmo CPF.

---

## 2. Metas, Não-Metas e Anti-Metas

### Metas (Goals)
1. **Listagem de Profissionais:** Tela administrativa em `/admin/profissionais` com tabela paginada/filtrável contendo Nome, Conselho Profissional (CRM/CRP/CREFITO/etc.), E-mail, Telefone e Status.
2. **Formulário Unificado:** Cadastro de profissional onde o administrador preenche dados pessoais e profissionais em uma única etapa.
3. **Reaproveitamento Inteligente de Pessoa:** No backend, ao salvar um profissional, verificar se o CPF já existe na tabela `Pessoa`. Se existir, reaproveitar o registro; se for novo, cadastrar a `Pessoa` e o `Profissional` em uma transação atômica.
4. **Validações no Frontend e Backend:** Formato de CPF, e-mail válido, campos obrigatórios, bloqueio de duplicação de vínculo de profissional.
5. **Cobertura de Testes Automatizados:** Testes unitários para o service de profissionais, tela de listagem e modal de cadastro via Vitest e React Testing Library.

### Não-Metas (Non-Goals)
- Upload de fotos ou anexos de diplomas nesta etapa (manter escopo enxuto).
- Gestão de agendas e marcação de consultas (será implementado no módulo específico de Agenda).
- Criação imediata de credenciais de login para o profissional (pode ser habilitado via tela de Usuários).

### Anti-Metas (Anti-Goals)
- Duplicar entidades de `Pessoa` para o mesmo CPF.
- Deixar o banco em estado inconsistente caso o cadastro falhe (a operação deve ser 100% transacional).

---

## 3. Abordagens Consideradas

### Opção 1: Endpoint Unificado Atômico no Backend + Frontend com Formulário Completo (Escolhida)
- **Descrição:** O backend aprimora o DTO `SaveProfissionalDTO` para aceitar dados pessoais e dados do conselho. Se o CPF já existir, utiliza a `Pessoa` existente; caso contrário, persiste a nova `Pessoa` e o `Profissional` numa única transação (`@Transactional`).
- **Por que foi escolhida:** Garante atomicidade, simplifica a chamada no frontend (1 requisição) e atende perfeitamente ao caso de uso de terapeutas que já existem no sistema como responsáveis.

### Opção 2: Orquestração no Frontend via dois endpoints separados (`POST /pessoas` + `POST /profissionais`) (Descartada)
- **Descrição:** O frontend cria a pessoa primeiro, obtém o ID e em seguida chama a criação do profissional.
- **Motivo do descarte:** Não é atômico; falhas parciais deixariam pessoas sem perfil profissional no banco e exigiria lógica extra de rollback no cliente.

---

## 4. Design Técnico Detalhado

### 4.1 Backend (Spring Boot)

#### `SaveProfissionalDTO.java`
Evoluir o DTO para suportar dados de pessoa caso seja um novo cadastro:
```java
public class SaveProfissionalDTO {
    private String idPessoa; // Opcional se os dados abaixo forem informados
    
    // Dados pessoais para novo cadastro ou atualização
    private String nomePessoa;
    private String CPFPessoa;
    private LocalDate datanascPessoa;
    private String RGPessoa;
    private String enderecoPessoa;
    private String telefonePessoa;
    private String celularPessoa;
    private String emailPessoa;
    
    // Dados profissionais
    @NotBlank(message = "O registro no conselho profissional é obrigatório")
    private String conselhoProf;
}
```

#### `ProfissionalDTO.java`
Retornar os dados consolidados da pessoa para exibição na UI:
```java
public class ProfissionalDTO {
    private String idProf;
    private String idPessoa;
    private String nomeProfissional;
    private String cpf;
    private String email;
    private String telefone;
    private String conselhoProf;
    private boolean ativo;
}
```

#### `ProfissionalService.java`
Lógica atômica:
```java
@Transactional
public Profissional createProfissional(SaveProfissionalDTO dto) {
    Pessoa pessoa;
    if (dto.getIdPessoa() != null) {
        pessoa = pessoaRepository.findById(dto.getIdPessoa()).orElseThrow();
    } else if (dto.getCPFPessoa() != null) {
        pessoa = pessoaRepository.findByCPFPessoa(dto.getCPFPessoa())
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
    } else {
        throw new IllegalArgumentException("Informe a pessoa existente ou os dados pessoais com CPF");
    }

    Profissional profissional = Profissional.builder()
        .pessoaProf(pessoa)
        .conselhoProf(dto.getConselhoProf())
        .build();

    return profissionalRepository.save(profissional);
}
```

---

### 4.2 Frontend (React + TypeScript + Tailwind)

#### Estrutura de Arquivos:
- `src/types/profissional.ts`: Tipos `Profissional`, `SaveProfissionalInput`.
- `src/services/profissionalService.ts` e `profissionalService.test.ts`: Métodos `list()`, `create()`, `delete()`.
- `src/pages/Profissionais.tsx` e `Profissionais.test.tsx`:
  - Cabeçalho com título, contagem e botão `+ Novo Profissional`.
  - Campo de busca instantânea (filtra por nome, conselho ou CPF).
  - Tabela com colunas:
    - **Profissional**: Nome completo e iniciais.
    - **Conselho**: Badge com CRM, CRP, CREFITO, etc.
    - **Contato**: E-mail e telefone/celular.
    - **Ações**: Botão de excluir/desativar com confirmação.
- `src/components/ProfissionalModal.tsx`:
  - Modal estilizado com Tailwind.
  - Campos: Nome Completo, CPF (com máscara), Data de Nascimento, E-mail, Celular, Conselho Profissional (ex.: `CRM/SP 123456`, `CRP 06/99999`).
  - Tratamento de loading, erro e validação de campos obrigatórios antes do submit.
- Integração de rotas em `src/App.tsx`:
  - `/admin/profissionais` mapeado para o componente `Profissionais`.

---

## 5. Plano de Testes e Validação

1. **Testes Unitários de Serviço:**
   - Chamada GET `/profissionais` retornando lista tipada.
   - Chamada POST `/profissionais` enviando payload unificado e recebendo profissional criado.
2. **Testes de Componente:**
   - Renderização da tabela vazia e com itens.
   - Filtro de busca na tabela.
   - Abertura e fechamento do modal.
   - Validação de campos obrigatórios no modal.
   - Envio bem-sucedido e atualização da lista.
3. **Teste de Integração Real:**
   - Cadastrar um médico/terapeuta pela UI administrativa e verificar sua persistência na base e exibição na listagem.

