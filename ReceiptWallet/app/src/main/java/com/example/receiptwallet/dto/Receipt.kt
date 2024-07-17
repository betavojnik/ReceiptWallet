package com.example.receiptwallet.dto

import com.example.receiptwallet.util.Constants
import java.util.Date


data class Receipt(
    var id: Int,
    val name: String,
    val buyingDate: Date,
    val guarantee: Int,
    var timeLeft: String,
    var daysLeft: Int,
    val pictureUUID: String,
    val category: Constants.Category,
    val cost: Double,
    val currency: Constants.Currency
)