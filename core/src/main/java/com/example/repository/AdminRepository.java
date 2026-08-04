package com.example.repository;

import com.example.model.Admin;
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
public class AdminRepository {
    private final JdbcTemplate jdbcTemplate;

    public AdminRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Admin> rowMapper = (rs, rowNum) -> new Admin(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getObject("created_at", LocalDateTime.class)
    );

    public Admin create(Admin admin) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime createdAt = admin.getCreatedAt() != null ? admin.getCreatedAt() : LocalDateTime.now();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO admin(name, email, password, created_at) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, admin.getName());
            ps.setString(2, admin.getEmail());
            ps.setString(3, admin.getPassword());
            ps.setObject(4, createdAt);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return new Admin(key != null ? key.longValue() : null, admin.getName(), admin.getEmail(), admin.getPassword(), createdAt);
    }

    public Optional<Admin> findById(Long id) {
        List<Admin> rows = jdbcTemplate.query("SELECT id, name, email, password, created_at FROM admin WHERE id = ?", rowMapper, id);
        return rows.stream().findFirst();
    }

    public List<Admin> findAll() {
        return jdbcTemplate.query("SELECT id, name, email, password, created_at FROM admin ORDER BY id", rowMapper);
    }

    public int update(Admin admin) {
        return jdbcTemplate.update(
                "UPDATE admin SET name = ?, email = ?, password = ?, created_at = ? WHERE id = ?",
                admin.getName(),
                admin.getEmail(),
                admin.getPassword(),
                admin.getCreatedAt(),
                admin.getId()
        );
    }

    public int delete(Long id) {
        return jdbcTemplate.update("DELETE FROM admin WHERE id = ?", id);
    }
}
