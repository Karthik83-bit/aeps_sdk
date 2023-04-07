package com.example.aeps_sdk.utils

import com.example.aeps_sdk.callbacks.OnDriverDataListener
import com.example.aeps_sdk.callbacks.OnFinishListener

class SdkConstants {
    companion object {
        lateinit var RECEIVE_DRIVER_DATA: String
        var onFinishListener: OnFinishListener? = null
        const val balanceEnquiry = "0"
        const val cashWithdrawal = "1"
        const val miniStatement = "2"
        const val aadhaarPay = "3"
        const val Wiseasy = "Wiseasy"
        const val morefun = "morefun"
        const val A910 = "A910"
        const val Newland = "Newland"
        const val pax = "pax"
        const val mATM2 = "mATM2"
        const val POS = "POS"
        const val integratedpos = "com.pos.integratedpos"
        const val matmservice_1 = "com.matm.matmservice_1"
        const val matmservice = "com.matm.matmservice"
        var IS_BETA_USER = false
        var transactionAmount = "0"
        var transactionType = ""
        var paramA = ""
        var paramB = ""
        var paramC = ""
        var API_USER_NAME_VALUE = ""
        var applicationType = ""
        var userNameFromCoreApp = ""
        var tokenFromCoreApp = ""
        var MANUFACTURE_FLAG = ""
        var DRIVER_ACTIVITY = ""
        var isSl = false
        var BRAND_NAME = ""
        var SHOP_NAME = ""
        var applicationUserName = ""
        var BANK_NAME = ""
        var bankItem = 0
        var bankIIN = ""
        var aadharNumberValue = ""
        var OnBackpressedValue = false
        var onDriverDataListener: OnDriverDataListener? = null
        var internalFPName = ""
        var Bluetoothname = ""
        var bluetoothConnector = false
        var firstCheck = true // This is hardcoded, to avoid the terms check
        var secondCheck = true // This is hardcoded, to avoid the terms check
        var AADHAAR_NUMBER = ""
        var MOBILENUMBER = ""
        var refeshUI = false
        var FAILEDVALUE = ""
        var skipReceiptPart = false
        var DEVICE_TYPE = "" //Added due to morefun and pax check
        var DEVICE_NAME = "" //Added due to morefun and pax check
        var loginID = ""
        var encryptedData = ""
        var USER_MOBILE_NO = ""
        var mobileNumberValue = ""
        var bankValue = ""

    }
}