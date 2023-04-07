package com.example.aeps_sdk.unifiedaeps

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aeps_sdk.BaseFragment
import com.example.aeps_sdk.application.AppController
import com.example.aeps_sdk.databinding.FragmentUnifiedAepsMiniStatementBinding
import com.example.aeps_sdk.unifiedaeps.models.response.MiniStatement
import com.example.aeps_sdk.unifiedaeps.viewmodel.UnifiedAepsViewModel
import com.example.aeps_sdk.utils.GetPosConnectedPrinter
import com.example.aeps_sdk.utils.SdkConstants
import com.example.aeps_sdk.vriddhi.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import wangpos.sdk4.libbasebinder.Printer
import java.io.IOException

class UnifiedAepsMiniStatementFragment : BaseFragment(), IAemCardScanner, IAemScrybe {
    private val TAG: String = UnifiedAepsTransactionStatusFragment::class.java.simpleName
    private var _binding: FragmentUnifiedAepsMiniStatementBinding? = null
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
    private var dataObject: String? = ""
    private var miniStatementsList: List<MiniStatement>? = null
    private var statementlistAdapter: UnifiedStatementListAdapter? = null
    private lateinit var unifiedAepsViewModel: UnifiedAepsViewModel
    private var mPrinter: Printer? = null
    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var printerList: ArrayList<String>? = null

    /* Parameters for the Vriddhi Printer*/
    private var mAemScrybeDevice: AEMScrybeDevice? = null
    private var mAemPrinter: AEMPrinter? = null
    private var mCardReader: CardReader? = null
    private var creditData: String? = null
    private var tempdata: String? = null
    private var replacedData: String? = null
    private var responseString: String? = null
    private var response: String? = null
    private var cardTrackType: CardReader.CARD_TRACK? = null
    private var responseArray = arrayOfNulls<String>(1)
    private var txnType: String? = null
    private var transactionTypeClass: TransactionType? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUnifiedAepsMiniStatementBinding.inflate(layoutInflater)
        unifiedAepsViewModel = (requireActivity().application as AppController).viewModel
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        transactionData()
        bindObserver()
        onClickListener()
        init()

    }

    private fun init() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            })
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bluetoothManager = requireActivity().getSystemService(
                BluetoothManager::class.java
            )
        }
        bluetoothAdapter = bluetoothManager!!.adapter
        mAemScrybeDevice = AEMScrybeDevice(this)
        printerList = ArrayList()
        binding.failureLayout.visibility = View.GONE
        binding.successLayout.visibility = View.VISIBLE
        binding.aadharNumTxt.text = aadhaarNo
        binding.transactionIdTxt.text = transactionID
        binding.bankNameTxt.text = bankName
        binding.accountBalanceTxt.text = balanceAmount

    }

    private fun onClickListener() {
        binding.okButton.setOnClickListener {
            requireActivity().finish()
        }
        binding.okSuccessButton.setOnClickListener {
            requireActivity().finish()
        }
        binding.successPrintButton.setOnClickListener {
            if (miniStatementsList == null) {
                Toast.makeText(
                    requireActivity(),
                    "No Data Found",
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                val deviceModel = Build.MODEL
                if (deviceModel.equals("A910", ignoreCase = true)) {

                } else if (deviceModel.equals("WPOS-3", ignoreCase = true)) {
                    //start printing with wiseasy internal printer
                    UnifiedPrintReceiptThread().start()
                } else {
                    registerForContextMenu(binding.successPrintButton)
                    if (bluetoothAdapter == null) {
                        Toast.makeText(
                            requireActivity(),
                            "Bluetooth NOT supported",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        if (bluetoothAdapter!!.isEnabled) {
                            if (GetPosConnectedPrinter.aemPrinter == null) {
                                printerList = mAemScrybeDevice!!.pairedPrinters
                                if (printerList!!.size > 0) {
                                    requireActivity().openContextMenu(it)
                                } else {
                                    showAlert("No Paired Printers found", requireContext())
                                }
                            } else {
                                mAemPrinter = GetPosConnectedPrinter.aemPrinter
                                callBluetoothFunction(
                                    binding.transactionIdTxt.text.toString(),
                                    binding.aadharNumTxt.text.toString(),
                                    binding.accountBalanceTxt.text.toString(),
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

    }

    private fun bindObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                unifiedAepsViewModel.miniStatementLiveData.observe(viewLifecycleOwner) { it ->
                    miniStatementsList = it
                    statementlistAdapter = miniStatementsList?.let {
                        UnifiedStatementListAdapter(
                            it,
                            requireActivity(), ::hideLoader
                        )
                    }
                    binding.statementList.adapter = statementlistAdapter
                    binding.statementList.layoutManager = LinearLayoutManager(requireActivity())
                    if (miniStatementsList == null || miniStatementsList?.size == 0) {
                        binding.successPrintButton.isVisible = false
                    }
                }
            }
        }
    }

    private fun transactionData() {
        dataObject = requireArguments().getString("txnData").toString()
        val `object` = JSONObject(dataObject)
        if (`object`.has("status")) {
            transactionStatus = `object`.getString("status").toString()
        }
        if (`object`.has("origin_identifier")) {
            aadhaarNo = `object`.getString("origin_identifier").toString()
        }
        if (`object`.has("createdDate")) {
            createdDate = `object`.getString("createdDate").toString()
        }
        if (`object`.has("txId")) {
            transactionID = `object`.getString("txId").toString()
        }
        if (`object`.has("bankName")) {
            bankName = `object`.getString("bankName").toString()
        }
        if (`object`.has("apiTid")) {
            referenceNo = `object`.getString("apiTid").toString()
        }
        if (`object`.has("balance")) {
            balanceAmount = `object`.getString("balance").toString()
        }
        if (`object`.has("transactionMode")) {
            transactionType = `object`.getString("transactionMode").toString()
        }
        if (`object`.has("isRetriable")) {
            isRetriable = `object`.getString("isRetriable").toBoolean()
        }
        if (`object`.has("apiComment")) {
            apiComment = `object`.getString("apiComment").toString()
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
            Log.e(
                TAG,
                "printReceipt: set density low 3"
            )
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
            result = mPrinter!!.printString("Success", 25, Printer.Align.CENTER, true, false)
            result = mPrinter!!.printString(
                "Transaction Id :" + binding.transactionIdTxt.text,
                20,
                Printer.Align.LEFT,
                false,
                false
            )
            result = mPrinter!!.printString(
                "Balance Amount :" + binding.accountBalanceTxt.text,
                20,
                Printer.Align.LEFT,
                false,
                false
            )
            result = mPrinter!!.printString(
                "Aadhaar Number :" + binding.aadharNumTxt.text,
                20,
                Printer.Align.LEFT,
                false,
                false
            )
            result = mPrinter!!.printString("STATEMENT", 23, Printer.Align.CENTER, true, false)
            if (miniStatementsList!!.isEmpty()) {
            } else {
                for (i in miniStatementsList!!.indices) {
                    val mSList = miniStatementsList!![i]
                    txnType = mSList.DebitCredit
                    transactionTypeClass = TransactionType(DebitOrCredit.valueOf(txnType!!))
                    result = mPrinter!!.printString(
                        mSList.Date
                            .toString() + "          " + transactionTypeClass!!.transactionType() + mSList.Amount,
                        20,
                        Printer.Align.LEFT,
                        false,
                        false
                    )
                    result = mPrinter!!.printString(
                        mSList.Type,
                        20,
                        Printer.Align.LEFT,
                        false,
                        false
                    )
                }
            }
            result = mPrinter!!.printStringExt(
                "Thank You",
                0,
                0f,
                2.0f,
                Printer.Font.SANS_SERIF,
                20,
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
                20,
                Printer.Align.RIGHT,
                true,
                true,
                false
            )
            result = mPrinter!!.printString(" ", 25, Printer.Align.CENTER, false, false)
            result = mPrinter!!.printString(" ", 25, Printer.Align.CENTER, false, false)
            Log.e(
                TAG,
                "printReceipt: print thank you result $result"
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
        var msg: String
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

    private fun callBluetoothFunction(
        txnId: String,
        aadharNo: String,
        accountBalance: String,
        view: View
    ) {
        try {
            mAemPrinter!!.POS_FontThreeInchCENTER()
            mAemPrinter!!.POS__FontThreeInchDOUBLEHIEGHT()
            mAemPrinter!!.print(SdkConstants.SHOP_NAME.trim())
            mAemPrinter!!.print("\n")
            mAemPrinter!!.POS_FontThreeInchCENTER()
            mAemPrinter!!.POS__FontThreeInchDOUBLEHIEGHT()
            mAemPrinter!!.print("SUCCESS")
            mAemPrinter!!.print("\n")
            mAemPrinter!!.print("\n")
            mAemPrinter!!.print("Txn id: $txnId")
            mAemPrinter!!.print("\n")
            mAemPrinter!!.print("Available Balance: ")
            mAemPrinter!!.print(accountBalance)
            mAemPrinter!!.print("\n")
            mAemPrinter!!.print("Aadhar No: $aadharNo")
            mAemPrinter!!.print("\n")
            mAemPrinter!!.print("\n")
            mAemPrinter!!.POS_FontThreeInchCENTER()
            mAemPrinter!!.print("Statement")
            mAemPrinter!!.print("\n")
            mAemPrinter!!.print("\n")
            if (miniStatementsList == null || miniStatementsList!!.isEmpty()) {
            } else {
                for (i in miniStatementsList!!.indices) {
                    val mSList = miniStatementsList!![i]
                    txnType = mSList.DebitCredit
                    transactionTypeClass = TransactionType(DebitOrCredit.valueOf(txnType!!))
                    if (mSList.Date == null || mSList.Type == null) {
                        mAemPrinter!!.print("""           ${transactionTypeClass!!.transactionType()}${mSList.Amount} """)
                    } else {
                        mAemPrinter!!.print(
                            mSList.Date.trim() + "          " + transactionTypeClass!!.transactionType() + mSList.Amount + "\n" + mSList.Type
                                .trim()
                        )
                    }
                    mAemPrinter!!.print("\n")
                }
            }
            mAemPrinter!!.print("Thank You")
            mAemPrinter!!.POS_FontThreeInchRIGHT()
            mAemPrinter!!.print("\n")
            mAemPrinter!!.print(SdkConstants.BRAND_NAME.trim())
            mAemPrinter!!.POS_FontThreeInchRIGHT()
            mAemPrinter!!.print("\n")
            mAemPrinter!!.print("\n")
            mAemPrinter!!.print("\n")
            mAemPrinter!!.print("\n")
            mAemPrinter!!.POS_FontThreeInchCENTER()
            mAemPrinter!!.print(" ")
            mAemPrinter!!.print(" ")
            mAemPrinter!!.print("\n")
        } catch (e: IOException) {
//            e.printStackTrace();
            try {
                GetPosConnectedPrinter.aemPrinter = null
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onScanMSR(buffer: String?, cardtrack: CardReader.CARD_TRACK?) {
        cardTrackType = cardtrack
        creditData = buffer
        requireActivity().runOnUiThread(Runnable {
        })
    }

    override fun onScanDLCard(buffer: String?) {
    }

    override fun onScanRCCard(buffer: String?) {
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

        requireActivity().runOnUiThread {
            try {
                mAemPrinter!!.print(data)
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
            val formattedData = arrayOf(strData.split("&".toRegex(), 3).toTypedArray())
            responseString = formattedData[0][2]
            responseArray[0] = responseString!!.replace("^", "")
            Log.e("Response Array", responseArray[0]!!)
            requireActivity().runOnUiThread {
                replacedData = tempdata!!.replace("|", "&")
                formattedData[0] = replacedData!!.split("&".toRegex(), 3).toTypedArray()
                response = formattedData[0][2]
                if (response!!.contains("BAT")) {
                }
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
            val formattedData = arrayOf(strData.split("&".toRegex(), 3).toTypedArray())
            responseString = formattedData[0][2]
            responseArray[0] = responseString!!.replace("^", "")
            Log.e("Response Array", responseArray[0]!!)
            requireActivity().runOnUiThread {
                replacedData = tempdata!!.replace("|", "&")
                formattedData[0] = replacedData!!.split("&".toRegex(), 3).toTypedArray()
                response = formattedData[0][2]
                if (response!!.contains("BAT")) {
                }
            }
        }
    }

    override fun onDiscoveryComplete(aemPrinterList: ArrayList<String>?) {
        printerList = aemPrinterList
        for (i in aemPrinterList!!.indices) {
            val deviceName = aemPrinterList[i]
            val status = mAemScrybeDevice!!.pairPrinter(deviceName)
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
        var printerName = item.title.toString()
        try {
            mAemScrybeDevice!!.connectToPrinter(printerName)
            mCardReader = mAemScrybeDevice!!.getCardReader(this)
            mAemPrinter = mAemScrybeDevice!!.aemPrinter
            GetPosConnectedPrinter.aemPrinter = mAemPrinter
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