package com.dukkan.domain.usecase

import com.dukkan.domain.model.Brand
import com.dukkan.domain.repository.BrandsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBrandsUseCase @Inject constructor(
    private val brandsRepository: BrandsRepository
) {
    operator fun invoke(): Flow<List<Brand>> = brandsRepository.getBrands()
}