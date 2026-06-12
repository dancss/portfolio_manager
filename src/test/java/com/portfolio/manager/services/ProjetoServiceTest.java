package com.portfolio.manager.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.portfolio.manager.models.Membro;
import com.portfolio.manager.models.Projeto;
import com.portfolio.manager.repositories.MembroRepository;
import com.portfolio.manager.repositories.ProjetoRepository;

@ExtendWith(MockitoExtension.class)
class ProjetoServiceTest {

    @Mock
    private ProjetoRepository projetoRepository;

    @Mock
    private MembroRepository membroRepository;

    @InjectMocks
    private ProjetoService projetoService;

    // =========================================================================
    // 🧪 REQUISITO 1: EXCLUSÃO DE PROJETOS (deletarProjeto)
    // =========================================================================

    @Test
    @DisplayName("Deve deletar projeto com sucesso quando status for permitido (ex: em análise)")
    void deveDeletarProjetoComSucesso() {
        Long idProjeto = 10L;
        Projeto projetoFake = new Projeto();
        projetoFake.setId(idProjeto);
        projetoFake.setStatus("em análise");

        when(projetoRepository.findById(idProjeto)).thenReturn(Optional.of(projetoFake));

        assertDoesNotThrow(() -> projetoService.deletarProjeto(idProjeto));

        verify(projetoRepository, times(1)).delete(projetoFake);
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Deve lançar IllegalStateException ao tentar deletar projeto com status 'iniciado'")
    void deveLancarExcecaoAoDeletarProjetoIniciado() {
        Long idProjeto = 15L;
        Projeto projetoFake = new Projeto();
        projetoFake.setId(idProjeto);
        projetoFake.setStatus("iniciado");

        when(projetoRepository.findById(idProjeto)).thenReturn(Optional.of(projetoFake));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            projetoService.deletarProjeto(idProjeto);
        });

        assertTrue(exception.getMessage().contains("Não é permitido excluir um projeto com status: iniciado"));
        verify(projetoRepository, never()).delete(any());
    }

    // =========================================================================
    // 🧪 REQUISITO 2: MÁQUINA DE ESTADOS (validarTransicaoStatus)
    // =========================================================================

    @Test
    @DisplayName("Deve permitir transição válida respeitando a sequência exata da regra")
    void devePermitirTransicaoValida() {
        // Testando um passo sequencial do ciclo
        assertDoesNotThrow(() -> projetoService.validarTransicaoStatus("em análise", "análise realizada"));
        assertDoesNotThrow(() -> projetoService.validarTransicaoStatus("planejado", "iniciado"));
    }

    @Test
    @DisplayName("Deve permitir transição para 'cancelado' a partir de qualquer estado do ciclo")
    void devePermitirTransicaoParaCanceladoALguemMomento() {
        assertDoesNotThrow(() -> projetoService.validarTransicaoStatus("em análise", "cancelado"));
        assertDoesNotThrow(() -> projetoService.validarTransicaoStatus("em andamento", "cancelado"));
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException ao tentar pular etapas no ciclo de vida")
    void deveLancarExcecaoAoPularEtapas() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            projetoService.validarTransicaoStatus("em análise", "iniciado");
        });

        assertEquals("Transição de status inválida de 'em análise' para 'iniciado'.", exception.getMessage());
    }

    // =========================================================================
    // 🧪 REQUISITO 3: ALOCAÇÃO DE EQUIPES (associarMembroAoProjeto)
    // =========================================================================

    @Test
    @DisplayName("Deve associar membro com sucesso quando passar por todas as regras restritas")
    void deveAssociarMembroComSucesso() {
        Long idProjeto = 1L;
        Long idMembro = 2L;

        Projeto projetoFake = new Projeto();
        projetoFake.setMembros(new HashSet<>());

        Membro membroFake = new Membro();
        membroFake.setAtribuicao("funcionário");

        when(projetoRepository.findById(idProjeto)).thenReturn(Optional.of(projetoFake));
        when(membroRepository.findById(idMembro)).thenReturn(Optional.of(membroFake));
        when(projetoRepository.findAll()).thenReturn(new ArrayList<>()); // Nenhuma alocação prévia ativa

        assertDoesNotThrow(() -> projetoService.associarMembroAoProjeto(idProjeto, idMembro));

        assertTrue(projetoFake.getMembros().contains(membroFake));
        verify(projetoRepository, times(1)).save(projetoFake);
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Deve barrar associação caso o membro possua atribuição diferente de 'funcionário' (Ex: GERENTE)")
    void deveBarrarMembroComAtribuicaoInvalida() {
        Long idProjeto = 1L;
        Long idMembro = 3L;

        Projeto projetoFake = new Projeto();
        Membro gerenteFake = new Membro();
        gerenteFake.setAtribuicao("GERENTE");

        when(projetoRepository.findById(idProjeto)).thenReturn(Optional.of(projetoFake));
        when(membroRepository.findById(idMembro)).thenReturn(Optional.of(gerenteFake));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            projetoService.associarMembroAoProjeto(idProjeto, idMembro);
        });

        assertEquals("Apenas membros com a atribuição 'funcionário' podem ser associados a um projeto.", exception.getMessage());
        verify(projetoRepository, never()).save(any());
    }

    // =========================================================================
    // 🧪 REQUISITO 4: CLASSIFICAÇÃO DE RISCO DINÂMICO
    // =========================================================================

    @Test
    @DisplayName("Deve classificar como 'Alto risco' quando orçamento for maior que 500k ou prazo maior que 6 meses")
    void deveCalcularAltoRisco() {
        Projeto projetoAltoRisco = new Projeto();
        projetoAltoRisco.setDataInicio(LocalDate.of(2026, 1, 1));
        projetoAltoRisco.setDataPrevisaoFim(LocalDate.of(2026, 9, 1)); // 8 meses
        projetoAltoRisco.setOrcamentoTotal(new BigDecimal("600000"));

        String risco = projetoService.calcularRiscoDinamico(projetoAltoRisco);

        assertEquals("Alto risco", risco);
    }
}