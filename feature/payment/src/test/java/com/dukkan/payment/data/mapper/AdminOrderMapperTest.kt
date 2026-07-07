package com.dukkan.payment.data.mapper

import com.dukkan.domain.model.Address
import com.dukkan.domain.model.Money
import com.dukkan.payment.domain.model.*
import com.dukkan.payment.admin.type.OrderCreateFinancialStatus
import com.dukkan.payment.admin.type.OrderCancelReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.math.BigDecimal
import com.apollographql.apollo.api.getOrNull

class AdminOrderMapperTest {

    @Test
    fun `OrderDraft toAdminInput maps all fields correctly`() {
        val address = Address(
            firstName = "John",
            lastName = "Doe",
            address1 = "123 Street",
            city = "Cairo",
            country = "Egypt",
            phone = "123456789"
        )
        val draft = OrderDraft(
            customerId = "cust_123",
            lineItems = listOf(
                OrderLineItemDraft(variantId = "var_1", quantity = 2)
            ),
            shippingLine = ShippingLineDraft(
                title = "Standard",
                code = "STD",
                price = Money(BigDecimal("10.0"), "EGP")
            ),
            currency = "EGP",
            shippingAddress = address
        )

        val input = draft.toAdminInput(OrderFinancialStatus.PAID)

        // Verification of construction
        assertNotNull(input.customer)
        assertNotNull(input.lineItems)
        assertNotNull(input.shippingLines)
        assertEquals("EGP", input.currency.getOrNull()?.rawValue)
        assertEquals(OrderCreateFinancialStatus.PAID, input.financialStatus.getOrNull())
    }

    @Test
    fun `OrderFinancialStatus toAdminFinancialStatus maps correctly`() {
        assertEquals(OrderCreateFinancialStatus.PENDING, OrderFinancialStatus.PENDING.toAdminFinancialStatus())
        assertEquals(OrderCreateFinancialStatus.PAID, OrderFinancialStatus.PAID.toAdminFinancialStatus())
    }

    @Test
    fun `OrderCancelReason toAdminCancelReason maps correctly`() {
        assertEquals(OrderCancelReason.CUSTOMER, com.dukkan.payment.domain.model.OrderCancelReason.CUSTOMER.toAdminCancelReason())
        assertEquals(OrderCancelReason.DECLINED, com.dukkan.payment.domain.model.OrderCancelReason.DECLINED.toAdminCancelReason())
        assertEquals(OrderCancelReason.FRAUD, com.dukkan.payment.domain.model.OrderCancelReason.FRAUD.toAdminCancelReason())
        assertEquals(OrderCancelReason.INVENTORY, com.dukkan.payment.domain.model.OrderCancelReason.INVENTORY.toAdminCancelReason())
        assertEquals(OrderCancelReason.OTHER, com.dukkan.payment.domain.model.OrderCancelReason.OTHER.toAdminCancelReason())
        assertEquals(OrderCancelReason.STAFF, com.dukkan.payment.domain.model.OrderCancelReason.STAFF.toAdminCancelReason())
    }
}
