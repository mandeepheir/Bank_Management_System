
package com.mandeep.bank_management_system;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/bank")
public class BankController {

    private final AccountRepository accountRepository;
    private final BankService bankService;
    private final BankTransactionRepository transactionRepository;

    public BankController(
            AccountRepository accountRepository,
            BankService bankService,
            BankTransactionRepository transactionRepository) {

        this.accountRepository = accountRepository;
        this.bankService = bankService;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Welcome to the Bank Management System!";
    }

    @GetMapping("/info")
    public String bankInfo() {
        return "Bank Management System API - Version 1.0";
    }

    // Get all accounts
    @GetMapping("/accounts")
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    // Get one account by ID
    @GetMapping("/accounts/{id}")
    public Account getAccountById(@PathVariable Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Account not found with ID: " + id));
    }

    // Create a new account
    @PostMapping("/accounts")
    public ResponseEntity<Account> createAccount(
            @RequestBody Account account) {

        if (account.getAccountHolderName() == null
                || account.getAccountHolderName().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Account holder name is required.");
        }

        if (account.getAccountNumber() == null
                || account.getAccountNumber().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Account number is required.");
        }

        if (account.getBalance() == null
                || account.getBalance().compareTo(BigDecimal.ZERO) < 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Balance must be zero or greater.");
        }

        if (accountRepository.existsByAccountNumber(
                account.getAccountNumber())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This account number already exists.");
        }

        // Let the database generate a new ID
        account.setId(null);

        Account savedAccount = accountRepository.save(account);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedAccount);
    }

    // Deposit money
    @PostMapping("/accounts/{id}/deposit")
    public Account deposit(
            @PathVariable Long id,
            @RequestBody AmountRequest request) {

        return bankService.deposit(id, request.getAmount());
    }

    // Withdraw money
    @PostMapping("/accounts/{id}/withdraw")
    public Account withdraw(
            @PathVariable Long id,
            @RequestBody AmountRequest request) {

        return bankService.withdraw(id, request.getAmount());
    }

    // Get transaction history for an account
    @GetMapping("/accounts/{id}/transactions")
    public List<TransactionResponse> getAccountTransactions(
            @PathVariable Long id) {

        if (!accountRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Account not found with ID: " + id);
        }

        return transactionRepository
                .findByAccountIdOrderByTransactionDateDesc(id)
                .stream()
                .map(transaction -> new TransactionResponse(
                        transaction.getId(),
                        id,
                        transaction.getAmount(),
                        transaction.getType(),
                        transaction.getTransactionDate()))
                .toList();
    }

    // Delete an account
    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<Void> deleteAccount(
            @PathVariable Long id) {

        if (!accountRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Account not found with ID: " + id);
        }

        accountRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    // Request body for deposits and withdrawals
    public static class AmountRequest {

        private BigDecimal amount;

        public AmountRequest() {
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }
}
