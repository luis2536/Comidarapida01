package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.InventoryItem
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val inventory by viewModel.inventory.collectAsState()
    val purchases by viewModel.purchases.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }

    val categories = listOf("Todos", "Panes", "Carnes", "Salsas", "Complementos", "Bebidas", "Descartables")

    val lowStockCount = inventory.count { it.currentStock <= it.minStockAlert }
    val totalItems = inventory.size

    val filteredList = inventory.filter { item ->
        val matchesCat = if (selectedCategory == "Todos") true else item.category.equals(selectedCategory, ignoreCase = true)
        val matchesQuery = if (searchQuery.isBlank()) true else item.name.contains(searchQuery, ignoreCase = true)
        matchesCat && matchesQuery
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openPurchaseDialog() },
                containerColor = AmberPrimary,
                contentColor = PosDarkBg,
                modifier = Modifier.testTag("btn_add_purchase")
            ) {
                Icon(Icons.Default.AddShoppingCart, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Registrar Compra", fontWeight = FontWeight.Bold)
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
            // Summary Cards Header
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
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StatusBlueBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Inventory, contentDescription = null, tint = StatusBlue)
                            }
                            Column {
                                Text("Insumos Registrados", color = TextMuted, fontSize = 11.sp)
                                Text("$totalItems rubros", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = if (lowStockCount > 0) StatusRedBg else PosDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (lowStockCount > 0) StatusRed.copy(alpha = 0.5f) else PosDarkBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (lowStockCount > 0) StatusRed.copy(alpha = 0.2f) else StatusGreenBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (lowStockCount > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (lowStockCount > 0) StatusRed else StatusGreen
                                )
                            }
                            Column {
                                Text("Alerta Stock Bajo", color = TextMuted, fontSize = 11.sp)
                                Text("$lowStockCount crítico", color = if (lowStockCount > 0) StatusRed else StatusGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }

            // Search input
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar insumo o ingrediente...", color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AmberPrimary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberPrimary,
                        unfocusedBorderColor = PosDarkBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedContainerColor = PosDarkSurface,
                        unfocusedContainerColor = PosDarkSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Category Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSel = selectedCategory == cat
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberPrimary,
                                selectedLabelColor = PosDarkBg,
                                containerColor = PosDarkSurface,
                                labelColor = TextLightGrey
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSel,
                                borderColor = if (isSel) AmberPrimary else PosDarkBorder
                            )
                        )
                    }
                }
            }

            // Stock Items List
            items(filteredList, key = { it.id }) { item ->
                InventoryItemRow(item = item)
            }

            // Recent purchases history section
            if (purchases.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Historial Reciente de Compras e Ingresos",
                        color = AmberGlow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                items(purchases.take(5)) { p ->
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
                                Text(p.inventoryItemName, color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text("Surtido: +${p.quantityAdded} • ${p.supplier}", color = TextMuted, fontSize = 11.sp)
                            }
                            Text(
                                text = "-$${String.format(Locale.US, "%.2f", p.costUsd)}",
                                color = StatusYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryItemRow(item: InventoryItem) {
    val isLow = item.currentStock <= item.minStockAlert
    val percentage = (item.currentStock / (item.minStockAlert * 2.5)).coerceIn(0.0, 1.0).toFloat()

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = PosDarkSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isLow) StatusRed.copy(alpha = 0.5f) else PosDarkBorder
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Categoría: ${item.category}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isLow) StatusRedBg else StatusGreenBg,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isLow) StatusRed.copy(alpha = 0.4f) else StatusGreen.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = if (isLow) "BAJO STOCK" else "NORMAL",
                        color = if (isLow) StatusRed else StatusGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Current Stock & Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Stock Actual: ${String.format(Locale.US, "%.1f", item.currentStock)} ${item.unit}",
                    color = if (isLow) StatusRed else AmberGlow,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
                Text(
                    text = "Mínimo: ${item.minStockAlert} ${item.unit}",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { percentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isLow) StatusRed else AmberPrimary,
                trackColor = PosDarkBorder
            )
        }
    }
}
