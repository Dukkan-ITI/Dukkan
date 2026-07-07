package com.dukkan.data.mapper

import com.dukkan.GetCartQuery
import com.dukkan.fragment.MoneyFields
import com.dukkan.type.CurrencyCode
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.math.BigDecimal

class CartMappersTest {

    @Test
    fun `GetCartQuery Cart toDomainModel maps all fields correctly`() {
        // Arrange
        val mockCart = mockk<GetCartQuery.Cart> {
            every { id } returns "cart_1"
            every { checkoutUrl } returns "https://checkout.com"
            every { totalQuantity } returns 3
            every { discountCodes } returns listOf(
                mockk {
                    every { code } returns "OFF10"
                    every { applicable } returns true
                }
            )
            every { cost } returns mockk {
                every { subtotalAmount.moneyFields } returns mockMoney("100.0", "USD")
                every { totalAmount.moneyFields } returns mockMoney("110.0", "USD")
                every { totalTaxAmount?.moneyFields } returns mockMoney("10.0", "USD")
                every { checkoutChargeAmount.moneyFields } returns mockMoney("110.0", "USD")
            }
            
            val mockNode = mockk<GetCartQuery.Node> {
                every { id } returns "line_1"
                every { quantity } returns 2
                every { cost } returns mockk {
                    every { totalAmount.moneyFields } returns mockMoney("40.0", "USD")
                    every { amountPerQuantity.moneyFields } returns mockMoney("20.0", "USD")
                    every { compareAtAmountPerQuantity?.moneyFields } returns mockMoney("25.0", "USD")
                }
                every { merchandise.onProductVariant } returns mockk {
                    every { id } returns "var_1"
                    every { title } returns "Variant Title"
                    every { price.moneyFields } returns mockMoney("20.0", "USD")
                    every { compareAtPrice?.moneyFields } returns mockMoney("25.0", "USD")
                    every { availableForSale } returns true
                    every { quantityAvailable } returns 10
                    every { selectedOptions } returns listOf(mockk {
                        every { name } returns "Color"
                        every { value } returns "Red"
                    })
                    every { image } returns mockk {
                        every { url } returns "https://image.com"
                        every { altText } returns "Alt"
                    }
                    every { product } returns mockk {
                        every { id } returns "prod_1"
                        every { title } returns "Product Title"
                        every { vendor } returns "Vendor Name"
                    }
                }
            }
            
            every { lines.edges } returns listOf(mockk { every { node } returns mockNode })
        }

        // Act
        val domainCart = mockCart.toDomainModel()

        // Assert
        assertEquals("cart_1", domainCart.id)
        assertEquals("https://checkout.com", domainCart.checkoutUrl)
        assertEquals(3, domainCart.totalQuantity)
        assertEquals(1, domainCart.discountCodes.size)
        assertEquals("OFF10", domainCart.discountCodes[0].code)
        
        // Cost
        assertEquals(BigDecimal("100.0"), domainCart.cost.subtotalAmount.amount)
        assertEquals("USD", domainCart.cost.subtotalAmount.currencyCode)
        assertEquals(BigDecimal("110.0"), domainCart.cost.totalAmount.amount)
        assertEquals(BigDecimal("10.0"), domainCart.cost.totalTaxAmount?.amount)
        
        // Lines
        assertEquals(1, domainCart.lines.size)
        val line = domainCart.lines[0]
        assertEquals("line_1", line.id)
        assertEquals(2, line.quantity)
        assertEquals(BigDecimal("40.0"), line.cost.totalAmount.amount)
        
        // Merchandise
        val variant = line.merchandise
        assertEquals("var_1", variant.id)
        assertEquals("Variant Title", variant.title)
        assertEquals(BigDecimal("20.0"), variant.price.amount)
        assertEquals("Red", variant.selectedOptions[0].value)
        assertEquals("https://image.com", variant.image?.url)
        assertEquals("prod_1", variant.product.id)
        assertEquals("Vendor Name", variant.product.vendor)
    }

    @Test(expected = IllegalStateException::class)
    fun `toDomainModel throws when merchandise is not ProductVariant`() {
        val mockCart = mockk<GetCartQuery.Cart> {
            every { id } returns "c1"
            every { checkoutUrl } returns "url"
            every { totalQuantity } returns 1
            every { discountCodes } returns emptyList()
            every { cost } returns mockk {
                every { subtotalAmount.moneyFields } returns mockMoney("10.0", "USD")
                every { totalAmount.moneyFields } returns mockMoney("10.0", "USD")
                every { totalTaxAmount?.moneyFields } returns null
                every { checkoutChargeAmount.moneyFields } returns mockMoney("10.0", "USD")
            }
            
            val mockNode = mockk<GetCartQuery.Node> {
                every { id } returns "l1"
                every { quantity } returns 1
                every { cost } returns mockk {
                    every { totalAmount.moneyFields } returns mockMoney("10.0", "USD")
                    every { amountPerQuantity.moneyFields } returns mockMoney("10.0", "USD")
                    every { compareAtAmountPerQuantity?.moneyFields } returns null
                }
                every { merchandise.onProductVariant } returns null // Not a variant
            }
            every { lines.edges } returns listOf(mockk { every { node } returns mockNode })
        }

        mockCart.toDomainModel()
    }

    private fun mockMoney(amountStr: String, currency: String): MoneyFields {
        val mockMoneyFields = mockk<MoneyFields>()
        every { mockMoneyFields.amount } returns amountStr
        every { mockMoneyFields.currencyCode.rawValue } returns currency
        return mockMoneyFields
    }
}
