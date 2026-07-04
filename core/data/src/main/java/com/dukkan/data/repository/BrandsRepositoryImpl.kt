package com.dukkan.data.repository

import com.dukkan.data.mapper.toDomainBrands
import com.dukkan.data.source.remote.apollo.ProductsDataSource
import com.dukkan.domain.model.Brand
import com.dukkan.domain.repository.BrandsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class BrandsRepositoryImpl @Inject constructor(
    private val productsDataSource: ProductsDataSource,
) : BrandsRepository {

    override fun getBrands(): Flow<List<Brand>> = flow {
        val remoteData = productsDataSource.fetchBrands()
        val domainData = remoteData.toDomainBrands()
        emit(domainData)
    }.catch { e ->
        android.util.Log.e("BrandsDebug", "Error fetching brands: ${e.message}", e)
        emit(emptyList())
    }.flowOn(Dispatchers.IO)
}