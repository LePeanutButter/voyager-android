package com.voyager.tourism.data.repository

import com.voyager.tourism.data.api.VoyagerAiApi
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Delegates every Voyager AI Retrofit endpoint through a single injectable repository type.
 */
@Singleton
class VoyagerAiRepositoryImpl @Inject constructor(
    api: VoyagerAiApi,
) : VoyagerAiRepository, VoyagerAiApi by api
