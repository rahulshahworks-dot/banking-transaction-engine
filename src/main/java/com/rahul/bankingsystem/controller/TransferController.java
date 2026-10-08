package com.rahul.bankingsystem.controller;

import com.rahul.bankingsystem.dto.TransferRequestDTO;
import com.rahul.bankingsystem.dto.TransferResponseDTO;
import com.rahul.bankingsystem.service.IdempotencyService;
import com.rahul.bankingsystem.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class TransferController {

    private final TransferService transferService;
    private final IdempotencyService idempotencyService;

    @PostMapping
    public ResponseEntity<TransferResponseDTO> createTransfer(
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransferRequestDTO request) {

        log.info("Received transfer request with idempotency key: {}", idempotencyKey);

        // 1. Check idempotency lock
        if (!idempotencyService.acquireLock(idempotencyKey)) {
            // Already processed or processing
            log.warn("Duplicate request detected for idempotency key: {}", idempotencyKey);
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409 Conflict
        }

        try {
            // Convert string idempotency key to UUID to act as the transaction key
            UUID transactionKey;
            try {
                transactionKey = UUID.fromString(idempotencyKey);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("X-Idempotency-Key must be a valid UUID");
            }

            // 2. Execute Transfer
            TransferResponseDTO response = transferService.executeTransfer(transactionKey, request);

            // 3. On success, extend TTL and mark completed
            idempotencyService.markCompleted(idempotencyKey);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // On business exception, release lock so user can retry with valid data
            idempotencyService.releaseLock(idempotencyKey);
            throw e;
        }
    }
}
