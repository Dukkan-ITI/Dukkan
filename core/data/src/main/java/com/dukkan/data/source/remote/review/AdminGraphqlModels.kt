package com.dukkan.data.source.remote.review

/** Lightweight DTOs used only for raw Admin GraphQL HTTP calls. */
data class AdminGraphqlRequest(
    val query: String,
    val variables: Map<String, Any?> = emptyMap(),
)

data class AdminGraphqlResponse(
    val data: Map<String, Any?>?,
    val errors: List<Map<String, Any?>>?,
)
