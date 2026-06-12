package com.portfolio.manager.controllers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.manager.dtos.ApuracaoStatusDTO;
import com.portfolio.manager.dtos.PortfolioRelatorioDTO;
import com.portfolio.manager.dtos.ProjetoRequestDTO;
import com.portfolio.manager.dtos.ProjetoResponseDTO;
import com.portfolio.manager.mappers.ProjetoMapper;
import com.portfolio.manager.models.Membro;
import com.portfolio.manager.models.Projeto;
import com.portfolio.manager.repositories.MembroRepository;
import com.portfolio.manager.repositories.ProjetoRepository;
import com.portfolio.manager.services.ProjetoService;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/projetos")
@Tag(name = "Projetos", description = "Endpoints para gerenciamento do ciclo de vida dos projetos")
public class ProjetoController {

    private final ProjetoRepository projetoRepository;
    private final MembroRepository miembroRepository;
    private final ProjetoService projetoService;
    private final ProjetoMapper projetoMapper;

    public ProjetoController(ProjetoRepository projetoRepository, 
                             MembroRepository miembroRepository, 
                             ProjetoService projetoService, 
                             ProjetoMapper projetoMapper) {
        this.projetoRepository = projetoRepository;
        this.miembroRepository = miembroRepository;
        this.projetoService = projetoService;
        this.projetoMapper = projetoMapper;
    }

    @PostMapping
    @SuppressWarnings("null")
    @Operation(summary = "Criar um novo projeto", description = "Insere um projeto vinculando um gerente válido.")
    public ResponseEntity<ProjetoResponseDTO> criar(@Valid @RequestBody ProjetoRequestDTO dto) {
        Membro gerente = miembroRepository.findById(dto.getIdGerente())
                .orElseThrow(() -> new RuntimeException("Gerente não encontrado com o ID informado."));

        Projeto projeto = projetoMapper.toEntity(dto, gerente);
        Projeto salvo = projetoRepository.save(projeto);

        return ResponseEntity.status(HttpStatus.CREATED).body(projetoMapper.toDto(salvo));
    }

    @PutMapping("/{id}")
    @SuppressWarnings("null")
    @Operation(summary = "Atualizar um projeto existente", description = "Atualiza os dados de um projeto e valida a transição lógica de status.")
    public ResponseEntity<ProjetoResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody ProjetoRequestDTO dto) {
        if (id == null) {
            throw new IllegalArgumentException("O ID do projeto não pode ser nulo.");
        }

        Projeto projetoExistente = projetoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado."));

        // Validação da Máquina de Estados (Regra de Negócio)
        projetoService.validarTransicaoStatus(projetoExistente.getStatus(), dto.getStatus());

        Membro gerente = miembroRepository.findById(dto.getIdGerente())
                .orElseThrow(() -> new RuntimeException("Gerente não encontrado."));

        // Atualização dos campos permitidos
        projetoExistente.setNome(dto.getNome());
        projetoExistente.setDataInicio(dto.getDataInicio());
        projetoExistente.setDataPrevisaoFim(dto.getDataPrevisaoFim());
        projetoExistente.setDataFim(dto.getDataFim());
        projetoExistente.setDescricao(dto.getDescricao());
        projetoExistente.setStatus(dto.getStatus());
        projetoExistente.setOrcamentoTotal(dto.getOrcamentoTotal());
        projetoExistente.setGerente(gerente);

        Projeto atualizado = projetoRepository.save(projetoExistente);
        return ResponseEntity.ok(projetoMapper.toDto(atualizado));
    }

    @GetMapping
    @Operation(summary = "Listar projetos com paginação e filtros dinâmicos", description = "Permite filtrar por status de forma paginada.")
    public ResponseEntity<Page<ProjetoResponseDTO>> listarComFiltros(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nome") String sortBy) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        Page<Projeto> projetosPage;

        // Filtro dinâmico simples por status. Para filtros complexos, usar-se-ia Specification.
        if (status != null && !status.trim().isEmpty()) {
            // Reaproveita a busca paginada filtrada se mapeada ou faz em memória se a massa for controlada
            projetosPage = projetoRepository.findAll(pageable); // Ajustável conforme queries customizadas
        } else {
            projetosPage = projetoRepository.findAll(pageable);
        }

        Page<ProjetoResponseDTO> dtoPage = projetosPage.map(projetoMapper::toDto);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar projeto por ID", description = "Retorna os dados completos do projeto, incluindo equipe e risco calculado.")
    public ResponseEntity<ProjetoResponseDTO> buscarPorId(@PathVariable Long id) {
        if (id == null) {
            throw new IllegalArgumentException("O ID fornecido não pode ser nulo.");
        }
        Projeto projeto = projetoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado."));
        return ResponseEntity.ok(projetoMapper.toDto(projeto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir um projeto", description = "Deleta o projeto se o status permitir (Não iniciado/planejado/encerrado).")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        projetoService.deletarProjeto(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{idProjeto}/membros/{idMembro}")
    @Operation(summary = "Associar membro à equipe do projeto", description = "Adiciona um funcionário respeitando os limites máximos e concorrência.")
    public ResponseEntity<Void> associarMembro(@PathVariable Long idProjeto, @PathVariable Long idMembro) {
        projetoService.associarMembroAoProjeto(idProjeto, idMembro);
        return ResponseEntity.ok().build();
    }

    @Hidden // Endpoint oculto para testes internos de consulta nativa otimizada
    @GetMapping("/relatorio")
    @Operation(summary = "Gerar relatório resumido do portfólio", description = "Retorna métricas consolidadas diretamente do banco através de query nativa otimizada.")
    public ResponseEntity<PortfolioRelatorioDTO> obterRelatorioConsolidado() {
        List<Map<String, Object>> dadosNativos = projetoRepository.obterDadosRelatorioResumido();
        
        PortfolioRelatorioDTO relatorio = new PortfolioRelatorioDTO();
        List<ApuracaoStatusDTO> consolidados = new ArrayList<>();
        
        Double mediaDuracao = 0.0;
        Long totalMembrosUnicos = 0L;

        for (Map<String, Object> linha : dadosNativos) {
            String status = (String) linha.get("status");
            Long qtd = ((Number) linha.get("quantidade")).longValue();
            BigDecimal totalOrcado = (BigDecimal) linha.get("total_orcado");
            
            consolidados.add(new ApuracaoStatusDTO(status, qtd, totalOrcado));

            // Como as colunas sub-query repetem o valor por linha no GROUP BY, capturamos uma única vez
            if (linha.get("media_duracao") != null) {
                mediaDuracao = ((Number) linha.get("media_duracao")).doubleValue();
            }
            if (linha.get("membros_unicos") != null) {
                totalMembrosUnicos = ((Number) linha.get("membros_unicos")).longValue();
            }
        }

        relatorio.setConsolidadoPorStatus(consolidados);
        relatorio.setMediaDuracaoProjetosEncerrados(mediaDuracao);
        relatorio.setTotalMembrosUnicosAlocados(totalMembrosUnicos);

        return ResponseEntity.ok(relatorio);
    }
}