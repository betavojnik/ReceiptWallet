package com.example.receiptwallet.converters

import androidx.room.TypeConverter
import com.example.receiptwallet.util.Constants

class CategoryConverter {
    @TypeConverter
    fun toCategory(value: String) = enumValueOf<Constants.Category>(value)

    @TypeConverter
    fun fromCategory(value: Constants.Category) = value.name
}
