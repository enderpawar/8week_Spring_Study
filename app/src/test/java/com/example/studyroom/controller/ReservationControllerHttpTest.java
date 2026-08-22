package com.example.studyroom.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReservationControllerHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void reserveReturnsSuccessResponseForValidBody() throws Exception {
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roomName": "A-101",
                                  "requesterName": "민지"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("예약 완료")));
    }

    @Test
    void reserveReturns400ForBlankBodyFields() throws Exception {
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roomName": "",
                                  "requesterName": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.roomName").value("방 이름은 비어있을 수 없습니다"))
                .andExpect(jsonPath("$.requesterName").value("예약자 이름은 비어있을 수 없습니다."));
    }

    @Test
    void cancelReturns404WhenReservationDoesNotExist() throws Exception {
        mockMvc.perform(post("/reservations/cancel/{id}", 999_999L).param("cancelReason","테스트 사유"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("예약을 찾을 수 없습니다. (id: 999999)"));
    }

    @Test
    void cancelReturns400WhenIdIsNotPositive() throws Exception {
        mockMvc.perform(post("/reservations/cancel/{id}", 0L).param("cancelReason","테스트사유2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]")
                        .value("예약 번호는 1 이상이어야 합니다"));
    }
}
