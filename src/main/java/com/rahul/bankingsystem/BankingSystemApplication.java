package com.rahul.bankingsystem;

import com.rahul.bankingsystem.model.Account;
import com.rahul.bankingsystem.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;

@SpringBootApplication
public class BankingSystemApplication {

    @Bean
    CommandLineRunner seedDemoAccounts(AccountRepository accountRepository) {
        return args -> {
            if (accountRepository.count() == 0) {
                accountRepository.save(Account.builder()
                        .accountNumber("AC-DEMO-001")
                        .balance(new BigDecimal("2500.00"))
                        .currency("USD")
                        .status("ACTIVE")
                        .build());
                accountRepository.save(Account.builder()
                        .accountNumber("AC-DEMO-002")
                        .balance(new BigDecimal("1250.00"))
                        .currency("USD")
                        .status("ACTIVE")
                        .build());
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(BankingSystemApplication.class, args);
    }

}