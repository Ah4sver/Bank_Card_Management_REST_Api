package com.example.bankcards.service;

import com.example.bankcards.dto.BalanceDto;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequestDto;
import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.EncryptionUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EncryptionUtil encryptionUtil;
    @InjectMocks
    private CardServiceImpl cardService;

    @Test
    void transferMoney_WhenSuccessful_ShouldUpdateBalancesAndSaveChanges() {

        String username = "testuser";
        User owner = new User();
        owner.setId(1L);
        owner.setUsername(username);

        Card fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setOwner(owner);
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setBalance(new BigDecimal("1000.00"));

        Card toCard = new Card();
        toCard.setId(2L);
        toCard.setOwner(owner);
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setBalance(new BigDecimal("500.00"));

        TransferRequestDto transferDto = new TransferRequestDto();
        transferDto.setFromCardId(10L);
        transferDto.setToCardId(20L);
        transferDto.setAmount(new BigDecimal("200.00"));

        when(cardRepository.findById(10L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(20L)).thenReturn(Optional.of(toCard));

        cardService.transferMoney(transferDto, username);

        assertEquals(new BigDecimal("800.00"), fromCard.getBalance());
        assertEquals(new BigDecimal("700.00"), toCard.getBalance());

        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    void transferMoney_WhenLowBalance_ShouldThrowException() {

        String username = "testuser";
        User owner = new User();
        owner.setUsername(username);

        Card fromCard = new Card();
        fromCard.setOwner(owner);
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setBalance(new BigDecimal("100.00"));

        Card toCard = new Card();
        toCard.setOwner(owner);
        toCard.setStatus(CardStatus.ACTIVE);

        TransferRequestDto transferDto = new TransferRequestDto();
        transferDto.setFromCardId(1L);
        transferDto.setToCardId(2L);
        transferDto.setAmount(new BigDecimal("200.00"));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        assertThrows(IllegalStateException.class, () -> {
            cardService.transferMoney(transferDto, username);
        });

        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void requestCardBlock_WhenCardIsActive_ShouldSetStatusToPendingBlock() {

        String username = "testuser";
        User owner = new User();
        owner.setUsername(username);

        Card activeCard = new Card();
        activeCard.setId(1L);
        activeCard.setOwner(owner);
        activeCard.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(activeCard));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        cardService.requestCardBlock(1L, username);

        assertEquals(CardStatus.PENDING_BLOCK, activeCard.getStatus());

        verify(cardRepository).save(activeCard);
    }

    @Test
    void requestCardBlock_WhenCardIsBlocked_ShouldThrowException() {

        String username = "testuser";
        User owner = new User();
        owner.setUsername(username);

        Card blockedCard = new Card();
        blockedCard.setId(1L);
        blockedCard.setOwner(owner);
        blockedCard.setStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(blockedCard));

        assertThrows(IllegalStateException.class, () -> {
            cardService.requestCardBlock(1L, username);
        });

        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void requestCardBlock_WhenCardIsExpired_ShouldThrowException() {

        String username = "testuser";
        User owner = new User();
        owner.setUsername(username);

        Card blockedCard = new Card();
        blockedCard.setId(1L);
        blockedCard.setOwner(owner);
        blockedCard.setStatus(CardStatus.EXPIRED);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(blockedCard));

        assertThrows(IllegalStateException.class, () -> {
            cardService.requestCardBlock(1L, username);
        });

        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void blockCardByAdmin_WhenCardIsActive_ShouldSetStatusToBlocked() {

        Card cardToBlock = new Card();
        cardToBlock.setId(1L);
        cardToBlock.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(cardToBlock));
        when(cardRepository.save(any(Card.class))).thenReturn(cardToBlock);

        cardService.blockCardByAdmin(1L);

        assertEquals(CardStatus.BLOCKED, cardToBlock.getStatus());

        verify(cardRepository).save(cardToBlock);
    }

    @Test
    void activateCardByAdmin_WhenCardIsBlocked_ShouldSetStatusToActive() {

        Card cardToActivate = new Card();
        cardToActivate.setId(1L);
        cardToActivate.setStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(cardToActivate));
        when(cardRepository.save(any(Card.class))).thenReturn(cardToActivate);

        cardService.activateCardByAdmin(1L);

        assertEquals(CardStatus.ACTIVE, cardToActivate.getStatus());

        verify(cardRepository).save(cardToActivate);
    }

    @Test
    void deleteCardByAdmin_WhenCardExists_ShouldCallDeleteById() {

        Long cardId = 1L;

        when(cardRepository.existsById(cardId)).thenReturn(true);

        doNothing().when(cardRepository).deleteById(cardId);

        cardService.deleteCardByAdmin(cardId);

        verify(cardRepository, times(1)).deleteById(cardId);
    }

    @Test
    void deleteCardByAdmin_WhenCardNotExists_ShouldThrowException() {

        Long cardId = 1L;

        when(cardRepository.existsById(cardId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            cardService.deleteCardByAdmin(cardId);
        });

        verify(cardRepository, never()).deleteById(anyLong());
    }

    @Test
    void getCardBalance_WhenUserIsOwner_ShouldReturnBalanceDto() {

        String username = "testuser";
        User owner = new User();
        owner.setUsername(username);

        Card card = new Card();
        card.setId(1L);
        card.setOwner(owner);
        card.setBalance(new BigDecimal("100.50"));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        BalanceDto result = cardService.getCardBalance(1L, username);

        assertNotNull(result);
        assertEquals(new BigDecimal("100.50"), result.getBalance());
    }

    @Test
    void getCardBalance_WhenUserIsNotOwner_ShouldThrowException() {

        String ownerUsername = "owner";
        String otherUsername = "other";

        User owner = new User();
        owner.setUsername(ownerUsername);

        Card card = new Card();
        card.setId(1L);
        card.setOwner(owner);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThrows(AccessDeniedException.class, () -> {
            cardService.getCardBalance(1L, otherUsername);
        });
    }

    @Test
    void createCard_ShouldCreateAndSaveCard_AndReturnMaskedDto() {

        User owner = new User();
        owner.setId(1L);

        CreateCardRequestDto requestDto = new CreateCardRequestDto();
        requestDto.setUserId(1L);
        requestDto.setCardNumber("1111222233334444");
        requestDto.setExpiryDate(LocalDate.now().plusYears(3));

        Card savedCard = new Card();
        savedCard.setId(1L);
        savedCard.setOwner(owner);
        savedCard.setCardNumber("ЗАШИФРОВАННЫЙ_НОМЕР");
        savedCard.setStatus(CardStatus.ACTIVE);
        savedCard.setBalance(BigDecimal.ZERO);
        savedCard.setExpiryDate(requestDto.getExpiryDate());

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(encryptionUtil.encrypt("1111222233334444")).thenReturn("ЗАШИФРОВАННЫЙ_НОМЕР");
        when(cardRepository.save(any(Card.class))).thenReturn(savedCard);
        when(encryptionUtil.decrypt("ЗАШИФРОВАННЫЙ_НОМЕР")).thenReturn("1111222233334444");

        CardDto resultDto = cardService.createCard(requestDto);

        assertNotNull(resultDto);
        assertEquals(1L, resultDto.getId());
        assertEquals("************4444", resultDto.getMaskedCardNumber());
        assertEquals(CardStatus.ACTIVE.name(), resultDto.getStatus());
        assertEquals(0, BigDecimal.ZERO.compareTo(resultDto.getBalance()));

        verify(userRepository).findById(1L);
        verify(encryptionUtil).encrypt("1111222233334444");
        verify(cardRepository).save(any(Card.class));
        verify(encryptionUtil).decrypt("ЗАШИФРОВАННЫЙ_НОМЕР");
    }

    @Test
    void getCardsByUsername_ShouldReturnPagedCards() {

        String username = "testuser";
        Long userId = 1L;

        User owner = new User();
        owner.setId(userId);
        owner.setUsername(username);

        Card cardFromDb = new Card();
        cardFromDb.setId(1L);
        cardFromDb.setOwner(owner);
        cardFromDb.setCardNumber("ЗАШИФРОВАННЫЙ_НОМЕР");
        cardFromDb.setExpiryDate(LocalDate.now().plusYears(3));
        cardFromDb.setBalance(new BigDecimal("10.00"));
        cardFromDb.setStatus(CardStatus.ACTIVE);

        List<Card> cardList = List.of(cardFromDb);
        Page<Card> pageFromDb = new PageImpl<>(cardList, PageRequest.of(0, 10), 1);

        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(owner));
        when(cardRepository.findByOwnerId(userId, pageable)).thenReturn(pageFromDb);

        when(encryptionUtil.decrypt("ЗАШИФРОВАННЫЙ_НОМЕР")).thenReturn("1111222233334444");


        Page<CardDto> resultPage = cardService.getCardsByUsername(username, pageable);


        assertNotNull(resultPage);
        assertEquals(1, resultPage.getTotalElements());
        assertEquals(1, resultPage.getContent().size());

        CardDto resultDto = resultPage.getContent().get(0);

        assertEquals(1L, resultDto.getId());
        assertEquals("************4444", resultDto.getMaskedCardNumber());
        assertEquals(CardStatus.ACTIVE.name(), resultDto.getStatus());
        assertEquals(new BigDecimal("10.00"), resultDto.getBalance());

        verify(userRepository).findByUsername(username);
        verify(cardRepository).findByOwnerId(userId, pageable);
        verify(encryptionUtil).decrypt("ЗАШИФРОВАННЫЙ_НОМЕР");
        verifyNoMoreInteractions(cardRepository);
        verifyNoMoreInteractions(encryptionUtil);
    }

    @Test
    void getCardsByUsername_WhenUserNotFound_ShouldThrowException() {

        String username = "testuser";
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            cardService.getCardsByUsername(username, pageable);
        });

        verify(userRepository).findByUsername(username);
        verifyNoMoreInteractions(cardRepository);
    }


}
