package com.dukkan.data.repository

import com.dukkan.data.source.local.data_source.favorites.FavoriteLocalDataSource
import com.dukkan.data.source.remote.data_source.favorite.FavoriteFirestoreDataSource
import com.dukkan.domain.model.FavoriteProduct
import com.dukkan.domain.usecase.product.GetProductByIdUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FavoriteRepositoryImplTest {

    @MockK
    lateinit var localDataSource: FavoriteLocalDataSource
    @MockK
    lateinit var firestoreDataSource: FavoriteFirestoreDataSource
    @MockK
    lateinit var firebaseAuth: FirebaseAuth
    @MockK
    lateinit var getProductByIdUseCase: GetProductByIdUseCase

    private lateinit var repository: FavoriteRepositoryImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        
        // Mock static Log to avoid crash
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.w(any(), any(), any()) } returns 0
        every { android.util.Log.e(any(), any(), any()) } returns 0

        repository = FavoriteRepositoryImpl(
            localDataSource,
            firestoreDataSource,
            firebaseAuth,
            getProductByIdUseCase
        )
    }

    @Test
    fun `addFavorite calls local and firestore data sources`() = runTest {
        // Arrange
        val product = FavoriteProduct("id", "title", "image", "10.0", "USD")
        val mockUser = mockk<FirebaseUser> { every { uid } returns "user_123" }
        every { firebaseAuth.currentUser } returns mockUser
        
        coEvery { localDataSource.addFavorite(any()) } returns Unit
        coEvery { firestoreDataSource.addFavoriteProductId(any(), any()) } returns Unit

        // Act
        repository.addFavorite(product)

        // Assert
        coVerify { localDataSource.addFavorite(any()) }
        coVerify { firestoreDataSource.addFavoriteProductId("id", "user_123") }
    }

    @Test
    fun `syncFavoritesOnLogin fetches remote and saves missing local favorites`() = runTest {
        // Arrange
        val userId = "user_123"
        val remoteIds = listOf("p1", "p2")
        coEvery { firestoreDataSource.getFavoriteProductIds(userId) } returns remoteIds
        every { localDataSource.getAllFavorites() } returns flowOf(emptyList())
        
        val product = mockk<com.dukkan.domain.model.Product>(relaxed = true) {
            every { id } returns "p1"
        }
        coEvery { getProductByIdUseCase("p1") } returns Result.success(product)
        coEvery { getProductByIdUseCase("p2") } returns Result.failure(Exception("Not found"))
        coEvery { localDataSource.addFavorite(any()) } returns Unit

        // Act
        repository.syncFavoritesOnLogin(userId)

        // Assert
        coVerify(exactly = 1) { localDataSource.addFavorite(any()) }
        coVerify { getProductByIdUseCase("p1") }
        coVerify { getProductByIdUseCase("p2") }
    }
}
