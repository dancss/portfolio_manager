-- ========================================================================
-- 1. POPULANDO A TABELA DE MEMBROS
-- Mantendo o padrão estrito de 'GERENTE' e 'funcionário' que valida no seu Service
-- ========================================================================
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

-- Ajusta a sequence do ID de membros para não dar conflito com os inserts manuais nos próximos POSTs
SELECT setval(pg_get_serial_sequence('membro', 'id'), COALESCE(MAX(id), 1)) FROM membro;


-- ========================================================================
-- 2. POPULANDO A TABELA DE PROJETOS (Corrigido para idgerente)
-- ========================================================================
INSERT INTO projeto (id, nome, descricao, status, orcamento_total, data_inicio, data_previsao_fim, data_fim, idgerente) VALUES
(10, 'Migração Cloud AWS', 'Migração de toda a infraestrutura on-premise para nuvem.', 'EM_ANALISE', 280000.00, '2026-01-15', '2026-08-30', NULL, 10),
(11, 'App Mobile Cliente', 'Desenvolvimento do novo aplicativo Android e iOS.', 'INICIADO', 450000.00, '2026-02-01', '2026-12-20', NULL, 10),
(12, 'Módulo de Compliance', 'Implementação das regras de auditoria e segurança da informação.', 'PLANEJADO', 95000.00, '2026-07-01', '2026-10-15', NULL, 11),
(13, 'Portal do Colaborador V2', 'Redesenho completo da Intranet e canais internos.', 'ENCERRADO', 120000.00, '2025-06-01', '2025-12-15', '2025-12-10', 12),
(14, 'Integração de ERP SAP', 'Unificação dos dados financeiros com a matriz global.', 'EM_ANALISE', 750000.00, '2026-05-20', '2027-03-01', NULL, 13),
(15, 'Dashboard Executivo AI', 'Análise preditiva de faturamento usando modelos de Machine Learning.', 'INICIADO', 310000.00, '2026-03-10', '2026-09-15', NULL, 11);

-- Ajusta a sequence do ID de projetos para os próximos POSTs do Swagger irem no número certo
SELECT setval(pg_get_serial_sequence('projeto', 'id'), COALESCE(MAX(id), 1)) FROM projeto;


-- ========================================================================
-- 3. VINCULANDO MEMBROS AOS PROJETOS (Equipes) - AJUSTADO
-- ========================================================================
INSERT INTO membros_projeto (idprojeto, idmembro) VALUES
-- Equipe do Projeto 10 (Migração Cloud)
(10, 14), -- Alexandre
(10, 15), -- Fernanda
(10, 16), -- Gabriel

-- Equipe do Projeto 11 (App Mobile)
(11, 15), -- Fernanda
(11, 17), -- Juliana
(11, 18), -- Lucas
(11, 19), -- Camila

-- Equipe do Projeto 13 (Portal V2 - Encerrado)
(13, 14), -- Alexandre
(13, 20), -- Roberto

-- Equipe do Projeto 15 (Dashboard AI)
(15, 16), -- Gabriel
(15, 18), -- Lucas
(15, 21); -- Patrícia


--corrigir status para bater exatamente como o desafio pede

-- 1. Projetos 10, 14 e o seu projeto inicial (ID 1): em análise
UPDATE projeto SET status = 'em análise' WHERE id IN (1, 10, 14);

-- 2. Projeto 12: análise realizada
UPDATE projeto SET status = 'análise realizada' WHERE id = 12;

-- 3. Projeto 11: análise aprovada
UPDATE projeto SET status = 'análise aprovada' WHERE id = 11;

-- 4. Projeto 15: iniciado
UPDATE projeto SET status = 'iniciado' WHERE id = 15;

-- 5. Projeto 13: encerrado
UPDATE projeto SET status = 'encerrado' WHERE id = 13;

--corrigir registro membro
UPDATE membro SET atribuicao = 'funcionário' WHERE id = 1;