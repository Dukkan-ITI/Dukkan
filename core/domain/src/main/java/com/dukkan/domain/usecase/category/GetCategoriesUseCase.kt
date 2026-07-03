package com.dukkan.domain.usecase.category

import com.dukkan.domain.model.Category.Category
import com.dukkan.domain.repository.CategoriesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val categoriesRepository: CategoriesRepository
) {
    operator fun invoke(): Flow<List<Category>> {
        return categoriesRepository.getCategories()
    }
}