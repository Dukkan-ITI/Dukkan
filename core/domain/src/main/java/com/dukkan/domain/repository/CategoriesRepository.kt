package com.dukkan.domain.repository

import com.dukkan.domain.model.Category.Category
import kotlinx.coroutines.flow.Flow

interface CategoriesRepository {
    fun getCategories(): Flow<List<Category>>
}
