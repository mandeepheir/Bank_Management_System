
package com.mandeep.bank_management_system;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class BankControllerTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private BankService bankService;

    @Mock
    private BankTransactionRepository transactionRepository;

    @Mock
    private AppUserRepository appUserRepository;

    private MockMvc mockMvc;
    private Account account;
    private AppUser user;
    private Principal principal;

    @BeforeEach
    void setUp() {
        BankController controller = new BankController(
                accountRepository,
                bankService,
                transactionRepository,
                appUserRepository
        );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        user = new AppUser("mandeep_test", "test-password");
        user.setUsername("mandeep_test");

        // AppUser has no public ID setter, so use a mocked user.
        user = mock(AppUser.class);
        when(user.getId()).thenReturn(1L);
        when(user.getUsername()).thenReturn("mandeep_test");

        principal = () -> "mandeep_test";

        account = new Account(
                1L,
                "Mandeep Test",
                "ACC9001",
                new BigDecimal("1000.00")
        );

        when(appUserRepository.findByUsername("mandeep_test"))
                .thenReturn(Optional.of(user));
    }

    @Test
    void helloEndpointShouldReturnWelcomeMessage() throws Exception {
        mockMvc.perform(get("/api/bank/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Welcome to the Bank Management System!"
                ));
    }

    @Test
    void getAccountsShouldReturnAccountList() throws Exception {
        when(accountRepository.findByOwnerId(1L))
                .thenReturn(List.of(account));

        mockMvc.perform(get("/api/bank/accounts")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].accountHolderName")
                        .value("Mandeep Test"))
                .andExpect(jsonPath("$[0].accountNumber")
                        .value("ACC9001"))
                .andExpect(jsonPath("$[0].balance").value(1000.00));
    }

    @Test
    void createAccountShouldReturnCreatedStatus() throws Exception {
        when(accountRepository.existsByAccountNumber("ACC9002"))
                .thenReturn(false);

        when(accountRepository.save(any(Account.class)))
                .thenReturn(new Account(
                        2L,
                        "Alex Smith",
                        "ACC9002",
                        new BigDecimal("500.00")
                ));

        mockMvc.perform(post("/api/bank/accounts")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountHolderName": "Alex Smith",
                                  "accountNumber": "ACC9002",
                                  "balance": 500.00
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.accountHolderName")
                        .value("Alex Smith"))
                .andExpect(jsonPath("$.accountNumber")
                        .value("ACC9002"))
                .andExpect(jsonPath("$.balance").value(500.00));

        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccountShouldRejectBlankHolderName() throws Exception {
        mockMvc.perform(post("/api/bank/accounts")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountHolderName": " ",
                                  "accountNumber": "ACC9003",
                                  "balance": 100.00
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void createAccountShouldRejectDuplicateAccountNumber()
            throws Exception {

        when(accountRepository.existsByAccountNumber("ACC9001"))
                .thenReturn(true);

        mockMvc.perform(post("/api/bank/accounts")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountHolderName": "Another User",
                                  "accountNumber": "ACC9001",
                                  "balance": 100.00
                                }
                                """))
                .andExpect(status().isConflict());

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void depositEndpointShouldCallBankService() throws Exception {
        when(bankService.deposit(
                1L, new BigDecimal("50.00"), 1L))
                .thenReturn(new Account(
                        1L,
                        "Mandeep Test",
                        "ACC9001",
                        new BigDecimal("1050.00")
                ));

        mockMvc.perform(post("/api/bank/accounts/1/deposit")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 50.00
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1050.00));

        verify(bankService).deposit(
                1L, new BigDecimal("50.00"), 1L
        );
    }

    @Test
    void withdrawalEndpointShouldCallBankService() throws Exception {
        when(bankService.withdraw(
                1L, new BigDecimal("25.00"), 1L))
                .thenReturn(new Account(
                        1L,
                        "Mandeep Test",
                        "ACC9001",
                        new BigDecimal("975.00")
                ));

        mockMvc.perform(post("/api/bank/accounts/1/withdraw")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 25.00
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(975.00));

        verify(bankService).withdraw(
                1L, new BigDecimal("25.00"), 1L
        );
    }
}
