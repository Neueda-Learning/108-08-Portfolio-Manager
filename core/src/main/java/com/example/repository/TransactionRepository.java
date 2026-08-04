package com.example.repository;

import com.example.model.TransactionHistory;
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
public class TransactionRepository {
    private final JdbcTemplate jdbcTemplate;

    public TransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<TransactionHistory> rowMapper = (rs, rowNum) -> new TransactionHistory(
            rs.getLong("id"),
            rs.getLong("portfolio_id"),
            rs.getLong("asset_id"),
            rs.getString("transaction_type"),
            rs.getBigDecimal("quantity"),
            rs.getBigDecimal("price"),
            rs.getObject("transaction_date", LocalDateTime.class)
    );

    public TransactionHistory create(TransactionHistory transaction) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime transactionDate = transaction.getTransactionDate() != null
                ? transaction.getTransactionDate()
                : LocalDateTime.now();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO transaction_history(portfolio_id, asset_id, transaction_type, quantity, price, transaction_date) VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, transaction.getPortfolioId());
            ps.setLong(2, transaction.getAssetId());
            ps.setString(3, transaction.getTransactionType());
            ps.setBigDecimal(4, transaction.getQuantity());
            ps.setBigDecimal(5, transaction.getPrice());
            ps.setObject(6, transactionDate);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return new TransactionHistory(key != null ? key.longValue() : null, transaction.getPortfolioId(), transaction.getAssetId(),
                transaction.getTransactionType(), transaction.getQuantity(), transaction.getPrice(), transactionDate);
    }

    public Optional<TransactionHistory> findById(Long id) {
        List<TransactionHistory> rows = jdbcTemplate.query(
                "SELECT id, portfolio_id, asset_id, transaction_type, quantity, price, transaction_date FROM transaction_history WHERE id = ?",
                rowMapper,
                id
        );
        return rows.stream().findFirst();
    }

    public List<TransactionHistory> findAll() {
        return jdbcTemplate.query(
                "SELECT id, portfolio_id, asset_id, transaction_type, quantity, price, transaction_date FROM transaction_history ORDER BY id",
                rowMapper
        );
    }

    public List<TransactionHistory> findByPortfolioId(Long portfolioId) {
        return jdbcTemplate.query(
                "SELECT id, portfolio_id, asset_id, transaction_type, quantity, price, transaction_date FROM transaction_history WHERE portfolio_id = ? ORDER BY transaction_date DESC, id DESC",
                rowMapper,
                portfolioId
        );
    }

    public int update(TransactionHistory transaction) {
        return jdbcTemplate.update(
                "UPDATE transaction_history SET portfolio_id = ?, asset_id = ?, transaction_type = ?, quantity = ?, price = ?, transaction_date = ? WHERE id = ?",
                transaction.getPortfolioId(),
                transaction.getAssetId(),
                transaction.getTransactionType(),
                transaction.getQuantity(),
                transaction.getPrice(),
                transaction.getTransactionDate(),
                transaction.getId()
        );
    }

    public int delete(Long id) {
        return jdbcTemplate.update("DELETE FROM transaction_history WHERE id = ?", id);
    }
}
