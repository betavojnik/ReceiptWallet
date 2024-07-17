package com.example.receiptwallet.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.receiptwallet.converters.CategoryConverter
import com.example.receiptwallet.converters.CurrencyConverter
import com.example.receiptwallet.dao.ReceiptDAO
import com.example.receiptwallet.models.Receipt
import com.example.receiptwallet.util.Constants

@Database(
    entities = [Receipt::class],
    version = Constants.DATABASE_VERSION,
    exportSchema = false
)
@TypeConverters(CategoryConverter::class, CurrencyConverter::class)
abstract class ReceiptDatabase : RoomDatabase() {
    abstract fun receiptDao(): ReceiptDAO

    companion object {
        @Volatile
        private var INSTANCE: ReceiptDatabase? = null

        fun getReceiptDatabase(context: Context): ReceiptDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReceiptDatabase::class.java,
                    Constants.DATABASE_NAME
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                return instance
            }
        }
    }
}