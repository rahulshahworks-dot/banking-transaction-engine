package com.rahul.bankingsystem.service;

import com.rahul.bankingsystem.dto.TransferRequestDTO;
import com.rahul.bankingsystem.dto.TransferResponseDTO;
import com.rahul.bankingsystem.event.TransactionCompletedEvent;
import com.rahul.bankingsystem.exception.InsufficientBalanceException;
import com.rahul.bankingsystem.exception.ResourceNotFoundException;
import com.rahul.bankingsystem.model.Account;
import com.rahul.bankingsystem.model.Transaction;
import com.rahul.bankingsystem.model.TransactionStatus;
import com.rahul.bankingsystem.repository.AccountRepository;
import com.rahul.bankingsystem.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public TransferResponseDTO executeTransfer(UUID transactionKey, TransferRequestDTO request) {
        log.info("Starting execution for transaction: {}", transactionKey);

        // Safety check to ensure we don't transfer to the same account
        if (request.getSourceAccountId().equals(request.getTargetAccountId())) {
            throw new IllegalArgumentException("Source and target accounts must be different");
        }

        // 1. Lock accounts in a deterministic order to prevent DB deadlocks
        // We always lock the account with the smaller ID first
        Long firstLockId = Math.min(request.getSourceAccountId(), request.getTargetAccountId());
        Long secondLockId = Math.max(request.getSourceAccountId(), request.getTargetAccountId());

        log.debug("Acquiring pessimistic lock for account ID: {}", firstLockId);
        Account firstAccount = accountRepository.findByIdForUpdate(firstLockId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + firstLockId));

        log.debug("Acquiring pessimistic lock for account ID: {}", secondLockId);
        Account secondAccount = accountRepository.findByIdForUpdate(secondLockId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + secondLockId));

        // 2. Assign source and target based on locks
        Account sourceAccount = firstAccount.getId().equals(request.getSourceAccountId()) ? firstAccount : secondAccount;
        Account targetAccount = firstAccount.getId().equals(request.getTargetAccountId()) ? firstAccount : secondAccount;

        // 3. Validation
        if (!sourceAccount.getCurrency().equals(request.getCurrency()) || !targetAccount.getCurrency().equals(request.getCurrency())) {
            throw new IllegalArgumentException("Currency mismatch");
        }

        if (!"ACTIVE".equals(sourceAccount.getStatus())) {
            throw new IllegalArgumentException("Source account is not active");
        }
        if (!"ACTIVE".equals(targetAccount.getStatus())) {
            throw new IllegalArgumentException("Target account is not active");
        }

        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            log.warn("Insufficient balance for account ID: {}. Current: {}, Required: {}",
                    sourceAccount.getId(), sourceAccount.getBalance(), request.getAmount());
            throw new InsufficientBalanceException("Insufficient funds in source account");
        }

        // 4. Execute the transfer
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
        targetAccount.setBalance(targetAccount.getBalance().add(request.getAmount()));

        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);

        // 5. Record the transaction
        Transaction transaction = Transaction.builder()
                .transactionKey(transactionKey)
                .sourceAccountId(sourceAccount.getId())
                .targetAccountId(targetAccount.getId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(TransactionStatus.COMPLETED)
                .build();

        transactionRepository.save(transaction);

        log.info("Transaction {} completed successfully", transactionKey);

        // 6. Publish event (will be sent to Kafka AFTER commit)
        eventPublisher.publishEvent(TransactionCompletedEvent.builder()
                .transactionKey(transactionKey)
                .sourceAccountId(sourceAccount.getId())
                .targetAccountId(targetAccount.getId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(TransactionStatus.COMPLETED.name())
                .build());

        return TransferResponseDTO.builder()
                .transactionKey(transactionKey)
                .message("Transfer completed successfully")
                .status(TransactionStatus.COMPLETED)
                .transferredAmount(request.getAmount())
                .currency(request.getCurrency())
                .build();
    }
}