package com.albaraka.models;

import com.albaraka.enums.OperationStatus;
import com.albaraka.enums.OperationType;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "operations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Operation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperationType type;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperationStatus status;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime validatedAt;
    
    private LocalDateTime executedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_source_id")
    private Account accountSource;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_destination_id")
    private Account accountDestination;

    @OneToOne(mappedBy = "operation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private Document document;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

