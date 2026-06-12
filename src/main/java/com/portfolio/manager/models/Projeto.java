package com.portfolio.manager.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "projeto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Projeto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    @Column(name = "data_previsao_fim")
    private LocalDate dataPrevisaoFim;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(name = "orcamento_total", precision = 15, scale = 2)
    private BigDecimal orcamentoTotal;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false, length = 50)
    private String status; // em análise, iniciado, planejado, etc.

    // Relacionamento com o Gerente Responsável (Muitos projetos para 1 Gerente)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idgerente", nullable = false)
    private Membro gerente;

    // Relacionamento de Membros Equipe (Muitos para Muitos)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "membros_projeto",
        joinColumns = @JoinColumn(name = "idprojeto"),
        inverseJoinColumns = @JoinColumn(name = "idmembro")
    )
    @Builder.Default
    private Set<Membro> membros = new HashSet<>();
}