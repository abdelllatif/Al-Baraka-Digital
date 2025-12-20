package com.albaraka.controllers;

import com.albaraka.config.CustomUserDetails;
import com.albaraka.dto.DocumentUploadResponse;
import com.albaraka.dto.OperationRequest;
import com.albaraka.models.Document;
import com.albaraka.models.Operation;
import com.albaraka.services.interfaces.OperationService;
import com.albaraka.services.interfaces.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/client/operations")
public class ClientOperationController {
    
    private final OperationService operationService;
    private final UserService userService;

    @Autowired
    public ClientOperationController(OperationService operationService, UserService userService) {
        this.operationService = operationService;
        this.userService = userService;
    }
    
    @PostMapping
    public ResponseEntity<Operation> createOperation(
            @Valid @RequestBody OperationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
       String email = customUserDetails.getUsername();
       Long clientId = userService.findByEmail(email).getId();
        Operation operation = operationService.createOperation(clientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(operation);
    }
    
    @PostMapping("/{id}/document")
    public ResponseEntity<DocumentUploadResponse> uploadDocument(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = customUserDetails.getUsername();
        Long clientId = userService.findByEmail(email).getId();
        Document document = operationService.uploadDocument(id, file, clientId);
        
        DocumentUploadResponse response = new DocumentUploadResponse(
            document.getId(),
            document.getFileName(),
            document.getFileType(),
            "Document uploaded successfully"
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<Operation>> getClientOperations() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = customUserDetails.getUsername();
        Long clientId = userService.findByEmail(email).getId();
        List<Operation> operations = operationService.getClientOperations(clientId);
        return ResponseEntity.ok(operations);
    }
}

