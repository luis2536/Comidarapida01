package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val products by viewModel.products.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val config by viewModel.config.collectAsState()
    val activeShift by viewModel.activeShift.collectAsState()

    val customerName by viewModel.customerName.collectAsState()
    val customerPhone by viewModel.customerPhone.collectAsState()
    val paymentMethod by viewModel.paymentMethod.collectAsState()
    val transactionRef by viewModel.transactionReference.collectAsState()
    val isDelivery by viewModel.isDelivery.collectAsState()
    val deliveryAddress by viewModel.deliveryAddress.collectAsState()

    var isCartSheetOpen by remember { mutableStateOf(false) }

    val categories = listOf("Todos", "Perros", "Hamburguesas", "Combos", "Bebidas", "Especiales")
    val filteredProducts = if (selectedCategory == "Todos") {
        products
    } else {
        products.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    val totalUsd = cartItems.sumOf { it.subtotalUsd }
    val totalBs = totalUsd * config.exchangeRate
    val totalItemsCount = cartItems.sumOf { it.quantity }

    Box(modifier = modifier.fillMaxSize().background(PosDarkBg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Category Selector Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setSelectedCategory(category) },
                        label = {
                            Text(
                                text = category,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberPrimary,
                            selectedLabelColor = PosDarkBg,
                            containerColor = PosDarkSurface,
                            labelColor = TextLightGrey
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) AmberPrimary else PosDarkBorder
                        )
                    )
                }
            }

            // Products Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 90.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        exchangeRate = config.exchangeRate,
                        onAddToCart = { viewModel.addToCart(product) }
                    )
                }
            }
        }

        // Bottom Sticky Order Summary Floating Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { isCartSheetOpen = true }
                .testTag("floating_cart_bar"),
            color = PosDarkElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, if (cartItems.isNotEmpty()) AmberPrimary else PosDarkBorder),
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (cartItems.isNotEmpty()) AmberPrimary else PosDarkBorder),
                        contentAlignment = Alignment.Center
                    ) {
                        BadgedBox(
                            badge = {
                                if (totalItemsCount > 0) {
                                    Badge(containerColor = OrangeFlame, contentColor = TextWhite) {
                                        Text(totalItemsCount.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Carrito",
                                tint = if (cartItems.isNotEmpty()) PosDarkBg else TextMuted
                            )
                        }
                    }

                    Column {
                        Text(
                            text = if (cartItems.isEmpty()) "Orden Vacía" else "$totalItemsCount ${if (totalItemsCount == 1) "producto" else "productos"}",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (cartItems.isEmpty()) "Toca productos para agregar" else "Ver pedido / Procesar pago",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", totalUsd)}",
                        color = AmberPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.2f", totalBs)} Bs.",
                        color = TextLightGrey,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Bottom Sheet for Full Cart & Checkout
        if (isCartSheetOpen) {
            ModalBottomSheet(
                onDismissRequest = { isCartSheetOpen = false },
                containerColor = PosDarkSurface,
                scrimColor = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxHeight(0.92f)
            ) {
                CartSheetContent(
                    cartItems = cartItems,
                    totalUsd = totalUsd,
                    totalBs = totalBs,
                    exchangeRate = config.exchangeRate,
                    customerName = customerName,
                    customerPhone = customerPhone,
                    paymentMethod = paymentMethod,
                    transactionRef = transactionRef,
                    isDelivery = isDelivery,
                    deliveryAddress = deliveryAddress,
                    onNameChange = { viewModel.setCustomerName(it) },
                    onPhoneChange = { viewModel.setCustomerPhone(it) },
                    onPaymentMethodChange = { viewModel.setPaymentMethod(it) },
                    onRefChange = { viewModel.setTransactionReference(it) },
                    onDeliveryChange = { viewModel.setIsDelivery(it) },
                    onAddressChange = { viewModel.setDeliveryAddress(it) },
                    onIncrement = { viewModel.updateCartQuantity(it, 1) },
                    onDecrement = { viewModel.updateCartQuantity(it, -1) },
                    onClearCart = { viewModel.clearCart() },
                    onProcessOrder = {
                        viewModel.processOrder(context)
                        isCartSheetOpen = false
                    }
                )
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    exchangeRate: Double,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val priceBs = product.priceUsd * exchangeRate

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onAddToCart() }
            .testTag("product_card_${product.id}"),
        color = PosDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, PosDarkBorder),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Icon Badge Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                when (product.badgeIcon) {
                                    "HOT_DOG" -> listOf(Color(0xFFE65100), Color(0xFFFF9800))
                                    "BURGER" -> listOf(Color(0xFFBF360C), Color(0xFFFF5722))
                                    "COMBO" -> listOf(Color(0xFF4A148C), Color(0xFF9C27B0))
                                    "DRINK" -> listOf(Color(0xFF0D47A1), Color(0xFF1976D2))
                                    else -> listOf(Color(0xFFF57F17), Color(0xFFFFB300))
                                }
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getProductIcon(product.badgeIcon),
                        contentDescription = null,
                        tint = TextWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PosDarkElevated
                ) {
                    Text(
                        text = product.category,
                        color = AmberGlow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = product.name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = TextWhite,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = product.description,
                color = TextMuted,
                fontSize = 10.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )

            Spacer(modifier = Modifier.weight(1f, fill = false))

            // Dual Pricing Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", product.priceUsd)}",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = AmberPrimary
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.2f", priceBs)} Bs.",
                        fontSize = 11.sp,
                        color = TextLightGrey,
                        fontWeight = FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AmberPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar",
                        tint = PosDarkBg,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

fun getProductIcon(type: String): ImageVector {
    return when (type) {
        "HOT_DOG" -> Icons.Default.Fastfood
        "BURGER" -> Icons.Default.LunchDining
        "COMBO" -> Icons.Default.TakeoutDining
        "DRINK" -> Icons.Default.LocalDrink
        "FRIES" -> Icons.Default.DinnerDining
        else -> Icons.Default.Fastfood
    }
}

@Composable
fun CartSheetContent(
    cartItems: List<CartItem>,
    totalUsd: Double,
    totalBs: Double,
    exchangeRate: Double,
    customerName: String,
    customerPhone: String,
    paymentMethod: PaymentMethod,
    transactionRef: String,
    isDelivery: Boolean,
    deliveryAddress: String,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPaymentMethodChange: (PaymentMethod) -> Unit,
    onRefChange: (String) -> Unit,
    onDeliveryChange: (Boolean) -> Unit,
    onAddressChange: (String) -> Unit,
    onIncrement: (String) -> Unit,
    onDecrement: (String) -> Unit,
    onClearCart: () -> Unit,
    onProcessOrder: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Sheet Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Orden Actual / Caja POS",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = "${cartItems.sumOf { it.quantity }} productos en la comanda",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            if (cartItems.isNotEmpty()) {
                TextButton(
                    onClick = onClearCart,
                    colors = ButtonDefaults.textButtonColors(contentColor = StatusRed)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Vaciar", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Order Items List
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay productos seleccionados", color = TextMuted, fontSize = 13.sp)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                cartItems.forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = PosDarkElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, PosDarkBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.product.name,
                                    color = TextWhite,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", item.product.priceUsd)} c/u",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .clickable { onDecrement(item.product.id) },
                                    color = PosDarkBorder
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "Quitar",
                                            tint = TextWhite,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = item.quantity.toString(),
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )

                                Surface(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .clickable { onIncrement(item.product.id) },
                                    color = AmberPrimary
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Sumar",
                                            tint = PosDarkBg,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", item.subtotalUsd)}",
                                    color = AmberGlow,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 12.dp), color = PosDarkBorder)

        // Customer Details Inputs
        Text(
            text = "Datos del Cliente & Comprobante",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextLightGrey
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = customerName,
                onValueChange = onNameChange,
                label = { Text("Nombre del Cliente", color = TextMuted) },
                placeholder = { Text("Ej. Juan Pérez", color = TextMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AmberPrimary,
                    unfocusedBorderColor = PosDarkBorder,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedContainerColor = PosDarkElevated,
                    unfocusedContainerColor = PosDarkElevated
                ),
                modifier = Modifier.weight(1f).testTag("customer_name_input")
            )

            OutlinedTextField(
                value = customerPhone,
                onValueChange = onPhoneChange,
                label = { Text("WhatsApp (+58)", color = TextMuted) },
                placeholder = { Text("4121234567", color = TextMuted) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AmberPrimary,
                    unfocusedBorderColor = PosDarkBorder,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedContainerColor = PosDarkElevated,
                    unfocusedContainerColor = PosDarkElevated
                ),
                modifier = Modifier.weight(1f).testTag("customer_phone_input")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Delivery Toggle
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = PosDarkElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, PosDarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.DeliveryDining, contentDescription = null, tint = AmberPrimary)
                    Text("¿Pedido para Delivery?", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Switch(
                    checked = isDelivery,
                    onCheckedChange = onDeliveryChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PosDarkBg,
                        checkedTrackColor = AmberPrimary,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = PosDarkBorder
                    )
                )
            }
        }

        if (isDelivery) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = deliveryAddress,
                onValueChange = onAddressChange,
                label = { Text("Dirección / Punto de Referencia para Delivery", color = TextMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AmberPrimary,
                    unfocusedBorderColor = PosDarkBorder,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedContainerColor = PosDarkElevated,
                    unfocusedContainerColor = PosDarkElevated
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Payment Method Selector
        Text(
            text = "Método de Pago",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextLightGrey
        )

        Spacer(modifier = Modifier.height(8.dp))

        val methods = PaymentMethod.values()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            methods.forEach { method ->
                val isSelected = paymentMethod == method
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onPaymentMethodChange(method) },
                    color = if (isSelected) AmberPrimary else PosDarkElevated,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) AmberPrimary else PosDarkBorder
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = method.title,
                            color = if (isSelected) PosDarkBg else TextLightGrey,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        if (paymentMethod.requiresRef) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = transactionRef,
                onValueChange = onRefChange,
                label = { Text("Código de Transacción / Últimos 4 dígitos", color = TextMuted) },
                placeholder = { Text("Ej. 4821", color = TextMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AmberPrimary,
                    unfocusedBorderColor = PosDarkBorder,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedContainerColor = PosDarkElevated,
                    unfocusedContainerColor = PosDarkElevated
                ),
                modifier = Modifier.fillMaxWidth().testTag("payment_ref_input")
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Total Summary Box
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = PosDarkElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, PosDarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total a Pagar (USD):", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        "$${String.format(Locale.US, "%.2f", totalUsd)}",
                        color = AmberPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Cálculo en Bolívares (Bs.):", color = TextMuted, fontSize = 12.sp)
                    Text(
                        "${String.format(Locale.US, "%.2f", totalBs)} Bs.",
                        color = TextLightGrey,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Process Payment Button
        Button(
            onClick = onProcessOrder,
            enabled = cartItems.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_process_payment"),
            colors = ButtonDefaults.buttonColors(
                containerColor = AmberPrimary,
                contentColor = PosDarkBg,
                disabledContainerColor = PosDarkBorder,
                disabledContentColor = TextMuted
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "PROCESAR PAGO ($${String.format(Locale.US, "%.2f", totalUsd)})",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
