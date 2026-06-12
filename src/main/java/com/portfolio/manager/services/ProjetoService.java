package com.portfolio.manager.services;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portfolio.manager.models.Membro;
import com.portfolio.manager.models.Projeto;
import com.portfolio.manager.repositories.MembroRepository;
import com.portfolio.manager.repositories.ProjetoRepository;

@Service
public class ProjetoService {

    private final ProjetoRepository projetoRepository;
    private final MembroRepository membroRepository;

    // Injeção de dependências via construtor
    public ProjetoService(ProjetoRepository projetoRepository, MembroRepository membroRepository) {
        this.projetoRepository = projetoRepository;
        this.membroRepository = membroRepository;
    }

/**
     * REGRA PRINCIPAL: Alocação de Membros com validações estritas do desafio
     */
    @Transactional
    public void associarMembroAoProjeto(Long idProjeto, Long idMembro) {
        // Correção de Null Safety: Valida a nulidade dos parâmetros antes do uso
        if (idProjeto == null || idMembro == null) {
            throw new IllegalArgumentException("Os identificadores do projeto e do membro não podem ser nulos.");
        }

        Projeto projeto = projetoRepository.findById(idProjeto)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado."));

        Membro membro = membroRepository.findById(idMembro)
                .orElseThrow(() -> new RuntimeException("Membro não encontrado."));

        // Validação 1: Apenas membros com atribuição "funcionário"
        if (!"funcionário".equalsIgnoreCase(membro.getAtribuicao())) {
            throw new IllegalArgumentException("Apenas membros com a atribuição 'funcionário' podem ser associados a um projeto.");
        }

        // Validação 2: Limite máximo de 10 membros por projeto
        if (projeto.getMembros().size() >= 10) {
            throw new IllegalStateException("O projeto já atingiu o limite máximo de 10 membros alocados.");
        }

        // Validação 3: Um membro não pode estar em mais de 3 projetos ativos simultaneamente
        // CORREÇÃO DEFINITIVA: Filtro de não-nulidade explícito para satisfazer a IDE sem usar anotações
        long projetosAtivosDoMembro = projeto.getMembros().contains(membro) ? 0 : 
                projetoRepository.findAll().stream()
                        .filter(java.util.Objects::nonNull) // Garante que o projeto mapeado não é nulo
                        .filter(p -> p.getMembros() != null && p.getMembros().contains(membro))
                        .filter(p -> {
                            String status = p.getStatus();
                            return status != null && !status.equalsIgnoreCase("encerrado") && !status.equalsIgnoreCase("cancelado");
                        })
                        .count();

        if (projetosAtivosDoMembro >= 3) {
            throw new IllegalStateException("O membro " + membro.getNome() + " já está alocado em 3 projetos ativos simultaneamente.");
        }

        // Se passar em todas as validações, adiciona o membro ao set
        projeto.getMembros().add(membro);
        projetoRepository.save(projeto);
    }

    // REGRA: Classificação Dinâmica de Risco (Não persistida no banco)
    public String calcularRiscoDinamico(Projeto projeto) {
        if (projeto == null || projeto.getDataInicio() == null || projeto.getDataPrevisaoFim() == null) {
            return "Indefinido";
        }

        long meses = ChronoUnit.MONTHS.between(projeto.getDataInicio(), projeto.getDataPrevisaoFim());
        BigDecimal orcamento = projeto.getOrcamentoTotal() != null ? projeto.getOrcamentoTotal() : BigDecimal.ZERO;

        if (orcamento.compareTo(new BigDecimal("500000")) > 0 || meses > 6) {
            return "Alto risco";
        } else if (orcamento.compareTo(new BigDecimal("100000")) <= 0 && meses <= 3) {
            return "Baixo risco";
        }
        return "Médio risco";
    }

    // REGRA: Validação de Exclusão por Status
    @Transactional
    public void deletarProjeto(Long id) {
        // CORREÇÃO LINHA 92: Fail-fast para evitar passar valor possivelmente nulo ao findById
        if (id == null) {
            throw new IllegalArgumentException("O ID do projeto para exclusão não pode ser nulo.");
        }

        Projeto projeto = projetoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado."));

        List<String> statusBloqueados = Arrays.asList("iniciado", "em andamento", "encerrado");
        if (projeto.getStatus() == null || statusBloqueados.contains(projeto.getStatus().toLowerCase())) {
            throw new IllegalStateException("Não é permitido excluir um projeto com status: " + projeto.getStatus());
        }

        projetoRepository.delete(projeto);
    }

    // REGRA: Máquina de Estados (Validação na atualização de status)
    public void validarTransicaoStatus(String statusAtual, String novoStatus) {
        if (statusAtual == null || novoStatus == null) {
            throw new IllegalArgumentException("Os status atual e novo não podem ser nulos.");
        }
        
        if (statusAtual.equalsIgnoreCase(novoStatus)) return;
        if (novoStatus.equalsIgnoreCase("cancelado")) return; // Cancelado é permitido de qualquer ponto

        String atual = statusAtual.toLowerCase();
        String proximo = novoStatus.toLowerCase();

        boolean transicaoValida = switch (atual) {
            case "em análise" -> proximo.equals("análise realizada");
            case "análise realizada" -> proximo.equals("análise aprovada");
            case "análise aprovada" -> proximo.equals("planejado");
            case "planejado" -> proximo.equals("iniciado");
            case "iniciado" -> proximo.equals("em andamento");
            case "em andamento" -> proximo.equals("encerrado");
            default -> false;
        };

        if (!transicaoValida) {
            throw new IllegalArgumentException("Transição de status inválida de '" + statusAtual + "' para '" + novoStatus + "'.");
        }
    }
}