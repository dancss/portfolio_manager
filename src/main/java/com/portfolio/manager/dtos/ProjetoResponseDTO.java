package com.portfolio.manager.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjetoResponseDTO {
    private Long id;
    private String nome;
    private LocalDate dataInicio;
    private LocalDate dataPrevisaoFim;
    private LocalDate dataFim;
    private String descricao;
    private String status;
    private BigDecimal orcamentoTotal;
    private String risco; // Calculado dinamicamente
    private MembroResponseDTO gerente;
    private Set<MembroResponseDTO> membros;
}