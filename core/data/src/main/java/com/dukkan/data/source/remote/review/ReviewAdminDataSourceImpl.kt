package com.dukkan.data.source.remote.review

import android.util.Log
import org.json.JSONArray
import java.time.Instant
import javax.inject.Inject

/**
 * Implements the four-step review submission flow documented in
 * shopify_product_reviews_metafields.md (Part 2):
 *
 *  1. metaobjectCreate  — create the review metaobject, capture returned GID
 *  2. Query product's current reviews.items metafield
 *  3. Append new GID, re-encode as JSON array string
 *  4. metafieldsSet     — attach updated list to the product
 */
class ReviewAdminDataSourceImpl @Inject constructor(
    private val service: ReviewAdminService,
) : ReviewAdminDataSource {

    companion object {
        private const val TAG = "ReviewAdmin"
        private const val METAOBJECT_TYPE = "dukkan_product_review"
        private const val NAMESPACE = "reviews"
        private const val KEY = "items"
    }

    override suspend fun submitReview(
        productGid: String,
        authorName: String,
        rating: Int,
        title: String,
        body: String,
    ): Result<Unit> = runCatching {
        // ── Step 1: Create the review metaobject ─────────────────────────────
        val createdAt = Instant.now().toString()

        val createMutation = """
            mutation CreateProductReview(${'$'}metaobject: MetaobjectCreateInput!) {
              metaobjectCreate(metaobject: ${'$'}metaobject) {
                metaobject { id }
                userErrors { field message }
              }
            }
        """.trimIndent()

        val createVariables = mapOf(
            "metaobject" to mapOf(
                "type" to METAOBJECT_TYPE,
                "capabilities" to mapOf(
                    "publishable" to mapOf("status" to "ACTIVE")
                ),
                "fields" to listOf(
                    mapOf("key" to "product",       "value" to productGid),
                    mapOf("key" to "customer_name", "value" to authorName),
                    mapOf("key" to "rating",        "value" to rating.toString()),
                    mapOf("key" to "title",         "value" to title),
                    mapOf("key" to "body",          "value" to body),
                    mapOf("key" to "created_at",    "value" to createdAt),
                    mapOf("key" to "approved",      "value" to "true"),
                )
            )
        )

        val createResponse = service.execute(AdminGraphqlRequest(createMutation, createVariables))
        val createData = createResponse.body()
            ?: throw Exception("No response from Admin API (create metaobject)")

        val userErrors = extractUserErrors(createData, "metaobjectCreate")
        if (userErrors.isNotEmpty()) throw Exception("Review creation failed: $userErrors")

        @Suppress("UNCHECKED_CAST")
        val newReviewGid = ((createData.data
            ?.get("metaobjectCreate") as? Map<String, Any?>)
            ?.get("metaobject") as? Map<String, Any?>)
            ?.get("id") as? String
            ?: throw Exception("Review metaobject ID not returned")

        Log.d(TAG, "Step 1 done — created metaobject: $newReviewGid")

        // ── Step 2: Fetch current reviews.items for the product ──────────────
        val readQuery = """
            query GetProductReviewRefs(${'$'}productId: ID!) {
              product(id: ${'$'}productId) {
                metafield(namespace: "$NAMESPACE", key: "$KEY") {
                  value
                }
              }
            }
        """.trimIndent()

        val readResponse = service.execute(
            AdminGraphqlRequest(readQuery, mapOf("productId" to productGid))
        )
        val readData = readResponse.body()
            ?: throw Exception("No response from Admin API (read metafield)")

        @Suppress("UNCHECKED_CAST")
        val existingValueJson = ((readData.data
            ?.get("product") as? Map<String, Any?>)
            ?.get("metafield") as? Map<String, Any?>)
            ?.get("value") as? String

        // ── Step 3: Append new GID to the existing list ──────────────────────
        val existingIds = if (existingValueJson != null) {
            val arr = JSONArray(existingValueJson)
            (0 until arr.length()).map { arr.getString(it) }
        } else emptyList()

        val updatedIds = existingIds + newReviewGid
        val updatedJson = JSONArray(updatedIds).toString()

        Log.d(TAG, "Step 3 done — updated IDs list: $updatedJson")

        // ── Step 4: Set reviews.items metafield on the product ───────────────
        val setMutation = """
            mutation SetProductReviews(${'$'}productId: ID!, ${'$'}reviewIdsJson: String!) {
              metafieldsSet(metafields: [{
                ownerId:   ${'$'}productId
                namespace: "$NAMESPACE"
                key:       "$KEY"
                type:      "list.metaobject_reference"
                value:     ${'$'}reviewIdsJson
              }]) {
                metafields { id }
                userErrors  { field message }
              }
            }
        """.trimIndent()

        val setVariables = mapOf(
            "productId"     to productGid,
            "reviewIdsJson" to updatedJson,
        )

        val setResponse = service.execute(AdminGraphqlRequest(setMutation, setVariables))
        val setData = setResponse.body()
            ?: throw Exception("No response from Admin API (metafieldsSet)")

        val setErrors = extractUserErrors(setData, "metafieldsSet")
        if (setErrors.isNotEmpty()) throw Exception("Attaching review failed: $setErrors")

        Log.d(TAG, "Step 4 done — review attached to product $productGid")
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractUserErrors(response: AdminGraphqlResponse, operationKey: String): List<String> {
        val op = response.data?.get(operationKey) as? Map<String, Any?> ?: return emptyList()
        val errors = op["userErrors"] as? List<Map<String, Any?>> ?: return emptyList()
        return errors.mapNotNull { it["message"] as? String }
    }
}
