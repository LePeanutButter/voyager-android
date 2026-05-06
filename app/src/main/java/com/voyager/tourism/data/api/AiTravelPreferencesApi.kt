package com.voyager.tourism.data.api

import com.voyager.tourism.data.dto.QuestionnaireStepRequestDto
import com.voyager.tourism.data.dto.QuestionnaireStepResponseDto
import com.voyager.tourism.data.dto.QuestionnaireSubmitRequestDto
import com.voyager.tourism.data.dto.QuestionnaireSubmitResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Voyager AI service — adaptive travel preference questionnaire.
 * Base URL is the AI microservice root ending with /api/v1/
 */
interface AiTravelPreferencesApi {

    @POST("travel-preferences/questionnaire/step")
    suspend fun postQuestionnaireStep(
        @Body body: QuestionnaireStepRequestDto
    ): Response<QuestionnaireStepResponseDto>

    @POST("travel-preferences/questionnaire/submit")
    suspend fun postQuestionnaireSubmit(
        @Body body: QuestionnaireSubmitRequestDto
    ): Response<QuestionnaireSubmitResponseDto>
}
