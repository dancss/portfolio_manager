package com.portfolio.manager.mappers;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.portfolio.manager.dtos.ProjetoRequestDTO;
import com.portfolio.manager.dtos.ProjetoResponseDTO;
import com.portfolio.manager.models.Membro;
import com.portfolio.manager.models.Projeto;
import com.portfolio.manager.services.ProjetoService;

@Component
public class ProjetoMapper {

    private final MembroMapper membroMapper;
    private final ProjetoService projetoService;

    // Injeção dos componentes necessários para o mapeamento completo
    public ProjetoMapper(MembroMapper membroMapper, ProjetoService projetoService) {
        this.membroMapper = membroMapper;
        this.projetoService = projetoService;
    }

    public Projeto toEntity(ProjetoRequestDTO dto, Membro gerente) {
        if (dto == null) {
            return null;
        }

        return Projeto.builder()
                .nome(dto.getNome())
                .dataInicio(dto.getDataInicio())
                .dataPrevisaoFim(dto.getDataPrevisaoFim())
                .dataFim(dto.getDataFim())
                .descricao(dto.getDescricao())
                .status(dto.getStatus())
                .orcamentoTotal(dto.getOrcamentoTotal())
                .gerente(gerente) // O gerente associado é resolvido pelo service antes da criação
                .build();
    }

    public ProjetoResponseDTO toDto(Projeto entity) {
        if (entity == null) {
            return null;
        }

        ProjetoResponseDTO dto = new ProjetoResponseDTO();
        dto.setId(entity.getId());
        dto.setNome(entity.getNome());
        dto.setDataInicio(entity.getDataInicio());
        dto.setDataPrevisaoFim(entity.getDataPrevisaoFim());
        dto.setDataFim(entity.getDataFim());
        dto.setDescricao(entity.getDescricao());
        dto.setStatus(entity.getStatus());
        dto.setOrcamentoTotal(entity.getOrcamentoTotal());
        
        // 1. Injeta o Risco calculado dinamicamente (Regra de Negócio isolada)
        dto.setRisco(projetoService.calcularRiscoDinamico(entity));

        // 2. Mapeia o Gerente Responsável
        if (entity.getGerente() != null) {
            dto.setGerente(membroMapper.toDto(entity.getGerente()));
        }

        // 3. Mapeia a Lista de Membros da Equipe de forma segura contra NullPointer
        if (entity.getMembros() != null) {
            Set<com.portfolio.manager.dtos.MembroResponseDTO> membrosDto = entity.getMembros().stream()
                    .map(membroMapper::toDto)
                    .collect(Collectors.toSet());
            dto.setMembros(membrosDto);
        } else {
            dto.setMembros(Collections.emptySet());
        }

        return dto;
    }
}