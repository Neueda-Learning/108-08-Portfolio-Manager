package com.example.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Benchmark {
    private Long id;
    private String name;
    private BigDecimal value;
    private LocalDateTime recordedDate;

    public Benchmark() {
    }

    public Benchmark(Long id, String name, BigDecimal value, LocalDateTime recordedDate) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.recordedDate = recordedDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public LocalDateTime getRecordedDate() {
        return recordedDate;
    }

    public void setRecordedDate(LocalDateTime recordedDate) {
        this.recordedDate = recordedDate;
    }
}
