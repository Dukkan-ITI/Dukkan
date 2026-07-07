package com.dukkan.data.source.remote.review

import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.dukkan.admin.CreateProductReviewMutation
import com.dukkan.admin.GetProductReviewRefsQuery
import com.dukkan.admin.SetProductReviewsMutation
import com.dukkan.admin.type.MetaobjectCapabilityDataInput
import com.dukkan.admin.type.MetaobjectCreateInput
import com.dukkan.admin.type.MetaobjectFieldInput
import com.dukkan.data.di.AdminApi
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.TimeZone
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
    @AdminApi private val apolloClient: ApolloClient,
) : ReviewAdminDataSource {

    companion object {
        private const val TAG = "ReviewAdmin"
        private const val METAOBJECT_TYPE = "dukkan_product_review"
    }

    override suspend fun submitReview(
        productGid: String,
        authorName: String,
        rating: Int,
        title: String,
        body: String,
    ): Result<String> = runCatching {
        // ── Step 1: Create the review metaobject ─────────────────────────────
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val createdAt = sdf.format(java.util.Date())

        val metaobjectInput = MetaobjectCreateInput(
            type = METAOBJECT_TYPE,
            capabilities = Optional.present(
                MetaobjectCapabilityDataInput(
                    publishable = Optional.present(
                        com.dukkan.admin.type.MetaobjectCapabilityDataPublishableInput(
                            status = com.dukkan.admin.type.MetaobjectStatus.ACTIVE
                        )
                    )
                )
            ),
            fields = Optional.present(
                listOf(
                    MetaobjectFieldInput(key = "product", value = productGid),
                    MetaobjectFieldInput(key = "customer_name", value = authorName),
                    MetaobjectFieldInput(key = "rating", value = rating.toString()),
                    MetaobjectFieldInput(key = "title", value = title),
                    MetaobjectFieldInput(key = "body", value = body),
                    MetaobjectFieldInput(key = "created_at", value = createdAt),
                    MetaobjectFieldInput(key = "approved", value = "true")
                )
            )
        )

        val createResponse =
            apolloClient.mutation(CreateProductReviewMutation(metaobjectInput)).execute()

        if (createResponse.hasErrors()) {
            throw Exception("Review creation failed: ${createResponse.errors?.joinToString { it.message }}")
        }

        val userErrors = createResponse.data?.metaobjectCreate?.userErrors
        if (!userErrors.isNullOrEmpty()) {
            throw Exception("Review creation failed: ${userErrors.joinToString { it.message }}")
        }

        val newReviewGid = createResponse.data?.metaobjectCreate?.metaobject?.id
            ?: throw Exception("Review metaobject ID not returned")

        Log.d(TAG, "Step 1 done — created metaobject: $newReviewGid")

        // ── Step 2: Fetch current reviews.items for the product ──────────────
        val readResponse = apolloClient.query(GetProductReviewRefsQuery(productGid)).execute()

        if (readResponse.hasErrors()) {
            throw Exception("Failed to fetch product metafields: ${readResponse.errors?.joinToString { it.message }}")
        }

        val existingValueJson = readResponse.data?.product?.metafield?.value

        // ── Step 3: Append new GID to the existing list ──────────────────────
        val existingIds = if (existingValueJson != null) {
            val arr = JSONArray(existingValueJson)
            (0 until arr.length()).map { arr.getString(it) }
        } else emptyList()

        val updatedIds = existingIds + newReviewGid
        val updatedJson = JSONArray(updatedIds).toString()

        Log.d(TAG, "Step 3 done — updated IDs list: $updatedJson")

        // ── Step 4: Set reviews.items metafield on the product ───────────────
        val setResponse =
            apolloClient.mutation(SetProductReviewsMutation(productGid, updatedJson)).execute()

        if (setResponse.hasErrors()) {
            throw Exception("Failed to attach review: ${setResponse.errors?.joinToString { it.message }}")
        }

        val setUserErrors = setResponse.data?.metafieldsSet?.userErrors
        if (!setUserErrors.isNullOrEmpty()) {
            throw Exception("Attaching review failed: ${setUserErrors.joinToString { it.message }}")
        }

        Log.d(TAG, "Step 4 done — review attached to product $productGid")

        newReviewGid
    }
}
