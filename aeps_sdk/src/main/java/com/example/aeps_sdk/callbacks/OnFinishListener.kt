package com.example.aeps_sdk.callbacks

interface OnFinishListener {
    fun onSdkFinish(statusTxt:String, paramA:String, statusDesc:String, jsonString: String?)
}