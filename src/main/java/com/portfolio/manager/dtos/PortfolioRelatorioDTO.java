package com.portfolio.manager.dtos;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PortfolioRelatorioDTO {
    private List<ApuracaoStatusDTO> consolidadoPorStatus;
    private Double mediaDuracaoProjetosEncerrados; // Em dias
    private Long totalMembrosUnicosAlocados;
}