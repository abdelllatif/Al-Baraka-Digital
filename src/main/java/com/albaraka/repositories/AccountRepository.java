package com.albaraka.repositories;

import com.albaraka.models.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findByOwnerId(Long ownerId);
    Optional<Account> findByIdAndOwnerId(Long id, Long ownerId);
}

