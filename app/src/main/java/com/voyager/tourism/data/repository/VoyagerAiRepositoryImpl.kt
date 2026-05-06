package com.voyager.tourism.data.repository

import com.voyager.tourism.data.api.VoyagerAiApi
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoyagerAiRepositoryImpl @Inject constructor(
    api: VoyagerAiApi,
) : VoyagerAiRepository, VoyagerAiApi by api
