package com.voyager.tourism.domain.repository

import com.voyager.tourism.data.api.CatalogApiService

/**
 * Domain-facing catalog API that delegates to [CatalogApiService] (Amadeus-backed Spring endpoints).
 */
interface CatalogRepository : CatalogApiService
