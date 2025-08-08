package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequestDto;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(controllers = AdminController.class)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private CardService cardService;
    @MockitoBean
    private JwtTokenProvider tokenProvider;
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_WhenUserIsAdmin_ShouldReturnCreated() throws Exception {

        CreateCardRequestDto requestDto = new CreateCardRequestDto();
        requestDto.setUserId(1L);
        requestDto.setCardNumber("1111222233334444");
        requestDto.setExpiryDate(LocalDate.now().plusYears(3));

        CardDto responseDto = new CardDto();
        responseDto.setId(1L);
        responseDto.setMaskedCardNumber("************4444");

        given(cardService.createCard(any(CreateCardRequestDto.class))).willReturn(responseDto);

        mockMvc.perform(post("/api/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.maskedCardNumber").value("************4444"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createCard_WhenUserIsNotAdmin_ShouldReturnForbidden() throws Exception {

        CreateCardRequestDto requestDto = new CreateCardRequestDto();

        mockMvc.perform(post("/api/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCards_ShouldReturnPagedCards() throws Exception {

        Page<CardDto> cardPage = new PageImpl<>(List.of(new CardDto()));
        given(cardService.getAllCards(any())).willReturn(cardPage);

        mockMvc.perform(get("/api/admin/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void blockCard_ShouldReturnOk() throws Exception {

        Long cardId = 1L;
        given(cardService.blockCardByAdmin(cardId)).willReturn(new CardDto());

        mockMvc.perform(patch("/api/admin/cards/{id}/block", cardId)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateCard_ShouldReturnOk() throws Exception {

        Long cardId = 1L;
        given(cardService.activateCardByAdmin(cardId)).willReturn(new CardDto());

        mockMvc.perform(patch("/api/admin/cards/{id}/activate", cardId)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCard_ShouldReturnNoContent() throws Exception {

        Long cardId = 1L;
        doNothing().when(cardService).deleteCardByAdmin(cardId);

        mockMvc.perform(delete("/api/admin/cards/{id}", cardId)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
