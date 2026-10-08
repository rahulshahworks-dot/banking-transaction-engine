package com.rahul.bankingsystem.controller;

import com.rahul.bankingsystem.dto.AccountOperationDTO;
import com.rahul.bankingsystem.dto.CreateAccountDTO;
import com.rahul.bankingsystem.model.Account;
import com.rahul.bankingsystem.repository.AccountRepository;
import com.rahul.bankingsystem.service.BankAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AccountController {

    private final BankAccountService bankAccountService;

    @GetMapping
    public List<Account> getAllAccounts() {
        return bankAccountService.getAllAccounts();
    }

    @GetMapping("/{id}")
    public Account getAccount(@PathVariable Long id) {
        return bankAccountService.getAccount(id);
    }

    @PostMapping
    public Account createAccount(@Valid @RequestBody CreateAccountDTO request) {
        return bankAccountService.createAccount(request);
    }

    @PostMapping("/{id}/deposit")
    public Account deposit(@PathVariable Long id, @Valid @RequestBody AccountOperationDTO request) {
        return bankAccountService.deposit(id, request);
    }

    @PostMapping("/{id}/withdraw")
    public Account withdraw(@PathVariable Long id, @Valid @RequestBody AccountOperationDTO request) {
        return bankAccountService.withdraw(id, request);
    }
}