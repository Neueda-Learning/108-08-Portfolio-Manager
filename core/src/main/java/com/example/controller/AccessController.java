package com.example.controller;

import com.example.model.AccessProfile;
import com.example.security.RoleAccess;
import com.example.security.RoleAccess.Role;
import com.example.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/access")
public class AccessController {
    private final DashboardService dashboardService;

    public AccessController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/me")
    public AccessProfile me(@RequestHeader("X-User-Role") String roleHeader,
                            @RequestHeader(value = "X-Admin-Id", required = false) Long adminIdHeader,
                            @RequestHeader(value = "X-Fund-Manager-Id", required = false) Long fundManagerIdHeader,
                            @RequestHeader(value = "X-Customer-Id", required = false) Long customerIdHeader) {
        Role role = RoleAccess.parseRole(roleHeader);
        return dashboardService.buildAccessProfile(role, adminIdHeader, fundManagerIdHeader, customerIdHeader);
    }
}
