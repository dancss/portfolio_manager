package com.portfolio.manager.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjetoRequestDTO {

    @NotBlank(message = "O nome do projeto é obrigatório.")
    @Size(max = 100, message = "O nome do projeto não pode exceder 100 caracteres.")
    private String nome;

    private LocalDate dataInicio;
    private LocalDate dataPrevisaoFim;
    private LocalDate dataFim;

    @Size(max = 500, message = "A descrição não pode exceder 500 caracteres.")
    private String descricao;

    @NotBlank(message = "O status inicial do projeto é obrigatório.")
    @Size(max = 50, message = "O status não pode exceder 50 caracteres.")
    private String status;

    private BigDecimal orcamentoTotal;

    @NotNull(message = "O ID do gerente responsável é obrigatório.")
    private Long idGerente;
}