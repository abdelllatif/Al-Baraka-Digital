package com.albaraka.controllers;

import com.albaraka.config.CustomUserDetails;
import com.albaraka.models.Operation;
import com.albaraka.services.interfaces.OperationService;
import com.albaraka.services.interfaces.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agent/operations")
public class AgentOperationController {
    
    private final OperationService operationService;
    private final UserService userService;
    private final CustomUserDetails customUserDetails;
    @Autowired
    public AgentOperationController(OperationService operationService, UserService userService, CustomUserDetails customUserDetails) {
        this.operationService = operationService;
        this.userService = userService;
        this.customUserDetails = customUserDetails;
    }
    
    @GetMapping("/pending")
    public ResponseEntity<List<Operation>> getPendingOperations() {
        List<Operation> operations = operationService.getPendingOperations();
        return ResponseEntity.ok(operations);
    }
    
    @PutMapping("/{id}/approve")
    public ResponseEntity<Operation> approveOperation(
            @PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = customUserDetails.getUsername();
        Long agentId = userService.findByEmail(email).getId();
        Operation operation = operationService.approveOperation(id, agentId);
        return ResponseEntity.ok(operation);
    }
    
    @PutMapping("/{id}/reject")
    public ResponseEntity<Operation> rejectOperation(
            @PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = customUserDetails.getUsername();
        Long agentId = userService.findByEmail(email).getId();
        Operation operation = operationService.rejectOperation(id, agentId);
        return ResponseEntity.ok(operation);
    }
}

