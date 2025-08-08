package com.example.bankcards.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class TransferRequestDto {

    @NotNull
    private Long fromCardId;

    @NotNull
    private Long toCardId;

    @NotNull
    @Positive(message = "Сумма перевода должна быть положительной")
    private BigDecimal amount;

    public TransferRequestDto() {
    }

    public Long getFromCardId() {
        return fromCardId;
    }

    public void setFromCardId(Long fromCardId) {
        this.fromCardId = fromCardId;
    }

    public Long getToCardId() {
        return toCardId;
    }

    public void setToCardId(Long toCardId) {
        this.toCardId = toCardId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
