package com.dukkan.domain.repository

import com.dukkan.domain.model.Brand
import kotlinx.coroutines.flow.Flow

interface BrandsRepository {
    fun getBrands(): Flow<List<Brand>>
}