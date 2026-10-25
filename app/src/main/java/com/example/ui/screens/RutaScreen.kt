package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeliveryOrder
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun RutaScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val deliveries by viewModel.deliveries.collectAsState()

    // Pre-populate sample delivery if empty so user can immediately test GPS simulation
    LaunchedEffect(deliveries) {
        if (deliveries.isEmpty()) {
            viewModel.repository.processSale(
                customerName = "Marcos Galíndez",
                customerPhone = "+584125556677",
                cartItems = listOf(
                    com.example.model.CartItem(
                        product = viewModel.products.value.firstOrNull() ?: return@LaunchedEffect,
                        quantity = 2
                    )
                ),
                paymentMethod = com.example.model.PaymentMethod.PAGO_MOVIL,
                reference = "9982",
                isDelivery = true,
                deliveryAddress = "Urb. El Rosal, Edif. Altamira Torre A, Apto 4-B",
                notes = "Timbre no sirve, llamar al llegar"
            )
        }
    }

    var selectedDeliveryId by remember(deliveries) {
        mutableStateOf(deliveries.firstOrNull()?.id ?: "")
    }

    val activeDelivery = deliveries.find { it.id == selectedDeliveryId } ?: deliveries.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PosDarkBg)
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // GPS Tracker Canvas Map Radar
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = PosDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, PosDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Navigation, contentDescription = null, tint = AmberPrimary)
                            Text(
                                text = "Simulación GPS & Monitoreo en Vivo",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (activeDelivery?.status) {
                                "Entregado" -> StatusGreenBg
                                "En camino" -> StatusYellowBg
                                "En destino" -> StatusBlueBg
                                else -> PosDarkElevated
                            }
                        ) {
                            Text(
                                text = (activeDelivery?.status ?: "Pendiente").uppercase(),
                                color = when (activeDelivery?.status) {
                                    "Entregado" -> StatusGreen
                                    "En camino" -> StatusYellow
                                    "En destino" -> StatusBlue
                                    else -> TextMuted
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Radar Canvas simulation
                    DeliveryMapCanvas(
                        progress = activeDelivery?.progress ?: 0f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Controls for active delivery
                    if (activeDelivery != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Assign GPS / Re-route
                            Button(
                                onClick = {
                                    val newLat = 10.4806 + (Math.random() - 0.5) * 0.03
                                    val newLng = -66.9036 + (Math.random() - 0.5) * 0.03
                                    viewModel.assignCustomerGps(
                                        activeDelivery.id,
                                        newLat,
                                        newLng,
                                        activeDelivery.address
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("btn_assign_gps"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PosDarkElevated,
                                    contentColor = AmberPrimary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Asignar GPS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Start / Advance Route
                            Button(
                                onClick = { viewModel.advanceDeliveryStep(activeDelivery.id) },
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(46.dp)
                                    .testTag("btn_start_delivery_route"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AmberPrimary,
                                    contentColor = PosDarkBg
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.DirectionsBike, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when (activeDelivery.status) {
                                        "Pendiente" -> "Iniciar Ruta"
                                        "En camino" -> "Llegar a Destino"
                                        "En destino" -> "Confirmar Entrega"
                                        else -> "Reiniciar Ruta"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // WhatsApp Notification to customer
                        Button(
                            onClick = {
                                val url = viewModel.repository.generateWhatsAppDeliveryNoticeUrl(activeDelivery)
                                viewModel.openWhatsApp(context, url)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("btn_whatsapp_delivery_notice"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF25D366),
                                contentColor = TextWhite
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Avisar al Cliente por WhatsApp (En Camino)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Active Deliveries List
        item {
            Text(
                text = "Pedidos en Ruta & Delivery (${deliveries.size})",
                color = TextLightGrey,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(deliveries, key = { it.id }) { item ->
            val isSelected = item.id == (activeDelivery?.id ?: "")
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) PosDarkElevated else PosDarkSurface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) AmberPrimary else PosDarkBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedDeliveryId = item.id }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.customerName,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", item.totalUsd)}",
                            color = AmberPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📍 ${item.address}",
                        color = TextLightGrey,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🛵 ${item.driverName}",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Progreso: ${(item.progress * 100).toInt()}% (${item.status})",
                            color = when (item.status) {
                                "Entregado" -> StatusGreen
                                "En camino" -> StatusYellow
                                else -> AmberGlow
                            },
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { item.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = when (item.status) {
                            "Entregado" -> StatusGreen
                            else -> AmberPrimary
                        },
                        trackColor = PosDarkBorder
                    )
                }
            }
        }
    }
}

@Composable
fun DeliveryMapCanvas(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier.background(Color(0xFF121318))) {
        val w = size.width
        val h = size.height

        // Dark map grid lines (streets)
        val gridColor = Color(0xFF1E2029)
        for (x in 0 until w.toInt() step 50) {
            drawLine(gridColor, Offset(x.toFloat(), 0f), Offset(x.toFloat(), h), strokeWidth = 1.5f)
        }
        for (y in 0 until h.toInt() step 50) {
            drawLine(gridColor, Offset(0f, y.toFloat()), Offset(w, y.toFloat()), strokeWidth = 1.5f)
        }

        // Restaurant Location (Central Node)
        val restaurantPos = Offset(w * 0.22f, h * 0.75f)

        // Customer Destination
        val customerPos = Offset(w * 0.78f, h * 0.25f)

        // Route Path with waypoints
        val midPoint = Offset(w * 0.55f, h * 0.65f)
        val midPoint2 = Offset(w * 0.45f, h * 0.35f)

        // Draw dotted route trajectory
        val pathColor = Color(0xFFFF9800).copy(alpha = 0.5f)
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)

        drawLine(pathColor, restaurantPos, midPoint, strokeWidth = 4f, pathEffect = pathEffect)
        drawLine(pathColor, midPoint, midPoint2, strokeWidth = 4f, pathEffect = pathEffect)
        drawLine(pathColor, midPoint2, customerPos, strokeWidth = 4f, pathEffect = pathEffect)

        // Restaurant Beacon (Food Truck Base)
        drawCircle(
            color = Color(0xFFFF5722).copy(alpha = 0.3f),
            radius = pulseRadius,
            center = restaurantPos
        )
        drawCircle(color = Color(0xFFFF5722), radius = 10f, center = restaurantPos)
        drawCircle(color = Color.White, radius = 4f, center = restaurantPos)

        // Customer Destination Marker
        drawCircle(color = Color(0xFF10B981).copy(alpha = 0.3f), radius = 18f, center = customerPos)
        drawCircle(color = Color(0xFF10B981), radius = 9f, center = customerPos)
        drawCircle(color = Color.White, radius = 3.5f, center = customerPos)

        // Motorized Driver Current Position interpolated by progress
        val currentBikePos = when {
            progress <= 0.33f -> {
                val p = progress / 0.33f
                Offset(
                    restaurantPos.x + (midPoint.x - restaurantPos.x) * p,
                    restaurantPos.y + (midPoint.y - restaurantPos.y) * p
                )
            }
            progress <= 0.66f -> {
                val p = (progress - 0.33f) / 0.33f
                Offset(
                    midPoint.x + (midPoint2.x - midPoint.x) * p,
                    midPoint.y + (midPoint2.y - midPoint.y) * p
                )
            }
            else -> {
                val p = ((progress - 0.66f) / 0.34f).coerceIn(0f, 1f)
                Offset(
                    midPoint2.x + (customerPos.x - midPoint2.x) * p,
                    midPoint2.y + (customerPos.y - midPoint2.y) * p
                )
            }
        }

        // Draw Driver Glow and Bike Icon node
        drawCircle(color = Color(0xFFFFC107).copy(alpha = 0.4f), radius = 16f, center = currentBikePos)
        drawCircle(color = Color(0xFFFF9800), radius = 8f, center = currentBikePos)
        drawCircle(color = Color(0xFF0F0F12), radius = 3f, center = currentBikePos)
    }
}
