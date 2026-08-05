package com.example.model;

import java.util.List;

public class PermissionSummary {
    private String role;
    private String description;
    private List<String> allowedActions;

    public PermissionSummary() {
    }

    public PermissionSummary(String role, String description, List<String> allowedActions) {
        this.role = role;
        this.description = description;
        this.allowedActions = allowedActions;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getAllowedActions() {
        return allowedActions;
    }

    public void setAllowedActions(List<String> allowedActions) {
        this.allowedActions = allowedActions;
    }
}
