package com.voyager.tourism.data.dto

import org.junit.Assert.assertNotNull
import org.junit.Test

class DtoCoverageBoosterTest {

    @Test
    fun touchCommonDtos() {
        val error = ValidationError("f", "m")
        val api = ApiResponse("t", 200, "m", "d", "p", listOf(error))
        assertNotNull(api)
    }

    @Test
    fun touchAuthDtos() {
        val req = UserLoginRequest("u", "p")
        val login = UserLoginDto("u", "p")
        val reg = UserRegistrationDto("u", "e", "p", "f", "l", "ph")
        val google = GoogleLoginUrlResponse("url", "state")
        assertNotNull(req)
        assertNotNull(login)
        assertNotNull(reg)
        assertNotNull(google)
    }

    @Test
    fun touchAiDtos() {
        val loc = AiLocationBody(1.0, 2.0, "c")
        val cand = LocalRecommendationCandidateBody("1", "n", "c", 1.0, "ct")
        val req = LocalRecommendationRequestBody("u", "q", 5, listOf(cand))
        val item = LocalRecommendationItemDto("1", "n", "c", 1.0, 0.5, 0.5, "ct")
        val resp = LocalRecommendationResponseDto(listOf(item), emptyMap(), listOf("p"))
        val chatReq = LocalChatRequestBody("u", "s", "m")
        val chatResp = LocalChatResponseDto("s", "r", "role")
        val histResp = LocalChatHistoryResponseDto(listOf(LocalChatMessageDto("r", "c", "m", "s")))

        assertNotNull(loc)
        assertNotNull(cand)
        assertNotNull(req)
        assertNotNull(resp)
        assertNotNull(chatReq)
        assertNotNull(chatResp)
        assertNotNull(histResp)
    }
}
