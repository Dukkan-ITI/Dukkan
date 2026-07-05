package com.dukkan.data.source.remote.apollo

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.dukkan.CollectionProductsQuery
import com.dukkan.GetBrandsQuery
import com.dukkan.GetCollectionsQuery
import com.dukkan.GetProductsByVendorQuery
import com.dukkan.ProductQuery
import com.dukkan.ProductsQuery
import com.dukkan.data.mapper.toDomainModel
import com.dukkan.type.CountryCode
import com.dukkan.type.LanguageCode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductsDataSourceImpl @Inject constructor(private val apolloClient: ApolloClient) : ProductsDataSource {
    override suspend fun getProducts(
        first: Int,
        after: String?,
        country: String,
        language: String,
    ): ProductsQuery.Data? {
        val response = apolloClient.query(
            ProductsQuery(
                first = Optional.present(first),
                after = Optional.presentIfNotNull(after),
                country = CountryCode.safeValueOf(country),
                language = LanguageCode.safeValueOf(language),
            )
        ).execute()
        if (response.hasErrors()) {
            throw Exception(response.errors?.first()?.message ?: "Unknown GraphQL Error")
        }
        return response.data
    }

    override suspend fun getProductById(
        id: String,
        country: String,
        language: String,
    ): ProductQuery.Product? {
        val response = apolloClient.query(
            ProductQuery(
                id = id,
                country = CountryCode.safeValueOf(country),
                language = LanguageCode.safeValueOf(language),
            )
        ).execute()

        return response.data?.product
    }

    override suspend fun fetchCategories(): List<GetCollectionsQuery.Node> {
        val response = apolloClient.query(GetCollectionsQuery(first = 20)).execute()

        if (response.hasErrors()) {
            android.util.Log.e("CategoriesDebug", "GraphQL Errors: ${response.errors}")
        }

//        val excludedHandles = setOf("frontpage", "automated-collection", "hydrogen")

        return response.data?.collections?.edges
            ?.mapNotNull { it.node }
//            ?.filter { it.handle !in excludedHandles }
            ?: emptyList()
    }

    override suspend fun getProductsByCollectionHandle(
        handle: String,
        first: Int,
        after: String?,
        country: String,
        language: String,
    ): List<com.dukkan.domain.model.Product> {
        val response = apolloClient.query(
            CollectionProductsQuery(
                handle = handle,
                first = first,
                after = Optional.presentIfNotNull(after),
                country = CountryCode.safeValueOf(country),
                language = LanguageCode.safeValueOf(language),
            )
        ).execute()

        if (response.hasErrors()) {
            throw Exception(response.errors?.first()?.message ?: "Unknown GraphQL Error")
        }

        return response.data?.collection?.products?.edges
            ?.mapNotNull { it.node?.toDomainModel() }
            ?: emptyList()
    }

    override suspend fun fetchBrands(): List<GetBrandsQuery.Node> {
        val response = apolloClient.query(GetBrandsQuery(first = 100)).execute()

        if (response.hasErrors()) {
            android.util.Log.e("BrandsDebug", "GraphQL Errors: ${response.errors}")
        }

        return response.data?.products?.edges
            ?.mapNotNull { it.node }
            ?: emptyList()
    }

    override suspend fun getProductsByVendor(
        vendor: String,
        first: Int,
        after: String?,
        country: String,
        language: String,
    ): List<com.dukkan.domain.model.Product> {
        val safeVendor = vendor.replace("'", "\\'")
        val response = apolloClient.query(
            GetProductsByVendorQuery(
                query = "vendor:'$safeVendor'",
                first = first,
                after = Optional.presentIfNotNull(after),
                country = Optional.present(CountryCode.safeValueOf(country)),
                language = Optional.present(LanguageCode.safeValueOf(language)),
            )
        ).execute()

        if (response.hasErrors()) {
            throw Exception(response.errors?.first()?.message ?: "Unknown GraphQL Error")
        }

        return response.data?.products?.edges
            ?.mapNotNull { it.node?.toDomainModel() }
            ?: emptyList()
    }
}