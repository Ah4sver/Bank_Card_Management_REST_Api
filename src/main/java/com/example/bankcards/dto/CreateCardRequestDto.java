package com.example.bankcards.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CreateCardRequestDto {

    @NotNull(message = "Id пользователя не может быть пустым")
    private Long userId;

    @NotBlank(message = "Требуется номер карты")
    @Size(min = 16, max = 16, message = "номер карты должен состоять из 16 цифр")
    private String cardNumber;

    @NotNull(message = "Требуется указать срок действия")
    @Future(message = "Дата истечения срока действия должна быть в будущем")
    private LocalDate expiryDate;

    public CreateCardRequestDto() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }
}
