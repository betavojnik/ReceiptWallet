package com.example.receiptwallet.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.receiptwallet.util.Constants
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "receipt_table")
@Parcelize
data class Receipt(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val pictureUUID: String,
    var name: String,
    var purchaseDate: String,
    var warrantyLength: Int,
    var category: Constants.Category,
    var cost: Double,
    var currency: Constants.Currency
) : Parcelable
