package com.dukkan.data.source.remote.review

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ReviewAdminService {
    @POST(".")
    suspend fun execute(@Body body: AdminGraphqlRequest): Response<AdminGraphqlResponse>
}
