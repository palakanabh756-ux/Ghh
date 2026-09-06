package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallets")
data class WalletEntity(
  @PrimaryKey val currencyCode: String,
  val balance: Double,
  val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "recipients")
data class RecipientEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val country: String,
  val currencyCode: String,
  val accountNumber: String,
  val bankOrProvider: String,
  val avatarColorHex: String = "#00875A",
  val isFavorite: Boolean = true
)

@Entity(tableName = "transfers")
data class TransferEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val referenceCode: String,
  val senderCurrency: String,
  val senderAmount: Double,
  val recipientCurrency: String,
  val recipientAmount: Double,
  val exchangeRate: Double,
  val transferFee: Double = 0.0, // Guaranteed 0 fees!
  val bankSavings: Double = 0.0,
  val recipientName: String,
  val recipientCountry: String,
  val deliveryMethod: String, // "Instant Bank Transfer", "Mobile Wallet", "Debit Card"
  val status: String, // "Delivered", "In Transit", "Processing"
  val timestampMillis: Long = System.currentTimeMillis(),
  val note: String = "Zero-fee international transfer"
)
