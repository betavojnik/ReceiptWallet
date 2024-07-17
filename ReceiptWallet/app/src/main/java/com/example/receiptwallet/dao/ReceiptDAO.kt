package com.example.receiptwallet.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.receiptwallet.models.Receipt

@Dao
interface ReceiptDAO {
    @Upsert
    suspend fun upsertReceipt(receipt: Receipt)

    @Delete
    suspend fun deleteReceipt(receipt: Receipt)

    @Query("SELECT * FROM receipt_table ORDER BY warrantyLength ASC")
    suspend fun getAllReceipts(): List<Receipt>

    @Update
    suspend fun updateReceipt(receipt: Receipt)

}