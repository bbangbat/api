package com.bbangbat.health.api

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class HealthControllerTest {
    private val mockMvc = MockMvcBuilders.standaloneSetup(HealthController()).build()

    @Test
    fun `헬스체크 요청에 UP 상태를 반환한다`() {
        mockMvc
            .get("/api/health")
            .andExpect {
                status { isOk() }
                jsonPath("$.status") { value("UP") }
            }
    }
}
