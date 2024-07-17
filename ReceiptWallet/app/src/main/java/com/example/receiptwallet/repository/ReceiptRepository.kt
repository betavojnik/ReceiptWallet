package com.example.receiptwallet.repository

import com.example.receiptwallet.dao.ReceiptDAO
import com.example.receiptwallet.models.Receipt

class ReceiptRepository(private val receiptDAO: ReceiptDAO) {
    suspend fun addNewReceipt(receipt: Receipt) {
        receiptDAO.upsertReceipt(receipt)
    }

    suspend fun removeReceipt(receipt: Receipt) {
        receiptDAO.deleteReceipt(receipt)
    }

    suspend fun getAllReceipts(): List<Receipt> {
        return receiptDAO.getAllReceipts()
    }

    suspend fun updateReceipt(receipt: Receipt) {
        receiptDAO.updateReceipt(receipt)
    }

}