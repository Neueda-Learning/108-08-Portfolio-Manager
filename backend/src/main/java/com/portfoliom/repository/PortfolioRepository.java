package com.portfoliom.repository;

import com.portfoliom.model.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    List<Portfolio> findByOwnerId(Long ownerId);
    Optional<Portfolio> findByIdAndOwnerId(Long id, Long ownerId);
}
