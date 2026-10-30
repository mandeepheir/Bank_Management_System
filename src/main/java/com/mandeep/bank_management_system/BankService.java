
package com.mandeep.bank_management_system;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BankService {

    private final AccountRepository accountRepository;
    private final BankTransactionRepository transactionRepository;

    public BankService(
            AccountRepository accountRepository,
            BankTransactionRepository transactionRepository) {

        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Account deposit(
            Long accountId, BigDecimal amount, Long userId) {

        validateAmount(amount);

        Account account = findAccount(accountId, userId);

        account.setBalance(account.getBalance().add(amount));

        Account savedAccount = accountRepository.save(account);

        BankTransaction transaction =
                new BankTransaction(account, amount, "DEPOSIT");

        transactionRepository.save(transaction);

        return savedAccount;
    }

    @Transactional
    public Account withdraw(
            Long accountId, BigDecimal amount, Long userId) {

        validateAmount(amount);

        Account account = findAccount(accountId, userId);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Insufficient funds.");
        }

        account.setBalance(account.getBalance().subtract(amount));

        Account savedAccount = accountRepository.save(account);

        BankTransaction transaction =
                new BankTransaction(account, amount, "WITHDRAWAL");

        transactionRepository.save(transaction);

        return savedAccount;
    }

    @Transactional(readOnly = true)
    public Account findAccount(Long accountId, Long userId) {

        return accountRepository
                .findByIdAndOwnerId(accountId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Account not found."));
    }

    private void validateAmount(BigDecimal amount) {

        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Amount must be greater than zero.");
        }
    }
}
