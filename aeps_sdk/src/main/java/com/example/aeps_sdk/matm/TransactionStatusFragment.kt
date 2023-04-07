package com.example.aeps_sdk.matm

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.example.aeps_sdk.BaseFragment
import com.example.aeps_sdk.R
import com.example.aeps_sdk.databinding.FragmentTransactionStatusBinding
import com.example.aeps_sdk.print.GetConnectToPrinter
import com.example.aeps_sdk.utils.FileUtils
import com.example.aeps_sdk.utils.GetPosConnectedPrinter
import com.example.aeps_sdk.utils.SdkConstants
import com.example.aeps_sdk.vriddhi.*
import com.itextpdf.text.*
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.text.pdf.draw.LineSeparator
import com.pax.dal.entity.EFontTypeAscii
import com.pax.dal.entity.EFontTypeExtCode
import wangpos.sdk4.libbasebinder.Printer
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class TransactionStatusFragment : BaseFragment(), IAemCardScanner, IAemScrybe {
    private val TAG: String =
        TransactionStatusFragment::class.java.simpleName

    private var _binding: FragmentTransactionStatusBinding? = null
    private val binding get() = _binding!!

    private var rrnNo: String? = null
    private var txnId: String? = null
    private var cardType: String? = null
    private var inVoice: String? = null
    private var transactionId: String? = null
    private var statusCode: String? = null
    private var transactionAmount: String? = null
    private var aid: String? = null
    private var mid: String? = null
    private var tid: String? = null
    private var flag: String? = null
    private var responseCode: String? = null
    private var transactionType: String? = null
    private var apprCode: String? = null
    private var cardNumber: String? = null
    private var cardHolderName: String? = null
    private var amount: String? = null
    private var appName: String? = null
    private var statusText: String? = null
    private var isPermissionGranted: Boolean = false
    private var filePath = ""

    private lateinit var currentDateTime: String

    private var mPrinter: Printer? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var printerList: ArrayList<String>? = null

    /* Parameters for the Vriddhi Printer*/
    private var mAemscrybedevice: AEMScrybeDevice? = null
    private var mAemprinter: AEMPrinter? = null
    private var mCardreader: CardReader? = null
    private var printerStatus = charArrayOf(
        0x1B.toChar(),
        0x7E.toChar(),
        0x42.toChar(),
        0x50.toChar(),
        0x7C.toChar(),
        0x47.toChar(),
        0x45.toChar(),
        0x54.toChar(),
        0x7C.toChar(),
        0x50.toChar(),
        0x52.toChar(),
        0x4E.toChar(),
        0x5F.toChar(),
        0x53.toChar(),
        0x54.toChar(),
        0x5E.toChar()
    )
    private var data: String? = null
    private var creditData: String? = null
    private var tempdata: String? = null
    private var replacedData: String? = null
    private var responseString: String? = null
    private var response: String? = null
    private var cardTrackType: CardReader.CARD_TRACK? = null
    private var responseArray = arrayOfNulls<String>(1)


    private val registerActivity =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->
            if (permission[Manifest.permission.READ_EXTERNAL_STORAGE] == true || permission[Manifest.permission.BLUETOOTH_CONNECT] == true || permission[Manifest.permission.BLUETOOTH] == true || permission[Manifest.permission.BLUETOOTH_SCAN] == true || permission[Manifest.permission.BLUETOOTH_ADMIN] == true) {
                isPermissionGranted = true
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTransactionStatusBinding.inflate(layoutInflater)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        onClickListener()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun onClickListener() {
        binding.finishBtn.setOnClickListener {
            if (SdkConstants.onFinishListener != null) {
                SdkConstants.onFinishListener!!.onSdkFinish(
                    statusText!!, SdkConstants.paramA, binding.amount.text.toString(), null
                )
            }
            requireActivity().finish()
        }
        binding.txndetailsBtn.setOnClickListener {
            showTransactionDetails(requireActivity())
        }
        binding.downloadBtn.setOnClickListener {
            if (isPermissionGranted) {
                val date = Date()
                val timeMilli = date.time
                println("Time in milliseconds using Date class: $timeMilli")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    createPdf(
                        FileUtils.commonDocumentDirPath("PDF") + timeMilli.toString() + "Order_Receipt.pdf"
                    )
                } else {
                    createPdf(
                        FileUtils.getAppPath(requireActivity()) + timeMilli.toString() + "Order_Receipt.pdf"
                    )
                }

            } else {
                takeMultiplePermission()
            }
        }
        binding.printBtn.setOnClickListener {
            val deviceModel = Build.MODEL
            if (deviceModel.equals("A910", ignoreCase = true)) {
                getPrintData(
                    binding.txnID.text.toString(),
                    binding.dateTime.text.toString(),
                    rrnNo!!,
                    mid!!,
                    tid!!,
                    cardType!!,
                    binding.accountnumber.text.toString(),
                    binding.transactionamount.text.toString()
                )
            } else if (deviceModel.equals(
                    "WPOS-3",
                    ignoreCase = true
                ) || deviceModel.contains("P5")
            ) {
                //start printing with wiseasy internal printer
                PrintThread().start()
            } else {
                getConnectToPrinter(it)
            }
        }

    }

    inner class PrintThread : Thread() {
        override fun run() {
            mPrinter = Printer(requireActivity())
            try {
                mPrinter!!.setPrintType(0) //Printer type 0 means it's an internal printer
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            checkPrinterStatus()
        }
    }

    private fun checkPrinterStatus() {
        try {
            val status = IntArray(1)
            mPrinter!!.getPrinterStatus(status)
            Log.e(
                TAG,
                "Printer Status is " + status[0]
            )
            val msg: String
            when (status[0]) {
                0x00 -> {
                    msg = "Printer status OK"
                    Log.e(
                        TAG,
                        "check printer status: $msg"
                    )
                    startPrinting()
                }
                0x01 -> {
                    msg = "Parameter error"
                    showLog(msg)
                }
                0x8A -> {
                    msg = "Out of Paper"
                    showLog(msg)
                }
                0x8B -> {
                    msg = "Overheat"
                    showLog(msg)
                }
                else -> {
                    msg = "Printer Error"
                    showLog(msg)
                }
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    private fun startPrinting() {
        val result: Int
        try {
            result = mPrinter!!.printInit()
            Log.e(
                TAG,
                "startPrinting: Printer init result $result"
            )
            mPrinter!!.clearPrintDataCache()
            if (result == 0) {
                printReceipt()
            } else {
                Toast.makeText(
                    requireActivity(),
                    "Printer initialization failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun printReceipt() {
        var result: Int
        try {
            Log.e(
                TAG,
                "printReceipt: set density low 1"
            )
            mPrinter!!.setGrayLevel(1)
            result = mPrinter!!.printStringExt(
                SdkConstants.SHOP_NAME,
                0,
                0f,
                2.0f,
                Printer.Font.SANS_SERIF,
                28,
                Printer.Align.CENTER,
                true,
                false,
                true
            )
            result =
                mPrinter!!.printString("Transaction Report", 24, Printer.Align.CENTER, true, false)
            result = if (statusText.equals("Success", ignoreCase = true)) {
                mPrinter!!.printString("Success", 24, Printer.Align.CENTER, true, false)
            } else {
                mPrinter!!.printString("Failure", 24, Printer.Align.CENTER, true, false)
            }
            result = mPrinter!!.printString("", 15, Printer.Align.CENTER, true, false)
            result = mPrinter!!.printString(
                "Txn Id :" + binding.txnID.text.toString(),
                20,
                Printer.Align.LEFT,
                false,
                false
            )
            result = mPrinter!!.printString(
                "Date/Time : " + binding.dateTime.text.toString(),
                20,
                Printer.Align.LEFT,
                false,
                false
            )
            result = mPrinter!!.printString("RRN :$rrnNo", 20, Printer.Align.LEFT, false, false)
            result = mPrinter!!.printString("MID :$mid", 20, Printer.Align.LEFT, false, false)
            result =
                mPrinter!!.printString("Terminal Id : $tid", 20, Printer.Align.LEFT, false, false)
            result = mPrinter!!.printString(
                "Card Number : " + binding.accountnumber.text.toString(),
                20,
                Printer.Align.LEFT,
                false,
                false
            )
            result = mPrinter!!.printString(
                "Balance Amount :$amount",
                20,
                Printer.Align.LEFT,
                false,
                false
            )
            if (transactionType.equals("cash", ignoreCase = true)) {
                result = mPrinter!!.printString(
                    "Txn Type : Cash Withdrawal",
                    20,
                    Printer.Align.LEFT,
                    false,
                    false
                )
                result = mPrinter!!.printString(
                    "Txn Amount :" + binding.transactionamount.text.toString(),
                    20,
                    Printer.Align.LEFT,
                    false,
                    false
                )
            } else {
                result = mPrinter!!.printString(
                    "Txn Type : Balance Enquiry",
                    20,
                    Printer.Align.LEFT,
                    false,
                    false
                )
                result = mPrinter!!.printString(
                    "Txn Amount :" + binding.transactionamount.text.toString(),
                    20,
                    Printer.Align.LEFT,
                    false,
                    false
                )
            }
            result = mPrinter!!.printStringExt(
                "Thank You !",
                0,
                0f,
                2.0f,
                Printer.Font.SANS_SERIF,
                22,
                Printer.Align.RIGHT,
                true,
                true,
                false
            )
            result = mPrinter!!.printStringExt(
                """
                ${SdkConstants.BRAND_NAME}
                
                
                
                """.trimIndent(),
                0,
                0f,
                2.0f,
                Printer.Font.SANS_SERIF,
                20,
                Printer.Align.RIGHT,
                false,
                true,
                false
            )

            //result = mPrinter.printString("------------------------------------------\n", 30, Printer.Align.CENTER, false, false);
            Log.e(
                TAG,
                "printReceipt: print thank you result $result"
            )
            result = mPrinter!!.printPaper(27)
            Log.e(
                TAG,
                "printReceipt: print step result $result"
            )
            showPrinterStatus(result)
            result = mPrinter!!.printFinish()
            Log.e(
                TAG,
                "printReceipt: printer finish result $result"
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun showPrinterStatus(result: Int) {
        var msg = ""
        when (result) {
            0x00 -> {
                msg = "Print Finish"
                showLog(msg)
            }
            0x01 -> {
                msg = "Parameter error"
                showLog(msg)
            }
            0x8A -> {
                msg = "Out of Paper"
                showLog(msg)
            }
            0x8B -> {
                msg = "Overheat"
                showLog(msg)
            }
            else -> {
                msg = "Printer Error"
                showLog(msg)
            }
        }
    }


    private fun showLog(msg: String) {
        requireActivity().runOnUiThread(Runnable {
            Toast.makeText(
                requireActivity(),
                msg,
                Toast.LENGTH_SHORT
            ).show()
        })
        Log.e(
            TAG,
            "Printer status: $msg"
        )
    }


    private fun getPrintData(
        txnId: String, date: String, reffNo: String,
        mid: String, terminalId: String, type: String,
        cardNumber: String, transactionAmt: String
    ) {
        Thread {
            GetConnectToPrinter.init()

            //Shop Name Set
            GetConnectToPrinter.fontSet(
                EFontTypeAscii.FONT_24_48,
                EFontTypeExtCode.FONT_24_48
            )
            GetConnectToPrinter.leftIndents("60".toShort())
            GetConnectToPrinter.setInvert(false)
            GetConnectToPrinter.printStr(SdkConstants.SHOP_NAME, null)
            GetConnectToPrinter.step("20".toInt())

            //Transaction Details Message
            GetConnectToPrinter.fontSet(
                EFontTypeAscii.FONT_16_32,
                EFontTypeExtCode.FONT_16_32
            )
            GetConnectToPrinter.printStr(getString(R.string.txn_report_txt), null)
            GetConnectToPrinter.step("20".toInt())

            //status Message
            GetConnectToPrinter.fontSet(
                EFontTypeAscii.FONT_16_32,
                EFontTypeExtCode.FONT_16_32
            )
            GetConnectToPrinter.leftIndents("110".toShort())
            GetConnectToPrinter.printStr(statusText, null)
            GetConnectToPrinter.step("30".toInt())
            GetConnectToPrinter.fontSet(
                EFontTypeAscii.FONT_12_24,
                EFontTypeExtCode.FONT_16_32
            )
            GetConnectToPrinter.spaceSet(0.toByte(), 0.toByte())
            GetConnectToPrinter.leftIndents("0".toShort())
            GetConnectToPrinter
                .printStr(getString(R.string.transaction_id_txt) + txnId, null)
            GetConnectToPrinter.step("10".toInt())
            GetConnectToPrinter
                .printStr(getString(R.string.date_time_txt) + date, null)
            GetConnectToPrinter.step("10".toInt())
            GetConnectToPrinter.printStr(getString(R.string.rrn_txt) + reffNo, null)
            GetConnectToPrinter.step("10".toInt())
            GetConnectToPrinter.printStr(getString(R.string.mid_txt) + mid, null)
            GetConnectToPrinter.step("10".toInt())
            GetConnectToPrinter
                .printStr(getString(R.string.terminal_id_txt) + terminalId, null)
            GetConnectToPrinter.step("10".toInt())
            GetConnectToPrinter
                .printStr(getString(R.string.card_number_txt) + cardNumber, null)
            GetConnectToPrinter.step("10".toInt())
            GetConnectToPrinter
                .printStr(getString(R.string.balance_amt_txt) + amount, null)
            GetConnectToPrinter.step("10".toInt())
            GetConnectToPrinter.printStr(
                getString(R.string.transaction_type_txt) + binding.tranTypeName.text.toString(),
                null
            )
            GetConnectToPrinter.step("10".toInt())

            //Transaction Amount
            GetConnectToPrinter.fontSet(
                EFontTypeAscii.FONT_12_24,
                EFontTypeExtCode.FONT_16_32
            )
            GetConnectToPrinter.spaceSet(0.toByte(), 0.toByte())
            GetConnectToPrinter.leftIndents("0".toShort())
            GetConnectToPrinter
                .printStr(getString(R.string.txn_amt_txt) + transactionAmt, null)
            GetConnectToPrinter.step("25".toInt())


            //Thank You Message
            GetConnectToPrinter.fontSet(
                EFontTypeAscii.FONT_16_32,
                EFontTypeExtCode.FONT_16_32
            )
            val thankYouString = getString(R.string.thanks_txt)
            if (thankYouString != null && thankYouString.length > 0) GetConnectToPrinter
                .printStr(thankYouString, null)
            GetConnectToPrinter.step("15".toInt())

            //Partner(Admin, MD etc.) Name Message
            GetConnectToPrinter.fontSet(
                EFontTypeAscii.FONT_12_24,
                EFontTypeExtCode.FONT_16_32
            )
            val brandName = SdkConstants.BRAND_NAME
            if (brandName.isNotEmpty()) GetConnectToPrinter
                .printStr(brandName, null)
            GetConnectToPrinter.step("100".toInt())
            GetConnectToPrinter.start()
        }.start()
    }

    private fun getConnectToPrinter(view: View) {
        registerForContextMenu(binding.printBtn)
        if (bluetoothAdapter == null) {
            Toast.makeText(
                requireActivity(),
                "Bluetooth NOT supported",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            if (bluetoothAdapter!!.isEnabled) {
                if (GetPosConnectedPrinter.aemPrinter == null) {
                    printerList = mAemscrybedevice!!.pairedPrinters
                    if (printerList!!.size > 0) {
                        requireActivity().openContextMenu(view)
                    } else {
                        showAlert("No Paired Printers found", requireActivity())
                    }
                } else {
                    mAemprinter = GetPosConnectedPrinter.aemPrinter
                    callBluetoothFunction(
                        binding.txnID.text.toString(),
                        binding.dateTime.text.toString(),
                        rrnNo!!,
                        mid!!,
                        tid!!,
                        cardType!!,
                        binding.accountnumber.text.toString(),
                        binding.transactionamount.text.toString(),
                        view
                    )
                }
            } else {
                GetPosConnectedPrinter.aemPrinter = null
                val turnOn = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(turnOn, 0)
            }
        }
    }

    private fun callBluetoothFunction(
        txnId: String,
        date: String,
        reffNo: String,
        mid: String,
        terminalId: String,
        type: String,
        cardNumber: String,
        transactionAmt: String,
        view: View
    ) {
        try {
            mAemprinter!!.setFontType(AEMPrinter.DOUBLE_HEIGHT)
            mAemprinter!!.setFontType(AEMPrinter.TEXT_ALIGNMENT_CENTER)
            mAemprinter!!.setFontType(AEMPrinter.FONT_NORMAL)
            mAemprinter!!.setFontType(AEMPrinter.FONT_002)
            mAemprinter!!.POS_FontThreeInchCENTER()
            mAemprinter!!.print(SdkConstants.SHOP_NAME)
            mAemprinter!!.print("\n")
            mAemprinter!!.setFontType(AEMPrinter.DOUBLE_HEIGHT)
            mAemprinter!!.setFontType(AEMPrinter.TEXT_ALIGNMENT_CENTER)
            mAemprinter!!.print("-----Transaction Report-----\n")
            mAemprinter!!.POS_FontThreeInchCENTER()
            mAemprinter!!.print(statusText)
            //            mAemprinter!!.print("\n\n");
//            mAemprinter!!.print(txnId);
            mAemprinter!!.print("\n")
            mAemprinter!!.print("Txn ID: $txnId")
            mAemprinter!!.print("\n")
            mAemprinter!!.print("Date/Time: $date")
            mAemprinter!!.print("\n")
            mAemprinter!!.print("RRN: $reffNo")
            mAemprinter!!.print("\n")
            mAemprinter!!.print("Mid : $mid")
            mAemprinter!!.print("\n")
            mAemprinter!!.print("Terminal ID: $terminalId")
            mAemprinter!!.print("\n")
            mAemprinter!!.print("Card No.: $cardNumber")
            mAemprinter!!.print("\n")
            mAemprinter!!.print("Balance Amount : $amount")
            mAemprinter!!.print("\n")
            mAemprinter!!.print("Txn Type : " + binding.tranTypeName.text.toString())
            mAemprinter!!.print("\n")
            mAemprinter!!.print("Txn Amount : $transactionAmt")
            mAemprinter!!.print("\n\n")
            mAemprinter!!.setFontType(AEMPrinter.FONT_002)
            mAemprinter!!.POS_FontThreeInchRIGHT()
            mAemprinter!!.print("Thank you \n")
            mAemprinter!!.POS_FontThreeInchRIGHT()
            mAemprinter!!.setFontType(AEMPrinter.TEXT_ALIGNMENT_RIGHT)
            mAemprinter!!.print(SdkConstants.BRAND_NAME)
            mAemprinter!!.setCarriageReturn()
            mAemprinter!!.setCarriageReturn()
            mAemprinter!!.setCarriageReturn()
            mAemprinter!!.setCarriageReturn()
            data = printerStatus()
            mAemprinter!!.print(data)
            mAemprinter!!.print("\n")
        } catch (e: IOException) {
//            e.printStackTrace();
            try {
                GetPosConnectedPrinter.aemPrinter = null
                getConnectToPrinter(view)
            } catch (exception: java.lang.Exception) {
                exception.printStackTrace()
            }
        }
    }

    @Throws(IOException::class)
    fun printerStatus(): String {
        val data = String(printerStatus)
        mAemprinter!!.print(data)
        return data
    }


    private fun createPdf(s: String) {
        filePath = s
        createPdfGenericMethod(s)
    }

    private fun createPdfGenericMethod(dest: String) {
        if (File(dest).exists()) {
            File(dest).delete()
        }
        try {
            /**
             * Creating Document
             */
            val document = Document()

            // Location to save
            PdfWriter.getInstance(document, FileOutputStream(dest))

            // Open to write
            document.open()

            // Document Settings
            document.pageSize = PageSize.A3
            document.addCreationDate()
            document.addAuthor("")
            document.addCreator("")
            document.setMargins(0f, 0f, 50f, 50f)
            val rect = Rectangle(577F, 825F, 18F, 15F)
            rect.enableBorderSide(1)
            rect.enableBorderSide(2)
            rect.enableBorderSide(4)
            rect.enableBorderSide(8)
            rect.border = Rectangle.BOX
            rect.borderWidth = 2f
            rect.borderColor = BaseColor.BLACK
            document.add(rect)

            /*commit git test*/
            val mColorAccent = BaseColor(0, 153, 204, 255)
            val mHeadingFontSize = 24.0f
            val mValueFontSize = 26.0f
            val urName =
                BaseFont.createFont("assets/fonts/brandon_medium.otf", "UTF-8", BaseFont.EMBEDDED)

            // LINE SEPARATOR
            val lineSeparator = LineSeparator()
            lineSeparator.lineColor = BaseColor(0, 0, 0, 68)
            val mOrderDetailsTitleFont = Font(urName, 36.0f, Font.NORMAL, BaseColor.BLACK)
            val mOrderDetailsTitleChunk = Chunk(SdkConstants.SHOP_NAME, mOrderDetailsTitleFont)
            Log.i("mOrderDetailsTitleChunk", mOrderDetailsTitleChunk.toString())
            val mOrderDetailsTitleParagraph = Paragraph(mOrderDetailsTitleChunk)
            mOrderDetailsTitleParagraph.alignment = Element.ALIGN_CENTER
            document.add(mOrderDetailsTitleParagraph)
            document.add(Paragraph("\n"))
            val mOrderShopTitleFont = Font(urName, 25.0f, Font.NORMAL, BaseColor.BLACK)
            val mOrderShopTitleChunk = Chunk("Receipt", mOrderShopTitleFont)
            val mOrderShopTitleParagraph = Paragraph(mOrderShopTitleChunk)
            mOrderShopTitleParagraph.alignment = Element.ALIGN_CENTER
            document.add(mOrderShopTitleParagraph)
            val mOrderDetailsTitleFont11: Font =
                if (statusText.equals("FAILED", ignoreCase = true)) {
                    Font(urName, 40.0f, Font.NORMAL, BaseColor.RED)
                } else {
                    Font(urName, 40.0f, Font.NORMAL, BaseColor.GREEN)
                }
            val mOrderDetailsTitleChunk1 = Chunk(statusText, mOrderDetailsTitleFont11)
            val mOrderDetailsTitleParagraph1 = Paragraph(mOrderDetailsTitleChunk1)
            mOrderDetailsTitleParagraph1.alignment = Element.ALIGN_CENTER
            document.add(mOrderDetailsTitleParagraph1)
            document.add(Paragraph("\n"))
            val mOrderDateFont = Font(urName, mHeadingFontSize, Font.NORMAL, mColorAccent)
            val mOrderDateValueFont = Font(urName, mValueFontSize, Font.NORMAL, BaseColor.BLACK)
            val p = Paragraph()
            p.add(Chunk("Date/Time : ", mOrderDateFont))
            p.add(Chunk(binding.dateTime.text.toString().trim { it <= ' ' }, mOrderDateValueFont))
            document.add(p)
            document.add(Paragraph("\n\n"))
            val p1 = Paragraph()
            p1.add(Chunk("Operation Performed : ", mOrderDateFont))
            p1.add(Chunk("mATM", mOrderDateValueFont))
            document.add(p1)
            document.add(Paragraph("\n\n"))
            val mOrderDetailsFont = Font(urName, 30.0f, Font.BOLD, mColorAccent)
            val mOrderDetailsChunk = Chunk("Transaction Details", mOrderDetailsFont)
            val mOrderDetailsParagraph = Paragraph(mOrderDetailsChunk)
            mOrderDetailsParagraph.alignment = Element.ALIGN_CENTER
            document.add(mOrderDetailsParagraph)
            document.add(Paragraph("\n"))
            val mOrderIdFont = Font(urName, mValueFontSize, Font.NORMAL, BaseColor.BLACK)
            val mOrderIdChunk = Chunk("Txn ID: " + binding.txnID.text.toString(), mOrderIdFont)
            val mOrderTxnParagraph = Paragraph(mOrderIdChunk)
            document.add(mOrderTxnParagraph)
            val mOrderIdValueChunk = Chunk("MID: $mid", mOrderIdFont)
            val mOrderaadharParagraph = Paragraph(mOrderIdValueChunk)
            document.add(mOrderaadharParagraph)
            val mBankNameChunk = Chunk("Terminal ID:  $tid", mOrderIdFont)
            val mBankNameParagraph = Paragraph(mBankNameChunk)
            document.add(mBankNameParagraph)
            val mOrderrrnChunk = Chunk("RRN: $rrnNo", mOrderIdFont)
            val mOrderrnParagraph = Paragraph(mOrderrrnChunk)
            document.add(mOrderrnParagraph)
            val mOrdertxnTypeChunk =
                Chunk("Card No.: " + binding.accountnumber.text.toString(), mOrderIdFont)
            val mOrdertxnTypeParagraph = Paragraph(mOrdertxnTypeChunk)
            document.add(mOrdertxnTypeParagraph)
            val mOrderbalanceChunk = Chunk("Balance Amount: Rs.$amount", mOrderIdFont)
            val mOrderbalanceParagraph = Paragraph(mOrderbalanceChunk)
            document.add(mOrderbalanceParagraph)
            val mOrderbalanceChunk1 =
                Chunk("Txn Type: " + binding.tranTypeName.text.toString(), mOrderIdFont)
            val mOrderbalanceParagraph1 = Paragraph(mOrderbalanceChunk1)
            document.add(mOrderbalanceParagraph1)
            val mOrdertxnAmtChunk =
                Chunk("Txn Amount:" + binding.transactionamount.text.toString(), mOrderIdFont)
            val mOrdertxnAmtParagraph = Paragraph(mOrdertxnAmtChunk)
            document.add(mOrdertxnAmtParagraph)
            val mOrderAcNameFont = Font(urName, mHeadingFontSize, Font.NORMAL, mColorAccent)
            val mOrderAcNameChunk = Chunk("Thank You", mOrderAcNameFont)
            val mOrderAcNameParagraph = Paragraph(mOrderAcNameChunk)
            mOrderAcNameParagraph.alignment = Element.ALIGN_RIGHT
            document.add(mOrderAcNameParagraph)
            val mOrderAcNameValueFont = Font(urName, mValueFontSize, Font.NORMAL, BaseColor.BLACK)
            val mOrderAcNameValueChunk = Chunk(SdkConstants.BRAND_NAME, mOrderAcNameValueFont)
            val mOrderAcNameValueParagraph = Paragraph(mOrderAcNameValueChunk)
            mOrderAcNameValueParagraph.alignment = Element.ALIGN_RIGHT
            document.add(mOrderAcNameValueParagraph)
            document.close()
            Toast.makeText(
                requireActivity(),
                "PDF saved in the internal storage",
                Toast.LENGTH_SHORT
            ).show()
            val bundle = Bundle()
            bundle.putString("filePath", dest)
            findNavController().navigate(
                R.id.action_transactionStatusFragment_to_previewPDFFragment2,
                bundle
            )
        } catch (ie: IOException) {
            Log.e("createPdf: Error ", "" + ie.localizedMessage)
        } catch (ie: DocumentException) {
            Log.e("createPdf: Error ", "" + ie.localizedMessage)
        } catch (ae: ActivityNotFoundException) {
            Toast.makeText(
                requireActivity(),
                "No application found to open this file.",
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun takeMultiplePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.BLUETOOTH
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.BLUETOOTH_ADMIN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                registerActivity.launch(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    )
                )
            }
        } else {
            registerActivity.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            )

        }
    }


    private fun showTransactionDetails(context: Context) {
        try {
            val dialog = Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.transaction_matm_details_layout)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
            )
            val rrefNum = dialog.findViewById<TextView>(R.id.rref_num)
            val midNo = dialog.findViewById<TextView>(R.id.midNo)
            val terminalId = dialog.findViewById<TextView>(R.id.terminalId)
            val cardNumber1 = dialog.findViewById<TextView>(R.id.card_number)
            val txnType = dialog.findViewById<TextView>(R.id.txnType)
            val cardTransactionAmount = dialog.findViewById<TextView>(R.id.card_transaction_amount)
            val dialogBtnClose = dialog.findViewById<Button>(R.id.close_Btn)

            rrefNum.text = rrnNo
            midNo.text = mid
            terminalId.text = tid
            cardNumber1.text = cardNumber
            cardTransactionAmount.text = binding.transactionamount.text.toString()
            txnType.text = binding.tranTypeName.text.toString()
            dialogBtnClose.setOnClickListener {
                dialog.cancel()
            }
            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @SuppressLint("SetTextI18n")
    private fun init() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            })

        rrnNo = requireArguments().getString("RRN_NO").toString()
        txnId = requireArguments().getString("TXN_ID").toString()
        cardType = requireArguments().getString("CARD_TYPE").toString()
        inVoice = requireArguments().getString("INVOICE").toString()
        transactionId = requireArguments().getString("TRANSACTION_ID").toString()
        statusCode = requireArguments().getString("status_code").toString()
        transactionAmount = requireArguments().getString("TRANSACTION_AMOUNT").toString()
        aid = requireArguments().getString("AID").toString()
        mid = requireArguments().getString("MID").toString()
        if (mid == "null" || mid == "") {
            mid = "NA"
        }
        tid = requireArguments().getString("TID").toString()
        flag = requireArguments().getString("flag").toString()
        responseCode = requireArguments().getString("RESPONSE_CODE").toString()
        transactionType = requireArguments().getString("TRANSACTION_TYPE").toString()
        apprCode = requireArguments().getString("APPR_CODE").toString()
        cardNumber = requireArguments().getString("CARD_NUMBER").toString()
        transactionType = requireArguments().getString("TRANSACTION_TYPE").toString()
        cardHolderName = requireArguments().getString("CARD_HOLDERNAME").toString()
        if (cardHolderName == "") {
            cardHolderName = "N/A"
        }
        amount = requireArguments().getString("AMOUNT").toString()
        appName = requireArguments().getString("APP_NAME").toString()

        val date = Calendar.getInstance().time
        val formatter: DateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH)
        currentDateTime = formatter.format(date)
        binding.dateTime.text = currentDateTime
        binding.accountholdername.text = cardHolderName
        binding.accountnumber.text = cardNumber
        binding.txnID.text = if (transactionId != null && transactionId!!.isNotEmpty()) {
            transactionId
        } else {
            "N/A"
        }
        if (cardType == null) {
            binding.cardType.isVisible = false
        } else {
            if (cardType.equals("MASTER", ignoreCase = true) || cardType.equals(
                    "mastercard", true
                )
            ) {
                binding.cardType.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(), R.drawable.mastercard
                    )
                )
            } else if (cardType.equals("visa", true)) {
                binding.cardType.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(), R.drawable.visacard
                    )
                )
            } else if (cardType.equals("RUPAY", true) || cardType.equals("RuPay Debit", true)) {
                binding.cardType.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(), R.drawable.rupaycard
                    )
                )
            }
        }
        if (amount != null) {
            amount = if (amount.equals("N/A", true) || amount.equals("NA", true)) {
                "N/A"
            } else {
                replaceWithZero(amount!!)
            }
        }
        if (transactionAmount != null && transactionAmount!!.isNotEmpty()) {
            transactionAmount =
                if (transactionAmount.equals("0", ignoreCase = true) || transactionAmount.equals(
                        "N/A", ignoreCase = true
                    ) || transactionAmount.equals(
                        "NA", ignoreCase = true
                    ) || transactionAmount.equals(
                        "null", ignoreCase = true
                    ) || transactionAmount == null
                ) {
                    "N/A"
                } else {
                    replaceWithZero(transactionAmount!!)
                }
        } else {
            transactionAmount = "NA"
        }
        if (flag.equals("failure", true)) {
            binding.txtStatus.setTextColor(
                ContextCompat.getColor(
                    requireActivity(), R.color.redpos
                )
            )
            binding.statusIcon.setImageResource(R.drawable.failed)
            statusText = "Failed"
            binding.bkgLayout.isVisible = true
            binding.downloadBtn.isVisible = true
            binding.printBtn.isVisible = true
            if (responseCode == "00") {
                binding.amount.text = "Transaction Failed"
                binding.amount.setTextColor(ContextCompat.getColor(requireActivity(), R.color.red))
            } else {
                binding.amount.text = statusCode
                binding.amount.setTextColor(ContextCompat.getColor(requireActivity(), R.color.red))
            }
            if (transactionType.equals("cash", true)) {
                binding.transactionamount.text = "₹$transactionAmount"
                binding.tranTypeName.text = "Cash Withdrawal"
            } else if (transactionType.equals("POS", true)) {
                binding.amount.text = transactionAmount
                binding.transactionamount.text = transactionAmount
            } else {
                binding.tranTypeName.text = "Balance Enquiry"
                binding.transactionamount.text = "NA"
            }
        } else {
            //Show Success
            //show download receipt button
            binding.statusIcon.setImageResource(R.drawable.pos_success)
            statusText = "Success"
            if (transactionType.equals("cash", ignoreCase = true)) {
                binding.amount.text = "₹$transactionAmount"
                binding.transactionamount.text = "₹$transactionAmount"
                binding.tranTypeName.text = "Cash Withdrawal"
                binding.txtStatus.text = "TRANSACTION SUCCESSFUL"
            } else {
                binding.amount.text = "Success"
                binding.txtStatus.text = "Balance Amount: Rs. $amount"
                binding.txtStatus.setTextColor(
                    ContextCompat.getColor(requireActivity(), R.color.black)
                )
                binding.tranTypeName.text = "Balance Enquiry"
                binding.transactionamount.text = "NA"
            }
        }
    }

    private fun replaceWithZero(amount: String): String? {
        val finalAmount = amount.toInt() / 100f
        val formatter = DecimalFormat("##,##,##,##0.00")
        return formatter.format(finalAmount.toString().toDouble())
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onScanMSR(buffer: String?, cardtrack: CardReader.CARD_TRACK?) {
        cardTrackType = cardtrack
        creditData = buffer
        requireActivity().runOnUiThread {
        }
    }

    override fun onScanDLCard(buffer: String?) {
        val dlCardData: CardReader.DLCardData = mCardreader!!.decodeDLData(buffer)
        val name = """
            NAME:${dlCardData.NAME}
            
            """.trimIndent()
        val SWD = """
            SWD Of: ${dlCardData.SWD_OF}
            
            """.trimIndent()
        val dob = """
            DOB: ${dlCardData.DOB}
            
            """.trimIndent()
        val dlNum = """
            DLNUM: ${dlCardData.DL_NUM}
            
            """.trimIndent()
        val issAuth = """
            ISS AUTH: ${dlCardData.ISS_AUTH}
            
            """.trimIndent()
        val doi = """
            DOI: ${dlCardData.DOI}
            
            """.trimIndent()
        val tp = """
            VALID TP: ${dlCardData.VALID_TP}
            
            """.trimIndent()
        val ntp = """
            VALID NTP: ${dlCardData.VALID_NTP}
            
            """.trimIndent()


        requireActivity().runOnUiThread {
            //                editText.setText(data);
        }
    }

    override fun onScanRCCard(buffer: String?) {
        val rcCardData: CardReader.RCCardData = mCardreader!!.decodeRCData(buffer)
        val regNum = """
            REG NUM: ${rcCardData.REG_NUM}
            
            """.trimIndent()
        val regName = """
            REG NAME: ${rcCardData.REG_NAME}
            
            """.trimIndent()
        val regUpto = """
            REG UPTO: ${rcCardData.REG_UPTO}
            
            """.trimIndent()

        val data = regNum + regName + regUpto

        requireActivity().runOnUiThread {
            //                editText.setText(data);
        }
    }

    override fun onScanRFD(buffer: String?) {
        val stringBuffer = StringBuffer()
        stringBuffer.append(buffer)
        var temp = ""
        try {
            temp = stringBuffer.deleteCharAt(8).toString()
        } catch (e: Exception) {
            // TODO: handle exception
        }
        val data = temp

        requireActivity().runOnUiThread { //rfText.setText("RF ID:   " + data);
            //                editText.setText("ID " + data);
            try {
                mAemprinter!!.print(data)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onScanPacket(buffer: String?) {
        if (buffer == "PRINTEROK") {
            val stringBuffer = StringBuffer()
            stringBuffer.append(buffer)
            var temp = ""
            try {
                temp = stringBuffer.toString()
            } catch (e: Exception) {
                // TODO: handle exception
            }
            tempdata = temp
            val strData: String = tempdata!!.replace("|", "&")
            //Log.e("BufferData",data);
            val formattedData = arrayOf(strData.split("&".toRegex(), 3).toTypedArray())
            // Log.e("Response Data",formattedData[2]);
            responseString = formattedData[0][2]
            responseArray[0] = responseString!!.replace("^", "")
            Log.e("Response Array", responseArray[0]!!)
            requireActivity().runOnUiThread {
                replacedData = tempdata!!.replace("|", "&")
                formattedData[0] = replacedData!!.split("&".toRegex(), 3).toTypedArray()
                response = formattedData[0][2]

            }
        } else {
            val stringBuffer = StringBuffer()
            stringBuffer.append(buffer)
            var temp = ""
            try {
                temp = stringBuffer.toString()
            } catch (e: Exception) {
                // TODO: handle exception
            }
            tempdata = temp
            val strData: String = tempdata!!.replace("|", "&")
            //Log.e("BufferData",data);
            val formattedData = arrayOf(strData.split("&".toRegex(), 3).toTypedArray())
            // Log.e("Response Data",formattedData[2]);
            responseString = formattedData[0][2]
            responseArray[0] = responseString!!.replace("^", "")
            Log.e("Response Array", responseArray[0]!!)
            requireActivity().runOnUiThread {
                replacedData = tempdata!!.replace("|", "&")
                formattedData[0] = replacedData!!.split("&".toRegex(), 3).toTypedArray()
                response = formattedData[0][2]

            }
        }
    }

    override fun onDiscoveryComplete(aemPrinterList: ArrayList<String>?) {
        printerList = aemPrinterList
        for (i in aemPrinterList!!.indices) {
            val deviceName = aemPrinterList[i]
            val status = mAemscrybedevice!!.pairPrinter(deviceName)
            Log.e("STATUS", status)
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.setHeaderTitle("Select Printer to connect")
        for (i in printerList!!.indices) {
            menu.add(0, v.id, 0, printerList!![i])
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        super.onContextItemSelected(item)
        val printerName = item.title.toString()
        try {
            mAemscrybedevice!!.connectToPrinter(printerName)
            mCardreader = mAemscrybedevice!!.getCardReader(this)
            mAemprinter = mAemscrybedevice!!.aemPrinter
            GetPosConnectedPrinter.aemPrinter = mAemprinter
            Toast.makeText(
                requireActivity(),
                "Connected with $printerName",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: IOException) {
            if (e.message!!.contains("Service discovery failed")) {
                Toast.makeText(
                    requireActivity(),
                    "Not Connected\n$printerName is unreachable or off otherwise it is connected with other device",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (e.message!!.contains("Device or resource busy")) {
                Toast.makeText(
                    requireActivity(),
                    "the device is already connected",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireActivity(),
                    "Unable to connect",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        return true
    }

}