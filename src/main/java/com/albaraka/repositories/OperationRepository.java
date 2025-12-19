package com.albaraka.repositories;

import com.albaraka.enums.OperationStatus;
import com.albaraka.models.Operation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperationRepository extends JpaRepository<Operation, Long> {
    List<Operation> findByAccountSourceOwnerId(Long ownerId);
    List<Operation> findByAccountDestinationOwnerId(Long ownerId);
    List<Operation> findByStatus(OperationStatus status);
    List<Operation> findByAccountSourceIdOrAccountDestinationId(Long sourceId, Long destinationId);
}

