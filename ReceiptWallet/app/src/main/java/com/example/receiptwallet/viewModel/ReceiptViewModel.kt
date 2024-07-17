package com.example.receiptwallet.viewModel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.receiptwallet.R
import com.example.receiptwallet.database.ReceiptDatabase
import com.example.receiptwallet.dto.ReceiptDto
import com.example.receiptwallet.models.Receipt
import com.example.receiptwallet.repository.ReceiptRepository
import com.example.receiptwallet.util.Constants
import com.example.receiptwallet.util.Constants.DAYS_IN_YEAR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

class ReceiptViewModel(application: Application) : BaseViewModel(application) {
    private val receiptRepository: ReceiptRepository
    val receiptLiveData: MutableLiveData<List<Receipt>> = MutableLiveData()
    var validationStatus =  mutableMapOf<String, Boolean>()
    var nameValue = ""
    var purchaseDateValue = ""
    var warrantyLengthValue = ""
    var categoryValue = ""
    var costValue = ""
    var currencyValue = ""

    val receiptDtoLiveData: MutableLiveData<List<ReceiptDto>> =
        MutableLiveData()

    private var receiptListDto = ArrayList<com.example.receiptwallet.dto.Receipt>()

    init {
        val receiptDAO = ReceiptDatabase.getReceiptDatabase(application).receiptDao()
        receiptRepository = ReceiptRepository(receiptDAO)
        getAllReceipts()
    }

    fun getAllReceipts() {
        viewModelScope.launch {
            val receipts = receiptRepository.getAllReceipts()

            if (receipts.isNotEmpty()) {
                val mappedReceiptListDto = receipts.map { receipt ->
                    val dateString = receipt.purchaseDate
                    val buyingDate: Date =
                        SimpleDateFormat(Constants.DATE_PATTERN).parse(dateString)

                    var receiptDto = ReceiptDto(
                        name = receipt.name,
                        buyingDate = buyingDate,
                        guarantee = receipt.warrantyLength,
                        category = receipt.category,
                        id = receipt.id,
                        cost = receipt.cost,
                        pictureUUID = receipt.pictureUUID,
                        currency = receipt.currency
                    )

                    val expirationDate = calculateExpirationDate(receiptDto)

                    calculateTimeLeft(receiptDto, expirationDate)
                    receiptDto
                }
                val sortedList = mappedReceiptListDto.sortedBy { it.daysLeft }
                receiptDtoLiveData.postValue(sortedList.toList())
            }
        }
    }

    fun removeReceiptFromDatabase(receipt: Receipt) {
        viewModelScope.launch(Dispatchers.IO) {
            receiptRepository.removeReceipt(receipt)
        }
    }

    fun addNewReceiptToDatabase(receipt: Receipt) {
        viewModelScope.launch(Dispatchers.IO) {
            receiptRepository.addNewReceipt(receipt)
        }
    }

    fun updateReceipt(receipt: Receipt) {
        viewModelScope.launch(Dispatchers.IO) {
            receiptRepository.updateReceipt(receipt)
        }
    }

    private fun calculateExpirationDate(receiptDto: ReceiptDto): Calendar {
        val calendar = Calendar.getInstance()
        calendar.time = receiptDto.buyingDate

        receiptDto.guarantee?.let {
            calendar.add(Calendar.MONTH, it)
        }

        return calendar
    }

    fun calculateTimeLeft(receiptDto: ReceiptDto, expirationDate: Calendar) {
        val app = getApplication<Application>()
        if (isToday(expirationDate)) {
            receiptDto.timeLeft =
                app.resources.getQuantityString(R.plurals.time_left_days, 1, 1)
        } else if (Calendar.getInstance().after(expirationDate)) {
            receiptDto.timeLeft = app.getString(R.string.time_left_expired)
        } else {
            val timeLeft =
                expirationDate.timeInMillis - Calendar.getInstance().timeInMillis
            val daysLeft = TimeUnit.MILLISECONDS.toDays(timeLeft)
            receiptDto.daysLeft = daysLeft.toInt()
            val yearsLeft = daysLeft / DAYS_IN_YEAR

            if (yearsLeft > Constants.EMPTY_YEAR_LEFT) {
                val yearsString =
                    if (yearsLeft.toInt() == Constants.ONE_YEAR_LEFT)
                        app.resources.getQuantityString(
                            R.plurals.time_left_years,
                            yearsLeft.toInt(),
                            yearsLeft.toInt(),
                            daysLeft % DAYS_IN_YEAR
                        )
                    else
                        app.resources.getQuantityString(
                            R.plurals.time_left_years,
                            yearsLeft.toInt(),
                            yearsLeft.toInt(),
                            daysLeft % DAYS_IN_YEAR
                        )
                receiptDto.timeLeft = yearsString
            } else {
                receiptDto.timeLeft =
                    app.resources.getQuantityString(
                        R.plurals.time_left_days,
                        daysLeft.toInt(),
                        daysLeft.toInt()
                    )
            }
        }
    }

    private fun isToday(date: Calendar): Boolean =
        date.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR) &&
                date.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) &&
                date.get(Calendar.DAY_OF_MONTH) == Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

    fun validateFields(
        name: String,
        purchaseDate: String,
        warrantyLength: String,
        category: String,
        cost: String,
        imageTag: String
    ): Map<String, Boolean> {

        if (imageTag == Constants.imageTag) {
            validationStatus[Constants.imageTag] = false
        }

        val fields = mapOf(
            Constants.name to name,
            Constants.purchaseDate to purchaseDate,
            Constants.warrantyLength to warrantyLength,
            Constants.category to category,
            Constants.cost to cost
        )

        for ((fieldName, fieldValue) in fields) {
            val isValidField = when {
                fieldValue.isEmpty() -> false
                (fieldName == Constants.warrantyLength && fieldValue.toInt() <= 0) -> false
                (fieldName == Constants.cost && fieldValue.toDouble() < 0) -> false
                else -> true
            }
            validationStatus[fieldName] = isValidField
        }
        return validationStatus
    }
}