package com.example.receiptwallet.util

object Constants {
    const val SPLASH_SCREEN_TIME: Long = 2500
    const val EMPTY_STRING: String = ""
    const val DAYS_IN_YEAR: Int = 365
    const val TRANSPARENT_BACKGROUND_ALPHA = 0.7f
    const val YEARS_AND_DAYS: String = "%1\\\$d year and %2\\\$d days"
    const val DATE_PATTERN: String = "dd/MM/yyyy"
    const val DATABASE_NAME: String = "receipt_database"
    const val DATABASE_VERSION: Int = 2
    const val ONE_YEAR_LEFT: Int = 1
    const val EMPTY_YEAR_LEFT: Int = 0
    const val name: String = "name"
    const val purchaseDate: String = "purchaseDate"
    const val warrantyLength: String = "warrantyLength"
    const val category: String = "category"
    const val cost: String = "cost"
    const val imageTag: String = "image_icon"
    const val image: String = "image"
    const val EMPTY_DAYS_LEFT: Int = 0
    const val EXPIRED: String = "expired"
    const val FILE_NAME_FORMAT = "yy-MM-dd-HH-mm-ss-SSS"
    const val NO_IMAGE = "noImage"
    const val TAG_CAMERA = "Camera error"
    const val TAG_CAMERA_MESSAGE = "Failed to start the camera"
    const val TAG_CAMERA_IMAGE_SAVE_MESSAGE = "Failed to take a picture"
    const val JPG_EXT = ".jpg"
    const val ONLY_IMAGES = "image/*"
    const val WELCOME = "WELCOME"
    const val NULL = "null"
    const val REQUEST_CODE = 101
    const val ZERO = 0
    const val MINUS_ONE = -1
    const val WARRANTY_ADDED = "Warranty added"
    const val WARRANTY_UPDATED = "Warranty updated"
    const val DATE_TIME_PATTERN_DETAILS = "EEE MMM dd HH:mm:ss zzz yyyy"
    const val MAX_BUFFER_SIZE = 1 * 1024 * 1024
    const val PERMISSIONS_NEEDED = "Allow camera permission in phone settings!"
    const val RECEIPT_WALLET = "Receipt Wallet"
    const val OK = "OK"
    var notificationShown: Boolean = false


    enum class Category {
        IT_COMPONENT,
        HOUSE_APPLIANCE,
        TV_AUDIO_VIDEO,
        FURNITURE,
        VEHICLE,
        OTHER
    }

    enum class Currency {
        RSD,
        EURO
    }
}