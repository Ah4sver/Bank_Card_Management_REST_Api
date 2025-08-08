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
import com.example.bankcards.util.CardUtil;
import com.example.bankcards.util.EncryptionUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;

    @Autowired
    public CardServiceImpl(CardRepository cardRepository, UserRepository userRepository, EncryptionUtil encryptionUtil) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.encryptionUtil = encryptionUtil;
    }

    @Override
    @Transactional
    public CardDto createCard(CreateCardRequestDto createCardRequestDto) {

        User owner = userRepository.findById(createCardRequestDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Не найден пользователь с id: " + createCardRequestDto.getUserId()));

        Card newCard = new Card();

        String encryptedCardNumber = encryptionUtil.encrypt(createCardRequestDto.getCardNumber());
        newCard.setCardNumber(encryptedCardNumber);

        newCard.setOwner(owner);
        newCard.setExpiryDate(createCardRequestDto.getExpiryDate());
        newCard.setBalance(BigDecimal.ZERO);
        newCard.setStatus(CardStatus.ACTIVE);

        Card savedCard = cardRepository.save(newCard);


        return mapToDto(savedCard);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardDto> getCardsByUsername(String username, Pageable pageable) {

        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Пользователь с именем " + username + " не найден"));

        Page<Card> cardsPage = cardRepository.findByOwnerId(user.getId(), pageable);

        return cardsPage.map(this::mapToDto);
    }

    @Override
    @Transactional
    public CardDto requestCardBlock(Long cardId, String username) {

        Card card = cardRepository.findById(cardId).orElseThrow(() -> new ResourceNotFoundException("Карта с id " + cardId + " не найдена"));

        if (!card.getOwner().getUsername().equals(username)) {
            throw new AccessDeniedException("Доступ запрещен: вы не можете управлять чужой картой");
        }
        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new IllegalStateException("Карта уже заблокирована");
        }
        if (card.getStatus() == CardStatus.PENDING_BLOCK) {
            throw new IllegalStateException("Карта уже ожидает блокировки");
        }
        if (card.getStatus() == CardStatus.EXPIRED) {
            throw new IllegalStateException("Нельзя заблокировать карту с истекшим сроком действия");
        }

        card.setStatus(CardStatus.PENDING_BLOCK);
        Card blockedCard = cardRepository.save(card);

        return mapToDto(blockedCard);
    }

    @Override
    @Transactional
    public void transferMoney(TransferRequestDto transferRequestDto, String username) {

        Long fromCardId = transferRequestDto.getFromCardId();
        Long toCardId = transferRequestDto.getToCardId();
        BigDecimal amount = transferRequestDto.getAmount();

        if (fromCardId.equals(toCardId)) {
            throw new IllegalArgumentException("Нельзя перевести деньги на ту же самую карту");
        }

        Card fromCard = cardRepository.findById(fromCardId).orElseThrow(() -> new ResourceNotFoundException("Карта-отправитель с id " + fromCardId + " не найдена"));
        Card toCard = cardRepository.findById(toCardId).orElseThrow(() -> new ResourceNotFoundException("Карта-отправитель с id " + fromCardId + " не найдена"));

        if (!fromCard.getOwner().getUsername().equals(username) || !toCard.getOwner().getUsername().equals(username)) {
            throw new AccessDeniedException("Доступ запрещен: вы можете переводить деньги только между своими картами");
        }
        if (fromCard.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException("Карта-отправитель неактивна. Перевод невозможен");
        }
        if (toCard.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException("Карта-получатель неактивна. Перевод невозможен");
        }
        if (fromCard.getBalance().compareTo(amount) <= 0) {
            throw new IllegalStateException("Недостаточно средств на карте-отправителе");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }

    @Override
    @Transactional
    public Page<CardDto> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable).map(this::mapToDto);
    }

    @Override
    @Transactional
    public CardDto blockCardByAdmin(Long cardId) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new ResourceNotFoundException("Карта с id " + cardId + " не найдена"));

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new IllegalStateException("Карта уже заблокирована.");
        }
        card.setStatus(CardStatus.BLOCKED);
        return mapToDto(cardRepository.save(card));
    }

    @Override
    @Transactional
    public CardDto activateCardByAdmin(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Карта с id " + cardId + " не найдена"));

        if (card.getStatus() != CardStatus.BLOCKED) {
            throw new IllegalStateException("Активировать можно только заблокированную карту.");
        }
        card.setStatus(CardStatus.ACTIVE);
        return mapToDto(cardRepository.save(card));
    }

    @Override
    @Transactional
    public void deleteCardByAdmin(Long cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new ResourceNotFoundException("Карта с id " + cardId + " не найдена");
        }
        cardRepository.deleteById(cardId);
    }

    @Override
    @Transactional
    public BalanceDto getCardBalance(Long cardId, String username) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Карта с id " + cardId + " не найдена"));

        if (!card.getOwner().getUsername().equals(username)) {
            throw new AccessDeniedException("Доступ запрещен: вы можете смотреть баланс только своей карты");
        }

        BalanceDto balanceDto = new BalanceDto();
        balanceDto.setBalance(card.getBalance());

        return balanceDto;
    }

    private CardDto mapToDto(Card card) {
        CardDto cardDto = new CardDto();
        cardDto.setId(card.getId());
        cardDto.setExpiryDate(card.getExpiryDate());
        cardDto.setBalance(card.getBalance());
        cardDto.setStatus(card.getStatus().name());

        String decryptedCardNumber = encryptionUtil.decrypt(card.getCardNumber());
        cardDto.setMaskedCardNumber(CardUtil.maskCardNumber(decryptedCardNumber));

        return cardDto;
    }

}
