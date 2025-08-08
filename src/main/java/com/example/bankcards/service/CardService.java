package com.example.bankcards.service;

import com.example.bankcards.dto.BalanceDto;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequestDto;
import com.example.bankcards.dto.TransferRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CardService {

    CardDto createCard(CreateCardRequestDto createCardRequestDto);

    Page<CardDto> getCardsByUsername(String username, Pageable pageable);

    CardDto requestCardBlock(Long cardId, String username);

    void transferMoney(TransferRequestDto transferRequestDto, String username);

    Page<CardDto> getAllCards(Pageable pageable);

    CardDto blockCardByAdmin(Long cardId);

    CardDto activateCardByAdmin(Long cardId);

    void deleteCardByAdmin(Long cardId);

    BalanceDto getCardBalance(Long cardId, String username);
}
