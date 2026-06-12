package com.portfolio.manager.dtos;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ApuracaoStatusDTO {
    private String status;
    private Long quantidadeProjetos;
    private BigDecimal totalOrcado;
}