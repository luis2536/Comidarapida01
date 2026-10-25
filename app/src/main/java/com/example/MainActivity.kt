package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.ScreenTab
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PosDarkBg

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                StreetFoodApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun StreetFoodApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val activeShift by viewModel.activeShift.collectAsStateWithLifecycle()
    val config by viewModel.config.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val inventory by viewModel.inventory.collectAsStateWithLifecycle()

    val showShiftDialog by viewModel.showShiftDialog.collectAsStateWithLifecycle()
    val showPurchaseDialog by viewModel.showPurchaseDialog.collectAsStateWithLifecycle()
    val staffToPay by viewModel.staffToPay.collectAsStateWithLifecycle()
    val lastSaleSuccess by viewModel.lastSaleSuccess.collectAsStateWithLifecycle()

    val cartItemCount = remember(cartItems) { cartItems.sumOf { it.quantity } }

    // Hardware back navigation: return to POS if on another tab
    BackHandler(enabled = currentScreen != ScreenTab.POS) {
        viewModel.setScreen(ScreenTab.POS)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(PosDarkBg),
        topBar = {
            StreetFoodTopBar(
                activeShift = activeShift,
                exchangeRate = config.exchangeRate,
                onOpenShiftClick = { viewModel.openShiftDialog() }
            )
        },
        bottomBar = {
            StreetFoodBottomNav(
                currentTab = currentScreen,
                onTabSelected = { viewModel.setScreen(it) },
                cartItemCount = cartItemCount
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(PosDarkBg)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    ScreenTab.POS -> PosScreen(viewModel = viewModel)
                    ScreenTab.STOCK -> StockScreen(viewModel = viewModel)
                    ScreenTab.STAFF -> StaffScreen(viewModel = viewModel)
                    ScreenTab.CUADRE -> CuadreScreen(viewModel = viewModel)
                    ScreenTab.RUTA -> RutaScreen(viewModel = viewModel)
                    ScreenTab.CONFIG -> ConfigScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Modal Dialogs
    if (showShiftDialog) {
        ShiftOpenDialog(
            activeShift = activeShift,
            onStartShift = { type, op, usd, bs ->
                viewModel.startShift(type, op, usd, bs)
            },
            onCloseShift = {
                viewModel.closeActiveShift(context)
            },
            onDismiss = {
                viewModel.closeShiftDialog()
            }
        )
    }

    if (showPurchaseDialog) {
        NewPurchaseDialog(
            inventoryItems = inventory,
            onRegisterPurchase = { invId, qty, cost, supplier ->
                viewModel.registerPurchase(invId, qty, cost, supplier)
            },
            onDismiss = { viewModel.closePurchaseDialog() }
        )
    }

    staffToPay?.let { member ->
        PayStaffDialog(
            staff = member,
            onSubmitPayment = { amt, concept ->
                viewModel.submitStaffPayment(member.id, amt, concept)
            },
            onDismiss = { viewModel.closePayStaffDialog() }
        )
    }

    lastSaleSuccess?.let { sale ->
        val whatsappUrl = remember(sale) {
            viewModel.repository.generateWhatsAppTicketUrl(sale)
        }
        DigitalReceiptDialog(
            sale = sale,
            whatsAppUrl = whatsappUrl,
            onSendWhatsApp = { url ->
                viewModel.openWhatsApp(context, url)
            },
            onDismiss = {
                viewModel.dismissLastSaleDialog()
            }
        )
    }
}
