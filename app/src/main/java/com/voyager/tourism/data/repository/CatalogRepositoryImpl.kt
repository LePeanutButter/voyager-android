package com.voyager.tourism.data.repository

import com.voyager.tourism.data.api.CatalogApiService
import com.voyager.tourism.domain.repository.CatalogRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogRepositoryImpl @Inject constructor(
    api: CatalogApiService,
) : CatalogRepository, CatalogApiService by api
