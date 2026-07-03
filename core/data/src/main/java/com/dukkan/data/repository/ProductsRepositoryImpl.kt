package com.dukkan.data.repository

import com.dukkan.data.mapper.toDomainModel
import com.dukkan.data.source.remote.apollo.ProductsDataSource
import com.dukkan.domain.model.Product
import com.dukkan.domain.repository.ProductsRepository
import com.dukkan.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first

class ProductsRepositoryImpl(
    private val productsDataSource: ProductsDataSource,
    private val settingsRepository: SettingsRepository,
) : ProductsRepository {
    override suspend fun getProductById(productId: String): Result<Product> {
        val country = settingsRepository.currency.first().countryCode
        val language = settingsRepository.language.first().languageCode
        productsDataSource.getProductById(productId, country = country, language = language).also {
            return if (it != null) {
                Result.success(it.toDomainModel())
            } else {
                Result.failure(Exception("Unknown error occurred"))
            }
        }
    }

    override suspend fun getProducts(limit: Int, after: String?): List<Product> {
        val country = settingsRepository.currency.first().countryCode
        val language = settingsRepository.language.first().languageCode
        val response = productsDataSource.getProducts(
            first = limit,
            after = after,
            country = country,
            language = language,
        )
        return response?.products?.edges?.mapNotNull { edge ->
            edge.node?.toDomainModel()
        } ?: emptyList()
    }
}
