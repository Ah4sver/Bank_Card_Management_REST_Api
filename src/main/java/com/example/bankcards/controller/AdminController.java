package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequestDto;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/admin")
@Tag(name = "Контроллер администратора", description = "Операции, доступные только администратору")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final CardService cardService;

    @Autowired
    public AdminController(CardService cardService) {
        this.cardService = cardService;
    }

    @Operation(summary = "Создание новой банковской карты", description = "Создает новую карту для указанного пользователя")
    @ApiResponse(responseCode = "201", description = "Карта успешно создана")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации данных")
    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    @ApiResponse(responseCode = "404", description = "Пользователь с указанным id не найден")
    @PostMapping("/cards")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDto> createCard(@Valid @RequestBody CreateCardRequestDto createCardRequestDto) {
        CardDto newCard = cardService.createCard(createCardRequestDto);
        return new ResponseEntity<>(newCard, HttpStatus.CREATED);
    }

    @Operation(summary = "Получить список всех карт", description = "Возвращает постраничный список всех карт в системе")
    @ApiResponse(responseCode = "200", description = "Успешное получение списка карт")
    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    @GetMapping("/cards")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CardDto>> getAllCards(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(cardService.getAllCards(pageable));
    }

    @Operation(summary = "Заблокировать карту", description = "Подтверждает блокировку карты, переводит в статус BLOCKED")
    @ApiResponse(responseCode = "200", description = "Карта успешно заблокирована")
    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    @ApiResponse(responseCode = "404", description = "Карта не найдена")
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка")
    @PatchMapping("/cards/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDto> blockCard(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.blockCardByAdmin(id));
    }

    @Operation(summary = "Активировать карту", description = "Активирует заблокированную карту, переводит в статус ACTIVE")
    @ApiResponse(responseCode = "200", description = "Карта успешно активирована")
    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    @ApiResponse(responseCode = "404", description = "Карта не найдена")
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка")
    @PatchMapping("/cards/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDto> activateCard(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.activateCardByAdmin(id));
    }

    @Operation(summary = "Удалить карту", description = "Удаляет карту из системы")
    @ApiResponse(responseCode = "204", description = "Карта успешно удалена")
    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    @ApiResponse(responseCode = "404", description = "Карта не найдена")
    @DeleteMapping("/cards/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCardByAdmin(id);
        return ResponseEntity.noContent().build();
    }
}
