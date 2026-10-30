
package com.mandeep.bank_management_system;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByAccountNumber(String accountNumber);

    List<Account> findByOwnerId(Long ownerId);

    Optional<Account> findByIdAndOwnerId(Long id, Long ownerId);
}
