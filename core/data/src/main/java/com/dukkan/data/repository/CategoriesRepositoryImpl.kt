package com.dukkan.data.repository

import com.dukkan.data.mapper.toDomainCategory
import com.dukkan.data.mapper.toHomeEntity
import com.dukkan.data.mapper.toDomainModel
import com.dukkan.data.source.local.dao.HomeDao
import com.dukkan.data.source.remote.apollo.ProductsDataSource
import com.dukkan.domain.model.Category.Category
import com.dukkan.domain.repository.CategoriesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CategoriesRepositoryImpl @Inject constructor(
    private val productsDataSource: ProductsDataSource,
    private val homeDao: HomeDao,
) : CategoriesRepository {

    override fun getCategories(): Flow<List<Category>> = flow {
        val remoteData = productsDataSource.fetchCategories()
        val domainData = remoteData.map { it.toDomainCategory() }

        if (domainData.isNotEmpty()) {
            homeDao.insertCategories(domainData.map { it.toHomeEntity() })
        }

        emit(domainData)
    }.catch { e ->
        android.util.Log.e("CategoriesDebug", "Error fetching categories: ${e.message}", e)
        val cached = homeDao.getCategories().first().map { it.toDomainModel() }
        if (cached.isNotEmpty()) {
            emit(cached)
        } else {
            emit(emptyList())
        }
    }
}
