package com.voyager.tourism.domain.repository

import com.voyager.tourism.data.api.VoyagerAiApi

/**
 * Typed façade over the FastAPI Voyager AI service ([VoyagerAiApi]).
 * Keeps domain use cases from depending on the Retrofit interface type directly.
 */
interface VoyagerAiRepository : VoyagerAiApi
