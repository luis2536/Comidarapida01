package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.StreetFoodRepository
import com.example.model.CartItem
import com.example.model.PaymentMethod
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var repository: StreetFoodRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        repository = StreetFoodRepository(context)
        repository.seedInitialData()
    }

    @Test
    fun `read string from context matches app name`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("Street Food Pro", appName)
    }

    @Test
    fun `initial products and exchange rate are seeded`() {
        val products = repository.products.value
        val config = repository.config.value

        assertTrue("Products should not be empty", products.isNotEmpty())
        assertEquals(45.0, config.exchangeRate, 0.001)
    }

    @Test
    fun `processing a sale deducts inventory and registers financial balance`() {
        val shift = repository.startShift(
            shiftType = "Turno Nocturno (6:00 PM - 2:00 AM)",
            operatorName = "Andrea",
            initialCashUsd = 20.0,
            initialCashBs = 900.0
        )
        assertNotNull(shift)

        val product = repository.products.value.first() // Perro Caliente Sencillo
        val initialBreadStock = repository.inventory.value.find { it.id == "inv_pan_perro" }?.currentStock ?: 0.0

        val result = repository.processSale(
            customerName = "Carlos Developer",
            customerPhone = "+584121112233",
            cartItems = listOf(CartItem(product = product, quantity = 2)),
            paymentMethod = PaymentMethod.PAGO_MOVIL,
            reference = "7731",
            isDelivery = false,
            deliveryAddress = "",
            notes = "Sin cebolla"
        )

        assertTrue("Sale should succeed", result.isSuccess)
        val sale = result.getOrNull()
        assertNotNull(sale)
        assertEquals(3.00, sale!!.totalUsd, 0.001)
        assertEquals(135.0, sale.totalBs, 0.001) // 3.00 * 45.0

        // Check inventory deduction: 2 breads should be deducted
        val updatedBreadStock = repository.inventory.value.find { it.id == "inv_pan_perro" }?.currentStock ?: 0.0
        assertEquals(initialBreadStock - 2.0, updatedBreadStock, 0.001)

        // Check financial summary
        val summary = repository.getShiftSummary(shift.id)
        assertNotNull(summary)
        assertEquals(1, summary!!.totalSalesCount)
        assertEquals(3.00, summary.totalSalesUsd, 0.001)
        assertEquals(3.00, summary.pagoMovilUsd, 0.001)
    }
}
