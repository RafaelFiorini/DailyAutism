# Design Doc: Módulo Financeiro (Particular Pré-Pago, Convênios, Guia PDF e Assinatura Eletrônica)

**Data:** 11/11/2026  
**Status:** Aprovado para Planejamento (Abordagem 1)  
**Autores:** Rafael Fiorini & Antigravity

---

## 1. Contexto e Motivação

O sistema **DailyAutism** já controla o cadastro multi-clínica de pacientes, responsáveis, profissionais, agenda de atendimentos (`Compromisso`), controle de guias/limites de sessões (`PacientePlanoSaude` e limite particular em `Paciente`) e gestão de documentos (`Arquivo`).

Para fechar o ciclo operacional das clínicas e preparar o aplicativo móvel (`DApp`), falta o **Módulo Financeiro**, atendendo a dois modelos de cobrança distintos dentro de cada clínica:

1. **Atendimento Particular (Pré-pago por sessões ou período mensal):**
   - A clínica possui uma tabela de valores única para todos os pacientes particulares (por especialidade/`TipoCompromisso`), além de pacotes/combos com desconto (ex.: 1 sessão R\$ 100, pacote de 10 sessões R\$ 850, ou combo mensal com cotas específicas por especialidade — ex.: `4 TO + 2 Psicologia = 6 sessões/mês`, fechado por 2 ou 4 meses = 12 ou 24 sessões).
   - O responsável paga antecipadamente na contratação e o sistema abate as sessões conforme são realizadas, permitindo ao responsável acompanhar a execução e o saldo restante.

2. **Atendimento via Plano de Saúde / Convênio (Pós-pago por comparecimento + Guia assinada):**
   - Cada convênio vinculado à clínica possui valores próprios repassados à clínica por especialidade (ex.: Unimed paga R\$ 70, Bradesco paga R\$ 75), além de prazos próprios de envio de lote e pagamento à clínica.
   - Ao final do período (geralmente mensal), a clínica consolida os atendimentos com comparecimento (`REALIZADO`), gera a **guia de cobrança em PDF** e notifica o responsável para conferir e **assinar eletronicamente** antes do envio ao convênio.

---

## 2. Metas, Não-Metas e Anti-Metas

### Metas (V1 — Fases 1 e 2)
1. **Tabela de Preços por Clínica:**
   - Preço particular padrão por especialidade (`TipoCompromisso`) válido para todos os pacientes particulares da clínica.
   - Cadastro de **Pacotes/Combos Particulares** com preço promocional e itens discriminados por especialidade.
   - Preço negociado por **Convênio x Especialidade** dentro da clínica, além de **dias limite de envio** e **prazo de recebimento (dias)** no vínculo [`ClinicaPlanoSaude`](file:///C:/Users/rafael.fiorini/DailyAutism/src/main/java/com/clinica/dailyautism/domain/entity/ClinicaPlanoSaude.java).
2. **Contratos / Fechamentos Particulares (Pré-pago com Abatimento):**
   - Criação de contrato/fechamento particular por quantidade de sessões ou por meses/período calculado, com cotas por especialidade (ex.: 8 TO + 4 Psicologia) e registro de pagamento antecipado.
   - Abatimento automático da cota da especialidade correspondente quando um compromisso é concluído como `REALIZADO` (e estorno caso revertido).
   - Visualização pelo `RESPONSAVEL` do que já está pago e das sessões abatidas/restantes.
3. **Faturamento de Convênios, Guia em PDF e Assinatura Eletrônica:**
   - Fechamento de competência/período por paciente + plano de saúde reunindo os compromissos `REALIZADO` ainda não faturados.
   - Geração real de **PDF da Guia de Cobrança** no backend (com `OpenPDF`), listando sessões, datas, especialidades, profissionais e valor total de repasse do convênio.
   - Fluxo de liberação para o `RESPONSAVEL`, aviso no painel/dashboard, conferência e **assinatura eletrônica própria no app/web** (gravando data/hora, usuário, CPF, IP e hash verificador no PDF assinado).
   - Registro de envio da guia ao convênio (calculando data prevista de recebimento pelo prazo do plano) e baixa de recebimento.
4. **Isolamento Multi-Clínica e Permissões:**
   - Toda configuração financeira, preço, contrato e fatura pertence a uma `Clinica` específica; perfis `ADMIN_CLINICA` (e `FUNCIONARIO_CLINICA` onde autorizado) acessam apenas os dados de suas clínicas vinculadas.

### Não-Metas (Reservadas para V2 / Próximos Módulos)
- **Repasse financeiro aos profissionais** (cálculo de comissão/pagamento por profissional atendido).
- **Regra de cobrança por antecedência de aviso em faltas** (ex.: falta sem aviso prévio com X horas cobra/abate sessão; com aviso prévio não cobra).
- **Integração oficial com assinatura gov.br** (requer credenciamento formal PJ/SERPRO e ambiente público HTTPS; a arquitetura deixará a interface de provedor desacoplada para plugar gov.br futuramente).
- **Emissão de Nota Fiscal (NFSe) ou boleto/PIX automático** via gateway bancário.

### Anti-Metas
- Alterar o preço vigente na tabela da clínica e **recalcular retroativamente** contratos já pagos ou guias já fechadas/assinadas (valores devem ser congelados no momento da contratação/fechamento).
- Permitir que o mesmo atendimento `REALIZADO` seja faturado em duas guias diferentes ou abatido duas vezes do mesmo pacote.
- Permitir envio ao convênio ou alteração de itens de uma guia após assinada pelo responsável sem cancelamento/retificação explícita.

---

## 3. Abordagens Consideradas

### Opção 1: Modelo de Dados Único + Entrega Incremental em 2 Etapas (Escolhida)
- **Descrição:** Modelar toda a estrutura relacional (tabelas de preços, pacotes, contratos particulares com itens por especialidade, fechamento de guias e assinatura eletrônica) de forma integrada, dividindo a implementação em:
  - **Etapa 1:** Tabela de Preços (Particular e Convênios + Prazos), Pacotes/Combos e Contratos Particulares com abatimento automático por especialidade e extrato para o responsável.
  - **Etapa 2:** Fechamento de Convênios por comparecimento, geração de PDF com OpenPDF, aviso/assinatura eletrônica do responsável e acompanhamento de envio/recebimento.
- **Vantagens:** Permite colocar os preços e o controle particular em uso/validação imediata nas duas clínicas antes de refinar o layout da guia em PDF e o fluxo de assinatura.

### Opção 2: Entrega Completa em Bloco Único (Descartada)
- **Descrição:** Construir todas as entidades, geração de PDF, assinatura e todas as interfaces web simultaneamente antes de qualquer validação em clínica.
- **Motivo do descarte:** Aumenta o risco de retrabalho de layout de guia e regras operacionais ao concentrar tudo em um único ciclo de testes.

### Opção 3: Impressão HTML no Navegador sem PDF no Backend (Descartada)
- **Motivo do descarte:** Não produz documento fechado com hash verificador, carimbo de assinatura eletrônica e armazenamento persistente para auditoria/envio ao plano.

---

## 4. Arquitetura e Modelo de Domínio

### 4.1 Configuração de Preços e Prazos da Clínica

1. **Evolução em `ClinicaPlanoSaude`:**
   - `diaCorteEnvio` (`Integer`, opcional): dia do mês sugerido para envio do lote/guia ao convênio.
   - `prazoPagamentoDias` (`Integer`, default `30`): prazo em dias após o envio para previsão de recebimento pela clínica.

2. **`TabelaPrecoClinica` (Preço Unitário por Especialidade):**
   - `idTabelaPreco` (`UUID`)
   - `clinica` (`@ManyToOne Clinica`)
   - `tipoCompromisso` (`@ManyToOne TipoCompromisso`)
   - `planoSaude` (`@ManyToOne PlanoSaude`, **nullable**: quando `null`, representa o preço padrão **Particular** da clínica para aquela especialidade; quando preenchido, representa o valor pago por aquele convênio)
   - `valorSessao` (`BigDecimal`, escala 2)
   - Constraint lógica de unicidade ativa por `(clinica, tipoCompromisso, planoSaude)`.

3. **`PacoteParticular` & `PacoteParticularItem` (Combos/Pacotes com Desconto):**
   - `PacoteParticular`:
     - `idPacote`, `clinica`, `nomePacote` (ex.: `"Pacote 10 Sessões"`, `"Combo Mensal 4 TO + 2 Psico"`), `descricao`, `valorTotalPacote` (`BigDecimal`), `valorSemDescontoCalculado` (`BigDecimal`), `validadeMesesSugerida` (`Integer`, opcional).
   - `PacoteParticularItem`:
     - `idPacoteItem`, `pacote` (`@ManyToOne`), `tipoCompromisso` (`@ManyToOne`), `quantidadeSessoes` (`Integer`).

### 4.2 Contratos Particulares e Abatimento (Etapa 1)

1. **`ContratoParticular`:**
   - `idContratoParticular` (`UUID`)
   - `clinica` (`@ManyToOne Clinica`)
   - `paciente` (`@ManyToOne Paciente`)
   - `responsavel` (`@ManyToOne Responsavel`, opcional caso próprio paciente)
   - `pacoteOrigem` (`@ManyToOne PacoteParticular`, opcional)
   - `descricao` (`String`, ex.: `"Combo 2 meses - 8 TO + 4 Psicologia"`)
   - `modalidade` (`Enum`: `POR_SESSOES` | `POR_PERIODO_MENSAL`)
   - `dataInicio` (`LocalDate`), `dataFimPrevista` (`LocalDate`, opcional)
   - `valorCobrado` (`BigDecimal`) — congelado na contratação
   - `statusPagamento` (`Enum`: `PENDENTE` | `PAGO` | `CANCELADO`)
   - `dataPagamento` (`LocalDate`), `formaPagamento` (`String`, opcional: `PIX`, `CARTAO`, `DINHEIRO`, `TRANSFERENCIA`)
   - `statusContrato` (`Enum`: `ATIVO` | `CONCLUIDO` | `CANCELADO`)

2. **`ContratoParticularItem` (Cotas por Especialidade):**
   - `idContratoItem` (`UUID`)
   - `contrato` (`@ManyToOne ContratoParticular`)
   - `tipoCompromisso` (`@ManyToOne TipoCompromisso`)
   - `quantidadeContratada` (`Integer`)
   - `quantidadeConsumida` (`Integer`, default `0`)
   - `valorUnitarioReferencia` (`BigDecimal`) — congelado para exibição em extrato

3. **`MovimentacaoContratoParticular` (Extrato de Abatimentos / Estornos):**
   - `idMovimentacao` (`UUID`)
   - `contratoItem` (`@ManyToOne ContratoParticularItem`)
   - `compromisso` (`@ManyToOne Compromisso`, nullable para ajustes manuais)
   - `tipoMovimentacao` (`Enum`: `ABATIMENTO` | `ESTORNO` | `AJUSTE_MANUAL`)
   - `quantidade` (`Integer`, ex.: `1`)
   - `dataMovimentacao` (`LocalDateTime`)
   - `observacao` (`String`)
   - Regra de unicidade ativa: um `Compromisso` gera no máximo um abatimento ativo por execução (evita débito duplicado se salvo duas vezes).

### 4.3 Faturamento de Convênios, Guia PDF e Assinatura Eletrônica (Etapa 2)

1. **`GuiaFaturamentoConvenio`:**
   - `idGuiaFaturamento` (`UUID`)
   - `clinica`, `paciente`, `planoSaude`, `responsavel` (responsável que assina)
   - `numeroLoteGuia` (`String`, sequencial/código da clínica, ex.: `FAT-202611-0001`)
   - `competenciaInicio` (`LocalDate`), `competenciaFim` (`LocalDate`)
   - `valorTotal` (`BigDecimal`) — soma dos itens congelados
   - `status` (`Enum`: `RASCUNHO` | `AGUARDANDO_ASSINATURA` | `ASSINADA` | `ENVIADA_CONVENIO` | `PAGA` | `CANCELADA`)
   - **Dados da Assinatura Eletrônica (desacoplados para futura extensão gov.br):**
     - `provedorAssinatura` (`Enum`: `INTERNO_DAILYAUTISM` | `GOV_BR`)
     - `assinadoPorUserId` (`String`), `assinadoPorNome` (`String`), `assinadoPorCpf` (`String`)
     - `dataHoraAssinatura` (`LocalDateTime`), `ipAssinatura` (`String`)
     - `codigoVerificador` (`String` curto/UUID parcial para conferência no PDF)
     - `hashPdfOriginal` (`String` SHA-256), `hashPdfAssinado` (`String` SHA-256)
   - **Envio e Recebimento do Plano:**
     - `dataEnvioConvenio` (`LocalDate`), `dataPrevisaoPagamento` (`LocalDate`, calculada via `ClinicaPlanoSaude.prazoPagamentoDias`), `dataPagamento` (`LocalDate`)
   - **Persistência do PDF:**
     - `caminhoPdf` / vínculo com `Arquivo` ou armazenamento gerenciado em disco (`storage/guias/`), permitindo download pré-assinatura e pós-assinatura.

2. **`GuiaFaturamentoItem`:**
   - `idGuiaItem` (`UUID`)
   - `guia` (`@ManyToOne GuiaFaturamentoConvenio`)
   - `compromisso` (`@ManyToOne Compromisso`) — unicidade garantida entre guias não canceladas
   - `dataHoraAtendimento` (`LocalDateTime`), `tipoCompromissoNome` (`String`), `profissionalNome` (`String`), `conselhoProfissional` (`String`)
   - `valorUnitarioConvenio` (`BigDecimal`) — congelado da `TabelaPrecoClinica` no momento da geração da guia

---

## 5. Regras Operacionais e Fluxos

### Fluxo Particular
1. `ADMIN_CLINICA` cadastra preços particulares por especialidade e pacotes/combos (opcional).
2. Ao contratar para um paciente particular, seleciona um pacote existente **ou** monta manualmente os itens por especialidade (ou calcula multiplicando sessões/mês × quantidade de meses), informa valor cobrado e marca se já foi pago.
3. Quando um `Compromisso` daquele paciente é marcado como `REALIZADO`:
   - Se o paciente possui `ContratoParticular` ativo (`statusContrato == ATIVO`) com item da mesma especialidade (`tipoCompromisso`) com saldo disponível (`quantidadeConsumida < quantidadeContratada`), o sistema incrementa `quantidadeConsumida` e registra `ABATIMENTO` em `MovimentacaoContratoParticular`.
   - Caso `quantidadeConsumida == quantidadeContratada` em todos os itens do contrato, o contrato passa para `CONCLUIDO` automaticamente.
   - Se o `Compromisso` voltar de `REALIZADO` para outro status (`AGENDADO`/`CANCELADO`), o sistema gera `ESTORNO`, devolvendo o saldo ao item.
4. O `RESPONSAVEL` acessa a aba **Financeiro** (visão somente leitura para particular) e vê pacotes contratados, pagamento, quantas sessões restam por especialidade e o histórico de sessões abatidas.

### Fluxo Convênio + PDF + Assinatura
1. `ADMIN_CLINICA` configura em `ClinicaPlanoSaude` o valor de repasse de cada especialidade por plano e o prazo de pagamento em dias.
2. Na tela **Faturamento Convênios**, seleciona clínica, convênio, período (ex.: `01/11/2026` a `30/11/2026`) e paciente(s). O sistema lista os compromissos `REALIZADO` com `planoSaude` ativo que ainda não pertencem a nenhuma guia ativa.
3. Ao gerar a guia (`RASCUNHO`), o sistema congela os valores e permite gerar o PDF de conferência.
4. Ao clicar em **Liberar para Assinatura**, o status muda para `AGUARDANDO_ASSINATURA`, gerando um alerta/pendência visível no Dashboard e no menu **Financeiro** do `RESPONSAVEL` vinculado ao paciente.
5. O `RESPONSAVEL` visualiza os atendimentos da guia (ou baixa o PDF), confirma a assinatura eletrônica (revalidando sua senha ou confirmação autenticada), e o backend registra `assinadoPor*`, `dataHoraAssinatura`, `ipAssinatura`, `codigoVerificador` e reemite o PDF com o **bloco/selo de assinatura eletrônica** no rodapé + status `ASSINADA`.
6. A clínica marca **Enviada ao Convênio** -> status `ENVIADA_CONVENIO` e `dataPrevisaoPagamento = dataEnvio + prazoPagamentoDias`. Ao receber, marca **Recebida** (`PAGA`).

---

## 6. Segurança e Perfis de Acesso (`DataSeeder`)

Novas ações granulares a serem criadas em `DataSeeder`:
- `VER_FINANCEIRO`, `GERENCIAR_PRECOS_CLINICA`, `GERENCIAR_CONTRATO_PARTICULAR`, `GERENCIAR_FATURAMENTO_CONVENIO`, `ASSINAR_GUIA_CONVENIO`.
- Distribuição sugerida:
  - `ADMIN` e `ADMIN_CLINICA`: todas acima (exceto `ASSINAR_GUIA_CONVENIO`, que é própria do responsável, embora `ADMIN` possa auditar).
  - `FUNCIONARIO_CLINICA` / `CLINICA`: `VER_FINANCEIRO`, `GERENCIAR_CONTRATO_PARTICULAR`, `GERENCIAR_FATURAMENTO_CONVENIO` (conforme política operacional da clínica, sem alterar tabela base de preços ou com leitura de preços).
  - `RESPONSAVEL`: `VER_FINANCEIRO` (apenas dos próprios pacientes) e `ASSINAR_GUIA_CONVENIO`.
  - `PROFISSIONAL`: sem acesso financeiro na V1.

---

## 7. Decisões Técnicas e Critérios de Borda
- **Biblioteca de PDF no backend:** adicionar `com.github.librepdf:openpdf` ao `pom.xml` (leve, licença LGPL/MPL e sem dependências de navegador).
- **Armazenamento dos PDFs das guias:** diretório dedicado (`storage/guias/`), seguindo o padrão de streaming/armazenamento de [`ArquivoService`](file:///C:/Users/rafael.fiorini/DailyAutism/src/main/java/com/clinica/dailyautism/domain/aplicationservice/ArquivoService.java).
- **Resolução de clínica por atendimento:** adicionar relacionamento opcional `Clinica` em [`Compromisso`](file:///C:/Users/rafael.fiorini/DailyAutism/src/main/java/com/clinica/dailyautism/domain/entity/Compromisso.java) (preenchido na criação quando identificável e usado com fallback pela clínica do paciente/profissional) para impedir cruzamento de faturamento entre clínicas caso um paciente frequente mais de uma unidade.
- **Execução clínica não bloqueante:** marcar um compromisso como `REALIZADO` nunca é bloqueado por falta de contrato particular; se houver contrato ativo com saldo naquela especialidade, o abatimento ocorre automaticamente na mesma transação; caso contrário, a sessão fica elegível para ajuste/abatimento posterior.
- **Confirmação de assinatura eletrônica:** o endpoint de assinatura exige que o usuário autenticado seja o `RESPONSAVEL` vinculado à guia (ou paciente próprio responsável) e confirme sua senha atual (`PasswordEncoder.matches`) no modal de assinatura antes de gerar o selo verificador e o hash SHA-256 do PDF.
