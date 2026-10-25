package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppConfig
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun ConfigScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val config by viewModel.config.collectAsState()

    var rateText by remember(config.exchangeRate) { mutableStateOf(config.exchangeRate.toString()) }
    var adminPhone by remember(config.adminPhone) { mutableStateOf(config.adminPhone) }
    var businessName by remember(config.businessName) { mutableStateOf(config.businessName) }
    var businessAddress by remember(config.businessAddress) { mutableStateOf(config.businessAddress) }
    var receiptTemplate by remember(config.receiptTemplate) { mutableStateOf(config.receiptTemplate) }
    var shiftReportTemplate by remember(config.shiftReportTemplate) { mutableStateOf(config.shiftReportTemplate) }

    var showResetConfirmDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PosDarkBg)
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Exchange Rate & Quick Update
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PosDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, AmberPrimary.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CurrencyExchange, contentDescription = null, tint = AmberPrimary)
                        Text(
                            text = "Tasa de Cambio del Día (Bs. / USD)",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = rateText,
                            onValueChange = { rateText = it },
                            label = { Text("Tasa Oficial / Paralela (Bs.)", color = TextMuted) },
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
                            modifier = Modifier.weight(1f).testTag("exchange_rate_input")
                        )

                        Button(
                            onClick = {
                                val rate = rateText.toDoubleOrNull()
                                if (rate != null && rate > 0) {
                                    viewModel.updateRate(rate)
                                    Toast.makeText(context, "Tasa actualizada: $rate Bs./USD", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmberPrimary,
                                contentColor = PosDarkBg
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(54.dp)
                        ) {
                            Text("Actualizar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Business Information
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PosDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, PosDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Datos del Establecimiento",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text("Nombre del Local", color = TextMuted) },
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

                    OutlinedTextField(
                        value = businessAddress,
                        onValueChange = { businessAddress = it },
                        label = { Text("Dirección / Ubicación", color = TextMuted) },
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

                    OutlinedTextField(
                        value = adminPhone,
                        onValueChange = { adminPhone = it },
                        label = { Text("WhatsApp del Administrador / Dueño", color = TextMuted) },
                        placeholder = { Text("+584120000000", color = TextMuted) },
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
                        modifier = Modifier.fillMaxWidth().testTag("admin_phone_input")
                    )
                }
            }
        }

        // WhatsApp Templates Editor
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PosDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, PosDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Plantillas de Mensajes WhatsApp",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Variables disponibles: {negocio}, {ticket}, {cliente}, {total_usd}, {total_bs}, {metodo}, {tasa}, {fecha}, {turno}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    OutlinedTextField(
                        value = receiptTemplate,
                        onValueChange = { receiptTemplate = it },
                        label = { Text("Plantilla Comprobante Cliente", color = TextMuted) },
                        minLines = 4,
                        maxLines = 8,
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

                    OutlinedTextField(
                        value = shiftReportTemplate,
                        onValueChange = { shiftReportTemplate = it },
                        label = { Text("Plantilla Cierre de Turno", color = TextMuted) },
                        minLines = 4,
                        maxLines = 8,
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

                    Button(
                        onClick = {
                            val newConfig = config.copy(
                                exchangeRate = rateText.toDoubleOrNull() ?: config.exchangeRate,
                                adminPhone = adminPhone.trim(),
                                businessName = businessName.trim(),
                                businessAddress = businessAddress.trim(),
                                receiptTemplate = receiptTemplate,
                                shiftReportTemplate = shiftReportTemplate
                            )
                            viewModel.updateConfig(newConfig)
                            Toast.makeText(context, "Configuración guardada exitosamente", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_save_config"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberPrimary,
                            contentColor = PosDarkBg
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Guardar Configuración Localmente", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Danger Zone: Reset Database
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = StatusRedBg.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, StatusRed.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = StatusRed)
                        Text(
                            text = "Zona de Peligro / Mantenimiento",
                            color = StatusRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Restaura la base de datos local a los valores iniciales de fábrica con el menú preconfigurado de perros, hamburguesas e insumos.",
                        color = TextLightGrey,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showResetConfirmDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("btn_reset_database")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, tint = StatusRed)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Borrar Todo / Resetear Base de Datos", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("¿Restaurar Base de Datos?", color = TextWhite) },
            text = { Text("Esta acción restablecerá el inventario inicial, personal y ventas a los valores por defecto del sistema.", color = TextMuted) },
            containerColor = PosDarkSurface,
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetDatabase()
                        showResetConfirmDialog = false
                        Toast.makeText(context, "Base de datos restaurada con éxito", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                ) {
                    Text("Sí, Resetear", color = TextWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            }
        )
    }
}
