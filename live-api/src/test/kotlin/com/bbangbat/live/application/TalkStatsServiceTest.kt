package com.bbangbat.live.application

import com.bbangbat.live.repository.TalkPersistenceAdapter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given

@ExtendWith(MockitoExtension::class)
class TalkStatsServiceTest {
    @Mock
    private lateinit var talkPersistenceAdapter: TalkPersistenceAdapter

    private lateinit var talkStatsService: TalkStatsService

    @BeforeEach
    fun setUp() {
        talkStatsService = TalkStatsService(talkPersistenceAdapter)
    }

    @Test
    fun `회원이 작성한 톡 수를 조회한다`() {
        given(talkPersistenceAdapter.countByAuthorId(1L)).willReturn(4L)

        val result = talkStatsService.countByAuthorId(1L)

        assertThat(result).isEqualTo(4L)
    }
}
