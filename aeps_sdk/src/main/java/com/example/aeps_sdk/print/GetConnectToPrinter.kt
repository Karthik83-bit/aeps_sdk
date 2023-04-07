package com.example.aeps_sdk.print

import android.graphics.Bitmap
import android.util.Log
import com.pax.dal.IPrinter
import com.pax.dal.entity.EFontTypeAscii
import com.pax.dal.entity.EFontTypeExtCode

open class GetConnectToPrinter private constructor() {

    companion object {
        private val printer: IPrinter = GetDalFromNeptuneLiteListener.getDal()!!.printer

        fun init() {
            try {
                printer.init()
                Log.e("TAG", "init: ")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("TAG", "init: ")
            }
        }

        val status: String
            get() = try {
                val status: Int = printer.getStatus()
                statusCode2Str(status)
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }


        fun start(): String {
            return try {
                val res: Int = printer.start()
                statusCode2Str(res)
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }


        private fun statusCode2Str(status: Int): String {
            var res = ""
            when (status) {
                0 -> res = "Success "
                1 -> res = "Printer is busy "
                2 -> res = "Out of paper "
                3 -> res = "The format of print data packet error "
                4 -> res = "Printer malfunctions "
                8 -> res = "Printer over heats "
                9 -> res = "Printer voltage is too low"
                240 -> res = "Printing is unfinished "
                252 -> res = " The printer has not installed font library "
                254 -> res = "Data package is too long "
                else -> {}
            }
            return res
        }

        private var getConnectToPrinter: GetConnectToPrinter? = null
        val instance: GetConnectToPrinter?
            get() {
                if (getConnectToPrinter == null) {
                    getConnectToPrinter = GetConnectToPrinter()
                }
                return getConnectToPrinter
            }


        fun leftIndents(indent: Short) {
            try {
                printer.leftIndent(indent.toInt())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val dotLine: Int
            get() {
                return try {
                    printer.dotLine
                } catch (e: Exception) {
                    e.printStackTrace()
                    -2
                }
            }

        fun setGray(level: Int) {
            try {
                printer.setGray(level)
                //            logTrue("setGray");
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun setDoubleWidth(isAscDouble: Boolean, isLocalDouble: Boolean) {
            try {
                printer.doubleWidth(isAscDouble, isLocalDouble)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun setDoubleHeight(isAscDouble: Boolean, isLocalDouble: Boolean) {
            try {
                printer.doubleHeight(isAscDouble, isLocalDouble)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun setInvert(isInvert: Boolean) {
            try {
                printer.invert(isInvert)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun cutPaper(mode: Int): String {
            return try {
                printer.cutPaper(mode)
                "cut paper successful"
            } catch (e: Exception) {
                e.printStackTrace()
                e.toString()
            }
        }

        val cutMode: String
            get() {
                var resultStr = ""
                return try {
                    val mode: Int = printer.getCutMode()
                    when (mode) {
                        0 -> resultStr = "Only support full paper cut"
                        1 -> resultStr = "Only support partial paper cutting "
                        2 -> resultStr = "support partial paper and full paper cutting "
                        -1 -> resultStr = "No cutting knife,not support"
                        else -> {}
                    }
                    resultStr
                } catch (e: Exception) {
                    e.printStackTrace()
                    e.toString()
                }
            }

        fun fontSet(asciiFontType: EFontTypeAscii?, cFontType: EFontTypeExtCode?) {
            try {
                printer.fontSet(asciiFontType, cFontType)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun spaceSet(wordSpace: Byte, lineSpace: Byte) {
            try {
                printer.spaceSet(wordSpace, lineSpace)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun printStr(str: String?, charset: String?) {
            try {
                printer.printStr(str, charset)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun step(b: Int) {
            try {
                printer.step(b)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun printBitmap(bitmap: Bitmap?) {
            try {
                printer.printBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
