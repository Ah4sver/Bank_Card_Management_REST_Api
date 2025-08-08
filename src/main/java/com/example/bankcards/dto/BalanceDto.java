package com.example.bankcards.dto;

import java.math.BigDecimal;

public class BalanceDto {
    private BigDecimal balance;

    public BalanceDto() {
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
