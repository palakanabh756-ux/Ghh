package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [WalletEntity::class, RecipientEntity::class, TransferEntity::class],
  version = 1,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun appDao(): AppDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "zeropay_database"
        )
          .addCallback(DatabaseCallback(scope))
          .build()
        INSTANCE = instance
        instance
      }
    }

    private class DatabaseCallback(
      private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          scope.launch(Dispatchers.IO) {
            populateInitialData(database.appDao())
          }
        }
      }

      suspend fun populateInitialData(dao: AppDao) {
        // Starter wallets
        dao.insertWallets(
          listOf(
            WalletEntity("USD", 2850.00),
            WalletEntity("EUR", 1420.00),
            WalletEntity("GBP", 980.00)
          )
        )

        // Starter international recipients
        dao.insertRecipients(
          listOf(
            RecipientEntity(
              name = "Priya Sharma",
              country = "India",
              currencyCode = "INR",
              accountNumber = "•••• 8291",
              bankOrProvider = "State Bank of India",
              avatarColorHex = "#00875A",
              isFavorite = true
            ),
            RecipientEntity(
              name = "Elena Müller",
              country = "Germany",
              currencyCode = "EUR",
              accountNumber = "DE89 •••• 4192",
              bankOrProvider = "Deutsche Bank",
              avatarColorHex = "#2563EB",
              isFavorite = true
            ),
            RecipientEntity(
              name = "Kenji Takahashi",
              country = "Japan",
              currencyCode = "JPY",
              accountNumber = "•••• 7412",
              bankOrProvider = "Mitsubishi UFJ",
              avatarColorHex = "#DB2777",
              isFavorite = true
            ),
            RecipientEntity(
              name = "Carlos Mendez",
              country = "Mexico",
              currencyCode = "MXN",
              accountNumber = "•••• 9012",
              bankOrProvider = "BBVA Bancomer",
              avatarColorHex = "#D97706",
              isFavorite = false
            ),
            RecipientEntity(
              name = "Sarah Jenkins",
              country = "United Kingdom",
              currencyCode = "GBP",
              accountNumber = "•••• 3381",
              bankOrProvider = "Barclays Bank",
              avatarColorHex = "#7C3AED",
              isFavorite = true
            )
          )
        )

        // Starter Zero-Fee Transfers
        val now = System.currentTimeMillis()
        dao.insertTransfers(
          listOf(
            TransferEntity(
              referenceCode = "ZP-948210",
              senderCurrency = "USD",
              senderAmount = 350.00,
              recipientCurrency = "INR",
              recipientAmount = 29166.00,
              exchangeRate = 83.33,
              transferFee = 0.0,
              bankSavings = 38.30,
              recipientName = "Priya Sharma",
              recipientCountry = "India",
              deliveryMethod = "Instant Bank Transfer",
              status = "Delivered",
              timestampMillis = now - 3600000L * 2,
              note = "Tuition & living support"
            ),
            TransferEntity(
              referenceCode = "ZP-841923",
              senderCurrency = "USD",
              senderAmount = 500.00,
              recipientCurrency = "EUR",
              recipientAmount = 460.83,
              exchangeRate = 0.921,
              transferFee = 0.0,
              bankSavings = 44.00,
              recipientName = "Elena Müller",
              recipientCountry = "Germany",
              deliveryMethod = "Instant SEPA Transfer",
              status = "Delivered",
              timestampMillis = now - 86400000L * 1,
              note = "Apartment deposit"
            ),
            TransferEntity(
              referenceCode = "ZP-719302",
              senderCurrency = "USD",
              senderAmount = 220.00,
              recipientCurrency = "JPY",
              recipientAmount = 32835.00,
              exchangeRate = 149.25,
              transferFee = 0.0,
              bankSavings = 33.36,
              recipientName = "Kenji Takahashi",
              recipientCountry = "Japan",
              deliveryMethod = "Digital Wallet",
              status = "Delivered",
              timestampMillis = now - 86400000L * 3,
              note = "Gift payment"
            )
          )
        )
      }
    }
  }
}
