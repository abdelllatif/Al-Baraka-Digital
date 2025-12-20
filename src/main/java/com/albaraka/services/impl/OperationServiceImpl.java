package com.albaraka.services.impl;

import com.albaraka.dto.OperationRequest;
import com.albaraka.enums.OperationStatus;
import com.albaraka.enums.OperationType;
import com.albaraka.exceptions.ResourceNotFoundException;
import com.albaraka.exceptions.UnauthorizedAccessException;
import com.albaraka.models.Account;
import com.albaraka.models.Document;
import com.albaraka.models.Operation;
import com.albaraka.models.User;
import com.albaraka.repositories.AccountRepository;
import com.albaraka.repositories.DocumentRepository;
import com.albaraka.repositories.OperationRepository;
import com.albaraka.repositories.UserRepository;
import com.albaraka.services.interfaces.OperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OperationServiceImpl implements OperationService {
    
    private static final BigDecimal THRESHOLD_AMOUNT = new BigDecimal("10000");
    
    private final OperationRepository operationRepository;
    private final AccountRepository accountRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    @Autowired
    public OperationServiceImpl(
            OperationRepository operationRepository,
            AccountRepository accountRepository,
            DocumentRepository documentRepository,
            UserRepository userRepository) {
        this.operationRepository = operationRepository;
        this.accountRepository = accountRepository;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }
    
    @Override
    @Transactional
    public Operation createOperation(Long clientId, OperationRequest request) {
        User client = userRepository.findById(clientId)
            .orElseThrow(() -> new ResourceNotFoundException("User", clientId));
        
        Operation operation = new Operation();
        operation.setType(request.getType());
        operation.setAmount(request.getAmount());
        switch (request.getType()) {
            case DEPOSIT:
                handleDeposit(request, operation, client);
                break;
            case WITHDRAWAL:
                handleWithdrawal(request, operation, client);
                break;
            case TRANSFER:
                handleTransfer(request, operation, client);
                break;
            default:
                throw new IllegalArgumentException("Invalid operation type");
        }
        
        if (request.getAmount().compareTo(THRESHOLD_AMOUNT) > 0) {
            operation.setStatus(OperationStatus.PENDING);
        } else {
            operation.setStatus(OperationStatus.APPROVED);
            // Auto-execute if approved
            executeOperation(operation);
        }
        
        return operationRepository.save(operation);
    }
    
    private void handleDeposit(OperationRequest request, Operation operation, User client) {
        Account account = accountRepository.findByOwnerId(client.getId())
            .stream()
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Account not found for client"));
        
        operation.setAccountDestination(account);
    }
    
    private void handleWithdrawal(OperationRequest request, Operation operation, User client) {
        Account account = accountRepository.findByOwnerId(client.getId())
            .stream()
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Account not found for client"));
        if (request.getAmount().compareTo(account.getBalance()) > 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        
        operation.setAccountSource(account);
    }
    
    private void handleTransfer(OperationRequest request, Operation operation, User client) {
        if (request.getAccountSourceId() == null || request.getAccountDestinationId() == null) {
            throw new IllegalArgumentException("Source and destination accounts are required for transfer");
        }
        
        Account sourceAccount = accountRepository.findByIdAndOwnerId(
            request.getAccountSourceId(), client.getId())
            .orElseThrow(() -> new UnauthorizedAccessException("Source account not found or does not belong to you"));
        
        Account destinationAccount = accountRepository.findById(request.getAccountDestinationId())
            .orElseThrow(() -> new ResourceNotFoundException("Account", request.getAccountDestinationId()));
        if (request.getAmount().compareTo(THRESHOLD_AMOUNT) <= 0) {
            if (request.getAmount().compareTo(sourceAccount.getBalance()) > 0) {
                throw new IllegalArgumentException("Insufficient balance");
            }
        }
        
        operation.setAccountSource(sourceAccount);
        operation.setAccountDestination(destinationAccount);
    }
    
    @Override
    @Transactional
    public Document uploadDocument(Long operationId, MultipartFile file,Long clientId) {
        Operation operation = operationRepository.findById(operationId)
            .orElseThrow(() -> new ResourceNotFoundException("Operation", operationId));
        
        if (operation.getStatus() != OperationStatus.PENDING) {
            throw new IllegalArgumentException("Documents can only be uploaded for pending operations");
        }
        if(operation.getAccountSource().getOwner().getId().equals(clientId))
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(uniqueFilename);
            
            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Create document entity
            Document document = new Document();
            document.setFileName(originalFilename != null ? originalFilename : uniqueFilename);
            document.setFileType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
            document.setStoragePath(filePath.toString());
            document.setOperation(operation);
            
            return documentRepository.save(document);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Operation> getClientOperations(Long clientId) {
        List<Operation> sourceOps = operationRepository.findByAccountSourceOwnerId(clientId);
        List<Operation> destOps = operationRepository.findByAccountDestinationOwnerId(clientId);
        sourceOps.addAll(destOps);
        return sourceOps.stream()
            .distinct()
            .toList();
    }
    
    @Override
    public List<Operation> getPendingOperations() {
        return operationRepository.findByStatus(OperationStatus.PENDING);
    }
    
    @Override
    @Transactional
    public Operation approveOperation(Long operationId, Long agentId) {
        User agent = userRepository.findById(agentId)
            .orElseThrow(() -> new ResourceNotFoundException("User", agentId));
        
        Operation operation = operationRepository.findById(operationId)
            .orElseThrow(() -> new ResourceNotFoundException("Operation", operationId));
        
        if (operation.getStatus() != OperationStatus.PENDING) {
            throw new IllegalArgumentException("Only pending operations can be approved");
        }
        operation.setStatus(OperationStatus.APPROVED);
        operation.setValidatedAt(LocalDateTime.now());
        executeOperation(operation);
        
        return operationRepository.save(operation);
    }
    
    @Override
    @Transactional
    public Operation rejectOperation(Long operationId, Long agentId) {
        User agent = userRepository.findById(agentId)
            .orElseThrow(() -> new ResourceNotFoundException("User", agentId));
        
        Operation operation = operationRepository.findById(operationId)
            .orElseThrow(() -> new ResourceNotFoundException("Operation", operationId));
        
        if (operation.getStatus() != OperationStatus.PENDING) {
            throw new IllegalArgumentException("Only pending operations can be rejected");
        }
        
        operation.setStatus(OperationStatus.REJECTED);
        operation.setValidatedAt(LocalDateTime.now());
        
        return operationRepository.save(operation);
    }
    
    @Transactional
    private void executeOperation(Operation operation) {
        if (operation.getStatus() != OperationStatus.APPROVED) {
            return;
        }
        
        switch (operation.getType()) {
            case DEPOSIT:
                executeDeposit(operation);
                break;
            case WITHDRAWAL:
                executeWithdrawal(operation);
                break;
            case TRANSFER:
                executeTransfer(operation);
                break;
        }
        
        operation.setStatus(OperationStatus.EXECUTED);
        operation.setExecutedAt(LocalDateTime.now());
        operationRepository.save(operation);
    }
    
    private void executeDeposit(Operation operation) {
        Account account = operation.getAccountDestination();
        account.setBalance(account.getBalance().add(operation.getAmount()));
        accountRepository.save(account);
    }
    
    private void executeWithdrawal(Operation operation) {
        Account account = operation.getAccountSource();
        if (operation.getAmount().compareTo(account.getBalance()) > 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        account.setBalance(account.getBalance().subtract(operation.getAmount()));
        accountRepository.save(account);
    }
    
    private void executeTransfer(Operation operation) {
        Account source = operation.getAccountSource();
        Account destination = operation.getAccountDestination();
        
        if (operation.getAmount().compareTo(source.getBalance()) > 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        
        source.setBalance(source.getBalance().subtract(operation.getAmount()));
        destination.setBalance(destination.getBalance().add(operation.getAmount()));
        
        accountRepository.save(source);
        accountRepository.save(destination);
    }
}

