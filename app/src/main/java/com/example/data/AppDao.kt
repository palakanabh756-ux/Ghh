package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
  // Wallets
  @Query("SELECT * FROM wallets ORDER BY balance DESC")
  fun getAllWallets(): Flow<List<WalletEntity>>

  @Query("SELECT * FROM wallets WHERE currencyCode = :code LIMIT 1")
  suspend fun getWalletDirect(code: String): WalletEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateWallet(wallet: WalletEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertWallets(wallets: List<WalletEntity>)

  // Recipients
  @Query("SELECT * FROM recipients ORDER BY isFavorite DESC, name ASC")
  fun getAllRecipients(): Flow<List<RecipientEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRecipient(recipient: RecipientEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRecipients(recipients: List<RecipientEntity>)

  @Delete
  suspend fun deleteRecipient(recipient: RecipientEntity)

  // Transfers
  @Query("SELECT * FROM transfers ORDER BY timestampMillis DESC")
  fun getAllTransfers(): Flow<List<TransferEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransfer(transfer: TransferEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransfers(transfers: List<TransferEntity>)

  @Query("UPDATE transfers SET status = :status WHERE id = :id")
  suspend fun updateTransferStatus(id: Long, status: String)
}
