package com.albaraka.controllers;

import com.albaraka.models.Operation;
import com.albaraka.services.interfaces.OperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agent/operations")
public class AgentOperationController {
    
    private final OperationService operationService;
    
    @Autowired
    public AgentOperationController(OperationService operationService) {
        this.operationService = operationService;
    }
    
    @GetMapping("/pending")
    public ResponseEntity<List<Operation>> getPendingOperations() {
        List<Operation> operations = operationService.getPendingOperations();
        return ResponseEntity.ok(operations);
    }
    
    @PutMapping("/{id}/approve")
    public ResponseEntity<Operation> approveOperation(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long agentId) {
        Operation operation = operationService.approveOperation(id, agentId);
        return ResponseEntity.ok(operation);
    }
    
    @PutMapping("/{id}/reject")
    public ResponseEntity<Operation> rejectOperation(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long agentId) {
        Operation operation = operationService.rejectOperation(id, agentId);
        return ResponseEntity.ok(operation);
    }
}

