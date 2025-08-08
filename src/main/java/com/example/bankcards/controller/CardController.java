package com.example.bankcards.controller;

import com.example.bankcards.dto.BalanceDto;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/cards")
@Tag(name = "Контроллер банковских карт", description = "Операции, доступные пользователю для управления своими картами")
@SecurityRequirement(name = "bearerAuth")
public class CardController {

    private final CardService cardService;

    @Autowired
    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @Operation(summary = "Получить список своих карт", description = "Возвращает постраничный список карт, принадлежащих текущему пользователю")
    @ApiResponse(responseCode = "200", description = "Успешное получение списка карт")
    @ApiResponse(responseCode = "403", description = "Доступ запрещен (неверный токен)")
    @GetMapping
    @PreAuthorize("hasrole('USER')")
    public ResponseEntity<Page<CardDto>> getUserCards(Authentication authentication, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

        String username = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);

        Page<CardDto> cards = cardService.getCardsByUsername(username, pageable);

        return ResponseEntity.ok(cards);
    }

    @Operation(summary = "Запросить блокировку своей карты", description = "Устанавливает статус карты на PENDING_BLOCK. Выполнить можно только для своей активной карты")
    @ApiResponse(responseCode = "200", description = "Запрос на блокировку успешно послан")
    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    @ApiResponse(responseCode = "404", description = "Карта не найдена")
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка")
    @PatchMapping("/{id}/request-block")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CardDto> blockMyCard(@PathVariable("id") Long cardId, Authentication authentication) {
        String username = authentication.getName();
        CardDto updatedCard = cardService.requestCardBlock(cardId, username);
        return ResponseEntity.ok(updatedCard);
    }

    @Operation(summary = "Перевод средств между своими картами", description = "Выполняет перевод с одной своей карты на другую")
    @ApiResponse(responseCode = "200", description = "Перевод выполнен успешно")
    @ApiResponse(responseCode = "400", description = "Некорректные данные")
    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> transferMoney(
            @Valid @RequestBody TransferRequestDto transferRequestDto,
            Authentication authentication) {

        String username = authentication.getName();
        cardService.transferMoney(transferRequestDto, username);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить баланс своей карты", description = "Возвращает текущий баланс для указанной карты. Доступно только для своих карт")
    @ApiResponse(responseCode = "200", description = "Баланс успешно получен")
    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    @ApiResponse(responseCode = "404", description = "Карта не найдена")
    @GetMapping("/{id}/balance")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BalanceDto> getBalance(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(cardService.getCardBalance(id, username));
    }

}
