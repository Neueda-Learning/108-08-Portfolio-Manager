package com.example.repository;

import com.example.model.PortfolioHolding;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class PortfolioHoldingRepository {
    private final JdbcTemplate jdbcTemplate;

    public PortfolioHoldingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<PortfolioHolding> rowMapper = (rs, rowNum) -> new PortfolioHolding(
            rs.getLong("id"),
            rs.getLong("portfolio_id"),
            rs.getLong("asset_id"),
            rs.getBigDecimal("quantity"),
            rs.getBigDecimal("average_buy_price"),
            rs.getBigDecimal("invested_amount"),
            rs.getBigDecimal("current_value")
    );

    public PortfolioHolding create(PortfolioHolding holding) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO portfolio_holding(portfolio_id, asset_id, quantity, average_buy_price, invested_amount, current_value) VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, holding.getPortfolioId());
            ps.setLong(2, holding.getAssetId());
            ps.setBigDecimal(3, holding.getQuantity());
            ps.setBigDecimal(4, holding.getAverageBuyPrice());
            ps.setBigDecimal(5, holding.getInvestedAmount());
            ps.setBigDecimal(6, holding.getCurrentValue());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return new PortfolioHolding(key != null ? key.longValue() : null, holding.getPortfolioId(), holding.getAssetId(),
                holding.getQuantity(), holding.getAverageBuyPrice(), holding.getInvestedAmount(), holding.getCurrentValue());
    }

    public Optional<PortfolioHolding> findById(Long id) {
        List<PortfolioHolding> rows = jdbcTemplate.query(
                "SELECT id, portfolio_id, asset_id, quantity, average_buy_price, invested_amount, current_value FROM portfolio_holding WHERE id = ?",
                rowMapper,
                id
        );
        return rows.stream().findFirst();
    }

    public List<PortfolioHolding> findAll() {
        return jdbcTemplate.query(
                "SELECT id, portfolio_id, asset_id, quantity, average_buy_price, invested_amount, current_value FROM portfolio_holding ORDER BY id",
                rowMapper
        );
    }

    public int update(PortfolioHolding holding) {
        return jdbcTemplate.update(
                "UPDATE portfolio_holding SET portfolio_id = ?, asset_id = ?, quantity = ?, average_buy_price = ?, invested_amount = ?, current_value = ? WHERE id = ?",
                holding.getPortfolioId(),
                holding.getAssetId(),
                holding.getQuantity(),
                holding.getAverageBuyPrice(),
                holding.getInvestedAmount(),
                holding.getCurrentValue(),
                holding.getId()
        );
    }

    public int delete(Long id) {
        return jdbcTemplate.update("DELETE FROM portfolio_holding WHERE id = ?", id);
    }
}
