package com.example.receiptwallet.converters

import androidx.room.TypeConverter
import com.example.receiptwallet.util.Constants

class CurrencyConverter {
    @TypeConverter
    fun toCurrency(value: String) = enumValueOf<Constants.Currency>(value)

    @TypeConverter
    fun fromCurrency(value: Constants.Currency) = value.name
}