package com.example.aeps_sdk.unifiedaeps

import android.Manifest.permission.*
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
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
import com.example.aeps_sdk.databinding.FragmentUnifiedAepsTransactionStatusBinding
import com.example.aeps_sdk.utils.FileUtils
import com.example.aeps_sdk.utils.GetPosConnectedPrinter
import com.example.aeps_sdk.utils.SdkConstants
import com.example.aeps_sdk.vriddhi.*
import com.itextpdf.text.*
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.text.pdf.draw.LineSeparator
import org.json.JSONObject
import wangpos.sdk4.libbasebinder.Printer
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class UnifiedAepsTransactionStatusFragment : BaseFragment(), IAemCardScanner, IAemScrybe {
    val TAG: String = UnifiedAepsTransactionStatusFragment::class.java.simpleName

    private var _binding: FragmentUnifiedAepsTransactionStatusBinding? = null
    private val binding get() = _binding!!
    private var transactionStatus: String? = ""
    private var aadhaarNo: String? = ""
    private var createdDate: String? = ""
    private var transactionID: String? = ""
    private var bankName: String? = ""
    private var referenceNo: String? = ""
    private var balanceAmount: String? = ""
    private var transactionType: String? = ""
    private var isRetriable: Boolean? = false
    private var apiComment: String? = ""
    private var statusTxt: String? = ""
    private var dataObject: String? = ""
    private var balance: String? = "N/A"
    private var isPermissionGranted: Boolean = false
    private var filePath = ""
    private var mPrinter: Printer? = null
    private var bluetoothManager: BluetoothManager? = null
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
            if (permission[READ_EXTERNAL_STORAGE] == true || permission[BLUETOOTH_CONNECT] == true || permission[BLUETOOTH] == true || permission[BLUETOOTH_SCAN] == true || permission[BLUETOOTH_ADMIN] == true) {
                isPermissionGranted = true
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUnifiedAepsTransactionStatusBinding.inflate(layoutInflater)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        transactionData()
        onClickListener()
        takeMultiplePermission()
        init()
    }

    private fun init() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                    SdkConstants.FAILEDVALUE = ""
                }
            })
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bluetoothManager = requireActivity().getSystemService(
                BluetoothManager::class.java
            )
        }
        bluetoothAdapter = bluetoothManager!!.adapter
        mAemscrybedevice = AEMScrybeDevice(this)
        printerList = ArrayList()
        if (dataObject == null) {
            binding.statusIcon.setImageResource(R.drawable.hero_failure)
            binding.balanceText.text = "Failed"
            statusTxt = "Failed"
            binding.retryBtn.isVisible = true
        } else {
            if (dataObject != null && transactionStatus != null) {
                aadhaarNo = if (aadhaarNo == "") {
                    "N/A"
                } else {
                    val buf = StringBuffer(aadhaarNo!!)
                    buf.replace(0, 10, "XXXX-XXXX-")
                    println(buf.length)
                    buf.toString()
                }
                if (createdDate != "") {
                    binding.dateTime.text = createdDate
                }
                binding.txnID.text = "Transaction ID: $transactionID"
                binding.bankName.text = bankName
                if (balanceAmount != "" && !balanceAmount.equals("N/A", ignoreCase = true)) {
                    balance = "Rs. $balanceAmount"
                }
                if (transactionType.equals(
                        "AEPS_CASH_WITHDRAWAL",
                        ignoreCase = true
                    ) || SdkConstants.transactionType == SdkConstants.cashWithdrawal
                ) {
                    binding.cardAmount.text =
                        "Txn Amount: Rs.  ${SdkConstants.transactionAmount}"
                } else if (transactionType.equals(
                        "AEPS_BALANCE_ENQUIRY",
                        ignoreCase = true
                    ) || SdkConstants.transactionType == SdkConstants.balanceEnquiry
                ) {
                    binding.cardAmount.text = "Balance Amount: $balance"
                } else {
                    binding.cardAmount.text = "Balance Amount: $balance"
                }
                if (transactionStatus.equals("SUCCESS", ignoreCase = true)) {
                    statusTxt = "SUCCESS"
                } else {
                    if (isRetriable!!) {
                        binding.retryBtn.isVisible = true
                        binding.balanceText.text = apiComment
                        binding.statusIcon.setImageResource(R.drawable.hero_failure)
                        statusTxt = "Failed"
                    } else {
                        binding.balanceText.text = apiComment
                        binding.statusIcon.setImageResource(R.drawable.hero_failure)
                        statusTxt = "Failed"
                    }
                }
            } else {
                binding.balanceText.text = apiComment
                binding.statusIcon.setImageResource(R.drawable.hero_failure)
                statusTxt = "Failed"
            }
        }
    }

    private fun onClickListener() {
        binding.txndetailsBtn.setOnClickListener {
            showTransactionDetails()
        }
        binding.closeBtn.setOnClickListener {
            requireActivity().finish()
            SdkConstants.FAILEDVALUE = ""
        }
        binding.retryBtn.setOnClickListener {
            SdkConstants.FAILEDVALUE = "FAILEDDATA"
            findNavController().popBackStack()
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
            if (deviceModel.equals("WPOS-3", ignoreCase = true)) {
                //start printing with wiseasy internal printer
                UnifiedPrintReceiptThread()
                    .start()
            } else {
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
                                requireActivity().openContextMenu(it)
                            } else {
                                showAlert("No Paired Printers found", requireContext())
                            }
                        } else {
                            mAemprinter = GetPosConnectedPrinter.aemPrinter
                            callBluetoothFunction(
                                binding.txnID.text.toString(),
                                aadhaarNo!!,
                                binding.dateTime.text.toString(),
                                binding.bankName.text.toString(),
                                referenceNo!!,
                                transactionType!!,
                                it
                            )
                        }
                    } else {
                        GetPosConnectedPrinter.aemPrinter = null
                        val turnOn = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        startActivityForResult(turnOn, 0)
                    }
                }
            }
        }
    }

    /*
     * @Method for normal printer(Vriddhi)
     * */
    private fun callBluetoothFunction(
        txnId: String,
        aadharNo: String,
        date: String,
        bank_name: String,
        reffNo: String,
        type: String,
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
            mAemprinter!!.print(statusTxt)
            mAemprinter!!.print("\n\n")
            mAemprinter!!.print(txnId)
            mAemprinter!!.print("\n")
            mAemprinter!!.print("Aadhaar Number: $aadharNo")
            mAemprinter!!.print("\n")
            mAemprinter!!.print("Date/Time: $date")
            mAemprinter!!.print("\n")
            mAemprinter!!.print("Bank Name: $bank_name")
            mAemprinter!!.print("\n")
            mAemprinter!!.print("RRN: $reffNo")
            mAemprinter!!.print("\n")
            mAemprinter!!.print("Balance Amount: $balance")
            mAemprinter!!.print("\n")
            if (transactionType
                    .equals("AEPS_CASH_WITHDRAWAL", ignoreCase = true)
            ) {
                mAemprinter!!.print("Transaction Amount: Rs. " + SdkConstants.transactionAmount)
            } else {
                mAemprinter!!.print("Transaction Amount: " + "N/A")
            }
            mAemprinter!!.print("\n")
            mAemprinter!!.print("Transaction Type: $type")
            mAemprinter!!.print("\n\n")
            mAemprinter!!.setFontType(AEMPrinter.FONT_002)
            mAemprinter!!.POS_FontThreeInchRIGHT()
            mAemprinter!!.print("Thank You \n")
            mAemprinter!!.setFontType(AEMPrinter.TEXT_ALIGNMENT_CENTER)
            mAemprinter!!.setFontType(AEMPrinter.TEXT_ALIGNMENT_RIGHT)
            mAemprinter!!.POS_FontThreeInchRIGHT()
            mAemprinter!!.print(SdkConstants.BRAND_NAME)
            mAemprinter!!.setCarriageReturn()
            mAemprinter!!.setCarriageReturn()
            mAemprinter!!.setCarriageReturn()
            mAemprinter!!.setCarriageReturn()
            data = printerStatus()!!
            mAemprinter!!.print(data)
            mAemprinter!!.print("\n")
        } catch (e: IOException) {
//            e.printStackTrace();
            try {
                getConnection(view)
            } catch (exception: Exception) {
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

    private fun getConnection(view: View) {
        GetPosConnectedPrinter.aemPrinter = null
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
                        aadhaarNo!!,
                        binding.dateTime.text.toString(),
                        binding.bankName.text.toString(),
                        referenceNo!!,
                        transactionType!!,
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


    inner class UnifiedPrintReceiptThread : Thread() {
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
            mPrinter?.getPrinterStatus(status)
            Log.e(TAG, "Printer Status is " + status[0])
            val msg: String
            when (status[0]) {
                0x00 -> {
                    msg = "Printer status OK"
                    Log.e(TAG, "check printer status: $msg")
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

    private fun showLog(msg: String) {
        requireActivity().runOnUiThread {
            Toast.makeText(
                requireActivity(),
                msg,
                Toast.LENGTH_SHORT
            ).show()
        }
        Log.e(TAG, "Printer status: $msg")
    }

    private fun startPrinting() {
        val result: Int
        try {
            result = mPrinter!!.printInit()
            Log.e(TAG, "startPrinting: Printer init result $result")
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun printReceipt() {
        var result: Int
        try {
            Log.e(TAG, "printReceipt: set density low 3")
            mPrinter!!.setGrayLevel(3)
            result = mPrinter!!.printStringExt(
                SdkConstants.SHOP_NAME,
                0,
                0f,
                2.0f,
                Printer.Font.SANS_SERIF,
                25,
                Printer.Align.CENTER,
                true,
                false,
                true
            )
            result =
                mPrinter!!.printString("Transaction Report", 25, Printer.Align.CENTER, true, false)
            result = if (statusTxt.equals("FAILED", ignoreCase = true)) {
                mPrinter!!.printString("Failure", 25, Printer.Align.CENTER, true, false)
            } else {
                mPrinter!!.printString("Success", 25, Printer.Align.CENTER, true, false)
            }
            result = mPrinter!!.printString(
                "------------------------------------------",
                30,
                Printer.Align.CENTER,
                true,
                false
            )
            result = mPrinter!!.printString(
                "Transaction Id :$transactionID",
                18,
                Printer.Align.LEFT,
                true,
                false
            )
            result = mPrinter!!.printString(
                "Aadhaar Number :$aadhaarNo",
                18,
                Printer.Align.LEFT,
                true,
                false
            )
            result = mPrinter!!.printString(
                "Date/Time : " + binding.dateTime.text.toString().trim { it <= ' ' },
                18,
                Printer.Align.LEFT,
                false,
                false
            )
            result =
                mPrinter!!.printString("Bank Name : $bankName", 18, Printer.Align.LEFT, true, false)
            result =
                mPrinter!!.printString("RRN : $referenceNo", 18, Printer.Align.LEFT, true, false)
            result = mPrinter!!.printString(
                "Balance Amount :$balance",
                18,
                Printer.Align.LEFT,
                true,
                false
            )
            result = if (transactionType
                    .equals("AEPS_CASH_WITHDRAWAL", ignoreCase = true)
            ) {
                mPrinter!!.printString(
                    "Transaction Amount : Rs ." + SdkConstants.transactionAmount,
                    18,
                    Printer.Align.LEFT,
                    true,
                    false
                )
            } else {
                mPrinter!!.printString(
                    "Transaction Amount :" + "N/A",
                    18,
                    Printer.Align.LEFT,
                    true,
                    false
                )
            }
            result = mPrinter!!.printString(
                "Transaction Type : $transactionType",
                18,
                Printer.Align.LEFT,
                true,
                false
            )
            result = mPrinter!!.printStringExt(
                "Thank You !",
                0,
                0f,
                2.0f,
                Printer.Font.SANS_SERIF,
                18,
                Printer.Align.RIGHT,
                true,
                true,
                false
            )
            result = mPrinter!!.printStringExt(
                SdkConstants.BRAND_NAME,
                0,
                0f,
                2.0f,
                Printer.Font.SANS_SERIF,
                18,
                Printer.Align.RIGHT,
                true,
                true,
                false
            )
            result = mPrinter!!.printString(" ", 25, Printer.Align.CENTER, false, false)
            result = mPrinter!!.printString(" ", 25, Printer.Align.CENTER, false, false)
            Log.e(
                TAG,
                "printReceipt: print thank You result $result"
            )
            result = mPrinter!!.printPaper(30)
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * in between printing if any error occur then this method will show the toast
     */
    private fun showPrinterStatus(result: Int) {
        val msg: String
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
            document.setMargins(0F, 0F, 50F, 50F)
            document.addCreationDate()
            document.addAuthor("")
            document.addCreator("")
            val rect = Rectangle(577F, 825F, 18F, 15F)
            rect.enableBorderSide(1)
            rect.enableBorderSide(2)
            rect.enableBorderSide(4)
            rect.enableBorderSide(8)
            rect.border = Rectangle.BOX
            rect.borderWidth = 2F
            rect.borderColor = BaseColor.BLACK
            document.add(rect)
            val mColorAccent = BaseColor(0, 153, 204, 255)
            val mHeadingFontSize = 24.0f
            val mValueFontSize = 26.0f

            /**
             * How to USE FONT....
             */
            val urName: BaseFont =
                BaseFont.createFont("assets/fonts/brandon_medium.otf", "UTF-8", BaseFont.EMBEDDED)

            // LINE SEPARATOR
            val lineSeparator = LineSeparator()
            lineSeparator.lineColor = BaseColor(0, 0, 0, 68)
            BaseFont.createFont(
                BaseFont.TIMES_ROMAN,
                BaseFont.CP1252,
                BaseFont.EMBEDDED
            )
            val mOrderDetailsTitleFont = Font(urName, 36.0f, Font.NORMAL, BaseColor.BLACK)
            val mOrderDetailsTitleChunk = Chunk(SdkConstants.SHOP_NAME, mOrderDetailsTitleFont)
            val mOrderDetailsTitleParagraph = Paragraph(mOrderDetailsTitleChunk)
            mOrderDetailsTitleParagraph.alignment = Element.ALIGN_CENTER
            document.add(mOrderDetailsTitleParagraph)
            val mOrderShopTitleFont = Font(urName, 25.0f, Font.NORMAL, BaseColor.BLACK)
            val mOrderShopTitleChunk = Chunk("Receipt", mOrderShopTitleFont)
            val mOrderShopTitleParagraph = Paragraph(mOrderShopTitleChunk)
            mOrderShopTitleParagraph.alignment = Element.ALIGN_CENTER
            document.add(mOrderShopTitleParagraph)
            val mOrderDetailsTitleFont11: Font =
                if (statusTxt.equals("FAILED", ignoreCase = true)) {
                    Font(urName, 40.0f, Font.NORMAL, BaseColor.RED)
                } else {
                    Font(urName, 40.0f, Font.NORMAL, BaseColor.GREEN)
                }
            val mOrderDetailsTitleChunk1 = Chunk(statusTxt, mOrderDetailsTitleFont11)
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
            document.add(Paragraph("\n"))
            val p1 = Paragraph()
            p1.add(Chunk("Operation Performed : ", mOrderDateFont))
            p1.add(Chunk("Unified AePS", mOrderDateValueFont))
            document.add(p1)
            document.add(Paragraph("\n"))
            val mOrderDetailsFont = Font(urName, 30.0f, Font.BOLD, mColorAccent)
            val mOrderDetailsChunk = Chunk("Transaction Details", mOrderDetailsFont)
            val mOrderDetailsParagraph = Paragraph(mOrderDetailsChunk)
            mOrderDetailsParagraph.alignment = Element.ALIGN_CENTER
            document.add(mOrderDetailsParagraph)
            document.add(Paragraph(""))
            document.add(Paragraph(""))
            document.add(Paragraph("\n\n"))


            // Fields of Order Details...
            // Adding Chunks for Title and value
            val mOrderIdFont = Font(urName, mValueFontSize, Font.NORMAL, BaseColor.BLACK)
            val mOrderIdChunk = Chunk("Transaction ID: $transactionID", mOrderIdFont)
            val mOrderTxnParagraph = Paragraph(mOrderIdChunk)
            document.add(mOrderTxnParagraph)
            val mOrderIdValueChunk = Chunk("Aadhaar Number: $aadhaarNo", mOrderIdFont)
            val mOrderaadharParagraph = Paragraph(mOrderIdValueChunk)
            document.add(mOrderaadharParagraph)
            val mBankNameChunk = Chunk(
                "Bank Name: " + binding.bankName.text.toString().trim { it <= ' ' },
                mOrderIdFont
            )
            val mBankNameParagraph = Paragraph(mBankNameChunk)
            document.add(mBankNameParagraph)
            val mOrderrrnChunk = Chunk("RRN: $referenceNo", mOrderIdFont)
            val mOrderrnParagraph = Paragraph(mOrderrrnChunk)
            document.add(mOrderrnParagraph)
            val mOrderbalanceChunk = Chunk("Balance Amount: $balance", mOrderIdFont)
            val mOrderbalanceParagraph = Paragraph(mOrderbalanceChunk)
            document.add(mOrderbalanceParagraph)
            if (transactionType
                    .equals("AEPS_CASH_WITHDRAWAL", ignoreCase = true)
            ) {
                val mOrdertxnAmtChunk =
                    Chunk("Transaction Amount: Rs." + SdkConstants.transactionAmount, mOrderIdFont)
                val mOrdertxnAmtParagraph = Paragraph(mOrdertxnAmtChunk)
                document.add(mOrdertxnAmtParagraph)
            } else {
                val mOrdertxnAmtChunk = Chunk("Transaction Amount: " + "N/A", mOrderIdFont)
                val mOrdertxnAmtParagraph = Paragraph(mOrdertxnAmtChunk)
                document.add(mOrdertxnAmtParagraph)
            }
            val mOrdertxnTypeChunk = Chunk("Transaction Type: $transactionType", mOrderIdFont)
            val mOrdertxnTypeParagraph = Paragraph(mOrdertxnTypeChunk)
            document.add(mOrdertxnTypeParagraph)
            document.add(Paragraph(""))
            document.add(Paragraph(""))
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
                R.id.action_unifiedAepsTransactionStatusFragment_to_previewPDFFragment,
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

    private fun showTransactionDetails() {
        try {
            val dialog = Dialog(requireActivity())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.transaction_aeps_details_layout)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
            val aadharNumber = dialog.findViewById<View>(R.id.aadhar_number) as TextView
            val refNum = dialog.findViewById<View>(R.id.rref_num) as TextView
            val cardTransactionType =
                dialog.findViewById<View>(R.id.card_transaction_type) as TextView
            val cardTransactionAmount =
                dialog.findViewById<View>(R.id.card_transaction_amount) as TextView
            aadharNumber.text = aadhaarNo
            refNum.text = referenceNo
            cardTransactionType.text = transactionType
            cardTransactionAmount.text = balance
            val dialogBtnClose = dialog.findViewById<View>(R.id.close_Btn) as Button
            dialogBtnClose.setOnClickListener { dialog.cancel() }
            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun transactionData() {
        dataObject = requireArguments().getString("txnData").toString()
        val `object` = JSONObject(dataObject)
        transactionStatus = `object`.getString("status").toString()
        aadhaarNo = `object`.getString("origin_identifier").toString()
        createdDate = `object`.getString("createdDate").toString()
        transactionID = `object`.getString("txId").toString()
        bankName = `object`.getString("bankName").toString()
        referenceNo = `object`.getString("apiTid").toString()
        balanceAmount = `object`.getString("balance").toString()
        transactionType = `object`.getString("transactionMode").toString()
        isRetriable = `object`.getString("isRetriable").toBoolean()
        apiComment = `object`.getString("apiComment").toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun takeMultiplePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    requireActivity(),
                    BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    requireActivity(),
                    BLUETOOTH
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    requireActivity(),
                    BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    requireActivity(),
                    BLUETOOTH_ADMIN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                registerActivity.launch(
                    arrayOf(
                        READ_EXTERNAL_STORAGE,
                        BLUETOOTH_CONNECT,
                        BLUETOOTH,
                        BLUETOOTH_SCAN,
                        BLUETOOTH_CONNECT
                    )
                )
            }
        } else {
            registerActivity.launch(
                arrayOf(
                    READ_EXTERNAL_STORAGE,
                    BLUETOOTH_CONNECT,
                    BLUETOOTH,
                    BLUETOOTH_SCAN,
                    BLUETOOTH_CONNECT
                )
            )

        }
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

        val data = name + SWD + dob + dlNum + issAuth + doi + tp + ntp

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

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
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