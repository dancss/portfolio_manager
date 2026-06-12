# Portfolio Manager API 🚀

API RESTful desenvolvida em Spring Boot para o gerenciamento estratégico de portfólio de projetos, permitindo o controle completo do ciclo de vida de projetos, alocação de equipes e extração de métricas consolidadas (PMO).

---

## 🛠️ Tecnologias Utilizadas

* **Java 17** (ou superior)
* **Spring Boot 3.x**
* **Spring Data JPA** (Persistência e ORM)
* **PostgreSQL** (Banco de dados relacional)
* **Spring Security** (Proteção de endpoints)
* **OpenAPI 3 / Swagger UI** (Documentação interativa da API)
* **Jakarta Validation** (Validação de regras de campos)
* **MapStruct / mappers** (Conversão de DTOs/Entidades)

---

## 📌 Regras de Negócio Implementadas

1. **Atribuição Estrita de Membros:** O sistema impede que membros com atribuição de `GERENTE` sejam alocados como membros comuns de equipes em projetos. Apenas membros com atribuição `funcionário` são permitidos em equipes.
2. **Máquina de Estados de Projetos:** A transição do status dos projetos segue uma sequência lógica estrita e não permite pular etapas:
   `em análise` ➔ `análise realizada` ➔ `análise aprovada` ➔ `iniciado` ➔ `planejado` ➔ `em andamento` ➔ `encerrado`.
   * *Exceção:* O status `cancelado` pode ser aplicado a qualquer momento do ciclo de vida.
3. **Restrição de Exclusão do PMO:** Um projeto não pode ser deletado do sistema se o seu status atual for `iniciado`, `em andamento` ou `encerrado`.

---

## 🚀 Como Rodar o Projeto Localmente

### Pré-requisitos
* Java 17 instalado e configurado nas variáveis de ambiente.
* Banco de dados PostgreSQL rodando localmente.

### 1. Configuração do Banco de Dados
Crie um banco de dados no PostgreSQL chamado `portfolio_db`. No seu arquivo `src/main/resources/application.properties` (ou `application.yml`), certifique-se de que as credenciais de acesso estejam corretas:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5143/portfolio_db
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
spring.jpa.hibernate.ddl-auto=update

2. Executar a Aplicação
Abra o projeto no seu terminal ou na sua IDE de preferência (VS Code / IntelliJ) e execute o comando:

mvn spring-boot:run

A API iniciará por padrão na porta 8080.

📊 Carga Inicial de Dados (Massa de Testes)
Para testar a paginação, os filtros e o relatório consolidado com dados reais e condizentes com as regras de negócio, execute o script SQL abaixo no console do seu banco de dados PostgreSQL:

-- 1. POPULANDO A TABELA DE MEMBROS
INSERT INTO membro (id, nome, atribuicao) VALUES
(10, 'Ricardo Oliveira', 'GERENTE'),
(11, 'Beatriz Souza', 'GERENTE'),
(12, 'Marcos Santos', 'GERENTE'),
(13, 'Mariana Costa', 'GERENTE'),
(14, 'Alexandre Silva', 'funcionário'),
(15, 'Fernanda Lima', 'funcionário'),
(16, 'Gabriel Almeida', 'funcionário'),
(17, 'Juliana Ribeiro', 'funcionário'),
(18, 'Lucas Pereira', 'funcionário'),
(19, 'Camila Rocha', 'funcionário'),
(20, 'Roberto Dias', 'funcionário'),
(21, 'Patrícia Gomes', 'funcionário');

SELECT setval(pg_get_serial_sequence('membro', 'id'), COALESCE(MAX(id), 1)) FROM membro;

-- 2. POPULANDO A TABELA DE PROJETOS
INSERT INTO projeto (id, nome, descricao, status, orcamento_total, data_inicio, data_previsao_fim, data_fim, idgerente) VALUES
(10, 'Migração Cloud AWS', 'Migração de toda a infraestrutura on-premise para nuvem.', 'em análise', 280000.00, '2026-01-15', '2026-08-30', NULL, 10),
(11, 'App Mobile Cliente', 'Desenvolvimento do novo aplicativo Android e iOS.', 'análise aprovada', 450000.00, '2026-02-01', '2026-12-20', NULL, 10),
(12, 'Módulo de Compliance', 'Implementação das regras de auditoria e segurança da informação.', 'análise realizada', 95000.00, '2026-07-01', '2026-10-15', NULL, 11),
(13, 'Portal do Colaborador V2', 'Redesenho completo da Intranet e canais internos.', 'encerrado', 120000.00, '2025-06-01', '2025-12-15', '2025-12-10', 12),
(14, 'Integração de ERP SAP', 'Unificação dos dados financeiros com a matriz global.', 'em análise', 750000.00, '2026-05-20', '2027-03-01', NULL, 13),
(15, 'Dashboard Executivo AI', 'Análise preditiva de faturamento usando modelos de Machine Learning.', 'iniciado', 310000.00, '2026-03-10', '2026-09-15', NULL, 11);

SELECT setval(pg_get_serial_sequence('projeto', 'id'), COALESCE(MAX(id), 1)) FROM projeto;

-- 3. VINCULANDO EQUIPES (Membros funcionário nos projetos)
INSERT INTO membros_projeto (idprojeto, idmembro) VALUES
(10, 14), (10, 15), (10, 16),
(11, 15), (11, 17), (11, 18), (11, 19),
(13, 14), (13, 20),
(15, 16), (15, 18), (15, 21);

📑 Documentação da API (Swagger)
Com a aplicação rodando, toda a documentação dos endpoints, payloads de requisição e respostas pode ser acessada interativamente através do Swagger UI pelo endereço:

🔗 http://localhost:8080/swagger-ui/index.html

Principais Endpoints Disponíveis:
POST /api/projetos - Criação de projetos vinculando gerentes válidos.

PUT /api/projetos/{id} - Atualização cadastral e validação da Máquina de Estados.

GET /api/projetos - Listagem dinâmica com filtros e paginação de mercado.

DELETE /api/projetos/{id} - Exclusão lógica baseada nas regras do PMO.

POST /api/projetos/{idProjeto}/membros/{idMembro} - Associação de equipe (apenas funcionários).

GET /api/projetos/relatorio - Extração de métricas de portfólio via query nativa otimizada.