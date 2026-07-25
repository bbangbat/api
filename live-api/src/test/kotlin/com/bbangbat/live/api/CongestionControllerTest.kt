package com.bbangbat.live.api

import com.bbangbat.auth.voter.VoterResolver
import com.bbangbat.live.application.CongestionService
import com.bbangbat.live.domain.Congestion
import com.bbangbat.live.domain.CongestionLevel
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class CongestionControllerTest {
    private lateinit var congestionService: CongestionService
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        congestionService = mock()
        val voterResolver: VoterResolver = mock()
        mockMvc = MockMvcBuilders.standaloneSetup(CongestionController(congestionService, voterResolver)).build()
    }

    @Test
    fun `경로의 storeId로 혼잡도를 단건 조회한다`() {
        given(congestionService.getCongestion(1L)).willReturn(congestion(1L))

        mockMvc
            .get("/api/congestion/1")
            .andExpect {
                status { isOk() }
                jsonPath("$.storeId") { value(1L) }
            }
    }

    @Test
    fun `storeIds로 혼잡도를 벌크 조회한다`() {
        given(congestionService.getCongestions(listOf(1L, 2L))).willReturn(
            linkedMapOf(
                1L to congestion(1L),
                2L to congestion(2L),
            ),
        )

        mockMvc
            .get("/api/congestion") {
                param("storeIds", "1,2")
            }.andExpect {
                status { isOk() }
                jsonPath("$[0].storeId") { value(1L) }
                jsonPath("$[1].storeId") { value(2L) }
            }
    }

    @Test
    fun `storeId와 storeIds를 함께 보내도 벌크 조회 핸들러만 실행한다`() {
        given(congestionService.getCongestions(listOf(2L))).willReturn(mapOf(2L to congestion(2L)))

        mockMvc
            .get("/api/congestion") {
                param("storeId", "1")
                param("storeIds", "2")
            }.andExpect {
                status { isOk() }
                jsonPath("$[0].storeId") { value(2L) }
            }
    }

    private fun congestion(storeId: Long): Congestion =
        Congestion.summarizeCounts(
            storeId = storeId,
            counts = mapOf(CongestionLevel.NORMAL to 1),
        )
}
