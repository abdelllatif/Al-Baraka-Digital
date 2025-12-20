package com.albaraka.security;

import com.albaraka.models.Account;
import com.albaraka.models.Operation;
import com.albaraka.models.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class ResourceAccessValidator {

    public void validateOperationOwnership(Operation operation, Long userId) {
        if (!operation.getAccountSource().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException(
                    "You do not have permission to access operation with id: " + operation.getId()
            );
        }
    }
    public void validateAccountOwnership(Account account, Long userId) {
        if (!account.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException(
                    "You do not have permission to access account with id: " + account.getId()
            );
        }
    }



}
