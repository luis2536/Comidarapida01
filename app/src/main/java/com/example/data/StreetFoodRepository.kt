package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

class StreetFoodRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("street_food_pro_db", Context.MODE_PRIVATE)

    // Observable In-Memory States
    private val _activeShift = MutableStateFlow<Shift?>(null)
    val activeShift: StateFlow<Shift?> = _activeShift.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _sales = MutableStateFlow<List<Sale>>(emptyList())
    val sales: StateFlow<List<Sale>> = _sales.asStateFlow()

    private val _inventory = MutableStateFlow<List<InventoryItem>>(emptyList())
    val inventory: StateFlow<List<InventoryItem>> = _inventory.asStateFlow()

    private val _purchases = MutableStateFlow<List<Purchase>>(emptyList())
    val purchases: StateFlow<List<Purchase>> = _purchases.asStateFlow()

    private val _staff = MutableStateFlow<List<StaffMember>>(emptyList())
    val staff: StateFlow<List<StaffMember>> = _staff.asStateFlow()

    private val _staffPayments = MutableStateFlow<List<StaffPayment>>(emptyList())
    val staffPayments: StateFlow<List<StaffPayment>> = _staffPayments.asStateFlow()

    private val _deliveries = MutableStateFlow<List<DeliveryOrder>>(emptyList())
    val deliveries: StateFlow<List<DeliveryOrder>> = _deliveries.asStateFlow()

    private val _config = MutableStateFlow(AppConfig())
    val config: StateFlow<AppConfig> = _config.asStateFlow()

    private var nextTicketNumber = 101

    init {
        loadFromStorage()
    }

    // ==========================================
    // INITIALIZATION & PERSISTENCE
    // ==========================================
    private fun loadFromStorage() {
        val hasData = prefs.getBoolean("has_initialized_data", false)
        if (!hasData) {
            seedInitialData()
        } else {
            try {
                loadConfig()
                loadInventory()
                loadProducts()
                loadStaff()
                loadShift()
                loadSales()
                loadPurchases()
                loadStaffPayments()
                loadDeliveries()
                nextTicketNumber = prefs.getInt("next_ticket_number", 101)
            } catch (e: Exception) {
                // Fail-safe fallback to seeded data
                seedInitialData()
            }
        }
    }

    fun seedInitialData() {
        _config.value = AppConfig(
            exchangeRate = 45.0,
            adminPhone = "+584123456789",
            businessName = "STREET FOOD PRO",
            businessSlogan = "Sabor Callejero Premium",
            businessAddress = "Food Truck Park, Calle 72"
        )

        val defaultInventory = listOf(
            InventoryItem("inv_pan_perro", "Pan de Perro Caliente", "Panes", 60.0, 15.0, "unid", 0.20),
            InventoryItem("inv_pan_burger", "Pan de Hamburguesa Brioche", "Panes", 45.0, 10.0, "unid", 0.35),
            InventoryItem("inv_salchicha", "Salchicha Especial Frankfurt", "Carnes", 70.0, 20.0, "unid", 0.25),
            InventoryItem("inv_carne", "Carne Molida Hamburguesa 120g", "Carnes", 50.0, 12.0, "unid", 0.80),
            InventoryItem("inv_salsa_tomate", "Salsa de Tomate Kétchup", "Salsas", 2500.0, 500.0, "g", 0.002),
            InventoryItem("inv_mayonesa", "Mayonesa Especial de Ajo", "Salsas", 2500.0, 500.0, "g", 0.0025),
            InventoryItem("inv_mostaza", "Mostaza Tradicional", "Salsas", 1200.0, 300.0, "g", 0.002),
            InventoryItem("inv_papitas", "Papitas Ralladas Crujientes", "Complementos", 3000.0, 600.0, "g", 0.003),
            InventoryItem("inv_queso", "Queso Blanco Rallado", "Complementos", 2500.0, 500.0, "g", 0.004),
            InventoryItem("inv_tocineta", "Tocineta Crujiente Picada", "Complementos", 1500.0, 300.0, "g", 0.008),
            InventoryItem("inv_servilletas", "Servilletas Blancas", "Descartables", 600.0, 100.0, "unid", 0.005),
            InventoryItem("inv_vasos", "Vasos Térmicos Descartables", "Descartables", 200.0, 40.0, "unid", 0.05),
            InventoryItem("inv_refresco_250", "Refresco Lata 250ml (Variado)", "Bebidas", 40.0, 10.0, "unid", 0.45),
            InventoryItem("inv_refresco_500", "Refresco Botella 500ml", "Bebidas", 35.0, 10.0, "unid", 0.70),
            InventoryItem("inv_refresco_1l", "Refresco Familiar 1 Litro", "Bebidas", 20.0, 5.0, "unid", 1.20)
        )
        _inventory.value = defaultInventory

        val defaultProducts = listOf(
            Product(
                id = "prod_perro_sencillo",
                name = "Perro Caliente Sencillo",
                category = "Perros",
                priceUsd = 1.50,
                description = "Salchicha frankfurt, salsas, papitas crocantes y lluvia de queso blanco",
                badgeIcon = "HOT_DOG",
                recipe = listOf(
                    RecipeItem("inv_pan_perro", 1.0),
                    RecipeItem("inv_salchicha", 1.0),
                    RecipeItem("inv_salsa_tomate", 20.0),
                    RecipeItem("inv_mayonesa", 20.0),
                    RecipeItem("inv_papitas", 25.0),
                    RecipeItem("inv_queso", 20.0),
                    RecipeItem("inv_servilletas", 2.0)
                )
            ),
            Product(
                id = "prod_perro_jumbo",
                name = "Perro Caliente Jumbo Especial",
                category = "Perros",
                priceUsd = 2.50,
                description = "Doble salchicha frankfurt, queso fundido, tocineta, papitas y todas las salsas",
                badgeIcon = "HOT_DOG",
                recipe = listOf(
                    RecipeItem("inv_pan_perro", 1.0),
                    RecipeItem("inv_salchicha", 2.0),
                    RecipeItem("inv_tocineta", 25.0),
                    RecipeItem("inv_salsa_tomate", 30.0),
                    RecipeItem("inv_mayonesa", 30.0),
                    RecipeItem("inv_papitas", 35.0),
                    RecipeItem("inv_queso", 30.0),
                    RecipeItem("inv_servilletas", 3.0)
                )
            ),
            Product(
                id = "prod_burger_tradicional",
                name = "Hamburguesa Tradicional",
                category = "Hamburguesas",
                priceUsd = 3.50,
                description = "Carne a la plancha 120g, queso blanco rallado, vegetales, papitas y aderezos de la casa",
                badgeIcon = "BURGER",
                recipe = listOf(
                    RecipeItem("inv_pan_burger", 1.0),
                    RecipeItem("inv_carne", 1.0),
                    RecipeItem("inv_queso", 30.0),
                    RecipeItem("inv_papitas", 30.0),
                    RecipeItem("inv_salsa_tomate", 25.0),
                    RecipeItem("inv_mayonesa", 25.0),
                    RecipeItem("inv_servilletas", 3.0)
                )
            ),
            Product(
                id = "prod_burger_doble_especial",
                name = "Hamburguesa Doble Especial Street",
                category = "Hamburguesas",
                priceUsd = 5.00,
                description = "Doble carne artesanal, tocineta crocante, queso fundido, papitas al hilo",
                badgeIcon = "BURGER",
                recipe = listOf(
                    RecipeItem("inv_pan_burger", 1.0),
                    RecipeItem("inv_carne", 2.0),
                    RecipeItem("inv_tocineta", 40.0),
                    RecipeItem("inv_queso", 50.0),
                    RecipeItem("inv_papitas", 40.0),
                    RecipeItem("inv_salsa_tomate", 35.0),
                    RecipeItem("inv_mayonesa", 35.0),
                    RecipeItem("inv_servilletas", 4.0)
                )
            ),
            Product(
                id = "prod_combo_callejero",
                name = "Combo Callejero Pro",
                category = "Combos",
                priceUsd = 3.00,
                description = "1 Perro Caliente Sencillo + Refresco 250ml + Porción extra de papitas",
                badgeIcon = "COMBO",
                recipe = listOf(
                    RecipeItem("inv_pan_perro", 1.0),
                    RecipeItem("inv_salchicha", 1.0),
                    RecipeItem("inv_salsa_tomate", 20.0),
                    RecipeItem("inv_mayonesa", 20.0),
                    RecipeItem("inv_papitas", 50.0),
                    RecipeItem("inv_queso", 25.0),
                    RecipeItem("inv_refresco_250", 1.0),
                    RecipeItem("inv_servilletas", 3.0)
                )
            ),
            Product(
                id = "prod_combo_dupla_burger",
                name = "Combo Burger Dupla Amigos",
                category = "Combos",
                priceUsd = 7.50,
                description = "2 Hamburguesas Tradicionales completas + 2 Refrescos 500ml",
                badgeIcon = "COMBO",
                recipe = listOf(
                    RecipeItem("inv_pan_burger", 2.0),
                    RecipeItem("inv_carne", 2.0),
                    RecipeItem("inv_queso", 60.0),
                    RecipeItem("inv_papitas", 60.0),
                    RecipeItem("inv_salsa_tomate", 50.0),
                    RecipeItem("inv_mayonesa", 50.0),
                    RecipeItem("inv_refresco_500", 2.0),
                    RecipeItem("inv_servilletas", 6.0)
                )
            ),
            Product(
                id = "prod_salchipapa_especial",
                name = "Salchipapa Urbana Familiar",
                category = "Especiales",
                priceUsd = 3.25,
                description = "Cama generosa de papas crujientes, doble salchicha picada, tocineta, queso y salsas",
                badgeIcon = "FRIES",
                recipe = listOf(
                    RecipeItem("inv_salchicha", 2.0),
                    RecipeItem("inv_tocineta", 30.0),
                    RecipeItem("inv_papitas", 120.0),
                    RecipeItem("inv_queso", 45.0),
                    RecipeItem("inv_salsa_tomate", 30.0),
                    RecipeItem("inv_mayonesa", 30.0),
                    RecipeItem("inv_servilletas", 3.0)
                )
            ),
            Product(
                id = "prod_refresco_250",
                name = "Refresco Lata 250ml",
                category = "Bebidas",
                priceUsd = 0.75,
                description = "Lata bien fría (Coca Cola, Pepsi, Chinotto, Frescolita)",
                badgeIcon = "DRINK",
                recipe = listOf(
                    RecipeItem("inv_refresco_250", 1.0),
                    RecipeItem("inv_servilletas", 1.0)
                )
            ),
            Product(
                id = "prod_refresco_500",
                name = "Refresco Botella 500ml",
                category = "Bebidas",
                priceUsd = 1.25,
                description = "Botella 500ml retornable / plástica bien helada",
                badgeIcon = "DRINK",
                recipe = listOf(
                    RecipeItem("inv_refresco_500", 1.0),
                    RecipeItem("inv_servilletas", 1.0)
                )
            ),
            Product(
                id = "prod_refresco_1l",
                name = "Refresco Familiar 1 Litro",
                category = "Bebidas",
                priceUsd = 2.00,
                description = "Refresco tamaño grande para compartir con 2 vasos térmicos",
                badgeIcon = "DRINK",
                recipe = listOf(
                    RecipeItem("inv_refresco_1l", 1.0),
                    RecipeItem("inv_vasos", 2.0),
                    RecipeItem("inv_servilletas", 2.0)
                )
            )
        )
        _products.value = defaultProducts

        val defaultStaff = listOf(
            StaffMember("staff_1", "Carlos Mendoza", "Planchero Principal", 15.0, "+584141112233", true, true),
            StaffMember("staff_2", "Andrea Rivas", "Cajera / POS", 12.0, "+584242223344", true, true),
            StaffMember("staff_3", "José Gregorio", "Motorizado Delivery", 12.0, "+584123334455", true, true),
            StaffMember("staff_4", "Miguel Rojas", "Ayudante de Cocina", 10.0, "+584164445566", true, true)
        )
        _staff.value = defaultStaff

        // Create an initial active shift for immediate POS usability
        val initialShift = Shift(
            id = "shift_${System.currentTimeMillis()}",
            shiftType = "Turno Nocturno (6:00 PM - 2:00 AM)",
            operatorName = "Andrea Rivas",
            startTime = System.currentTimeMillis() - 7200000L, // 2 hours ago
            isActive = true,
            initialCashUsd = 20.0,
            initialCashBs = 900.0
        )
        _activeShift.value = initialShift

        _sales.value = emptyList()
        _purchases.value = emptyList()
        _staffPayments.value = emptyList()
        _deliveries.value = emptyList()

        nextTicketNumber = 101

        saveAll()
        prefs.edit().putBoolean("has_initialized_data", true).apply()
    }

    // ==========================================
    // SHIFT OPERATIONS
    // ==========================================
    fun startShift(shiftType: String, operatorName: String, initialCashUsd: Double, initialCashBs: Double): Shift {
        val newShift = Shift(
            id = "shift_${System.currentTimeMillis()}",
            shiftType = shiftType,
            operatorName = if (operatorName.isBlank()) "Operador en Turno" else operatorName.trim(),
            startTime = System.currentTimeMillis(),
            isActive = true,
            initialCashUsd = initialCashUsd,
            initialCashBs = initialCashBs
        )
        _activeShift.value = newShift
        saveShift()
        return newShift
    }

    fun closeShift() {
        val current = _activeShift.value ?: return
        val closed = current.copy(
            endTime = System.currentTimeMillis(),
            isActive = false
        )
        _activeShift.value = closed
        saveShift()
    }

    // ==========================================
    // POS SALES & STOCK DEDUCTION
    // ==========================================
    fun processSale(
        customerName: String,
        customerPhone: String,
        cartItems: List<CartItem>,
        paymentMethod: PaymentMethod,
        reference: String,
        isDelivery: Boolean,
        deliveryAddress: String,
        notes: String
    ): Result<Sale> {
        val shift = _activeShift.value
        if (shift == null || !shift.isActive) {
            return Result.failure(IllegalStateException("No hay un turno de ventas activo. Por favor, apertura un turno."))
        }
        if (cartItems.isEmpty()) {
            return Result.failure(IllegalArgumentException("El carrito de compras está vacío."))
        }
        if (paymentMethod.requiresRef && reference.trim().isBlank()) {
            return Result.failure(IllegalArgumentException("El método ${paymentMethod.title} requiere número de referencia bancaria."))
        }

        val rate = _config.value.exchangeRate
        val totalUsd = cartItems.sumOf { it.subtotalUsd }
        val totalBs = totalUsd * rate

        val saleItems = cartItems.map {
            SaleItem(
                productId = it.product.id,
                productName = it.product.name,
                quantity = it.quantity,
                unitPriceUsd = it.product.priceUsd,
                subtotalUsd = it.subtotalUsd
            )
        }

        val saleId = "sale_${System.currentTimeMillis()}"
        val ticket = nextTicketNumber++
        prefs.edit().putInt("next_ticket_number", nextTicketNumber).apply()

        val sale = Sale(
            id = saleId,
            ticketNumber = ticket,
            shiftId = shift.id,
            timestamp = System.currentTimeMillis(),
            customerName = if (customerName.isBlank()) "Cliente Mostrador #$ticket" else customerName.trim(),
            customerPhone = customerPhone.trim(),
            items = saleItems,
            totalUsd = totalUsd,
            totalBs = totalBs,
            exchangeRate = rate,
            paymentMethod = paymentMethod,
            reference = reference.trim(),
            isDelivery = isDelivery,
            deliveryAddress = deliveryAddress.trim(),
            notes = notes.trim()
        )

        // Deduct inventory items according to product recipes
        val currentInv = _inventory.value.toMutableList()
        for (cartItem in cartItems) {
            for (recipeItem in cartItem.product.recipe) {
                val index = currentInv.indexOfFirst { it.id == recipeItem.inventoryItemId }
                if (index != -1) {
                    val inv = currentInv[index]
                    val usedAmount = recipeItem.quantityUsed * cartItem.quantity
                    val newStock = (inv.currentStock - usedAmount).coerceAtLeast(0.0)
                    currentInv[index] = inv.copy(currentStock = newStock)
                }
            }
        }
        _inventory.value = currentInv
        saveInventory()

        // Append to sales
        val updatedSales = _sales.value.toMutableList().apply { add(0, sale) }
        _sales.value = updatedSales
        saveSales()

        // If delivery, create delivery order
        if (isDelivery) {
            val orderSummary = cartItems.joinToString(", ") { "${it.quantity}x ${it.product.name}" }
            val delivery = DeliveryOrder(
                id = "del_${System.currentTimeMillis()}",
                saleId = sale.id,
                customerName = sale.customerName,
                customerPhone = sale.customerPhone,
                address = if (deliveryAddress.isBlank()) "Dirección Principal delivery" else deliveryAddress,
                lat = 10.4806 + (Math.random() - 0.5) * 0.02,
                lng = -66.9036 + (Math.random() - 0.5) * 0.02,
                status = "Pendiente",
                progress = 0f,
                driverName = "José Gregorio (Motorizado)",
                orderSummary = orderSummary,
                totalUsd = totalUsd
            )
            val updatedDeliveries = _deliveries.value.toMutableList().apply { add(0, delivery) }
            _deliveries.value = updatedDeliveries
            saveDeliveries()
        }

        return Result.success(sale)
    }

    // ==========================================
    // INVENTORY & PURCHASES
    // ==========================================
    fun registerPurchase(inventoryItemId: String, quantityAdded: Double, costUsd: Double, supplier: String) {
        val currentInv = _inventory.value.toMutableList()
        val index = currentInv.indexOfFirst { it.id == inventoryItemId }
        val item = if (index != -1) currentInv[index] else null

        if (item != null) {
            currentInv[index] = item.copy(currentStock = item.currentStock + quantityAdded)
            _inventory.value = currentInv
            saveInventory()

            val purchase = Purchase(
                id = "pur_${System.currentTimeMillis()}",
                shiftId = _activeShift.value?.id ?: "global",
                timestamp = System.currentTimeMillis(),
                inventoryItemId = inventoryItemId,
                inventoryItemName = item.name,
                quantityAdded = quantityAdded,
                costUsd = costUsd,
                supplier = if (supplier.isBlank()) "Distribuidora de Alimentos" else supplier.trim()
            )
            val updatedPurchases = _purchases.value.toMutableList().apply { add(0, purchase) }
            _purchases.value = updatedPurchases
            savePurchases()
        }
    }

    fun addNewInventoryItem(name: String, category: String, initialStock: Double, minStock: Double, unit: String, costUsd: Double) {
        val newItem = InventoryItem(
            id = "inv_${System.currentTimeMillis()}",
            name = name.trim(),
            category = category,
            currentStock = initialStock,
            minStockAlert = minStock,
            unit = unit,
            costPerUnitUsd = costUsd
        )
        val updated = _inventory.value.toMutableList().apply { add(newItem) }
        _inventory.value = updated
        saveInventory()
    }

    // ==========================================
    // STAFF & PAYMENTS
    // ==========================================
    fun toggleStaffAttendance(staffId: String) {
        val updated = _staff.value.map {
            if (it.id == staffId) it.copy(isPresent = !it.isPresent) else it
        }
        _staff.value = updated
        saveStaff()
    }

    fun addStaffMember(name: String, role: String, dailyRateUsd: Double, phone: String) {
        val member = StaffMember(
            id = "staff_${System.currentTimeMillis()}",
            name = name.trim(),
            role = role.trim(),
            dailyRateUsd = dailyRateUsd,
            phone = phone.trim(),
            isActive = true,
            isPresent = true
        )
        val updated = _staff.value.toMutableList().apply { add(member) }
        _staff.value = updated
        saveStaff()
    }

    fun payStaff(staffId: String, amountUsd: Double, concept: String) {
        val member = _staff.value.find { it.id == staffId } ?: return
        val payment = StaffPayment(
            id = "pay_${System.currentTimeMillis()}",
            shiftId = _activeShift.value?.id ?: "global",
            staffId = staffId,
            staffName = member.name,
            amountUsd = amountUsd,
            timestamp = System.currentTimeMillis(),
            concept = if (concept.isBlank()) "Jornada laboral del día" else concept.trim()
        )
        val updated = _staffPayments.value.toMutableList().apply { add(0, payment) }
        _staffPayments.value = updated
        saveStaffPayments()
    }

    // ==========================================
    // DELIVERIES & GPS SIMULATION
    // ==========================================
    fun updateDeliveryStatus(deliveryId: String, status: String, progress: Float) {
        val updated = _deliveries.value.map {
            if (it.id == deliveryId) it.copy(status = status, progress = progress) else it
        }
        _deliveries.value = updated
        saveDeliveries()
    }

    fun advanceDeliveryStep(deliveryId: String) {
        val delivery = _deliveries.value.find { it.id == deliveryId } ?: return
        val (newStatus, newProgress) = when {
            delivery.progress < 0.25f -> Pair("En camino", 0.40f)
            delivery.progress < 0.70f -> Pair("En destino", 0.85f)
            else -> Pair("Entregado", 1.0f)
        }
        updateDeliveryStatus(deliveryId, newStatus, newProgress)
    }

    fun assignCustomerGps(deliveryId: String, lat: Double, lng: Double, address: String) {
        val updated = _deliveries.value.map {
            if (it.id == deliveryId) it.copy(lat = lat, lng = lng, address = address) else it
        }
        _deliveries.value = updated
        saveDeliveries()
    }

    // ==========================================
    // CONFIGURATION & RATE
    // ==========================================
    fun updateExchangeRate(newRate: Double) {
        _config.value = _config.value.copy(exchangeRate = newRate)
        saveConfig()
    }

    fun updateConfig(newConfig: AppConfig) {
        _config.value = newConfig
        saveConfig()
    }

    // ==========================================
    // FINANCIAL SUMMARY FOR CURRENT SHIFT
    // ==========================================
    data class ShiftFinancialSummary(
        val shift: Shift,
        val totalSalesCount: Int,
        val totalSalesUsd: Double,
        val totalSalesBs: Double,
        val puntoDeVentaUsd: Double,
        val pagoMovilUsd: Double,
        val efectivoUsd: Double,
        val efectivoBsUsd: Double,
        val purchasesExpensesUsd: Double,
        val staffPayoutsUsd: Double,
        val totalExpensesUsd: Double,
        val netProfitUsd: Double
    )

    fun getShiftSummary(shiftId: String? = _activeShift.value?.id): ShiftFinancialSummary? {
        val s = _activeShift.value ?: return null
        val targetShiftId = shiftId ?: s.id
        val shiftSales = _sales.value.filter { it.shiftId == targetShiftId }
        val shiftPurchases = _purchases.value.filter { it.shiftId == targetShiftId }
        val shiftStaffPayments = _staffPayments.value.filter { it.shiftId == targetShiftId }

        val totalSalesUsd = shiftSales.sumOf { it.totalUsd }
        val totalSalesBs = shiftSales.sumOf { it.totalBs }

        val puntoUsd = shiftSales.filter { it.paymentMethod == PaymentMethod.PUNTO_DE_VENTA }.sumOf { it.totalUsd }
        val pagoMovilUsd = shiftSales.filter { it.paymentMethod == PaymentMethod.PAGO_MOVIL }.sumOf { it.totalUsd }
        val efecUsd = shiftSales.filter { it.paymentMethod == PaymentMethod.EFECTIVO_USD }.sumOf { it.totalUsd }
        val efecBsUsd = shiftSales.filter { it.paymentMethod == PaymentMethod.EFECTIVO_BS }.sumOf { it.totalUsd }

        val purchasesExp = shiftPurchases.sumOf { it.costUsd }
        val staffExp = shiftStaffPayments.sumOf { it.amountUsd }
        val totalExp = purchasesExp + staffExp
        val netProfit = totalSalesUsd - totalExp

        return ShiftFinancialSummary(
            shift = s,
            totalSalesCount = shiftSales.size,
            totalSalesUsd = totalSalesUsd,
            totalSalesBs = totalSalesBs,
            puntoDeVentaUsd = puntoUsd,
            pagoMovilUsd = pagoMovilUsd,
            efectivoUsd = efecUsd,
            efectivoBsUsd = efecBsUsd,
            purchasesExpensesUsd = purchasesExp,
            staffPayoutsUsd = staffExp,
            totalExpensesUsd = totalExp,
            netProfitUsd = netProfit
        )
    }

    // ==========================================
    // WHATSAPP URL GENERATORS
    // ==========================================
    fun generateWhatsAppTicketUrl(sale: Sale): String {
        val cfg = _config.value
        val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date(sale.timestamp))
        val shiftStr = _activeShift.value?.shiftType ?: "Turno General"

        val refLine = if (sale.reference.isNotBlank()) "🔢 *Ref Bancaria:* ${sale.reference}" else ""
        val detalleItems = sale.items.joinToString("\n") {
            "  • ${it.quantity}x ${it.productName} ($${String.format(Locale.US, "%.2f", it.unitPriceUsd)}) -> $${String.format(Locale.US, "%.2f", it.subtotalUsd)}"
        }
        val deliveryInfo = if (sale.isDelivery) {
            "📍 *Dirección de Entrega:* ${sale.deliveryAddress}"
        } else {
            "🏠 *Entrega:* En Mostrador / Local"
        }

        var message = cfg.receiptTemplate
            .replace("{negocio}", cfg.businessName)
            .replace("{ticket}", sale.ticketNumber.toString())
            .replace("{fecha}", dateStr)
            .replace("{turno}", shiftStr)
            .replace("{cliente}", sale.customerName)
            .replace("{metodo}", sale.paymentMethod.title)
            .replace("{ref_line}", refLine)
            .replace("{detalle_items}", detalleItems)
            .replace("{total_usd}", String.format(Locale.US, "%.2f", sale.totalUsd))
            .replace("{total_bs}", String.format(Locale.US, "%.2f", sale.totalBs))
            .replace("{tasa}", String.format(Locale.US, "%.2f", sale.exchangeRate))
            .replace("{modalidad}", if (sale.isDelivery) "🛵 Delivery Express" else "🍽️ Para Llevar / Mesa")
            .replace("{delivery_info}", deliveryInfo)

        val cleanPhone = sale.customerPhone.replace(Regex("[^0-9]"), "")
        val encodedMsg = URLEncoder.encode(message.trim(), "UTF-8")
        return if (cleanPhone.isNotBlank()) {
            "https://wa.me/$cleanPhone?text=$encodedMsg"
        } else {
            "https://wa.me/?text=$encodedMsg"
        }
    }

    fun generateWhatsAppShiftReportUrl(): String {
        val summary = getShiftSummary()
        val cfg = _config.value
        val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date(summary?.shift?.startTime ?: System.currentTimeMillis()))

        val message = if (summary != null) {
            cfg.shiftReportTemplate
                .replace("{negocio}", cfg.businessName)
                .replace("{turno}", summary.shift.shiftType)
                .replace("{operador}", summary.shift.operatorName)
                .replace("{fecha}", dateStr)
                .replace("{total_pedidos}", summary.totalSalesCount.toString())
                .replace("{ventas_usd}", String.format(Locale.US, "%.2f", summary.totalSalesUsd))
                .replace("{ventas_bs}", String.format(Locale.US, "%.2f", summary.totalSalesBs))
                .replace("{total_punto}", String.format(Locale.US, "%.2f", summary.puntoDeVentaUsd))
                .replace("{total_pagomovil}", String.format(Locale.US, "%.2f", summary.pagoMovilUsd))
                .replace("{total_efectivo_usd}", String.format(Locale.US, "%.2f", summary.efectivoUsd))
                .replace("{total_efectivo_bs}", String.format(Locale.US, "%.2f", summary.efectivoBsUsd))
                .replace("{gastos_compras}", String.format(Locale.US, "%.2f", summary.purchasesExpensesUsd))
                .replace("{gastos_personal}", String.format(Locale.US, "%.2f", summary.staffPayoutsUsd))
                .replace("{total_gastos}", String.format(Locale.US, "%.2f", summary.totalExpensesUsd))
                .replace("{utilidad_neta}", String.format(Locale.US, "%.2f", summary.netProfitUsd))
        } else {
            "No hay resumen financiero activo disponible."
        }

        val cleanAdminPhone = cfg.adminPhone.replace(Regex("[^0-9]"), "")
        val encodedMsg = URLEncoder.encode(message.trim(), "UTF-8")
        return if (cleanAdminPhone.isNotBlank()) {
            "https://wa.me/$cleanAdminPhone?text=$encodedMsg"
        } else {
            "https://wa.me/?text=$encodedMsg"
        }
    }

    fun generateWhatsAppDeliveryNoticeUrl(delivery: DeliveryOrder): String {
        val msg = """
🛵💨 *¡HOLA ${delivery.customerName.uppercase()}! TU PEDIDO ESTÁ EN CAMINO*
------------------------------------------
🍔 *Detalle:* ${delivery.orderSummary}
💵 *Monto:* $${String.format(Locale.US, "%.2f", delivery.totalUsd)}
📍 *Destino:* ${delivery.address}
🛵 *Motorizado:* ${delivery.driverName}
⏳ *Estatus:* ${delivery.status}

¡Prepárate para recibir el mejor sabor callejero de Street Food Pro! 🔥
        """.trimIndent()

        val cleanPhone = delivery.customerPhone.replace(Regex("[^0-9]"), "")
        val encoded = URLEncoder.encode(msg, "UTF-8")
        return if (cleanPhone.isNotBlank()) "https://wa.me/$cleanPhone?text=$encoded" else "https://wa.me/?text=$encoded"
    }

    // ==========================================
    // EXPORT TO EXCEL / CSV & FORMAL AUDIT TEXT
    // ==========================================
    fun exportSalesCsv(): String {
        val sb = StringBuilder()
        sb.append("Ticket,Fecha,Cliente,Telefono,MetodoPago,Referencia,TotalUSD,Tasa,TotalBS,Delivery\n")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        for (sale in _sales.value) {
            sb.append("${sale.ticketNumber},")
            sb.append("\"${sdf.format(Date(sale.timestamp))}\",")
            sb.append("\"${sale.customerName}\",")
            sb.append("\"${sale.customerPhone}\",")
            sb.append("\"${sale.paymentMethod.title}\",")
            sb.append("\"${sale.reference}\",")
            sb.append("${sale.totalUsd},")
            sb.append("${sale.exchangeRate},")
            sb.append("${sale.totalBs},")
            sb.append(if (sale.isDelivery) "SI" else "NO")
            sb.append("\n")
        }
        return sb.toString()
    }

    fun generateFormalAuditReport(): String {
        val summary = getShiftSummary()
        val cfg = _config.value
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US)
        val now = sdf.format(Date())

        val sb = StringBuilder()
        sb.append("====================================================\n")
        sb.append("        STREET FOOD PRO - AUDITORÍA FORMAL DE CIERRE\n")
        sb.append("====================================================\n")
        sb.append("ESTABLECIMIENTO: ${cfg.businessName}\n")
        sb.append("DIRECCIÓN: ${cfg.businessAddress}\n")
        sb.append("FECHA DE AUDITORÍA: $now\n")
        sb.append("TURNO: ${summary?.shift?.shiftType ?: "N/A"}\n")
        sb.append("OPERADOR EN CAJA: ${summary?.shift?.operatorName ?: "N/A"}\n")
        sb.append("TASA DE CAMBIO BCV/DÍA: ${cfg.exchangeRate} Bs./USD\n")
        sb.append("----------------------------------------------------\n")
        sb.append("1. RESULTADOS DE OPERACIÓN Y VENTAS\n")
        sb.append("----------------------------------------------------\n")
        sb.append("Cantidad de Tickets Procesados: ${summary?.totalSalesCount ?: 0}\n")
        sb.append("Ventas Brutas Totales (USD):    $${String.format(Locale.US, "%.2f", summary?.totalSalesUsd ?: 0.0)}\n")
        sb.append("Ventas Brutas Totales (Bs):     ${String.format(Locale.US, "%.2f", summary?.totalSalesBs ?: 0.0)} Bs.\n\n")
        sb.append("DESGLOSE POR CANAL DE PAGO:\n")
        sb.append("  • Punto de Venta (Tarjeta):   $${String.format(Locale.US, "%.2f", summary?.puntoDeVentaUsd ?: 0.0)}\n")
        sb.append("  • Pago Móvil Interbancario:   $${String.format(Locale.US, "%.2f", summary?.pagoMovilUsd ?: 0.0)}\n")
        sb.append("  • Efectivo Divisas USD:       $${String.format(Locale.US, "%.2f", summary?.efectivoUsd ?: 0.0)}\n")
        sb.append("  • Efectivo Bolívares Bs:      $${String.format(Locale.US, "%.2f", summary?.efectivoBsUsd ?: 0.0)}\n\n")
        sb.append("----------------------------------------------------\n")
        sb.append("2. EGRESOS Y COSTOS OPERACIONALES\n")
        sb.append("----------------------------------------------------\n")
        sb.append("Compras e Ingresos de Insumos:  $${String.format(Locale.US, "%.2f", summary?.purchasesExpensesUsd ?: 0.0)}\n")
        sb.append("Pagos / Anticipos a Personal:   $${String.format(Locale.US, "%.2f", summary?.staffPayoutsUsd ?: 0.0)}\n")
        sb.append("Total Egresos Registrados:      $${String.format(Locale.US, "%.2f", summary?.totalExpensesUsd ?: 0.0)}\n\n")
        sb.append("----------------------------------------------------\n")
        sb.append("3. BALANCE NETO FINANCIERO\n")
        sb.append("----------------------------------------------------\n")
        sb.append("UTILIDAD NETA OPERATIVA:        $${String.format(Locale.US, "%.2f", summary?.netProfitUsd ?: 0.0)} USD\n")
        sb.append("====================================================\n")
        sb.append("Firma Responsable Turno: ___________________________\n")
        sb.append("Firma Administrador:     ___________________________\n")
        return sb.toString()
    }

    // ==========================================
    // PERSISTENCE HELPERS
    // ==========================================
    private fun saveAll() {
        saveConfig()
        saveInventory()
        saveProducts()
        saveStaff()
        saveShift()
        saveSales()
        savePurchases()
        saveStaffPayments()
        saveDeliveries()
    }

    private fun saveConfig() {
        val c = _config.value
        val json = JSONObject().apply {
            put("exchangeRate", c.exchangeRate)
            put("adminPhone", c.adminPhone)
            put("businessName", c.businessName)
            put("businessSlogan", c.businessSlogan)
            put("businessAddress", c.businessAddress)
            put("receiptTemplate", c.receiptTemplate)
            put("shiftReportTemplate", c.shiftReportTemplate)
        }
        prefs.edit().putString("config_json", json.toString()).apply()
    }

    private fun loadConfig() {
        val str = prefs.getString("config_json", null) ?: return
        val json = JSONObject(str)
        _config.value = AppConfig(
            exchangeRate = json.optDouble("exchangeRate", 45.0),
            adminPhone = json.optString("adminPhone", "+584120000000"),
            businessName = json.optString("businessName", "STREET FOOD PRO"),
            businessSlogan = json.optString("businessSlogan", "Sabor Callejero Premium"),
            businessAddress = json.optString("businessAddress", "Food Truck Park"),
            receiptTemplate = json.optString("receiptTemplate", _config.value.receiptTemplate),
            shiftReportTemplate = json.optString("shiftReportTemplate", _config.value.shiftReportTemplate)
        )
    }

    private fun saveInventory() {
        val arr = JSONArray()
        for (i in _inventory.value) {
            val obj = JSONObject().apply {
                put("id", i.id)
                put("name", i.name)
                put("category", i.category)
                put("currentStock", i.currentStock)
                put("minStockAlert", i.minStockAlert)
                put("unit", i.unit)
                put("costPerUnitUsd", i.costPerUnitUsd)
            }
            arr.put(obj)
        }
        prefs.edit().putString("inventory_json", arr.toString()).apply()
    }

    private fun loadInventory() {
        val str = prefs.getString("inventory_json", null) ?: return
        val arr = JSONArray(str)
        val list = mutableListOf<InventoryItem>()
        for (idx in 0 until arr.length()) {
            val obj = arr.getJSONObject(idx)
            list.add(
                InventoryItem(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    category = obj.getString("category"),
                    currentStock = obj.getDouble("currentStock"),
                    minStockAlert = obj.getDouble("minStockAlert"),
                    unit = obj.getString("unit"),
                    costPerUnitUsd = obj.optDouble("costPerUnitUsd", 0.0)
                )
            )
        }
        _inventory.value = list
    }

    private fun saveProducts() {
        val arr = JSONArray()
        for (p in _products.value) {
            val obj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("category", p.category)
                put("priceUsd", p.priceUsd)
                put("description", p.description)
                put("badgeIcon", p.badgeIcon)
                val recArr = JSONArray()
                for (r in p.recipe) {
                    val rObj = JSONObject().apply {
                        put("invId", r.inventoryItemId)
                        put("qty", r.quantityUsed)
                    }
                    recArr.put(rObj)
                }
                put("recipe", recArr)
            }
            arr.put(obj)
        }
        prefs.edit().putString("products_json", arr.toString()).apply()
    }

    private fun loadProducts() {
        val str = prefs.getString("products_json", null) ?: return
        val arr = JSONArray(str)
        val list = mutableListOf<Product>()
        for (idx in 0 until arr.length()) {
            val obj = arr.getJSONObject(idx)
            val recList = mutableListOf<RecipeItem>()
            val recArr = obj.optJSONArray("recipe")
            if (recArr != null) {
                for (rIdx in 0 until recArr.length()) {
                    val rObj = recArr.getJSONObject(rIdx)
                    recList.add(RecipeItem(rObj.getString("invId"), rObj.getDouble("qty")))
                }
            }
            list.add(
                Product(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    category = obj.getString("category"),
                    priceUsd = obj.getDouble("priceUsd"),
                    description = obj.optString("description", ""),
                    badgeIcon = obj.optString("badgeIcon", "BURGER"),
                    recipe = recList
                )
            )
        }
        _products.value = list
    }

    private fun saveStaff() {
        val arr = JSONArray()
        for (s in _staff.value) {
            val obj = JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                put("role", s.role)
                put("dailyRateUsd", s.dailyRateUsd)
                put("phone", s.phone)
                put("isActive", s.isActive)
                put("isPresent", s.isPresent)
            }
            arr.put(obj)
        }
        prefs.edit().putString("staff_json", arr.toString()).apply()
    }

    private fun loadStaff() {
        val str = prefs.getString("staff_json", null) ?: return
        val arr = JSONArray(str)
        val list = mutableListOf<StaffMember>()
        for (idx in 0 until arr.length()) {
            val obj = arr.getJSONObject(idx)
            list.add(
                StaffMember(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    role = obj.getString("role"),
                    dailyRateUsd = obj.getDouble("dailyRateUsd"),
                    phone = obj.optString("phone", ""),
                    isActive = obj.optBoolean("isActive", true),
                    isPresent = obj.optBoolean("isPresent", true)
                )
            )
        }
        _staff.value = list
    }

    private fun saveShift() {
        val s = _activeShift.value
        if (s == null) {
            prefs.edit().remove("active_shift_json").apply()
            return
        }
        val obj = JSONObject().apply {
            put("id", s.id)
            put("shiftType", s.shiftType)
            put("operatorName", s.operatorName)
            put("startTime", s.startTime)
            if (s.endTime != null) put("endTime", s.endTime)
            put("isActive", s.isActive)
            put("initialCashUsd", s.initialCashUsd)
            put("initialCashBs", s.initialCashBs)
        }
        prefs.edit().putString("active_shift_json", obj.toString()).apply()
    }

    private fun loadShift() {
        val str = prefs.getString("active_shift_json", null) ?: return
        val obj = JSONObject(str)
        _activeShift.value = Shift(
            id = obj.getString("id"),
            shiftType = obj.getString("shiftType"),
            operatorName = obj.getString("operatorName"),
            startTime = obj.getLong("startTime"),
            endTime = if (obj.has("endTime")) obj.getLong("endTime") else null,
            isActive = obj.optBoolean("isActive", true),
            initialCashUsd = obj.optDouble("initialCashUsd", 0.0),
            initialCashBs = obj.optDouble("initialCashBs", 0.0)
        )
    }

    private fun saveSales() {
        val arr = JSONArray()
        for (s in _sales.value) {
            val obj = JSONObject().apply {
                put("id", s.id)
                put("ticketNumber", s.ticketNumber)
                put("shiftId", s.shiftId)
                put("timestamp", s.timestamp)
                put("customerName", s.customerName)
                put("customerPhone", s.customerPhone)
                put("totalUsd", s.totalUsd)
                put("totalBs", s.totalBs)
                put("exchangeRate", s.exchangeRate)
                put("paymentMethod", s.paymentMethod.name)
                put("reference", s.reference)
                put("isDelivery", s.isDelivery)
                put("deliveryAddress", s.deliveryAddress)
                put("notes", s.notes)
                val itemsArr = JSONArray()
                for (it in s.items) {
                    val itObj = JSONObject().apply {
                        put("productId", it.productId)
                        put("productName", it.productName)
                        put("quantity", it.quantity)
                        put("unitPriceUsd", it.unitPriceUsd)
                        put("subtotalUsd", it.subtotalUsd)
                    }
                    itemsArr.put(itObj)
                }
                put("items", itemsArr)
            }
            arr.put(obj)
        }
        prefs.edit().putString("sales_json", arr.toString()).apply()
    }

    private fun loadSales() {
        val str = prefs.getString("sales_json", null) ?: return
        val arr = JSONArray(str)
        val list = mutableListOf<Sale>()
        for (idx in 0 until arr.length()) {
            val obj = arr.getJSONObject(idx)
            val itemsArr = obj.optJSONArray("items") ?: JSONArray()
            val items = mutableListOf<SaleItem>()
            for (i in 0 until itemsArr.length()) {
                val itObj = itemsArr.getJSONObject(i)
                items.add(
                    SaleItem(
                        productId = itObj.getString("productId"),
                        productName = itObj.getString("productName"),
                        quantity = itObj.getInt("quantity"),
                        unitPriceUsd = itObj.getDouble("unitPriceUsd"),
                        subtotalUsd = itObj.getDouble("subtotalUsd")
                    )
                )
            }
            val pmStr = obj.optString("paymentMethod", PaymentMethod.EFECTIVO_USD.name)
            val pm = try { PaymentMethod.valueOf(pmStr) } catch (e: Exception) { PaymentMethod.EFECTIVO_USD }
            list.add(
                Sale(
                    id = obj.getString("id"),
                    ticketNumber = obj.getInt("ticketNumber"),
                    shiftId = obj.getString("shiftId"),
                    timestamp = obj.getLong("timestamp"),
                    customerName = obj.getString("customerName"),
                    customerPhone = obj.optString("customerPhone", ""),
                    items = items,
                    totalUsd = obj.getDouble("totalUsd"),
                    totalBs = obj.getDouble("totalBs"),
                    exchangeRate = obj.optDouble("exchangeRate", 45.0),
                    paymentMethod = pm,
                    reference = obj.optString("reference", ""),
                    isDelivery = obj.optBoolean("isDelivery", false),
                    deliveryAddress = obj.optString("deliveryAddress", ""),
                    notes = obj.optString("notes", "")
                )
            )
        }
        _sales.value = list
    }

    private fun savePurchases() {
        val arr = JSONArray()
        for (p in _purchases.value) {
            val obj = JSONObject().apply {
                put("id", p.id)
                put("shiftId", p.shiftId)
                put("timestamp", p.timestamp)
                put("inventoryItemId", p.inventoryItemId)
                put("inventoryItemName", p.inventoryItemName)
                put("quantityAdded", p.quantityAdded)
                put("costUsd", p.costUsd)
                put("supplier", p.supplier)
            }
            arr.put(obj)
        }
        prefs.edit().putString("purchases_json", arr.toString()).apply()
    }

    private fun loadPurchases() {
        val str = prefs.getString("purchases_json", null) ?: return
        val arr = JSONArray(str)
        val list = mutableListOf<Purchase>()
        for (idx in 0 until arr.length()) {
            val obj = arr.getJSONObject(idx)
            list.add(
                Purchase(
                    id = obj.getString("id"),
                    shiftId = obj.getString("shiftId"),
                    timestamp = obj.getLong("timestamp"),
                    inventoryItemId = obj.getString("inventoryItemId"),
                    inventoryItemName = obj.getString("inventoryItemName"),
                    quantityAdded = obj.getDouble("quantityAdded"),
                    costUsd = obj.getDouble("costUsd"),
                    supplier = obj.optString("supplier", "")
                )
            )
        }
        _purchases.value = list
    }

    private fun saveStaffPayments() {
        val arr = JSONArray()
        for (p in _staffPayments.value) {
            val obj = JSONObject().apply {
                put("id", p.id)
                put("shiftId", p.shiftId)
                put("staffId", p.staffId)
                put("staffName", p.staffName)
                put("amountUsd", p.amountUsd)
                put("timestamp", p.timestamp)
                put("concept", p.concept)
            }
            arr.put(obj)
        }
        prefs.edit().putString("staff_payments_json", arr.toString()).apply()
    }

    private fun loadStaffPayments() {
        val str = prefs.getString("staff_payments_json", null) ?: return
        val arr = JSONArray(str)
        val list = mutableListOf<StaffPayment>()
        for (idx in 0 until arr.length()) {
            val obj = arr.getJSONObject(idx)
            list.add(
                StaffPayment(
                    id = obj.getString("id"),
                    shiftId = obj.getString("shiftId"),
                    staffId = obj.getString("staffId"),
                    staffName = obj.getString("staffName"),
                    amountUsd = obj.getDouble("amountUsd"),
                    timestamp = obj.getLong("timestamp"),
                    concept = obj.optString("concept", "")
                )
            )
        }
        _staffPayments.value = list
    }

    private fun saveDeliveries() {
        val arr = JSONArray()
        for (d in _deliveries.value) {
            val obj = JSONObject().apply {
                put("id", d.id)
                put("saleId", d.saleId)
                put("customerName", d.customerName)
                put("customerPhone", d.customerPhone)
                put("address", d.address)
                put("lat", d.lat)
                put("lng", d.lng)
                put("status", d.status)
                put("progress", d.progress.toDouble())
                put("driverName", d.driverName)
                put("orderSummary", d.orderSummary)
                put("totalUsd", d.totalUsd)
            }
            arr.put(obj)
        }
        prefs.edit().putString("deliveries_json", arr.toString()).apply()
    }

    private fun loadDeliveries() {
        val str = prefs.getString("deliveries_json", null) ?: return
        val arr = JSONArray(str)
        val list = mutableListOf<DeliveryOrder>()
        for (idx in 0 until arr.length()) {
            val obj = arr.getJSONObject(idx)
            list.add(
                DeliveryOrder(
                    id = obj.getString("id"),
                    saleId = obj.getString("saleId"),
                    customerName = obj.getString("customerName"),
                    customerPhone = obj.optString("customerPhone", ""),
                    address = obj.getString("address"),
                    lat = obj.getDouble("lat"),
                    lng = obj.getDouble("lng"),
                    status = obj.optString("status", "Pendiente"),
                    progress = obj.optDouble("progress", 0.0).toFloat(),
                    driverName = obj.optString("driverName", "José Gregorio"),
                    orderSummary = obj.optString("orderSummary", ""),
                    totalUsd = obj.optDouble("totalUsd", 0.0)
                )
            )
        }
        _deliveries.value = list
    }
}
