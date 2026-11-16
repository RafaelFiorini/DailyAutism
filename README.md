# 🧩 DailyAutism — Plataforma Integrada de Gestão Clínica e Terapêutica do TEA

> **DailyAutism** é um ecossistema digital desenvolvido para conectar **Clínicas Especializadas**, **Terapeutas/Profissionais Multidisciplinares** e **Famílias/Responsáveis** de pessoas no Espectro Autista (TEA), centralizando rotinas terapêuticas, agendamentos, documentos clínicos e conciliação financeira.

---

## 💙 Motivação

O **DailyAutism** nasceu de uma vivência real e profunda. Como pai de uma criança diagnosticada com Transtorno do Espectro Autista (TEA) nível 3 de suporte, senti na pele a sobrecarga de gerenciar terapias diárias, múltiplos profissionais, guias de convênio, laudos espalhados e repasses financeiros.

Este projeto existe para transformar essa jornada: dando às famílias transparência e facilidade, aos profissionais autonomia para organizar sua agenda e finanças, e às clínicas uma gestão eficiente, segura e em conformidade com as normas de saúde.

---

## 🏗️ Arquitetura do Sistema

O repositório principal é composto por uma arquitetura desacoplada e escalável:

* **Backend:** API RESTful robusta desenvolvida em **Java 17** com **Spring Boot 3**, orientada a Domínio (DDD) e Clean Architecture, com persistência em **PostgreSQL**, controle de migrações via **Flyway**, auditoria de entidades (`AuditorAware`) e soft-delete.
* **Segurança:** Autenticação stateless via **JWT** e controle de acesso baseado em papéis (**RBAC**): `ADMIN`, `ADMIN_CLINICA`, `FUNCIONARIO_CLINICA`, `PROFISSIONAL` e `RESPONSAVEL`.
* **Frontend Web:** SPA moderna e responsiva construída em **React 18**, **TypeScript**, **Vite** e **Tailwind CSS**, com tipagem estrita e suite de testes automatizados com **Vitest**.

---

## ✨ Principais Funcionalidades

### 🏥 1. Gestão Multi-Clínicas e Pacientes
* Cadastro e gestão de clínicas, profissionais, pacientes e seus respectivos responsáveis legais.
* Vínculo de operadoras de planos de saúde, prazos contratuais de faturamento e regras de carência.
* Prontuário terapêutico digital com histórico unificado de intervenções.

### 📅 2. Agenda Inteligente & Multi-Clínicas
* Timeline de compromissos com detecção automática de conflitos de horários entre salas e profissionais.
* Suporte a atendimentos recorrentes (periodicidades configuráveis) e presença/status em tempo real.
* Visão contextual por clínica e conciliação imediata com o faturamento.

### 📑 3. Gestão Eletrônica de Documentos & Laudos
* Repositório central de arquivos clínicos (PEI, relatórios multidisciplinares, laudos e receitas).
* Níveis de privacidade granular: liberação seletiva para a família e controle de sigilo profissional.

### 💰 4. Módulo Financeiro Institucional (Clínicas & Admins)
* **Contratos Particulares & Combos:** Gestão de pacotes pré-pagos, controle de saldo de sessões consumidas e extrato de utilizações.
* **Tabela de Preços:** Parametrização de valores particulares e convênios por especialidade.
* **Faturamento de Convênios & Guias:** Geração de lotes de faturamento, conferência e fluxo de assinatura eletrônica de guias.

### 💼 5. Portal do Profissional & Repasses
* **Conta Corrente de Repasses:** Registro contábil congelado por sessão realizada (valor fixo ou percentual acordado com cada clínica).
* **Atendimentos Particulares Diretos:** Autonomia para precificar e registrar sessões particulares fora das clínicas.
* **Extrato e Quitação:** Baixa individual ou em lote por clínica no fechamento do mês.

---

## 🛠️ Tecnologias Utilizadas

| Camada | Tecnologias |
| :--- | :--- |
| **Backend** | Java 17, Spring Boot 3, Spring Data JPA, Spring Security, Hibernate, PostgreSQL, Flyway, Maven, JUnit 5, Mockito |
| **Frontend** | React 18, TypeScript, Vite, Tailwind CSS, Lucide Icons, React Router 6, Vitest, Testing Library |
| **Mobile** | Aplicativo nativo complementar em Android (Kotlin + Jetpack Compose) em repositório dedicado `DApp` |

---

## 🚀 Como Executar o Projeto Localmente

### Pré-requisitos
* Java JDK 17+
* Node.js 18+ e pnpm / npm
* PostgreSQL 14+
* Maven 3.8+

### 1. Clonar o Repositório
```bash
git clone https://github.com/RafaelFiorini/DailyAutism.git
cd DailyAutism
```

### 2. Configurar o Banco de Dados
Configure as credenciais do seu banco PostgreSQL no arquivo `src/main/resources/application.yml` ou via variáveis de ambiente:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/dailyautism
    username: seu_usuario
    password: sua_senha
```

### 3. Executar o Backend
```bash
./mvnw spring-boot:run
```
*A API iniciará por padrão na porta `8080` e executará automaticamente as migrações do banco via Flyway.*

### 4. Executar o Frontend Web
```bash
cd frontend
npm install
npm run dev
```
*Acesse no navegador através de `http://localhost:5173`.*

---

## 👨‍💻 Autor

**Rafael Henrique Fiorini**  
* Desenvolvedor & Idealizador do Projeto  
* LinkedIn: [rafael-henrique-fiorini](https://linkedin.com/in/rafael-henrique-fiorini)  
* GitHub: [@RafaelFiorini](https://github.com/RafaelFiorini)
