package com.portfolio.manager.mappers;

import org.springframework.stereotype.Component;

import com.portfolio.manager.dtos.MembroRequestDTO;
import com.portfolio.manager.dtos.MembroResponseDTO;
import com.portfolio.manager.models.Membro;

@Component
public class MembroMapper {

    public Membro toEntity(MembroRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return Membro.builder()
                .nome(dto.getNome())
                .atribuicao(dto.getAtribuicao())
                .build();
    }

    public MembroResponseDTO toDto(Membro entity) {
        if (entity == null) {
            return null;
        }

        MembroResponseDTO dto = new MembroResponseDTO();
        dto.setId(entity.getId());
        dto.setNome(entity.getNome());
        dto.setAtribuicao(entity.getAtribuicao());
        return dto;
    }
}