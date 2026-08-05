package com.example.service;

import com.example.model.Customer;
import com.example.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer create(Customer customer) {
        return customerRepository.create(customer);
    }

    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id);
    }

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public List<Customer> findByFundManagerId(Long fundManagerId) {
        return customerRepository.findByFundManagerId(fundManagerId);
    }

    public int update(Customer customer) {
        return customerRepository.update(customer);
    }

    public int delete(Long id) {
        return customerRepository.delete(id);
    }
}
