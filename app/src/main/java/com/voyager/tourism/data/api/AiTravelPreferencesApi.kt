package com.voyager.tourism.data.api

import com.voyager.tourism.data.dto.QuestionnaireStepRequestDto
import com.voyager.tourism.data.dto.QuestionnaireStepResponseDto
import com.voyager.tourism.data.dto.QuestionnaireSubmitRequestDto
import com.voyager.tourism.data.dto.QuestionnaireSubmitResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Voyager AI service client for the adaptive travel preference questionnaire.
 * The Retrofit base URL is the AI microservice root ending with `/api/v1/`.
 */
interface AiTravelPreferencesApi {

    /**
     * Submits one questionnaire step and receives the next step or completion hint.
     */
    @POST("travel-preferences/questionnaire/step")
    suspend fun postQuestionnaireStep(
        @Body body: QuestionnaireStepRequestDto
    ): Response<QuestionnaireStepResponseDto>

    /**
     * Submits the full questionnaire answers for persistence and scoring.
     */
    @POST("travel-preferences/questionnaire/submit")
    suspend fun postQuestionnaireSubmit(
        @Body body: QuestionnaireSubmitRequestDto
    ): Response<QuestionnaireSubmitResponseDto>
}
