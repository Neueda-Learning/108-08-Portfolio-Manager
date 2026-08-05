package com.example.service;

import com.example.model.Admin;
import com.example.repository.AdminRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {
    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public Admin create(Admin admin) {
        return adminRepository.create(admin);
    }

    public Optional<Admin> findById(Long id) {
        return adminRepository.findById(id);
    }

    public List<Admin> findAll() {
        return adminRepository.findAll();
    }

    public int update(Admin admin) {
        return adminRepository.update(admin);
    }

    public int delete(Long id) {
        return adminRepository.delete(id);
    }
}
