package com.example.model;

import java.math.BigDecimal;

public class DashboardTimePoint {
    private String label;
    private BigDecimal value;

    public DashboardTimePoint() {
    }

    public DashboardTimePoint(String label, BigDecimal value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
