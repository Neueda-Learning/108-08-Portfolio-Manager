package com.example.repository;

import com.example.model.Asset;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class AssetRepository {
    private final JdbcTemplate jdbcTemplate;

    public AssetRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Asset> rowMapper = (rs, rowNum) -> new Asset(
            rs.getLong("id"),
            rs.getString("symbol"),
            rs.getString("name"),
            rs.getString("asset_type"),
            rs.getBigDecimal("current_price")
    );

    public Asset create(Asset asset) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO asset(symbol, name, asset_type, current_price) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, asset.getSymbol());
            ps.setString(2, asset.getName());
            ps.setString(3, asset.getAssetType());
            ps.setBigDecimal(4, asset.getCurrentPrice());
            return ps;
        }, keyHolder);
        Number key = null;
        Map<String, Object> keys = keyHolder.getKeys();
        if (keys != null && keys.get("id") instanceof Number idValue) {
            key = idValue;
        } else if (keyHolder.getKeyList().size() == 1 && keyHolder.getKeyList().get(0).get("id") instanceof Number idValue) {
            key = idValue;
        } else if (keyHolder.getKeyList().size() == 1 && keyHolder.getKeyList().get(0).size() == 1) {
            Object firstValue = keyHolder.getKeyList().get(0).values().iterator().next();
            if (firstValue instanceof Number numberValue) {
                key = numberValue;
            }
        }
        return new Asset(key != null ? key.longValue() : null, asset.getSymbol(), asset.getName(), asset.getAssetType(),
                asset.getCurrentPrice());
    }

    public Optional<Asset> findById(Long id) {
        List<Asset> rows = jdbcTemplate.query(
                "SELECT id, symbol, name, asset_type, current_price FROM asset WHERE id = ?",
                rowMapper,
                id
        );
        return rows.stream().findFirst();
    }

    public List<Asset> findAll() {
        return jdbcTemplate.query(
                "SELECT id, symbol, name, asset_type, current_price FROM asset ORDER BY id",
                rowMapper
        );
    }

    public Optional<Asset> findBySymbol(String symbol) {
        List<Asset> rows = jdbcTemplate.query(
                "SELECT id, symbol, name, asset_type, current_price FROM asset WHERE symbol = ?",
                rowMapper,
                symbol
        );
        return rows.stream().findFirst();
    }

    public int update(Asset asset) {
        return jdbcTemplate.update(
                "UPDATE asset SET symbol = ?, name = ?, asset_type = ?, current_price = ? WHERE id = ?",
                asset.getSymbol(),
                asset.getName(),
                asset.getAssetType(),
                asset.getCurrentPrice(),
                asset.getId()
        );
    }

    public int delete(Long id) {
        return jdbcTemplate.update("DELETE FROM asset WHERE id = ?", id);
    }
}
