package com.dukkan.data.repository

import com.dukkan.data.source.remote.location.LocationIQApiService
import com.dukkan.domain.model.LocationDetail
import com.dukkan.domain.repository.PlacesRepository
import android.util.Log

class PlacesRepositoryImpl(
    private val apiService: LocationIQApiService,
    private val apiKey: String
) : PlacesRepository {
    override suspend fun getAutocompletePredictions(query: String): Result<List<LocationDetail>> {
        return try {
            val response = apiService.autocomplete(apiKey = apiKey, query = query)
            val details = response.map { dto ->
                LocationDetail(
                    id = dto.placeId ?: "",
                    name = dto.address?.name ?: dto.displayName?.substringBefore(",") ?: "",
                    address = dto.displayName ?: "",
                    latitude = dto.lat?.toDoubleOrNull() ?: 0.0,
                    longitude = dto.lon?.toDoubleOrNull() ?: 0.0,
                    city = dto.address?.city ?: dto.address?.town ?: dto.address?.village,
                    state = dto.address?.state,
                    country = dto.address?.country,
                    postalCode = dto.address?.postcode
                )
            }
            Result.success(details)
        } catch (e: Exception) {
            Log.e("PlacesRepositoryImpl", "Error fetching predictions", e)
            Result.failure(e)
        }
    }
}
