package com.rahul.bankingsystem.service;

import com.rahul.bankingsystem.dto.AccountOperationDTO;
import com.rahul.bankingsystem.dto.CreateAccountDTO;
import com.rahul.bankingsystem.exception.InsufficientBalanceException;
import com.rahul.bankingsystem.exception.ResourceNotFoundException;
import com.rahul.bankingsystem.model.Account;
import com.rahul.bankingsystem.model.Transaction;
import com.rahul.bankingsystem.model.TransactionStatus;
import com.rahul.bankingsystem.repository.AccountRepository;
import com.rahul.bankingsystem.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public Account getAccount(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + id));
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Transactional
    public Account createAccount(CreateAccountDTO request) {
        String currency = request.getCurrency().trim().toUpperCase();
        if (!SetOfCurrencies.contains(currency)) {
            throw new IllegalArgumentException("Supported currencies are USD, EUR, and GBP");
        }
        BigDecimal openingBalance = request.getOpeningBalance() == null
                ? BigDecimal.ZERO : request.getOpeningBalance();
        return accountRepository.save(Account.builder()
                .accountNumber("AC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .balance(openingBalance)
                .currency(currency)
                .status("ACTIVE")
                .build());
    }

    @Transactional
    public Account deposit(Long id, AccountOperationDTO request) {
        Account account = getAccountForUpdate(id);
        ensureActive(account);
        account.setBalance(account.getBalance().add(request.getAmount()));
        Account updated = accountRepository.save(account);
        recordOperation(updated, request.getAmount(), "DEPOSIT");
        return updated;
    }

    @Transactional
    public Account withdraw(Long id, AccountOperationDTO request) {
        Account account = getAccountForUpdate(id);
        ensureActive(account);
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient funds in account");
        }
        account.setBalance(account.getBalance().subtract(request.getAmount()));
        Account updated = accountRepository.save(account);
        recordOperation(updated, request.getAmount(), "WITHDRAWAL");
        return updated;
    }

    private Account getAccountForUpdate(Long id) {
        return accountRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + id));
    }

    private void ensureActive(Account account) {
        if (!"ACTIVE".equals(account.getStatus())) {
            throw new IllegalArgumentException("Account is not active");
        }
    }

    private void recordOperation(Account account, BigDecimal amount, String operationType) {
        transactionRepository.save(Transaction.builder()
                .transactionKey(UUID.randomUUID())
                .sourceAccountId(account.getId())
                .targetAccountId(account.getId())
                .amount(amount)
                .currency(account.getCurrency())
                .status(TransactionStatus.COMPLETED)
                .operationType(operationType)
                .build());
    }


    private static final class SetOfCurrencies {
        private static boolean contains(String value) {
            return "USD".equals(value) || "EUR".equals(value) || "GBP".equals(value) || "INR".equals(value);
        }
    }
}
