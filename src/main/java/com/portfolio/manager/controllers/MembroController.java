package com.portfolio.manager.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.manager.dtos.MembroRequestDTO;
import com.portfolio.manager.dtos.MembroResponseDTO;
import com.portfolio.manager.mappers.MembroMapper;
import com.portfolio.manager.models.Membro;
import com.portfolio.manager.repositories.MembroRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/membros")
@Tag(name = "Membros", description = "Endpoints para gerenciamento de membros (Gerentes e Funcionários)")
@SuppressWarnings({"null", "unchecked"}) // IMPEDE QUE A IDE RECLAME DE REGRAS DE NULIDADE DAS BIBLIOTECAS EXTERNAS (SPRING DATA / MAPSTRUCT)
public class MembroController {

    private final MembroRepository membroRepository;
    private final MembroMapper membroMapper;

    // Injeção de dependências limpa via construtor
    public MembroController(MembroRepository membroRepository, MembroMapper membroMapper) {
        this.membroRepository = membroRepository;
        this.membroMapper = membroMapper;
    }

@PostMapping
    @Operation(summary = "Cadastrar um novo membro", description = "Insere um novo membro no sistema com atribuição de 'gerente' ou 'funcionário'.")
    public ResponseEntity<MembroResponseDTO> cadastrarMembro(@Valid @RequestBody MembroRequestDTO requestDTO) {
        Membro membro = membroMapper.toEntity(requestDTO);
        
        Membro membroSalvo = membroRepository.save(membro);
        
        // SOLUÇÃO DEFINITIVA VIA CASTING: Coage o compilador a aceitar o tipo limpando a desconfiança de nulidade
        MembroResponseDTO responseDTO = membroMapper.toDto((Membro) membroSalvo);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping
    @Operation(summary = "Listar todos os membros", description = "Retorna uma lista completa de todos os membros cadastrados no banco de dados.")
    public ResponseEntity<List<MembroResponseDTO>> listarTodos() {
        List<MembroResponseDTO> membros = membroRepository.findAll().stream()
                .map(membroMapper::toDto)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(membros);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar membro por ID", description = "Retorna os detalhes de um membro específico a partir do seu identificador.")
    public ResponseEntity<MembroResponseDTO> buscarPorId(@PathVariable Long id) {
        if (id == null) {
            throw new IllegalArgumentException("O ID fornecido não pode ser nulo.");
        }

        Membro membro = membroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Membro não encontrado com o ID: " + id));
                
        return ResponseEntity.ok(membroMapper.toDto(membro));
    }
}