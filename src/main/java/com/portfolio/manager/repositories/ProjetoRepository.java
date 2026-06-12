package com.portfolio.manager.repositories;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.portfolio.manager.models.Projeto;


public interface ProjetoRepository extends JpaRepository<Projeto, Long> {

    // Query otimizada para o relatório consolidador exigido no desafio
    @Query(value = "SELECT " +
                   "  status, " +
                   "  COUNT(*) as quantidade, " +
                   "  SUM(orcamento_total) as total_orcado, " +
                   "  AVG(CASE WHEN status = 'encerrado' THEN (data_fim - data_inicio) ELSE NULL END) as media_duracao, " +
                   "  (SELECT COUNT(DISTINCT idmembro) FROM membros_projeto) as membros_unicos " +
                   "FROM projeto " +
                   "GROUP BY status", nativeQuery = true)
    List<Map<String, Object>> obterDadosRelatorioResumido();
}