package com.example.repository;

import com.example.model.Benchmark;
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
public class BenchmarkRepository {
    private final JdbcTemplate jdbcTemplate;

    public BenchmarkRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Benchmark> rowMapper = (rs, rowNum) -> new Benchmark(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getBigDecimal("value"),
            rs.getObject("recorded_date", LocalDateTime.class)
    );

    public Benchmark create(Benchmark benchmark) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime recordedDate = benchmark.getRecordedDate() != null ? benchmark.getRecordedDate() : LocalDateTime.now();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO benchmark(name, value, recorded_date) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, benchmark.getName());
            ps.setBigDecimal(2, benchmark.getValue());
            ps.setObject(3, recordedDate);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return new Benchmark(key != null ? key.longValue() : null, benchmark.getName(), benchmark.getValue(), recordedDate);
    }

    public Optional<Benchmark> findById(Long id) {
        List<Benchmark> rows = jdbcTemplate.query(
                "SELECT id, name, value, recorded_date FROM benchmark WHERE id = ?",
                rowMapper,
                id
        );
        return rows.stream().findFirst();
    }

    public List<Benchmark> findAll() {
        return jdbcTemplate.query("SELECT id, name, value, recorded_date FROM benchmark ORDER BY id", rowMapper);
    }

    public Optional<Benchmark> findEarliestByName(String name) {
        List<Benchmark> rows = jdbcTemplate.query(
                "SELECT id, name, value, recorded_date FROM benchmark WHERE name = ? ORDER BY recorded_date ASC LIMIT 1",
                rowMapper,
                name
        );
        return rows.stream().findFirst();
    }

    public Optional<Benchmark> findLatestByName(String name) {
        List<Benchmark> rows = jdbcTemplate.query(
                "SELECT id, name, value, recorded_date FROM benchmark WHERE name = ? ORDER BY recorded_date DESC LIMIT 1",
                rowMapper,
                name
        );
        return rows.stream().findFirst();
    }

    public int update(Benchmark benchmark) {
        return jdbcTemplate.update(
                "UPDATE benchmark SET name = ?, value = ?, recorded_date = ? WHERE id = ?",
                benchmark.getName(),
                benchmark.getValue(),
                benchmark.getRecordedDate(),
                benchmark.getId()
        );
    }

    public int delete(Long id) {
        return jdbcTemplate.update("DELETE FROM benchmark WHERE id = ?", id);
    }
}
