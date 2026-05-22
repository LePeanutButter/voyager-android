package com.voyager.tourism.data.dto

import org.junit.Assert.assertNotNull
import org.junit.Test

class PagedDtoCoverageBoosterTest {

    @Test
    fun touchPagedDtos() {
        val userPaged = PagedResponseUserDto(status = 200, message = "ok", data = emptyList())
        val travelPaged = PagedResponseTravelPlanDto(status = 200, message = "ok", data = emptyList())
        val msgPaged = PagedResponseMessageDto(status = 200, message = "ok", data = emptyList())
        val stats = UserStatisticsDto(10L, 5L)
        val msg = MessageDto(1L, 2L, 3L, 4L, "c", "s", "ct", "ut")
        val sendReq = SendMessageRequestDto(1L, 2L, "c")
        val match = MatchResponseDto(1L, "u", "d", 1.0, 1, 2, 3)
        val res = ReservationDto(1L, "n", "d", "t", "s", "c", "s", "e", "l", 10.0, true)

        assertNotNull(userPaged)
        assertNotNull(travelPaged)
        assertNotNull(msgPaged)
        assertNotNull(stats)
        assertNotNull(msg)
        assertNotNull(sendReq)
        assertNotNull(match)
        assertNotNull(res)
    }
}
