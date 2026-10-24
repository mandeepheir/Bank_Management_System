
package com.mandeep.bank_management_system;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {

    private final Long id;
    private final Long accountId;
    private final BigDecimal amount;
    private final String type;
    private final LocalDateTime transactionDate;

    public TransactionResponse(
            Long id,
            Long accountId,
            BigDecimal amount,
            String type,
            LocalDateTime transactionDate) {

        this.id = id;
        this.accountId = accountId;
        this.amount = amount;
        this.type = type;
        this.transactionDate = transactionDate;
    }

    public Long getId() {
        return id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }
}
