package com.dukkan.data.repository

import com.dukkan.data.mapper.toDomainModel
import com.dukkan.data.mapper.toPredictiveSearchResult
import com.dukkan.data.mapper.toSearchResult
import com.dukkan.data.source.local.dao.HomeDao
import com.dukkan.data.source.remote.apollo.SearchDataSource
import com.dukkan.domain.model.PageInfo
import com.dukkan.domain.model.PredictiveSearchResult
import com.dukkan.domain.model.SearchProduct
import com.dukkan.domain.model.SearchResult
import com.dukkan.domain.repository.SearchRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val searchDataSource: SearchDataSource,
    private val homeDao: HomeDao
) : SearchRepository {

    override suspend fun searchProducts(
        query: String,
        first: Int,
        after: String?,
        filters: com.dukkan.domain.model.SearchFilter?
    ): Result<SearchResult> = runCatching {
        try {
            searchDataSource.searchProducts(query, first, after, filters)
                ?.toSearchResult()
                ?: SearchResult(emptyList(), PageInfo(false, null), 0)
        } catch (e: Exception) {
            // Fallback to local search in home_products cache
            val cachedProducts = homeDao.getProducts().first()
                .map { it.toDomainModel() }
                .filter { product ->
                    product.title.contains(query, ignoreCase = true) ||
                            product.vendor.contains(query, ignoreCase = true) ||
                            (product.description?.contains(query, ignoreCase = true) == true)
                }

            SearchResult(
                products = cachedProducts.map { product ->
                    SearchProduct(
                        id = product.id,
                        title = product.title,
                        vendor = product.vendor,
                        productType = product.productType ?: "",
                        availableForSale = true,
                        price = product.minPrice,
                        imageUrl = product.featuredImage?.url,
                        imageAltText = product.featuredImage?.altText,
                        variants = emptyList(),
                        averageRating = product.averageRating,
                        reviewCount = product.reviews.size
                    )
                },
                pageInfo = PageInfo(false, null),
                totalCount = cachedProducts.size
            )
        }
    }

    override suspend fun predictiveSearch(query: String): Result<PredictiveSearchResult> =
        runCatching {
            try {
                searchDataSource.predictiveSearch(query)
                    ?.toPredictiveSearchResult()
                    ?: PredictiveSearchResult(emptyList(), emptyList())
            } catch (e: Exception) {
                // Predictive search fallback: just search cached titles/vendors
                val cachedProducts = homeDao.getProducts().first()
                    .map { it.toDomainModel() }
                    .filter { product ->
                        product.title.contains(query, ignoreCase = true) ||
                                product.vendor.contains(query, ignoreCase = true)
                    }
                    .take(5)

                PredictiveSearchResult(
                    products = cachedProducts.map { product ->
                        SearchProduct(
                            id = product.id,
                            title = product.title,
                            vendor = product.vendor,
                            productType = product.productType ?: "",
                            availableForSale = true,
                            price = product.minPrice,
                            imageUrl = product.featuredImage?.url,
                            imageAltText = product.featuredImage?.altText,
                            variants = emptyList(),
                            averageRating = product.averageRating,
                            reviewCount = product.reviews.size
                        )
                    },
                    collections = emptyList()
                )
            }
        }
}
