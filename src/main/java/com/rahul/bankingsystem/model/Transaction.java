package com.rahul.bankingsystem.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_transaction_key", columnList = "transactionKey", unique = true),
        @Index(name = "idx_source_account", columnList = "sourceAccountId"),
        @Index(name = "idx_target_account", columnList = "targetAccountId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID transactionKey;

    @Column(nullable = false)
    private Long sourceAccountId;

    @Column(nullable = false)
    private Long targetAccountId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(length = 20)
    private String operationType;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}