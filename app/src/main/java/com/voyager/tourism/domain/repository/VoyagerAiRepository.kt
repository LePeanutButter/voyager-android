package com.voyager.tourism.domain.repository

import com.voyager.tourism.data.api.VoyagerAiApi

/**
 * Acceso tipado al microservicio FastAPI (`VoyagerAiApi`).
 * Delega todos los endpoints de IA para que la capa de dominio no dependa directamente del tipo Retrofit en los casos de uso.
 */
interface VoyagerAiRepository : VoyagerAiApi
