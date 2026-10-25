package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CuadreScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeShift by viewModel.activeShift.collectAsState()
    val sales by viewModel.sales.collectAsState()
    val purchases by viewModel.purchases.collectAsState()
    val staffPayments by viewModel.staffPayments.collectAsState()
    val config by viewModel.config.collectAsState()

    val summary = remember(activeShift, sales, purchases, staffPayments, config) {
        viewModel.repository.getShiftSummary()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PosDarkBg)
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Shift Info Header
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PosDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, PosDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cuadre y Cierre de Caja",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (activeShift?.isActive == true) StatusGreenBg else StatusRedBg
                        ) {
                            Text(
                                text = if (activeShift?.isActive == true) "TURNO ACTIVO" else "TURNO CERRADO",
                                color = if (activeShift?.isActive == true) StatusGreen else StatusRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${activeShift?.shiftType ?: "Sin Turno"} • Encargado: ${activeShift?.operatorName ?: "N/A"}",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Financial KPI Cards (Ventas, Gastos, Utilidad)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Total Sales KPI
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PosDarkElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Ventas Brutas del Turno", color = TextMuted, fontSize = 12.sp)
                            Text(
                                "$${String.format(Locale.US, "%.2f", summary?.totalSalesUsd ?: 0.0)} USD",
                                color = AmberPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp
                            )
                            Text(
                                "${String.format(Locale.US, "%.2f", summary?.totalSalesBs ?: 0.0)} Bs.",
                                color = TextLightGrey,
                                fontSize = 12.sp
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AmberPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${summary?.totalSalesCount ?: 0} Ventas",
                                color = AmberPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Egresos y Utilidad Neta
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PosDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, PosDarkBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Total Egresos / Gastos", color = TextMuted, fontSize = 11.sp)
                            Text(
                                "$${String.format(Locale.US, "%.2f", summary?.totalExpensesUsd ?: 0.0)}",
                                color = StatusRed,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Text(
                                "Compras: $${String.format(Locale.US, "%.1f", summary?.purchasesExpensesUsd ?: 0.0)} • Pers: $${String.format(Locale.US, "%.1f", summary?.staffPayoutsUsd ?: 0.0)}",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PosDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusGreen.copy(alpha = 0.4f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Ganancia Neta Turno", color = TextMuted, fontSize = 11.sp)
                            Text(
                                "$${String.format(Locale.US, "%.2f", summary?.netProfitUsd ?: 0.0)}",
                                color = StatusGreen,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Text("Caja Limpia Estimada", color = TextMuted, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // Breakdown by Payment Method
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PosDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, PosDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Desglose por Medios de Pago",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    PaymentBreakdownRow(
                        title = "Punto de Venta (Tarjetas)",
                        amountUsd = summary?.puntoDeVentaUsd ?: 0.0,
                        tagColor = StatusBlue
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = PosDarkBorder)

                    PaymentBreakdownRow(
                        title = "Pago Móvil Interbancario",
                        amountUsd = summary?.pagoMovilUsd ?: 0.0,
                        tagColor = StatusPurple
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = PosDarkBorder)

                    PaymentBreakdownRow(
                        title = "Efectivo Divisas ($ USD)",
                        amountUsd = summary?.efectivoUsd ?: 0.0,
                        tagColor = StatusGreen
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = PosDarkBorder)

                    PaymentBreakdownRow(
                        title = "Efectivo Bolívares (Bs)",
                        amountUsd = summary?.efectivoBsUsd ?: 0.0,
                        tagColor = AmberPrimary
                    )
                }
            }
        }

        // Action Buttons: WhatsApp Report, PDF / Formal Audit, Excel CSV
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Send WhatsApp Report Button
                Button(
                    onClick = {
                        val url = viewModel.repository.generateWhatsAppShiftReportUrl()
                        viewModel.openWhatsApp(context, url)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_whatsapp_shift_report"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF25D366),
                        contentColor = TextWhite
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ENVIAR CIERRE POR WHATSAPP AL DUEÑO",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Formal Report / PDF button
                    OutlinedButton(
                        onClick = { viewModel.shareFormalReport(context) },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("btn_formal_report_pdf"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AmberPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reporte Formal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Excel / CSV Export button
                    OutlinedButton(
                        onClick = { viewModel.shareSalesCsv(context) },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("btn_export_excel_csv"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusGreen),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Descargar Excel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Close Shift Button
                if (activeShift?.isActive == true) {
                    OutlinedButton(
                        onClick = { viewModel.closeActiveShift(context) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("btn_close_shift_definitely"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = StatusRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cerrar Turno Definitivamente", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Recent Shift Sales Log
        item {
            Text(
                text = "Detalle de Ventas Registradas (${sales.size})",
                color = TextLightGrey,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(sales.take(15), key = { it.id }) { sale ->
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val timeStr = sdf.format(Date(sale.timestamp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = PosDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, PosDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Ticket #${sale.ticketNumber}",
                                color = AmberPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "• $timeStr",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = "${sale.customerName} (${sale.paymentMethod.title})",
                            color = TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (sale.reference.isNotBlank()) {
                            Text("Ref: ${sale.reference}", color = TextMuted, fontSize = 10.sp)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", sale.totalUsd)}",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${String.format(Locale.US, "%.2f", sale.totalBs)} Bs.",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentBreakdownRow(
    title: String,
    amountUsd: Double,
    tagColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(tagColor)
            )
            Text(title, color = TextLightGrey, fontSize = 12.sp)
        }
        Text(
            text = "$${String.format(Locale.US, "%.2f", amountUsd)} USD",
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}
