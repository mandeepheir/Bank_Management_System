
package com.mandeep.bank_management_system;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private BankTransactionRepository transactionRepository;

    @InjectMocks
    private BankService bankService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account(
            1L,
            "Mandeep Test",
            "ACC9001",
            new BigDecimal("1000.00")
        );
    }

    @Test
    void depositShouldIncreaseBalanceAndRecordTransaction() {
        when(accountRepository.findById(1L))
            .thenReturn(Optional.of(account));

        when(accountRepository.save(any(Account.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Account result = bankService.deposit(
            1L, new BigDecimal("200.00")
        );

        assertEquals(new BigDecimal("1200.00"), result.getBalance());

        verify(accountRepository).save(account);
        verify(transactionRepository).save(any(BankTransaction.class));
    }

    @Test
    void withdrawShouldDecreaseBalanceAndRecordTransaction() {
        when(accountRepository.findById(1L))
            .thenReturn(Optional.of(account));

        when(accountRepository.save(any(Account.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Account result = bankService.withdraw(
            1L, new BigDecimal("300.00")
        );

        assertEquals(new BigDecimal("700.00"), result.getBalance());

        verify(accountRepository).save(account);
        verify(transactionRepository).save(any(BankTransaction.class));
    }

    @Test
    void withdrawShouldRejectInsufficientFunds() {
        when(accountRepository.findById(1L))
            .thenReturn(Optional.of(account));

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> bankService.withdraw(1L, new BigDecimal("1500.00"))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
    }

    @Test
    void depositShouldRejectZeroAmount() {
        assertThrows(
            ResponseStatusException.class,
            () -> bankService.deposit(1L, BigDecimal.ZERO)
        );

        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
    }

    @Test
    void withdrawShouldRejectNegativeAmount() {
        assertThrows(
            ResponseStatusException.class,
            () -> bankService.withdraw(1L, new BigDecimal("-10.00"))
        );

        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
    }

    @Test
    void depositShouldRejectNullAmount() {
        assertThrows(
            ResponseStatusException.class,
            () -> bankService.deposit(1L, null)
        );

        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
    }

    @Test
    void operationShouldRejectMissingAccount() {
        when(accountRepository.findById(99L))
            .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> bankService.deposit(99L, new BigDecimal("100.00"))
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
    }
}
