package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.StreetFoodRepository
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ScreenTab(val title: String) {
    POS("POS"),
    STOCK("Stock"),
    STAFF("Personal"),
    CUADRE("Cuadre"),
    RUTA("Ruta GPS"),
    CONFIG("Ajustes")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository = StreetFoodRepository(application.applicationContext)

    // Repository flows
    val activeShift = repository.activeShift
    val products = repository.products
    val inventory = repository.inventory
    val staff = repository.staff
    val sales = repository.sales
    val purchases = repository.purchases
    val staffPayments = repository.staffPayments
    val deliveries = repository.deliveries
    val config = repository.config

    // UI Navigation State
    private val _currentScreen = MutableStateFlow(ScreenTab.POS)
    val currentScreen: StateFlow<ScreenTab> = _currentScreen.asStateFlow()

    // POS Cart State
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Todos")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _customerName = MutableStateFlow("")
    val customerName: StateFlow<String> = _customerName.asStateFlow()

    private val _customerPhone = MutableStateFlow("")
    val customerPhone: StateFlow<String> = _customerPhone.asStateFlow()

    private val _paymentMethod = MutableStateFlow(PaymentMethod.PUNTO_DE_VENTA)
    val paymentMethod: StateFlow<PaymentMethod> = _paymentMethod.asStateFlow()

    private val _transactionReference = MutableStateFlow("")
    val transactionReference: StateFlow<String> = _transactionReference.asStateFlow()

    private val _isDelivery = MutableStateFlow(false)
    val isDelivery: StateFlow<Boolean> = _isDelivery.asStateFlow()

    private val _deliveryAddress = MutableStateFlow("")
    val deliveryAddress: StateFlow<String> = _deliveryAddress.asStateFlow()

    private val _orderNotes = MutableStateFlow("")
    val orderNotes: StateFlow<String> = _orderNotes.asStateFlow()

    // Sale confirmation modal state
    private val _lastSaleSuccess = MutableStateFlow<Sale?>(null)
    val lastSaleSuccess: StateFlow<Sale?> = _lastSaleSuccess.asStateFlow()

    // UI Dialog flags
    private val _showShiftDialog = MutableStateFlow(false)
    val showShiftDialog: StateFlow<Boolean> = _showShiftDialog.asStateFlow()

    private val _showPurchaseDialog = MutableStateFlow(false)
    val showPurchaseDialog: StateFlow<Boolean> = _showPurchaseDialog.asStateFlow()

    private val _showStaffDialog = MutableStateFlow(false)
    val showStaffDialog: StateFlow<Boolean> = _showStaffDialog.asStateFlow()

    private val _staffToPay = MutableStateFlow<StaffMember?>(null)
    val staffToPay: StateFlow<StaffMember?> = _staffToPay.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // If no shift is open at launch, show the shift opening dialog
        if (activeShift.value == null || !activeShift.value!!.isActive) {
            _showShiftDialog.value = true
        }
    }

    fun setScreen(tab: ScreenTab) {
        _currentScreen.value = tab
    }

    fun setSelectedCategory(cat: String) {
        _selectedCategory.value = cat
    }

    // ==========================================
    // CART OPERATIONS
    // ==========================================
    fun addToCart(product: Product) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        if (index != -1) {
            val item = current[index]
            current[index] = item.copy(quantity = item.quantity + 1)
        } else {
            current.add(CartItem(product = product, quantity = 1))
        }
        _cartItems.value = current
    }

    fun updateCartQuantity(productId: String, delta: Int) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index != -1) {
            val item = current[index]
            val newQty = item.quantity + delta
            if (newQty <= 0) {
                current.removeAt(index)
            } else {
                current[index] = item.copy(quantity = newQty)
            }
            _cartItems.value = current
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _customerName.value = ""
        _customerPhone.value = ""
        _transactionReference.value = ""
        _isDelivery.value = false
        _deliveryAddress.value = ""
        _orderNotes.value = ""
    }

    fun setCustomerName(name: String) { _customerName.value = name }
    fun setCustomerPhone(phone: String) { _customerPhone.value = phone }
    fun setPaymentMethod(method: PaymentMethod) { _paymentMethod.value = method }
    fun setTransactionReference(ref: String) { _transactionReference.value = ref }
    fun setIsDelivery(isDel: Boolean) { _isDelivery.value = isDel }
    fun setDeliveryAddress(addr: String) { _deliveryAddress.value = addr }
    fun setOrderNotes(notes: String) { _orderNotes.value = notes }

    // ==========================================
    // PROCESS SALE
    // ==========================================
    fun processOrder(context: Context) {
        if (_cartItems.value.isEmpty()) {
            Toast.makeText(context, "El carrito está vacío. Agrega productos.", Toast.LENGTH_SHORT).show()
            return
        }

        val result = repository.processSale(
            customerName = _customerName.value,
            customerPhone = _customerPhone.value,
            cartItems = _cartItems.value,
            paymentMethod = _paymentMethod.value,
            reference = _transactionReference.value,
            isDelivery = _isDelivery.value,
            deliveryAddress = _deliveryAddress.value,
            notes = _orderNotes.value
        )

        result.onSuccess { sale ->
            _lastSaleSuccess.value = sale
            clearCart()
            Toast.makeText(context, "¡Venta registrada exitosamente! Ticket #${sale.ticketNumber}", Toast.LENGTH_LONG).show()
        }.onFailure { error ->
            _errorMessage.value = error.message
            Toast.makeText(context, error.message ?: "Error al procesar", Toast.LENGTH_LONG).show()
        }
    }

    fun dismissLastSaleDialog() {
        _lastSaleSuccess.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // ==========================================
    // SHIFT ACTIONS
    // ==========================================
    fun openShiftDialog() { _showShiftDialog.value = true }
    fun closeShiftDialog() { _showShiftDialog.value = false }

    fun startShift(shiftType: String, operatorName: String, initialUsd: Double, initialBs: Double) {
        repository.startShift(shiftType, operatorName, initialUsd, initialBs)
        _showShiftDialog.value = false
        _currentScreen.value = ScreenTab.POS
    }

    fun closeActiveShift(context: Context) {
        repository.closeShift()
        Toast.makeText(context, "Turno cerrado exitosamente.", Toast.LENGTH_SHORT).show()
        _showShiftDialog.value = true
    }

    // ==========================================
    // INVENTORY / PURCHASES
    // ==========================================
    fun openPurchaseDialog() { _showPurchaseDialog.value = true }
    fun closePurchaseDialog() { _showPurchaseDialog.value = false }

    fun registerPurchase(invId: String, qty: Double, cost: Double, supplier: String) {
        repository.registerPurchase(invId, qty, cost, supplier)
        _showPurchaseDialog.value = false
    }

    // ==========================================
    // STAFF ACTIONS
    // ==========================================
    fun openStaffDialog() { _showStaffDialog.value = true }
    fun closeStaffDialog() { _showStaffDialog.value = false }

    fun addStaff(name: String, role: String, dailyUsd: Double, phone: String) {
        repository.addStaffMember(name, role, dailyUsd, phone)
        _showStaffDialog.value = false
    }

    fun toggleStaffAttendance(id: String) {
        repository.toggleStaffAttendance(id)
    }

    fun openPayStaffDialog(member: StaffMember) {
        _staffToPay.value = member
    }

    fun closePayStaffDialog() {
        _staffToPay.value = null
    }

    fun submitStaffPayment(staffId: String, amount: Double, concept: String) {
        repository.payStaff(staffId, amount, concept)
        _staffToPay.value = null
    }

    // ==========================================
    // DELIVERIES
    // ==========================================
    fun advanceDeliveryStep(deliveryId: String) {
        repository.advanceDeliveryStep(deliveryId)
    }

    fun assignCustomerGps(deliveryId: String, lat: Double, lng: Double, address: String) {
        repository.assignCustomerGps(deliveryId, lat, lng, address)
    }

    // ==========================================
    // CONFIG
    // ==========================================
    fun updateRate(newRate: Double) {
        repository.updateExchangeRate(newRate)
    }

    fun updateConfig(cfg: AppConfig) {
        repository.updateConfig(cfg)
    }

    fun resetDatabase() {
        repository.seedInitialData()
        clearCart()
    }

    // ==========================================
    // INTENTS (WHATSAPP, SHARE, EXPORT)
    // ==========================================
    fun openWhatsApp(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareFormalReport(context: Context) {
        val report = repository.generateFormalAuditReport()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Auditoría de Cierre - Street Food Pro")
            putExtra(Intent.EXTRA_TEXT, report)
        }
        val chooser = Intent.createChooser(intent, "Compartir Auditoría / Imprimir")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun shareSalesCsv(context: Context) {
        val csv = repository.exportSalesCsv()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/comma-separated-values"
            putExtra(Intent.EXTRA_SUBJECT, "Ventas_StreetFoodPro.csv")
            putExtra(Intent.EXTRA_TEXT, csv)
        }
        val chooser = Intent.createChooser(intent, "Exportar Archivo CSV / Excel")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
