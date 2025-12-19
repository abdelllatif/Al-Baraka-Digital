package com.albaraka.services.interfaces;

import com.albaraka.dto.OperationRequest;
import com.albaraka.models.Document;
import com.albaraka.models.Operation;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface OperationService {
    Operation createOperation(Long clientId, OperationRequest request);
    Document uploadDocument(Long operationId, MultipartFile document);
    List<Operation> getClientOperations(Long clientId);
    List<Operation> getPendingOperations();
    Operation approveOperation(Long operationId, Long agentId);
    Operation rejectOperation(Long operationId, Long agentId);
}

