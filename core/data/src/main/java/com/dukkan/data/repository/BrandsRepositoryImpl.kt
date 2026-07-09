package com.dukkan.data.repository

import com.dukkan.data.mapper.toDomainBrands
import com.dukkan.data.mapper.toDomainModel
import com.dukkan.data.mapper.toHomeEntity
import com.dukkan.data.source.local.dao.HomeDao
import com.dukkan.data.source.remote.apollo.ProductsDataSource
import com.dukkan.domain.model.Brand
import com.dukkan.domain.repository.BrandsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class BrandsRepositoryImpl @Inject constructor(
    private val productsDataSource: ProductsDataSource,
    private val homeDao: HomeDao,
) : BrandsRepository {

    override fun getBrands(): Flow<List<Brand>> = flow {
        val remoteData = productsDataSource.fetchBrands()
        val domainData = remoteData.toDomainBrands()

        if (domainData.isNotEmpty()) {
            homeDao.insertBrands(domainData.map { it.toHomeEntity() })
        }

        emit(domainData)
    }.catch { e ->
        android.util.Log.e("BrandsDebug", "Error fetching brands: ${e.message}", e)
        val cached = homeDao.getBrands().first().map { it.toDomainModel() }
        if (cached.isNotEmpty()) {
            emit(cached)
        } else {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)
}
