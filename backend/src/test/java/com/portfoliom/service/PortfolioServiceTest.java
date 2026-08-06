package com.portfoliom.service;

import com.portfoliom.dto.PortfolioRequest;
import com.portfoliom.exception.ResourceNotFoundException;
import com.portfoliom.model.Portfolio;
import com.portfoliom.model.Role;
import com.portfoliom.model.User;
import com.portfoliom.repository.PortfolioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @InjectMocks
    private PortfolioService portfolioService;

    private User owner;
    private Portfolio portfolio;

    @BeforeEach
    void setUp() {
        owner = new User("owner", "encoded", Role.OWNER);
        owner.setId(7L);
        portfolio = new Portfolio("Tech Growth", "Tech stocks");
        portfolio.setId(1L);
        portfolio.setOwner(owner);
    }

    @Test
    void getAll_returnsAllPortfolios() {
        when(portfolioRepository.findByOwnerId(7L)).thenReturn(List.of(portfolio));

        List<Portfolio> result = portfolioService.getAll(7L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Tech Growth");
    }

    @Test
    void getById_returnsPortfolio_whenExists() {
        when(portfolioRepository.findByIdAndOwnerId(1L, 7L)).thenReturn(Optional.of(portfolio));

        Portfolio result = portfolioService.getById(1L, 7L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Tech Growth");
    }

    @Test
    void getById_throwsException_whenNotFound() {
        when(portfolioRepository.findByIdAndOwnerId(99L, 7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> portfolioService.getById(99L, 7L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_savesAndReturnsPortfolio() {
        PortfolioRequest request = new PortfolioRequest();
        request.setName("New Portfolio");
        request.setDescription("Description");

        Portfolio saved = new Portfolio("New Portfolio", "Description");
        saved.setId(2L);

        when(portfolioRepository.save(any(Portfolio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Portfolio result = portfolioService.create(request, owner);

        assertThat(result.getName()).isEqualTo("New Portfolio");
        assertThat(result.getOwner()).isEqualTo(owner);
        verify(portfolioRepository, times(1)).save(any(Portfolio.class));
    }

    @Test
    void update_updatesFieldsAndSaves() {
        PortfolioRequest request = new PortfolioRequest();
        request.setName("Updated Name");
        request.setDescription("Updated Desc");

        when(portfolioRepository.findByIdAndOwnerId(1L, 7L)).thenReturn(Optional.of(portfolio));
        when(portfolioRepository.save(any(Portfolio.class))).thenAnswer(inv -> inv.getArgument(0));

        Portfolio result = portfolioService.update(1L, request, 7L);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getDescription()).isEqualTo("Updated Desc");
    }

    @Test
    void delete_removesPortfolio() {
        when(portfolioRepository.findByIdAndOwnerId(1L, 7L)).thenReturn(Optional.of(portfolio));

        portfolioService.delete(1L, 7L);

        verify(portfolioRepository, times(1)).delete(portfolio);
    }

    @Test
    void delete_throwsException_whenPortfolioNotFound() {
        when(portfolioRepository.findByIdAndOwnerId(99L, 7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> portfolioService.delete(99L, 7L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getOrCreateDefault_createsPortfolioWhenOwnerHasNone() {
        when(portfolioRepository.findByOwnerId(7L)).thenReturn(List.of());
        when(portfolioRepository.save(any(Portfolio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Portfolio result = portfolioService.getOrCreateDefault(owner);

        assertThat(result.getName()).isEqualTo("My Portfolio");
        assertThat(result.getDescription()).isEqualTo("Default portfolio");
        assertThat(result.getOwner()).isEqualTo(owner);
    }

    @Test
    void deleteAllForOwner_deletesOnlyOwnedPortfolios() {
        List<Portfolio> owned = List.of(portfolio, new Portfolio("Income", "Bonds", owner));
        when(portfolioRepository.findByOwnerId(7L)).thenReturn(owned);

        portfolioService.deleteAllForOwner(7L);

        verify(portfolioRepository).deleteAll(owned);
    }
}
