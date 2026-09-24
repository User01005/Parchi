package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BillRepository
import com.example.model.BillEntity
import com.example.model.BillItem
import com.example.parser.VoiceBillParser
import com.example.voice.VoiceInputManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class AppScreen {
    Home,
    Receipt
}

enum class HomeNavTab {
    Receipts,
    Create,
    DailySales
}

data class BillUiState(
    val currentScreen: AppScreen = AppScreen.Home,
    val selectedHomeTab: HomeNavTab = HomeNavTab.Receipts,
    val searchQuery: String = "",
    val storeName: String = "Manmohan Di Hatti",
    val billNumber: String = generateBillNumber(),
    val formattedDateTime: String = getFormattedCurrentDateTime(),
    val customerName: String = "",
    val customerPhone: String = "",
    val customerHouseNo: String = "",
    val items: List<BillItem> = emptyList(), // Starts empty as requested
    val isListening: Boolean = false,
    val isProcessing: Boolean = false,
    val liveTranscript: String = "",
    val rmsLevel: Float = 0f,
    val statusMessage: String? = null,
    val showCustomerEditDialog: Boolean = false,
    val editingItem: BillItem? = null,
    val showEditOptionsDialog: Boolean = false, // Speak or Manual
    val showManualEditSheet: Boolean = false,
    val isReceiptFinished: Boolean = false,
    val currentViewingBillId: Long? = null
) {
    val totalAmount: Double
        get() = items.mapNotNull { it.price }.sum()

    val isAllVerified: Boolean
        get() = items.isNotEmpty() && items.all { it.isVerified }

    val unpricedItemsCount: Int
        get() = items.count { it.price == null }

    companion object {
        fun generateBillNumber(): String {
            return String.format("%04d", (1..9999).random())
        }

        fun getFormattedCurrentDateTime(): String {
            return SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault()).format(Date()).uppercase()
        }
    }
}

class BillViewModel(application: Application) : AndroidViewModel(application) {

    val repository: BillRepository
    private var voiceInputManager: VoiceInputManager? = null

    private val _uiState = MutableStateFlow(BillUiState())
    val uiState: StateFlow<BillUiState> = _uiState.asStateFlow()

    val savedBills: StateFlow<List<BillEntity>>

    // Filtered bills based on real-time search query
    val filteredBills: StateFlow<List<BillEntity>>

    init {
        val database = AppDatabase.getInstance(application)
        repository = BillRepository(database.billDao())
        savedBills = repository.allBills.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        filteredBills = combine(savedBills, _uiState) { bills, state ->
            val query = state.searchQuery.trim().lowercase()
            if (query.isEmpty()) {
                bills
            } else {
                bills.filter { bill ->
                    bill.billNumber.lowercase().contains(query) ||
                    bill.customerName.lowercase().contains(query) ||
                    bill.customerPhone.lowercase().contains(query) ||
                    bill.customerHouseNo.lowercase().contains(query) ||
                    bill.dateDisplay.lowercase().contains(query) ||
                    bill.itemsJson.lowercase().contains(query)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun selectHomeTab(tab: HomeNavTab) {
        if (tab == HomeNavTab.Create) {
            startNewReceipt()
        } else {
            _uiState.value = _uiState.value.copy(selectedHomeTab = tab)
        }
    }

    fun navigateToHome() {
        if (_uiState.value.isListening) {
            stopVoiceRecording()
        }
        _uiState.value = _uiState.value.copy(currentScreen = AppScreen.Home)
    }

    fun startNewReceipt() {
        if (_uiState.value.isListening) {
            stopVoiceRecording()
        }
        _uiState.value = BillUiState(
            currentScreen = AppScreen.Receipt,
            storeName = _uiState.value.storeName,
            billNumber = BillUiState.generateBillNumber(),
            formattedDateTime = BillUiState.getFormattedCurrentDateTime(),
            customerName = "",
            customerPhone = "",
            customerHouseNo = "",
            items = emptyList(), // Clean fresh empty receipt
            isReceiptFinished = false,
            currentViewingBillId = null
        )
    }

    fun openExistingBill(bill: BillEntity) {
        val parsedItems = repository.parseItemsJson(bill.itemsJson)
        _uiState.value = _uiState.value.copy(
            currentScreen = AppScreen.Receipt,
            billNumber = bill.billNumber,
            formattedDateTime = bill.formattedDateTime,
            customerName = bill.customerName,
            customerPhone = bill.customerPhone,
            customerHouseNo = bill.customerHouseNo,
            items = parsedItems,
            isReceiptFinished = true,
            currentViewingBillId = bill.id
        )
    }

    private val sessionTranscriptBuilder = StringBuilder()

    fun toggleVoiceRecording() {
        if (_uiState.value.isListening) {
            stopVoiceRecording()
        } else {
            startVoiceRecording()
        }
    }

    fun startVoiceRecording() {
        sessionTranscriptBuilder.clear()
        _uiState.value = _uiState.value.copy(
            isListening = true,
            isProcessing = false,
            liveTranscript = "",
            statusMessage = null
        )

        val context = getApplication<Application>().applicationContext
        voiceInputManager?.destroy()
        voiceInputManager = VoiceInputManager(
            context = context,
            onPartialResult = { partial ->
                _uiState.value = _uiState.value.copy(liveTranscript = partial)
            },
            onFinalResult = { finalResult ->
                if (finalResult.isNotBlank()) {
                    sessionTranscriptBuilder.append(" ").append(finalResult.trim())
                    _uiState.value = _uiState.value.copy(liveTranscript = finalResult)
                }
            },
            onError = { error ->
                Log.d("BillViewModel", "Voice recognizer event: $error")
                if (error.contains("permission", ignoreCase = true)) {
                    _uiState.value = _uiState.value.copy(
                        isListening = false,
                        isProcessing = false,
                        statusMessage = "Microphone permission required"
                    )
                }
            },
            onRmsChanged = { rms ->
                _uiState.value = _uiState.value.copy(rmsLevel = rms)
            }
        )
        voiceInputManager?.startListening()
    }

    fun stopVoiceRecording() {
        voiceInputManager?.stopListening()
        val pendingTranscript = _uiState.value.liveTranscript.trim()
        if (pendingTranscript.isNotBlank() && !sessionTranscriptBuilder.contains(pendingTranscript)) {
            sessionTranscriptBuilder.append(" ").append(pendingTranscript)
        }

        val fullSessionAudio = sessionTranscriptBuilder.toString().trim()

        if (fullSessionAudio.isNotBlank()) {
            _uiState.value = _uiState.value.copy(
                isListening = false,
                isProcessing = true,
                liveTranscript = ""
            )

            viewModelScope.launch {
                // Batch processing: wait 2 to 3 seconds as requested before displaying the finalized list
                delay(2400)
                processSpeechTranscript(fullSessionAudio)
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    isReceiptFinished = _uiState.value.items.isNotEmpty()
                )
            }
        } else {
            _uiState.value = _uiState.value.copy(
                isListening = false,
                isProcessing = false,
                liveTranscript = "",
                isReceiptFinished = _uiState.value.items.isNotEmpty()
            )
        }
    }

    fun processSpeechTranscript(transcript: String) {
        if (transcript.isBlank()) return

        val currentItems = _uiState.value.items.toMutableList()
        val nextSerial = currentItems.size + 1

        val parseResult = VoiceBillParser.parseTranscript(transcript, startingSerial = nextSerial)

        // Update customer details if spoken
        var updatedCustomerName = _uiState.value.customerName
        var updatedCustomerPhone = _uiState.value.customerPhone
        var updatedCustomerHouseNo = _uiState.value.customerHouseNo

        parseResult.customerInfo.name?.let { updatedCustomerName = it }
        parseResult.customerInfo.phone?.let { updatedCustomerPhone = it }
        parseResult.customerInfo.houseNo?.let { updatedCustomerHouseNo = it }

        currentItems.addAll(parseResult.items)

        val reindexed = currentItems.mapIndexed { index, item ->
            item.copy(serialNumber = index + 1)
        }

        _uiState.value = _uiState.value.copy(
            items = reindexed,
            customerName = updatedCustomerName,
            customerPhone = updatedCustomerPhone,
            customerHouseNo = updatedCustomerHouseNo,
            statusMessage = null, // Silent, clean UI
            liveTranscript = "",
            isReceiptFinished = reindexed.isNotEmpty()
        )

        // Automatically save receipt to Room database
        if (reindexed.isNotEmpty()) {
            saveCurrentReceiptSilently()
        }
    }

    fun toggleItemVerification(item: BillItem) {
        val updated = _uiState.value.items.map {
            if (it.id == item.id) it.copy(isVerified = !it.isVerified) else it
        }
        _uiState.value = _uiState.value.copy(items = updated)
        saveCurrentReceiptSilently()
    }

    fun openEditOptions() {
        _uiState.value = _uiState.value.copy(showEditOptionsDialog = true)
    }

    fun dismissEditOptions() {
        _uiState.value = _uiState.value.copy(showEditOptionsDialog = false)
    }

    fun onSelectEditSpeak() {
        _uiState.value = _uiState.value.copy(showEditOptionsDialog = false)
        startVoiceRecording()
    }

    fun onSelectEditManual() {
        _uiState.value = _uiState.value.copy(
            showEditOptionsDialog = false,
            showManualEditSheet = true
        )
    }

    fun dismissManualEditSheet() {
        _uiState.value = _uiState.value.copy(showManualEditSheet = false)
    }

    fun openEditItem(item: BillItem) {
        _uiState.value = _uiState.value.copy(editingItem = item)
    }

    fun dismissEditItem() {
        _uiState.value = _uiState.value.copy(editingItem = null)
    }

    fun updateItem(updatedItem: BillItem) {
        val updatedList = _uiState.value.items.map {
            if (it.id == updatedItem.id) updatedItem else it
        }
        _uiState.value = _uiState.value.copy(
            items = updatedList,
            editingItem = null,
            statusMessage = "Updated ${updatedItem.itemName}"
        )
        saveCurrentReceiptSilently()
    }

    fun deleteItem(item: BillItem) {
        val updatedList = _uiState.value.items.filterNot { it.id == item.id }
        val reindexed = updatedList.mapIndexed { index, current ->
            current.copy(serialNumber = index + 1)
        }
        _uiState.value = _uiState.value.copy(
            items = reindexed,
            statusMessage = "Removed ${item.itemName}",
            isReceiptFinished = reindexed.isNotEmpty()
        )
        saveCurrentReceiptSilently()
    }

    fun addNewItemDirectly(itemName: String, weightOrQty: String, price: Double?) {
        val current = _uiState.value.items.toMutableList()
        val newItem = BillItem(
            serialNumber = current.size + 1,
            itemName = itemName,
            weightOrQuantity = weightOrQty,
            price = price,
            isVerified = false
        )
        current.add(newItem)
        _uiState.value = _uiState.value.copy(
            items = current,
            statusMessage = "Added $itemName",
            isReceiptFinished = true
        )
        saveCurrentReceiptSilently()
    }

    fun openCustomerEdit() {
        _uiState.value = _uiState.value.copy(showCustomerEditDialog = true)
    }

    fun dismissCustomerEdit() {
        _uiState.value = _uiState.value.copy(showCustomerEditDialog = false)
    }

    fun saveCustomerInfo(name: String, phone: String, houseNo: String) {
        _uiState.value = _uiState.value.copy(
            customerName = name,
            customerPhone = phone,
            customerHouseNo = houseNo,
            showCustomerEditDialog = false,
            statusMessage = "Customer details updated"
        )
        saveCurrentReceiptSilently()
    }

    fun saveCurrentReceiptSilently() {
        val state = _uiState.value
        if (state.items.isEmpty()) return

        viewModelScope.launch {
            val id = repository.saveBill(
                billNumber = state.billNumber,
                formattedDateTime = state.formattedDateTime,
                customerName = state.customerName,
                customerPhone = state.customerPhone,
                customerHouseNo = state.customerHouseNo,
                items = state.items,
                totalAmount = state.totalAmount,
                isVerified = state.isAllVerified
            )
            _uiState.value = _uiState.value.copy(currentViewingBillId = id)
        }
    }

    fun deleteSavedBill(entity: BillEntity) {
        viewModelScope.launch {
            repository.deleteBill(entity)
            if (_uiState.value.currentViewingBillId == entity.id) {
                navigateToHome()
            }
        }
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        voiceInputManager?.destroy()
    }
}
