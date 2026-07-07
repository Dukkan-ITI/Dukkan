package com.dukkan.domain.repository

import com.dukkan.domain.model.LocationDetail

interface PlacesRepository {
    suspend fun getAutocompletePredictions(query: String): Result<List<LocationDetail>>
}
