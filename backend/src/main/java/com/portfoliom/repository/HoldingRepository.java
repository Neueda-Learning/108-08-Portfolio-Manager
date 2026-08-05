package com.portfoliom.repository;

import com.portfoliom.model.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {

    List<Holding> findByPortfolioId(Long portfolioId);

    Optional<Holding> findByIdAndPortfolioId(Long id, Long portfolioId);

    List<Holding> findByPortfolio_Owner_Id(Long ownerId);

    Optional<Holding> findByIdAndPortfolio_Owner_Id(Long id, Long ownerId);
}
