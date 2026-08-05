package com.example.service;

import com.example.model.Account;
import com.example.repository.AccountJdbcRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {
    private final AccountJdbcRepository accountRepository;

    public AccountService(AccountJdbcRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account createAccount(Account account)
    {
        return accountRepository.createAccount(account);
    }

    public List<Account> getAllAccounts()
    {
        return accountRepository.getAllAccounts();
    }
}
