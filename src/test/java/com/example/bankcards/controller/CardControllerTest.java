package com.example.bankcards.controller;

import com.example.bankcards.dto.BalanceDto;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.security.CustomUserDetailsService;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CardController.class)
public class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private CardService cardService;
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getUserCards_ShouldReturnPagedCards() throws Exception {

        CardDto cardDto = new CardDto();
        cardDto.setId(1L);
        Page<CardDto> cardPage = new PageImpl<>(List.of(cardDto));

        given(cardService.getCardsByUsername(eq("testuser"), any()))
                .willReturn(cardPage);


        mockMvc.perform(get("/api/cards")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void requestBlock_WhenUserIsOwner_ShouldReturnOk() throws Exception {

        Long cardId = 1L;
        CardDto responseDto = new CardDto();
        responseDto.setId(cardId);
        responseDto.setStatus("PENDING_BLOCK");

        given(cardService.requestCardBlock(eq(cardId), eq("testuser"))).willReturn(responseDto);


        mockMvc.perform(patch("/api/cards/{id}/request-block", cardId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_BLOCK"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void transferMoney_WithValidRequest_ShouldReturnNoContent() throws Exception {

        TransferRequestDto requestDto = new TransferRequestDto();
        requestDto.setFromCardId(1L);
        requestDto.setToCardId(2L);
        requestDto.setAmount(new BigDecimal("100"));

        doNothing().when(cardService).transferMoney(any(TransferRequestDto.class), eq("testuser"));

        mockMvc.perform(post("/api/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getBalance_WhenCardExists_ShouldReturnBalance() throws Exception {

        Long cardId = 1L;
        BalanceDto balanceDto = new BalanceDto();
        balanceDto.setBalance(new BigDecimal("100.50"));

        given(cardService.getCardBalance(eq(cardId), eq("testuser"))).willReturn(balanceDto);

        mockMvc.perform(get("/api/cards/{id}/balance", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(100.50));
    }
}
