package com.example.repository;

import com.example.model.Customer;
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
public class CustomerRepository {
    private final JdbcTemplate jdbcTemplate;

    public CustomerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Customer> rowMapper = (rs, rowNum) -> new Customer(
            rs.getLong("id"),
            rs.getLong("fund_manager_id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getObject("created_at", LocalDateTime.class)
    );

    public Customer create(Customer customer) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime createdAt = customer.getCreatedAt() != null ? customer.getCreatedAt() : LocalDateTime.now();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO customer(fund_manager_id, name, email, phone, created_at) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, customer.getFundManagerId());
            ps.setString(2, customer.getName());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getPhone());
            ps.setObject(5, createdAt);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return new Customer(key != null ? key.longValue() : null, customer.getFundManagerId(), customer.getName(),
                customer.getEmail(), customer.getPhone(), createdAt);
    }

    public Optional<Customer> findById(Long id) {
        List<Customer> rows = jdbcTemplate.query(
                "SELECT id, fund_manager_id, name, email, phone, created_at FROM customer WHERE id = ?",
                rowMapper,
                id
        );
        return rows.stream().findFirst();
    }

    public List<Customer> findAll() {
        return jdbcTemplate.query(
                "SELECT id, fund_manager_id, name, email, phone, created_at FROM customer ORDER BY id",
                rowMapper
        );
    }

    public List<Customer> findByFundManagerId(Long fundManagerId) {
        return jdbcTemplate.query(
                "SELECT id, fund_manager_id, name, email, phone, created_at FROM customer WHERE fund_manager_id = ? ORDER BY id",
                rowMapper,
                fundManagerId
        );
    }

    public int update(Customer customer) {
        return jdbcTemplate.update(
                "UPDATE customer SET fund_manager_id = ?, name = ?, email = ?, phone = ?, created_at = ? WHERE id = ?",
                customer.getFundManagerId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getCreatedAt(),
                customer.getId()
        );
    }

    public int delete(Long id) {
        return jdbcTemplate.update("DELETE FROM customer WHERE id = ?", id);
    }
}
