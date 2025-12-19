package com.albaraka.controllers;

import com.albaraka.dto.DocumentUploadResponse;
import com.albaraka.dto.OperationRequest;
import com.albaraka.models.Document;
import com.albaraka.models.Operation;
import com.albaraka.services.interfaces.OperationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/client/operations")
public class ClientOperationController {
    
    private final OperationService operationService;
    
    @Autowired
    public ClientOperationController(OperationService operationService) {
        this.operationService = operationService;
    }
    
    @PostMapping
    public ResponseEntity<Operation> createOperation(
            @RequestHeader("X-User-Id") Long clientId,
            @Valid @RequestBody OperationRequest request) {
        Operation operation = operationService.createOperation(clientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(operation);
    }
    
    @PostMapping("/{id}/document")
    public ResponseEntity<DocumentUploadResponse> uploadDocument(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        Document document = operationService.uploadDocument(id, file);
        
        DocumentUploadResponse response = new DocumentUploadResponse(
            document.getId(),
            document.getFileName(),
            document.getFileType(),
            "Document uploaded successfully"
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<Operation>> getClientOperations(
            @RequestHeader("X-User-Id") Long clientId) {
        List<Operation> operations = operationService.getClientOperations(clientId);
        return ResponseEntity.ok(operations);
    }
}

