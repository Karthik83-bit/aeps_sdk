package com.example.aeps_sdk.utils

import com.example.aeps_sdk.vriddhi.AEMPrinter

class GetPosConnectedPrinter(aemPrinter: AEMPrinter) {
    private var aemPrinter: AEMPrinter
        get() = Companion.aemPrinter!!

    companion object {
         var aemPrinter: AEMPrinter? = null
    }

    init {
        this.aemPrinter = aemPrinter
    }
}