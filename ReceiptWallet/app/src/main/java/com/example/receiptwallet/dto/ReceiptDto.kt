package com.example.receiptwallet.dto

import androidx.room.util.EMPTY_STRING_ARRAY
import com.example.receiptwallet.util.Constants
import com.example.receiptwallet.util.Constants.EMPTY_DAYS_LEFT
import com.example.receiptwallet.util.Constants.EMPTY_STRING
import java.util.Date


data class ReceiptDto(
    var id: Int,
    val name: String,
    val buyingDate: Date,
    val guarantee: Int,
    var timeLeft: String = EMPTY_STRING,
    var daysLeft: Int = EMPTY_DAYS_LEFT,
    val pictureUUID: String,
    val category: Constants.Category,
    val cost: Double,
    val currency: Constants.Currency
)