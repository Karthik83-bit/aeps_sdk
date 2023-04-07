package com.example.aeps_sdk.callbacks

interface OnDriverDataListener {
    fun onFingerClick(
        aadharNo: String?,
        mobileNumber: String?,
        bankName: String?,
        driverFlag: String?,
        listener: OnDriverDataListener?
    )

}