package com.portfolio.manager.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MembroRequestDTO {

    @NotBlank(message = "O nome do membro é obrigatório.")
    @Size(max = 100, message = "O nome não pode exceder 100 caracteres.")
    private String nome;

    @NotBlank(message = "A atribuição (cargo) do membro é obrigatória.")
    @Size(max = 100, message = "A atribuição não pode exceder 100 caracteres.")
    private String atribuicao;
}