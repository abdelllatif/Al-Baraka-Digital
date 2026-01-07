package com.albaraka.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller

public class RedirectController {
    @GetMapping("/")
    public String root(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/web/login";
        }
        String role = authentication.getAuthorities().toString();
        if (role.contains("CLIENT")) return "redirect:/web/client/dashboard";
        if (role.contains("AGENT_BANCAIRE")) return "redirect:/web/agent/dashboard";
        if (role.contains("ADMIN")) return "redirect:/web/admin/dashboard";
        return "redirect:/login";
    }

}
