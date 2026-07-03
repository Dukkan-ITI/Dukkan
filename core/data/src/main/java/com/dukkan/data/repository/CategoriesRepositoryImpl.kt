package com.dukkan.data.repository

import com.dukkan.data.mapper.toDomainCategory
import com.dukkan.data.source.remote.apollo.ProductsDataSource
import com.dukkan.domain.model.Category.Category
import com.dukkan.domain.repository.CategoriesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CategoriesRepositoryImpl @Inject constructor(
    private val productsDataSource: ProductsDataSource,
) : CategoriesRepository {

    override fun getCategories(): Flow<List<Category>> = flow {
        val remoteData = productsDataSource.fetchCategories()
        val domainData = remoteData.map { it.toDomainCategory() }
        emit(domainData)
    }.catch { e ->
        android.util.Log.e("CategoriesDebug", "Error fetching categories: ${e.message}", e)
        emit(emptyList())
    }
}
