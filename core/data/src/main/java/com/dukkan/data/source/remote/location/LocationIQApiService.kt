package com.dukkan.data.source.remote.location

import retrofit2.http.GET
import retrofit2.http.Query

interface LocationIQApiService {
    @GET("v1/autocomplete.php")
    suspend fun autocomplete(
        @Query("key") apiKey: String,
        @Query("q") query: String,
        @Query("limit") limit: Int = 5,
        @Query("format") format: String = "json"
    ): List<LocationIQPredictionDto>
}
