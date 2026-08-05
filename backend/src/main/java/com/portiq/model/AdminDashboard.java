package com.example.model;

import java.math.BigDecimal;
import java.util.List;

public class AdminDashboard {
    private long totalAdmins;
    private long totalFundManagers;
    private long totalCustomers;
    private long totalPortfolios;
    private long totalAssets;
    private BigDecimal assetsUnderManagement;
    private List<PermissionSummary> permissionMatrix;
    private List<UserAccessView> managedUsers;

    public AdminDashboard() {
    }

    public AdminDashboard(long totalAdmins, long totalFundManagers, long totalCustomers, long totalPortfolios,
                          long totalAssets, BigDecimal assetsUnderManagement, List<PermissionSummary> permissionMatrix,
                          List<UserAccessView> managedUsers) {
        this.totalAdmins = totalAdmins;
        this.totalFundManagers = totalFundManagers;
        this.totalCustomers = totalCustomers;
        this.totalPortfolios = totalPortfolios;
        this.totalAssets = totalAssets;
        this.assetsUnderManagement = assetsUnderManagement;
        this.permissionMatrix = permissionMatrix;
        this.managedUsers = managedUsers;
    }

    public long getTotalAdmins() {
        return totalAdmins;
    }

    public void setTotalAdmins(long totalAdmins) {
        this.totalAdmins = totalAdmins;
    }

    public long getTotalFundManagers() {
        return totalFundManagers;
    }

    public void setTotalFundManagers(long totalFundManagers) {
        this.totalFundManagers = totalFundManagers;
    }

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public long getTotalPortfolios() {
        return totalPortfolios;
    }

    public void setTotalPortfolios(long totalPortfolios) {
        this.totalPortfolios = totalPortfolios;
    }

    public long getTotalAssets() {
        return totalAssets;
    }

    public void setTotalAssets(long totalAssets) {
        this.totalAssets = totalAssets;
    }

    public BigDecimal getAssetsUnderManagement() {
        return assetsUnderManagement;
    }

    public void setAssetsUnderManagement(BigDecimal assetsUnderManagement) {
        this.assetsUnderManagement = assetsUnderManagement;
    }

    public List<PermissionSummary> getPermissionMatrix() {
        return permissionMatrix;
    }

    public void setPermissionMatrix(List<PermissionSummary> permissionMatrix) {
        this.permissionMatrix = permissionMatrix;
    }

    public List<UserAccessView> getManagedUsers() {
        return managedUsers;
    }

    public void setManagedUsers(List<UserAccessView> managedUsers) {
        this.managedUsers = managedUsers;
    }
}
