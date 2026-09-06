package com.example.data

import com.example.model.CurrencyCatalog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.random.Random

class PaymentRepository(
  private val dao: AppDao,
  private val coroutineScope: CoroutineScope
) {
  val allWallets: Flow<List<WalletEntity>> = dao.getAllWallets()
  val allRecipients: Flow<List<RecipientEntity>> = dao.getAllRecipients()
  val allTransfers: Flow<List<TransferEntity>> = dao.getAllTransfers()

  suspend fun sendInternationalTransfer(
    senderCurrency: String,
    senderAmount: Double,
    recipientCurrency: String,
    recipientName: String,
    recipientCountry: String,
    recipientAccount: String,
    recipientBank: String,
    deliveryMethod: String,
    note: String
  ): Result<TransferEntity> {
    val wallet = dao.getWalletDirect(senderCurrency)
    val currentBalance = wallet?.balance ?: 0.0

    if (currentBalance < senderAmount) {
      return Result.failure(
        Exception("Insufficient balance in your $senderCurrency wallet ($${String.format("%.2f", currentBalance)})")
      )
    }

    // Deduct from wallet
    dao.insertOrUpdateWallet(
      WalletEntity(
        currencyCode = senderCurrency,
        balance = currentBalance - senderAmount,
        lastUpdated = System.currentTimeMillis()
      )
    )

    // Calculate mid-market rate (0% markup, 0 fees)
    val rate = CurrencyCatalog.calculateExchangeRate(senderCurrency, recipientCurrency)
    val recipientAmount = senderAmount * rate
    val savings = CurrencyCatalog.calculateBankSavings(senderAmount, senderCurrency)
    val randomRef = "ZP-${Random.nextInt(100000, 999999)}"

    val transfer = TransferEntity(
      referenceCode = randomRef,
      senderCurrency = senderCurrency,
      senderAmount = senderAmount,
      recipientCurrency = recipientCurrency,
      recipientAmount = recipientAmount,
      exchangeRate = rate,
      transferFee = 0.0,
      bankSavings = savings,
      recipientName = recipientName,
      recipientCountry = recipientCountry,
      deliveryMethod = deliveryMethod,
      status = "In Transit",
      timestampMillis = System.currentTimeMillis(),
      note = note.ifBlank { "Zero-fee international transfer" }
    )

    val id = dao.insertTransfer(transfer)

    // Auto-save recipient if not already existing
    coroutineScope.launch(Dispatchers.IO) {
      dao.insertRecipient(
        RecipientEntity(
          name = recipientName,
          country = recipientCountry,
          currencyCode = recipientCurrency,
          accountNumber = recipientAccount.ifBlank { "Direct Wire" },
          bankOrProvider = recipientBank.ifBlank { "International Bank" },
          avatarColorHex = pickColorForName(recipientName),
          isFavorite = true
        )
      )

      // Simulate ultra-fast zero-fee clearing network
      delay(2500)
      dao.updateTransferStatus(id, "Delivered")
    }

    return Result.success(transfer.copy(id = id))
  }

  suspend fun topUpWallet(currencyCode: String, amount: Double) {
    val existing = dao.getWalletDirect(currencyCode)
    val newBalance = (existing?.balance ?: 0.0) + amount
    dao.insertOrUpdateWallet(
      WalletEntity(
        currencyCode = currencyCode,
        balance = newBalance,
        lastUpdated = System.currentTimeMillis()
      )
    )
  }

  suspend fun convertCurrencyZeroFee(
    fromCode: String,
    toCode: String,
    fromAmount: Double
  ): Result<Unit> {
    val fromWallet = dao.getWalletDirect(fromCode)
    val fromBalance = fromWallet?.balance ?: 0.0
    if (fromBalance < fromAmount) {
      return Result.failure(Exception("Insufficient balance in $fromCode wallet"))
    }

    val rate = CurrencyCatalog.calculateExchangeRate(fromCode, toCode)
    val toAmount = fromAmount * rate

    val toWallet = dao.getWalletDirect(toCode)
    val toBalance = toWallet?.balance ?: 0.0

    dao.insertOrUpdateWallet(
      WalletEntity(
        currencyCode = fromCode,
        balance = fromBalance - fromAmount,
        lastUpdated = System.currentTimeMillis()
      )
    )

    dao.insertOrUpdateWallet(
      WalletEntity(
        currencyCode = toCode,
        balance = toBalance + toAmount,
        lastUpdated = System.currentTimeMillis()
      )
    )

    // Record internal conversion transfer
    val randomRef = "ZP-CV${Random.nextInt(10000, 99999)}"
    dao.insertTransfer(
      TransferEntity(
        referenceCode = randomRef,
        senderCurrency = fromCode,
        senderAmount = fromAmount,
        recipientCurrency = toCode,
        recipientAmount = toAmount,
        exchangeRate = rate,
        transferFee = 0.0,
        bankSavings = CurrencyCatalog.calculateBankSavings(fromAmount, fromCode),
        recipientName = "Self ($toCode Balance)",
        recipientCountry = "Internal Wallet",
        deliveryMethod = "Instant 0-Fee FX",
        status = "Delivered",
        timestampMillis = System.currentTimeMillis(),
        note = "Instant multi-currency exchange"
      )
    )

    return Result.success(Unit)
  }

  suspend fun addRecipient(recipient: RecipientEntity): Long {
    return dao.insertRecipient(recipient)
  }

  suspend fun deleteRecipient(recipient: RecipientEntity) {
    dao.deleteRecipient(recipient)
  }

  private fun pickColorForName(name: String): String {
    val colors = listOf("#00875A", "#2563EB", "#7C3AED", "#DB2777", "#D97706", "#0D9488")
    val index = (name.hashCode() and 0x7FFFFFFF) % colors.size
    return colors[index]
  }
}
