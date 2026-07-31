package com.example.service;

import com.example.model.Asset;
import com.example.repository.AssetRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AssetService {
    private final AssetRepository assetRepository;

    public AssetService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    public Asset create(Asset asset) {
        return assetRepository.create(asset);
    }

    public Optional<Asset> findById(Long id) {
        return assetRepository.findById(id);
    }

    public List<Asset> findAll() {
        return assetRepository.findAll();
    }

    public int update(Asset asset) {
        return assetRepository.update(asset);
    }

    public int delete(Long id) {
        return assetRepository.delete(id);
    }
}
