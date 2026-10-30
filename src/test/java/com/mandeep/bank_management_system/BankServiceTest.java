
package com.mandeep.bank_management_system;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    private final Long userId = 1L;

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
        when(accountRepository.findByIdAndOwnerId(1L, userId))
            .thenReturn(Optional.of(account));

        when(accountRepository.save(any(Account.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Account result = bankService.deposit(
            1L, new BigDecimal("200.00"), userId
        );

        assertEquals(new BigDecimal("1200.00"), result.getBalance());

        verify(accountRepository).save(account);
        verify(transactionRepository).save(any(BankTransaction.class));
    }

    @Test
    void withdrawShouldDecreaseBalanceAndRecordTransaction() {
        when(accountRepository.findByIdAndOwnerId(1L, userId))
            .thenReturn(Optional.of(account));

        when(accountRepository.save(any(Account.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Account result = bankService.withdraw(
            1L, new BigDecimal("300.00"), userId
        );

        assertEquals(new BigDecimal("700.00"), result.getBalance());

        verify(accountRepository).save(account);
        verify(transactionRepository).save(any(BankTransaction.class));
    }

    @Test
    void withdrawShouldRejectInsufficientFunds() {
        when(accountRepository.findByIdAndOwnerId(1L, userId))
            .thenReturn(Optional.of(account));

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> bankService.withdraw(
                1L, new BigDecimal("1500.00"), userId
            )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
    }

    @Test
    void depositShouldRejectZeroAmount() {
        assertThrows(
            ResponseStatusException.class,
            () -> bankService.deposit(1L, BigDecimal.ZERO, userId)
        );

        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
    }

    @Test
    void withdrawShouldRejectNegativeAmount() {
        assertThrows(
            ResponseStatusException.class,
            () -> bankService.withdraw(
                1L, new BigDecimal("-10.00"), userId
            )
        );

        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
    }

    @Test
    void depositShouldRejectNullAmount() {
        assertThrows(
            ResponseStatusException.class,
            () -> bankService.deposit(1L, null, userId)
        );

        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
    }

    @Test
    void operationShouldRejectMissingAccount() {
        when(accountRepository.findByIdAndOwnerId(99L, userId))
            .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> bankService.deposit(
                99L, new BigDecimal("100.00"), userId
            )
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
    }

    @Test
    void depositShouldRecordCorrectTransactionDetails() {
        when(accountRepository.findByIdAndOwnerId(1L, userId))
            .thenReturn(Optional.of(account));

        when(accountRepository.save(any(Account.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        BigDecimal amount = new BigDecimal("200.00");

        bankService.deposit(1L, amount, userId);

        ArgumentCaptor<BankTransaction> captor =
            ArgumentCaptor.forClass(BankTransaction.class);

        verify(transactionRepository).save(captor.capture());

        BankTransaction transaction = captor.getValue();

        assertEquals(account, transaction.getAccount());
        assertEquals(amount, transaction.getAmount());
        assertEquals("DEPOSIT", transaction.getType());
    }

    @Test
    void withdrawShouldRecordCorrectTransactionDetails() {
        when(accountRepository.findByIdAndOwnerId(1L, userId))
            .thenReturn(Optional.of(account));

        when(accountRepository.save(any(Account.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        BigDecimal amount = new BigDecimal("300.00");

        bankService.withdraw(1L, amount, userId);

        ArgumentCaptor<BankTransaction> captor =
            ArgumentCaptor.forClass(BankTransaction.class);

        verify(transactionRepository).save(captor.capture());

        BankTransaction transaction = captor.getValue();

        assertEquals(account, transaction.getAccount());
        assertEquals(amount, transaction.getAmount());
        assertEquals("WITHDRAWAL", transaction.getType());
    }
}
