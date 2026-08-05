package com.example.model;

public class UserAccessView {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String scope;

    public UserAccessView() {
    }

    public UserAccessView(Long id, String name, String email, String role, String scope) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.scope = scope;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
