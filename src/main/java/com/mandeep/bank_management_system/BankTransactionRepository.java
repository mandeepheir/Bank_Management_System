
package com.mandeep.bank_management_system;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BankTransactionRepository
        extends JpaRepository<BankTransaction, Long> {

    List<BankTransaction> findByAccountIdOrderByTransactionDateDesc(Long accountId);
}
