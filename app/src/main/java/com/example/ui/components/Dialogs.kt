package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftOpenDialog(
    activeShift: Shift?,
    onStartShift: (shiftType: String, operatorName: String, initialUsd: Double, initialBs: Double) -> Unit,
    onCloseShift: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedShiftType by remember {
        mutableStateOf(
            activeShift?.shiftType ?: "Turno Nocturno (6:00 PM - 2:00 AM)"
        )
    }
    var operatorName by remember {
        mutableStateOf(activeShift?.operatorName ?: "")
    }
    var initialUsdText by remember { mutableStateOf("20.00") }
    var initialBsText by remember { mutableStateOf("900.00") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val shiftOptions = listOf(
        "Turno Mañana (11:00 AM - 6:00 PM)",
        "Turno Nocturno (6:00 PM - 2:00 AM)"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = PosDarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, PosDarkBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(listOf(OrangeFlame, AmberPrimary))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTimeFilled,
                        contentDescription = null,
                        tint = TextWhite,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (activeShift?.isActive == true) "Gestión de Turno Actual" else "Apertura de Turno",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                Text(
                    text = if (activeShift?.isActive == true)
                        "Turno actualmente en curso"
                    else
                        "Inicializa la sesión de ventas para comenzar a operar el POS",
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Shift Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedShiftType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Seleccionar Turno", color = TextMuted) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberPrimary,
                            unfocusedBorderColor = PosDarkBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = PosDarkElevated,
                            unfocusedContainerColor = PosDarkElevated
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false },
                        modifier = Modifier.background(PosDarkElevated)
                    ) {
                        shiftOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, color = TextWhite) },
                                onClick = {
                                    selectedShiftType = option
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Operator Name Input
                OutlinedTextField(
                    value = operatorName,
                    onValueChange = { operatorName = it },
                    label = { Text("Nombre del Operador / Cajero", color = TextMuted) },
                    placeholder = { Text("Ej. Andrea Rivas", color = TextMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = AmberGlow)
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberPrimary,
                        unfocusedBorderColor = PosDarkBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedContainerColor = PosDarkElevated,
                        unfocusedContainerColor = PosDarkElevated
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shift_operator_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Initial Cash Box USD & Bs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = initialUsdText,
                        onValueChange = { initialUsdText = it },
                        label = { Text("Base Caja ($)", color = TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberPrimary,
                            unfocusedBorderColor = PosDarkBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = PosDarkElevated,
                            unfocusedContainerColor = PosDarkElevated
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = initialBsText,
                        onValueChange = { initialBsText = it },
                        label = { Text("Base Caja (Bs)", color = TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberPrimary,
                            unfocusedBorderColor = PosDarkBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = PosDarkElevated,
                            unfocusedContainerColor = PosDarkElevated
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Primary Start Button
                Button(
                    onClick = {
                        val usd = initialUsdText.toDoubleOrNull() ?: 0.0
                        val bs = initialBsText.toDoubleOrNull() ?: 0.0
                        onStartShift(selectedShiftType, operatorName, usd, bs)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_start_shift"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberPrimary,
                        contentColor = PosDarkBg
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "APERTURAR TURNO DE VENTAS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                if (activeShift?.isActive == true) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onCloseShift,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = StatusRed
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, tint = StatusRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CERRAR TURNO ACTUAL",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDismiss) {
                    Text("Continuar sin Cambios", color = TextMuted, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun DigitalReceiptDialog(
    sale: Sale,
    onSendWhatsApp: (url: String) -> Unit,
    whatsAppUrl: String,
    onDismiss: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
    val dateStr = dateFormat.format(Date(sale.timestamp))

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = PosDarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, StatusGreen.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success Badge
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(StatusGreenBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = StatusGreen,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "¡Venta Procesada con Éxito!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                Text(
                    text = "Ticket #${sale.ticketNumber} • $dateStr",
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                )

                // Ticket Card Details
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
                            Text("Cliente:", color = TextMuted, fontSize = 12.sp)
                            Text(sale.customerName, color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }

                        if (sale.customerPhone.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Teléfono / WhatsApp:", color = TextMuted, fontSize = 12.sp)
                                Text(sale.customerPhone, color = AmberGlow, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Método:", color = TextMuted, fontSize = 12.sp)
                            Text(sale.paymentMethod.title, color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }

                        if (sale.reference.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Ref. Bancaria:", color = TextMuted, fontSize = 12.sp)
                                Text(sale.reference, color = TextLightGrey, fontSize = 12.sp)
                            }
                        }

                        Divider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = PosDarkBorder
                        )

                        // Products summary
                        Text(
                            text = "Productos Facturados:",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )

                        sale.items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${item.quantity}x ${item.productName}",
                                    color = TextLightGrey,
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", item.subtotalUsd)}",
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Divider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = PosDarkBorder
                        )

                        // Dual Currency Totals
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("TOTAL USD:", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                "$${String.format(Locale.US, "%.2f", sale.totalUsd)}",
                                color = AmberPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("TOTAL Bs.:", color = TextMuted, fontSize = 12.sp)
                            Text(
                                "${String.format(Locale.US, "%.2f", sale.totalBs)} Bs.",
                                color = TextLightGrey,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // WhatsApp Send Button
                Button(
                    onClick = { onSendWhatsApp(whatsAppUrl) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_send_whatsapp_ticket"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF25D366), // WhatsApp Brand Green
                        contentColor = TextWhite
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "WhatsApp",
                        tint = TextWhite
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ENVIAR COMPROBANTE WHATSAPP",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_next_sale"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PosDarkBorder),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Siguiente Venta (Listo)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPurchaseDialog(
    inventoryItems: List<InventoryItem>,
    onRegisterPurchase: (invId: String, qty: Double, cost: Double, supplier: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedItemId by remember {
        mutableStateOf(inventoryItems.firstOrNull()?.id ?: "")
    }
    var quantityText by remember { mutableStateOf("10") }
    var costText by remember { mutableStateOf("5.00") }
    var supplierText by remember { mutableStateOf("Distribuidora Central") }
    var isExpanded by remember { mutableStateOf(false) }

    val selectedItem = inventoryItems.find { it.id == selectedItemId }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = PosDarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, PosDarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Registrar Compra de Insumos",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = "Suma stock automáticamente y lo asienta en los gastos del turno",
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                )

                // Select Item dropdown
                ExposedDropdownMenuBox(
                    expanded = isExpanded,
                    onExpandedChange = { isExpanded = !isExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedItem?.name ?: "Seleccione insumo",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Insumo o Ingrediente", color = TextMuted) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberPrimary,
                            unfocusedBorderColor = PosDarkBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = PosDarkElevated,
                            unfocusedContainerColor = PosDarkElevated
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { isExpanded = false },
                        modifier = Modifier.background(PosDarkElevated)
                    ) {
                        inventoryItems.forEach { item ->
                            DropdownMenuItem(
                                text = { Text("${item.name} (${item.unit})", color = TextWhite) },
                                onClick = {
                                    selectedItemId = item.id
                                    isExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Cantidad (${selectedItem?.unit ?: "unid"})", color = TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberPrimary,
                            unfocusedBorderColor = PosDarkBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = PosDarkElevated,
                            unfocusedContainerColor = PosDarkElevated
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = costText,
                        onValueChange = { costText = it },
                        label = { Text("Costo Total ($)", color = TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberPrimary,
                            unfocusedBorderColor = PosDarkBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = PosDarkElevated,
                            unfocusedContainerColor = PosDarkElevated
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = supplierText,
                    onValueChange = { supplierText = it },
                    label = { Text("Proveedor / Nota", color = TextMuted) },
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

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PosDarkBorder)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            val qty = quantityText.toDoubleOrNull() ?: 0.0
                            val cost = costText.toDoubleOrNull() ?: 0.0
                            if (selectedItemId.isNotBlank() && qty > 0) {
                                onRegisterPurchase(selectedItemId, qty, cost, supplierText)
                            }
                        },
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberPrimary,
                            contentColor = PosDarkBg
                        )
                    ) {
                        Text("Registrar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PayStaffDialog(
    staff: StaffMember,
    onSubmitPayment: (amount: Double, concept: String) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf(staff.dailyRateUsd.toString()) }
    var conceptText by remember { mutableStateOf("Pago de jornada diaria") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = PosDarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, PosDarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Registrar Pago de Personal",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = "Asignado a: ${staff.name} (${staff.role})",
                    fontSize = 12.sp,
                    color = AmberGlow,
                    modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Monto a Pagar ($ USD)", color = TextMuted) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = conceptText,
                    onValueChange = { conceptText = it },
                    label = { Text("Concepto", color = TextMuted) },
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

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PosDarkBorder)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (amt > 0) {
                                onSubmitPayment(amt, conceptText)
                            }
                        },
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberPrimary,
                            contentColor = PosDarkBg
                        )
                    ) {
                        Text("Confirmar Pago", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
