package com.dukkan.data.repository

import com.dukkan.data.mapper.toDomainModel
import com.dukkan.data.mapper.toHomeEntity
import com.dukkan.data.source.local.dao.HomeDao
import com.dukkan.data.source.remote.apollo.ProductsDataSource
import com.dukkan.domain.model.Product
import com.dukkan.domain.repository.ProductsRepository
import com.dukkan.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first

class ProductsRepositoryImpl(
    private val productsDataSource: ProductsDataSource,
    private val settingsRepository: SettingsRepository,
    private val homeDao: HomeDao,
) : ProductsRepository {
    override suspend fun getProductById(productId: String): Result<Product> {
        val country = settingsRepository.currency.first().countryCode
        val language = settingsRepository.language.first().languageCode
        
        // Try to get from local home cache first (only if it matches the ID)
        val cached = homeDao.getProducts().first().find { it.id == productId }
        if (cached != null) {
            return Result.success(cached.toDomainModel())
        }

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
        
        return try {
            val response = productsDataSource.getProducts(
                first = limit,
                after = after,
                country = country,
                language = language,
            )
            val products = response?.products?.edges?.mapNotNull { edge ->
                edge.node?.toDomainModel()
            } ?: emptyList()

            // Cache if this is the first page for Home
            if (after == null && products.isNotEmpty()) {
                homeDao.insertProducts(products.map { it.toHomeEntity() })
            }
            
            products
        } catch (e: Exception) {
            // Fallback to cache if first page
            if (after == null) {
                homeDao.getProducts().first().map { it.toDomainModel() }
            } else {
                emptyList()
            }
        }
    }

    override suspend fun getProductsByCollectionId(
        categoryId: String,
        limit: Int,
        after: String?,
    ): List<Product> {
        val country = settingsRepository.currency.first().countryCode
        val language = settingsRepository.language.first().languageCode
        return productsDataSource.getProductsByCollectionId(
            categoryId = categoryId,
            first = limit,
            after = after,
            country = country,
            language = language,
        )
    }

    override suspend fun getProductsByType(
        type: String,
        limit: Int,
        after: String?,
    ): List<Product> {
        val country = settingsRepository.currency.first().countryCode
        val language = settingsRepository.language.first().languageCode
        return productsDataSource.getProductsByVendor(
            vendor = type,
            first = limit,
            after = after,
            country = country,
            language = language,
        )
    }
}
