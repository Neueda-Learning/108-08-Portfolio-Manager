package com.example.repository;

import com.example.model.Account;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

@Repository
public class AccountJdbcRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert accountInsert;

    public AccountJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.accountInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("accounts")
                .usingColumns("name", "email")
                .withoutTableColumnMetaDataAccess()
                .usingGeneratedKeyColumns("id");
    }

    private final RowMapper<Account> accountRowMapper = (ResultSet rs, int rowNum) -> new Account(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("email")
    );

    public Account createAccount(Account account) {
        Number generatedId = accountInsert.executeAndReturnKey(Map.of(
                "name", account.name(),
                "email", account.email()
        ));
        return new Account(generatedId.intValue(), account.name(), account.email());
    }

    public List<Account> getAllAccounts() {
        return jdbcTemplate.query(
                "SELECT id, name, email FROM accounts ORDER BY id",
                accountRowMapper
        );
    }
}
