package com.example.service;

import com.example.model.FundManager;
import com.example.repository.FundManagerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FundManagerService {
    private final FundManagerRepository fundManagerRepository;

    public FundManagerService(FundManagerRepository fundManagerRepository) {
        this.fundManagerRepository = fundManagerRepository;
    }

    public FundManager create(FundManager fundManager) {
        return fundManagerRepository.create(fundManager);
    }

    public Optional<FundManager> findById(Long id) {
        return fundManagerRepository.findById(id);
    }

    public List<FundManager> findAll() {
        return fundManagerRepository.findAll();
    }

    public int update(FundManager fundManager) {
        return fundManagerRepository.update(fundManager);
    }

    public int delete(Long id) {
        return fundManagerRepository.delete(id);
    }
}

