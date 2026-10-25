package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.StaffMember
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val staffList by viewModel.staff.collectAsState()
    val staffPayments by viewModel.staffPayments.collectAsState()
    val showStaffDialog by viewModel.showStaffDialog.collectAsState()
    val staffToPay by viewModel.staffToPay.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    val totalActiveStaff = staffList.count { it.isPresent }
    val estimatedPayroll = staffList.filter { it.isPresent }.sumOf { it.dailyRateUsd }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = AmberPrimary,
                contentColor = PosDarkBg,
                modifier = Modifier.testTag("btn_add_staff")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Nuevo Trabajador", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = PosDarkBg,
        modifier = modifier.fillMaxSize()
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Summary Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = PosDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, PosDarkBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Personal en Turno", color = TextMuted, fontSize = 11.sp)
                            Text("$totalActiveStaff activos", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = PosDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, PosDarkBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Nómina Estimada Hoy", color = TextMuted, fontSize = 11.sp)
                            Text("$${String.format(Locale.US, "%.2f", estimatedPayroll)} USD", color = AmberPrimary, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }
                }
            }

            // Staff items
            items(staffList, key = { it.id }) { member ->
                StaffMemberCard(
                    member = member,
                    onToggleAttendance = { viewModel.toggleStaffAttendance(member.id) },
                    onPayClick = { viewModel.openPayStaffDialog(member) }
                )
            }

            // Recent payments list
            if (staffPayments.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Pagos Realizados en este Turno",
                        color = AmberGlow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                items(staffPayments.take(6)) { pay ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
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
                            Column {
                                Text(pay.staffName, color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text(pay.concept, color = TextMuted, fontSize = 11.sp)
                            }
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", pay.amountUsd)} USD",
                                color = StatusGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Staff Dialog
    if (showAddDialog) {
        AddStaffDialog(
            onDismiss = { showAddDialog = false },
            onAddStaff = { name, role, rate, phone ->
                viewModel.addStaff(name, role, rate, phone)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun StaffMemberCard(
    member: StaffMember,
    onToggleAttendance: () -> Unit,
    onPayClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = PosDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, PosDarkBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Avatar Initials
                val initials = member.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (member.isPresent) AmberPrimary else PosDarkBorder),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = if (member.isPresent) PosDarkBg else TextMuted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Column {
                    Text(
                        text = member.name,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${member.role} • $${String.format(Locale.US, "%.2f", member.dailyRateUsd)}/día",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = if (member.isPresent) "● En Turno (Presente)" else "○ Fuera de Turno",
                        color = if (member.isPresent) StatusGreen else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleAttendance,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PosDarkElevated)
                ) {
                    Icon(
                        imageVector = if (member.isPresent) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = "Asistencia",
                        tint = if (member.isPresent) StatusGreen else StatusRed,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Button(
                    onClick = onPayClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PosDarkElevated,
                        contentColor = AmberPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pagar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddStaffDialog(
    onDismiss: () -> Unit,
    onAddStaff: (name: String, role: String, rate: Double, phone: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Planchero") }
    var rateText by remember { mutableStateOf("12.00") }
    var phone by remember { mutableStateOf("") }

    val roles = listOf("Planchero", "Cajero / POS", "Despachador", "Motorizado Delivery", "Ayudante de Cocina", "Encargado")

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
                Text("Registrar Nuevo Trabajador", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre Completo", color = TextMuted) },
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

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Cargo / Rol", color = TextMuted) },
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

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = rateText,
                    onValueChange = { rateText = it },
                    label = { Text("Pago Diario ($ USD)", color = TextMuted) },
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
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono de Contacto", color = TextMuted) },
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
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            val rate = rateText.toDoubleOrNull() ?: 10.0
                            if (name.isNotBlank()) {
                                onAddStaff(name, role, rate, phone)
                            }
                        },
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary, contentColor = PosDarkBg)
                    ) {
                        Text("Guardar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
