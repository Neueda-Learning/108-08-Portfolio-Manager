package com.example.repository;

import com.example.model.FundManager;
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
public class FundManagerRepository {
    private final JdbcTemplate jdbcTemplate;

    public FundManagerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<FundManager> rowMapper = (rs, rowNum) -> new FundManager(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getObject("created_at", LocalDateTime.class)
    );

    public FundManager create(FundManager fundManager) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime createdAt = fundManager.getCreatedAt() != null ? fundManager.getCreatedAt() : LocalDateTime.now();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO fund_manager(name, email, phone, created_at) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, fundManager.getName());
            ps.setString(2, fundManager.getEmail());
            ps.setString(3, fundManager.getPhone());
            ps.setObject(4, createdAt);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return new FundManager(key != null ? key.longValue() : null, fundManager.getName(), fundManager.getEmail(),
                fundManager.getPhone(), createdAt);
    }

    public Optional<FundManager> findById(Long id) {
        List<FundManager> rows = jdbcTemplate.query(
                "SELECT id, name, email, phone, created_at FROM fund_manager WHERE id = ?",
                rowMapper,
                id
        );
        return rows.stream().findFirst();
    }

    public List<FundManager> findAll() {
        return jdbcTemplate.query("SELECT id, name, email, phone, created_at FROM fund_manager ORDER BY id", rowMapper);
    }

    public int update(FundManager fundManager) {
        return jdbcTemplate.update(
                "UPDATE fund_manager SET name = ?, email = ?, phone = ?, created_at = ? WHERE id = ?",
                fundManager.getName(),
                fundManager.getEmail(),
                fundManager.getPhone(),
                fundManager.getCreatedAt(),
                fundManager.getId()
        );
    }

    public int delete(Long id) {
        return jdbcTemplate.update("DELETE FROM fund_manager WHERE id = ?", id);
    }
}

