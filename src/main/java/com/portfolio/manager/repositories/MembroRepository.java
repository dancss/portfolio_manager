package com.portfolio.manager.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.portfolio.manager.models.Membro;


public interface MembroRepository extends JpaRepository<Membro, Long> {
}