
package com.mandeep.bank_management_system;

import java.math.BigDecimal;
import java.security.Principal;
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
    private final AppUserRepository appUserRepository;

    public BankController(
            AccountRepository accountRepository,
            BankService bankService,
            BankTransactionRepository transactionRepository,
            AppUserRepository appUserRepository) {

        this.accountRepository = accountRepository;
        this.bankService = bankService;
        this.transactionRepository = transactionRepository;
        this.appUserRepository = appUserRepository;
    }

    private AppUser getCurrentUser(Principal principal) {

        return appUserRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found."));
    }

    @GetMapping("/hello")
    public String hello() {
        return "Welcome to the Bank Management System!";
    }

    @GetMapping("/info")
    public String bankInfo() {
        return "Bank Management System API - Version 1.0";
    }

    // Get accounts belonging to the logged-in user
    @GetMapping("/accounts")
    public List<Account> getAllAccounts(Principal principal) {

        AppUser user = getCurrentUser(principal);

        return accountRepository.findByOwnerId(user.getId());
    }

    // Get one account belonging to the logged-in user
    @GetMapping("/accounts/{id}")
    public Account getAccountById(
            @PathVariable Long id,
            Principal principal) {

        AppUser user = getCurrentUser(principal);

        return bankService.findAccount(id, user.getId());
    }

    // Create a new account for the logged-in user
    @PostMapping("/accounts")
    public ResponseEntity<Account> createAccount(
            @RequestBody Account account,
            Principal principal) {

        AppUser user = getCurrentUser(principal);

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

        account.setId(null);
        account.setOwner(user);

        Account savedAccount = accountRepository.save(account);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedAccount);
    }

    // Deposit money into the user's own account
    @PostMapping("/accounts/{id}/deposit")
    public Account deposit(
            @PathVariable Long id,
            @RequestBody AmountRequest request,
            Principal principal) {

        AppUser user = getCurrentUser(principal);

        return bankService.deposit(
                id, request.getAmount(), user.getId());
    }

    // Withdraw money from the user's own account
    @PostMapping("/accounts/{id}/withdraw")
    public Account withdraw(
            @PathVariable Long id,
            @RequestBody AmountRequest request,
            Principal principal) {

        AppUser user = getCurrentUser(principal);

        return bankService.withdraw(
                id, request.getAmount(), user.getId());
    }

    // Get transaction history for the user's own account
    @GetMapping("/accounts/{id}/transactions")
    public List<TransactionResponse> getAccountTransactions(
            @PathVariable Long id,
            Principal principal) {

        AppUser user = getCurrentUser(principal);

        bankService.findAccount(id, user.getId());

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

    // Delete the user's own account
    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<Void> deleteAccount(
            @PathVariable Long id,
            Principal principal) {

        AppUser user = getCurrentUser(principal);

        bankService.findAccount(id, user.getId());

        accountRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }

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
