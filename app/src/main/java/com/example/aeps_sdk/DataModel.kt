package com.example.aeps_sdk


class DataModel {
    var transactionAmount: String? = null
    var transactionType: String? = null
    var paramA: String? = null
    var paramB: String? = null
    var paramC: String? = null
    var applicationType: String? = null
    var tokenFromCoreApp: String? = null
    var userNameFromCoreApp: String? = null
    var API_USER_NAME_VALUE: String? = null
    var DRIVER_ACTIVITY: String? = null
    var BRAND_NAME: String? = null
    var SHOP_NAME: String? = null
    var skipReceiptPart: Boolean? = null
    var internalFPName: String? = null

    var DEVICE_TYPE:String?=null
    var loginID:String?=null
    var encryptedData:String?=null
    var DEVICE_NAME:String?=null
    var USER_MOBILE_NO:String?=null
    var IS_BETA_USER:Boolean?=null

    constructor() {}
    constructor(
        transactionAmount: String?,
        transactionType: String?,
        paramA: String?,
        paramB: String?,
        paramC: String?,
        applicationType: String?,
        tokenFromCoreApp: String?,
        userNameFromCoreApp: String?,
        API_USER_NAME_VALUE: String?,
        DRIVER_ACTIVITY: String?,
        BRAND_NAME: String?,
        SHOP_NAME: String?,
        skipReceiptPart: Boolean?,
        internalFPName:String?,
        DEVICE_TYPE:String?,
        loginID:String?,
        encryptedData:String?,
        DEVICE_NAME:String?,
        USER_MOBILE_NO:String?,
        IS_BETA_USER:Boolean?
    ) {
        this.transactionAmount = transactionAmount
        this.transactionType = transactionType
        this.paramA = paramA
        this.paramB = paramB
        this.paramC = paramC
        this.applicationType = applicationType
        this.tokenFromCoreApp = tokenFromCoreApp
        this.userNameFromCoreApp = userNameFromCoreApp
        this.API_USER_NAME_VALUE = API_USER_NAME_VALUE
        this.DRIVER_ACTIVITY = DRIVER_ACTIVITY
        this.BRAND_NAME = BRAND_NAME
        this.SHOP_NAME = SHOP_NAME
        this.skipReceiptPart = skipReceiptPart
        this.internalFPName=internalFPName
        this.DEVICE_TYPE=DEVICE_TYPE
        this.loginID=loginID
        this.encryptedData=encryptedData
        this.DEVICE_NAME=DEVICE_NAME
        this.USER_MOBILE_NO=USER_MOBILE_NO
        this.IS_BETA_USER=IS_BETA_USER
    }
}

