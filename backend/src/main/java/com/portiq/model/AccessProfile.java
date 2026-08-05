package com.example.model;

import java.util.List;

public class AccessProfile {
    private String role;
    private Long adminId;
    private Long fundManagerId;
    private Long customerId;
    private String landingPage;
    private List<String> permissions;

    public AccessProfile() {
    }

    public AccessProfile(String role, Long adminId, Long fundManagerId, Long customerId, String landingPage,
                         List<String> permissions) {
        this.role = role;
        this.adminId = adminId;
        this.fundManagerId = fundManagerId;
        this.customerId = customerId;
        this.landingPage = landingPage;
        this.permissions = permissions;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public Long getFundManagerId() {
        return fundManagerId;
    }

    public void setFundManagerId(Long fundManagerId) {
        this.fundManagerId = fundManagerId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getLandingPage() {
        return landingPage;
    }

    public void setLandingPage(String landingPage) {
        this.landingPage = landingPage;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}
