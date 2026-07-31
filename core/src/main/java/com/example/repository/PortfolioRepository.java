package com.example.repository;

import com.example.model.Portfolio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class PortfolioRepository {
    private final JdbcTemplate jdbcTemplate;

    public PortfolioRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Portfolio> rowMapper = (rs, rowNum) -> new Portfolio(
            rs.getLong("id"),
            rs.getLong("customer_id"),
            rs.getString("portfolio_name"),
            rs.getBigDecimal("total_investment"),
            rs.getBigDecimal("current_value"),
            rs.getObject("created_at", LocalDateTime.class)
    );

    public Portfolio create(Portfolio portfolio) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime createdAt = portfolio.getCreatedAt() != null ? portfolio.getCreatedAt() : LocalDateTime.now();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO portfolio(customer_id, portfolio_name, total_investment, current_value, created_at) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, portfolio.getCustomerId());
            ps.setString(2, portfolio.getPortfolioName());
            ps.setBigDecimal(3, portfolio.getTotalInvestment());
            ps.setBigDecimal(4, portfolio.getCurrentValue());
            ps.setObject(5, createdAt);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return new Portfolio(key != null ? key.longValue() : null, portfolio.getCustomerId(), portfolio.getPortfolioName(),
                portfolio.getTotalInvestment(), portfolio.getCurrentValue(), createdAt);
    }

    public Optional<Portfolio> findById(Long id) {
        List<Portfolio> rows = jdbcTemplate.query(
                "SELECT id, customer_id, portfolio_name, total_investment, current_value, created_at FROM portfolio WHERE id = ?",
                rowMapper,
                id
        );
        return rows.stream().findFirst();
    }

    public List<Portfolio> findAll() {
        return jdbcTemplate.query(
                "SELECT id, customer_id, portfolio_name, total_investment, current_value, created_at FROM portfolio ORDER BY id",
                rowMapper
        );
    }

    public int update(Portfolio portfolio) {
        return jdbcTemplate.update(
                "UPDATE portfolio SET customer_id = ?, portfolio_name = ?, total_investment = ?, current_value = ?, created_at = ? WHERE id = ?",
                portfolio.getCustomerId(),
                portfolio.getPortfolioName(),
                portfolio.getTotalInvestment(),
                portfolio.getCurrentValue(),
                portfolio.getCreatedAt(),
                portfolio.getId()
        );
    }

    public int delete(Long id) {
        return jdbcTemplate.update("DELETE FROM portfolio WHERE id = ?", id);
    }
}
