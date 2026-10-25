package com.example.model

data class Shift(
    val id: String,
    val shiftType: String, // "Turno Mañana (11:00 AM - 6:00 PM)" or "Turno Nocturno (6:00 PM - 2:00 AM)"
    val operatorName: String,
    val startTime: Long,
    val endTime: Long? = null,
    val isActive: Boolean = true,
    val initialCashUsd: Double = 0.0,
    val initialCashBs: Double = 0.0
)

data class RecipeItem(
    val inventoryItemId: String,
    val quantityUsed: Double
)

data class Product(
    val id: String,
    val name: String,
    val category: String, // "Perros", "Hamburguesas", "Combos", "Bebidas", "Especiales"
    val priceUsd: Double,
    val description: String,
    val badgeIcon: String = "BURGER", // "HOT_DOG", "BURGER", "COMBO", "DRINK", "FRIES"
    val recipe: List<RecipeItem> = emptyList()
)

data class CartItem(
    val product: Product,
    val quantity: Int = 1,
    val notes: String = ""
) {
    val subtotalUsd: Double get() = product.priceUsd * quantity
}

enum class PaymentMethod(val id: String, val title: String, val requiresRef: Boolean) {
    PUNTO_DE_VENTA("punto", "Punto de Venta", true),
    PAGO_MOVIL("pagomovil", "Pago Móvil", true),
    EFECTIVO_USD("efectivo_usd", "Efectivo USD", false),
    EFECTIVO_BS("efectivo_bs", "Efectivo Bs", false)
}

data class SaleItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPriceUsd: Double,
    val subtotalUsd: Double
)

data class Sale(
    val id: String,
    val ticketNumber: Int,
    val shiftId: String,
    val timestamp: Long,
    val customerName: String,
    val customerPhone: String,
    val items: List<SaleItem>,
    val totalUsd: Double,
    val totalBs: Double,
    val exchangeRate: Double,
    val paymentMethod: PaymentMethod,
    val reference: String = "",
    val isDelivery: Boolean = false,
    val deliveryAddress: String = "",
    val notes: String = ""
)

data class InventoryItem(
    val id: String,
    val name: String,
    val category: String, // "Panes", "Carnes", "Salsas", "Complementos", "Bebidas", "Descartables"
    val currentStock: Double,
    val minStockAlert: Double,
    val unit: String, // "unid", "g", "ml"
    val costPerUnitUsd: Double
)

data class Purchase(
    val id: String,
    val shiftId: String,
    val timestamp: Long,
    val inventoryItemId: String,
    val inventoryItemName: String,
    val quantityAdded: Double,
    val costUsd: Double,
    val supplier: String = "Proveedor Local"
)

data class StaffMember(
    val id: String,
    val name: String,
    val role: String, // "Planchero", "Cajero", "Despachador", "Motorizado", "Encargado"
    val dailyRateUsd: Double,
    val phone: String = "",
    val isActive: Boolean = true,
    val isPresent: Boolean = true
)

data class StaffPayment(
    val id: String,
    val shiftId: String,
    val staffId: String,
    val staffName: String,
    val amountUsd: Double,
    val timestamp: Long,
    val concept: String = "Pago diario de jornada"
)

data class DeliveryOrder(
    val id: String,
    val saleId: String,
    val customerName: String,
    val customerPhone: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val status: String = "Pendiente", // "Pendiente", "En camino", "En destino", "Entregado"
    val progress: Float = 0f, // 0.0f to 1.0f
    val driverName: String = "José Gregorio (Motorizado)",
    val orderSummary: String = "",
    val totalUsd: Double = 0.0
)

data class AppConfig(
    val exchangeRate: Double = 45.0,
    val adminPhone: String = "+584120000000",
    val businessName: String = "STREET FOOD PRO",
    val businessSlogan: String = "Sabor Callejero Premium",
    val businessAddress: String = "Food Truck Park - Calle Principal",
    val receiptTemplate: String = """
🍟 *{negocio}* - COMPROBANTE DIGITAL 🌭
------------------------------------------
🧾 *Ticket #:* {ticket}
📅 *Fecha:* {fecha}
🕒 *Turno:* {turno}
👤 *Cliente:* {cliente}
💳 *Método de Pago:* {metodo}
{ref_line}
------------------------------------------
📦 *DETALLE DE LA ORDEN:*
{detalle_items}
------------------------------------------
💵 *TOTAL A PAGAR:*
💲 USD: ${'$'}{total_usd}
🇻🇪 Bs.: {total_bs} Bs. (Tasa: {tasa})
------------------------------------------
🛵 *Modalidad:* {modalidad}
{delivery_info}
¡Muchas gracias por su preferencia! 🔥
    """.trimIndent(),
    val shiftReportTemplate: String = """
📊 *REPORTE DE CIERRE DE TURNO* 📊
*{negocio}*
------------------------------------------
🕒 *Turno:* {turno}
👤 *Operador:* {operador}
📅 *Fecha:* {fecha}
------------------------------------------
💰 *RESUMEN DE VENTAS:*
• Total Pedidos: {total_pedidos}
• Ventas Totales USD: ${'$'}{ventas_usd}
• Ventas Totales Bs: {ventas_bs} Bs.

💳 *DESGLOSE POR MÉTODO:*
• Punto de Venta: ${'$'}{total_punto} USD
• Pago Móvil: ${'$'}{total_pagomovil} USD
• Efectivo USD: ${'$'}{total_efectivo_usd} USD
• Efectivo Bs: ${'$'}{total_efectivo_bs} USD

📉 *GASTOS DEL TURNO:*
• Compras Insumos: ${'$'}{gastos_compras} USD
• Pagos de Personal: ${'$'}{gastos_personal} USD
• Total Egresos: ${'$'}{total_gastos} USD

⭐ *BALANCE NETO:*
• Utilidad Neta del Turno: ${'$'}{utilidad_neta} USD
------------------------------------------
Generado por Street Food Pro Mobile POS 🚀
    """.trimIndent()
)
