package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.PaymentRepository
import com.example.data.RecipientEntity
import com.example.data.TransferEntity
import com.example.data.WalletEntity
import com.example.model.CurrencyCatalog
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SendUiState(
  val isOpen: Boolean = false,
  val fromCurrency: String = "USD",
  val toCurrency: String = "INR",
  val amountInput: String = "300",
  val recipient: RecipientEntity? = null,
  val recipientName: String = "",
  val recipientCountry: String = "India",
  val recipientAccount: String = "",
  val recipientBank: String = "Direct Bank",
  val deliveryMethod: String = "Instant Bank Wire",
  val note: String = "",
  val isSending: Boolean = false,
  val completedTransfer: TransferEntity? = null
)

data class ConvertUiState(
  val isOpen: Boolean = false,
  val fromCurrency: String = "USD",
  val toCurrency: String = "EUR",
  val amountInput: String = "100",
  val isConverting: Boolean = false
)

data class TopUpUiState(
  val isOpen: Boolean = false,
  val currencyCode: String = "USD",
  val amountInput: String = "500",
  val method: String = "Instant Debit Card"
)

class PaymentViewModel(application: Application) : AndroidViewModel(application) {
  private val repository: PaymentRepository

  val wallets: StateFlow<List<WalletEntity>>
  val recipients: StateFlow<List<RecipientEntity>>
  val transfers: StateFlow<List<TransferEntity>>

  private val _sendUiState = MutableStateFlow(SendUiState())
  val sendUiState: StateFlow<SendUiState> = _sendUiState.asStateFlow()

  private val _convertUiState = MutableStateFlow(ConvertUiState())
  val convertUiState: StateFlow<ConvertUiState> = _convertUiState.asStateFlow()

  private val _topUpUiState = MutableStateFlow(TopUpUiState())
  val topUpUiState: StateFlow<TopUpUiState> = _topUpUiState.asStateFlow()

  private val _selectedTransferForReceipt = MutableStateFlow<TransferEntity?>(null)
  val selectedTransferForReceipt: StateFlow<TransferEntity?> = _selectedTransferForReceipt.asStateFlow()

  private val _activeTab = MutableStateFlow(0) // 0: Home, 1: Recipients, 2: Zero-Fee Rates
  val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

  private val _transactionFilter = MutableStateFlow("All") // "All", "Sent", "Exchanges"
  val transactionFilter: StateFlow<String> = _transactionFilter.asStateFlow()

  private val _snackbarMessage = MutableSharedFlow<String>()
  val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

  init {
    val database = AppDatabase.getDatabase(application, viewModelScope)
    repository = PaymentRepository(database.appDao(), viewModelScope)

    wallets = repository.allWallets.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      emptyList()
    )

    recipients = repository.allRecipients.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      emptyList()
    )

    transfers = repository.allTransfers.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      emptyList()
    )
  }

  // Calculate total balance converted to USD for quick glance
  val totalBalanceInUsd: StateFlow<Double> = wallets.combine(transfers) { walletList, _ ->
    walletList.sumOf { wallet ->
      val curr = CurrencyCatalog.findByCode(wallet.currencyCode)
      wallet.balance * curr.rateToUsd
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

  // Total money saved by using ZeroPay zero-fees
  val totalSavedFees: StateFlow<Double> = transfers.combine(wallets) { transferList, _ ->
    transferList.sumOf { it.bankSavings }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

  fun setActiveTab(index: Int) {
    _activeTab.value = index
  }

  fun setTransactionFilter(filter: String) {
    _transactionFilter.value = filter
  }

  fun openSendSheet(prefillRecipient: RecipientEntity? = null, prefillCurrency: String? = null) {
    _sendUiState.value = SendUiState(
      isOpen = true,
      recipient = prefillRecipient,
      fromCurrency = prefillCurrency ?: "USD",
      toCurrency = prefillRecipient?.currencyCode ?: "INR",
      recipientName = prefillRecipient?.name ?: "",
      recipientCountry = prefillRecipient?.country ?: "India",
      recipientAccount = prefillRecipient?.accountNumber ?: "",
      recipientBank = prefillRecipient?.bankOrProvider ?: "Direct Wire",
      completedTransfer = null
    )
  }

  fun closeSendSheet() {
    _sendUiState.value = _sendUiState.value.copy(isOpen = false, completedTransfer = null)
  }

  fun updateSendAmount(amount: String) {
    val clean = amount.filter { it.isDigit() || it == '.' }
    _sendUiState.value = _sendUiState.value.copy(amountInput = clean)
  }

  fun updateSendFromCurrency(code: String) {
    _sendUiState.value = _sendUiState.value.copy(fromCurrency = code)
  }

  fun updateSendToCurrency(code: String) {
    _sendUiState.value = _sendUiState.value.copy(toCurrency = code)
  }

  fun swapCurrencies() {
    val current = _sendUiState.value
    _sendUiState.value = current.copy(
      fromCurrency = current.toCurrency,
      toCurrency = current.fromCurrency
    )
  }

  fun selectRecipient(recipient: RecipientEntity) {
    _sendUiState.value = _sendUiState.value.copy(
      recipient = recipient,
      recipientName = recipient.name,
      recipientCountry = recipient.country,
      recipientAccount = recipient.accountNumber,
      recipientBank = recipient.bankOrProvider,
      toCurrency = recipient.currencyCode
    )
  }

  fun updateCustomRecipientDetails(
    name: String,
    country: String,
    account: String,
    bank: String
  ) {
    _sendUiState.value = _sendUiState.value.copy(
      recipientName = name,
      recipientCountry = country,
      recipientAccount = account,
      recipientBank = bank
    )
  }

  fun updateDeliveryMethod(method: String) {
    _sendUiState.value = _sendUiState.value.copy(deliveryMethod = method)
  }

  fun updateNote(note: String) {
    _sendUiState.value = _sendUiState.value.copy(note = note)
  }

  fun executeTransfer() {
    val state = _sendUiState.value
    val amount = state.amountInput.toDoubleOrNull() ?: 0.0
    if (amount <= 0.0) {
      viewModelScope.launch { _snackbarMessage.emit("Please enter a valid transfer amount.") }
      return
    }
    if (state.recipientName.isBlank()) {
      viewModelScope.launch { _snackbarMessage.emit("Please specify a recipient name.") }
      return
    }

    _sendUiState.value = state.copy(isSending = true)

    viewModelScope.launch {
      val result = repository.sendInternationalTransfer(
        senderCurrency = state.fromCurrency,
        senderAmount = amount,
        recipientCurrency = state.toCurrency,
        recipientName = state.recipientName,
        recipientCountry = state.recipientCountry,
        recipientAccount = state.recipientAccount,
        recipientBank = state.recipientBank,
        deliveryMethod = state.deliveryMethod,
        note = state.note
      )

      result.onSuccess { transfer ->
        _sendUiState.value = _sendUiState.value.copy(
          isSending = false,
          completedTransfer = transfer
        )
        val saved = String.format("%.2f", transfer.bankSavings)
        _snackbarMessage.emit("Sent ${state.fromCurrency} $amount with $0 fees! Saved $$saved vs banks.")
      }.onFailure { error ->
        _sendUiState.value = _sendUiState.value.copy(isSending = false)
        _snackbarMessage.emit(error.message ?: "Transfer could not be completed.")
      }
    }
  }

  // Convert Sheet functions
  fun openConvertSheet(fromCurrency: String = "USD") {
    val to = if (fromCurrency == "USD") "EUR" else "USD"
    _convertUiState.value = ConvertUiState(
      isOpen = true,
      fromCurrency = fromCurrency,
      toCurrency = to,
      amountInput = "100"
    )
  }

  fun closeConvertSheet() {
    _convertUiState.value = _convertUiState.value.copy(isOpen = false)
  }

  fun updateConvertAmount(amount: String) {
    _convertUiState.value = _convertUiState.value.copy(amountInput = amount.filter { it.isDigit() || it == '.' })
  }

  fun updateConvertCurrencies(from: String, to: String) {
    _convertUiState.value = _convertUiState.value.copy(fromCurrency = from, toCurrency = to)
  }

  fun executeConversion() {
    val state = _convertUiState.value
    val amount = state.amountInput.toDoubleOrNull() ?: 0.0
    if (amount <= 0.0) return

    _convertUiState.value = state.copy(isConverting = true)
    viewModelScope.launch {
      val result = repository.convertCurrencyZeroFee(state.fromCurrency, state.toCurrency, amount)
      _convertUiState.value = _convertUiState.value.copy(isConverting = false)
      result.onSuccess {
        _convertUiState.value = _convertUiState.value.copy(isOpen = false)
        _snackbarMessage.emit("Converted $amount ${state.fromCurrency} to ${state.toCurrency} with 0% markup!")
      }.onFailure { err ->
        _snackbarMessage.emit(err.message ?: "Conversion failed.")
      }
    }
  }

  // Top Up Dialog functions
  fun openTopUpSheet(currencyCode: String = "USD") {
    _topUpUiState.value = TopUpUiState(isOpen = true, currencyCode = currencyCode)
  }

  fun closeTopUpSheet() {
    _topUpUiState.value = _topUpUiState.value.copy(isOpen = false)
  }

  fun updateTopUpAmount(amount: String) {
    _topUpUiState.value = _topUpUiState.value.copy(amountInput = amount.filter { it.isDigit() || it == '.' })
  }

  fun updateTopUpCurrency(code: String) {
    _topUpUiState.value = _topUpUiState.value.copy(currencyCode = code)
  }

  fun executeTopUp() {
    val state = _topUpUiState.value
    val amount = state.amountInput.toDoubleOrNull() ?: 0.0
    if (amount <= 0) return

    viewModelScope.launch {
      repository.topUpWallet(state.currencyCode, amount)
      _topUpUiState.value = _topUpUiState.value.copy(isOpen = false)
      _snackbarMessage.emit("Added ${state.currencyCode} ${String.format("%.2f", amount)} with zero deposit fees.")
    }
  }

  // Receipt viewing
  fun viewTransferReceipt(transfer: TransferEntity) {
    _selectedTransferForReceipt.value = transfer
  }

  fun closeReceipt() {
    _selectedTransferForReceipt.value = null
  }

  fun addNewRecipient(name: String, country: String, currency: String, account: String, bank: String) {
    viewModelScope.launch {
      repository.addRecipient(
        RecipientEntity(
          name = name,
          country = country,
          currencyCode = currency,
          accountNumber = account,
          bankOrProvider = bank,
          isFavorite = true
        )
      )
      _snackbarMessage.emit("Recipient $name added successfully!")
    }
  }
}
