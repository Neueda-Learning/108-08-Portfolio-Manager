package com.portiq.service;

import com.portiq.dto.PortfolioRequest;
import com.portiq.exception.ResourceNotFoundException;
import com.portiq.model.Portfolio;
import com.portiq.model.User;
import com.portiq.repository.PortfolioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * All operations are scoped to a specific owner (the customer whose data is being accessed) so
 * that each customer's portfolios stay isolated from every other customer's. The owner may be
 * the authenticated user themselves (regular login) or, for fund-manager operations, the
 * customer being administered.
 */
@Service
@Transactional
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;

    public PortfolioService(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }

    @Transactional(readOnly = true)
    public List<Portfolio> getAll(Long ownerId) {
        return portfolioRepository.findByOwnerId(ownerId);
    }

    @Transactional(readOnly = true)
    public Portfolio getById(Long id, Long ownerId) {
        return portfolioRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found with id: " + id));
    }

    public Portfolio create(PortfolioRequest request, User owner) {
        Portfolio portfolio = new Portfolio(request.getName(), request.getDescription(), owner);
        return portfolioRepository.save(portfolio);
    }

    public Portfolio update(Long id, PortfolioRequest request, Long ownerId) {
        Portfolio portfolio = getById(id, ownerId);
        portfolio.setName(request.getName());
        portfolio.setDescription(request.getDescription());
        return portfolioRepository.save(portfolio);
    }

    public void delete(Long id, Long ownerId) {
        Portfolio portfolio = getById(id, ownerId);
        portfolioRepository.delete(portfolio);
    }

    public Portfolio getOrCreateDefault(User owner) {
        return portfolioRepository.findByOwnerId(owner.getId()).stream()
                .findFirst()
                .orElseGet(() -> portfolioRepository.save(new Portfolio("My Portfolio", "Default portfolio", owner)));
    }

    /**
     * Deletes every portfolio (and, via cascade, holdings) belonging to the given owner. Used
     * when a fund manager removes a customer account.
     */
    public void deleteAllForOwner(Long ownerId) {
        portfolioRepository.deleteAll(portfolioRepository.findByOwnerId(ownerId));
    }
}
