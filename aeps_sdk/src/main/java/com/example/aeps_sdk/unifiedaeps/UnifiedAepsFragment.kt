package com.example.aeps_sdk.unifiedaeps

import android.Manifest.permission.*
import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.aeps_sdk.BaseFragment
import com.example.aeps_sdk.R
import com.example.aeps_sdk.utils.Util
import com.example.aeps_sdk.application.AppController
import com.example.aeps_sdk.callbacks.OnDriverDataListener
import com.example.aeps_sdk.databinding.FragmentUnifiedAepsBinding
import com.example.aeps_sdk.location.GpsTracker
import com.example.aeps_sdk.unifiedaeps.models.req.TransactionRequest
import com.example.aeps_sdk.unifiedaeps.viewmodel.UnifiedAepsViewModel
import com.example.aeps_sdk.unifiedaeps.viewmodel.UnifiedAepsViewModelFactory
import com.example.aeps_sdk.utils.ChangeTransformationMethod
import com.example.aeps_sdk.utils.NetworkResults
import com.example.aeps_sdk.utils.SdkConstants
import com.google.gson.Gson
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class UnifiedAepsFragment : BaseFragment(), OnDriverDataListener {
    private var aadhaarBool: Boolean = true
    private var virtualBool: Boolean = false
    private var _binding: FragmentUnifiedAepsBinding? = null
    private val binding get() = _binding!!
    private val marker: String = "|" // filtered in layout not to be in the string
    private var mKeyListenerSet = false
    private var mWannaDeleteHyphen = false
    private var mInside = false
    private var balanceInquiryAadhaarNo = ""
    private var flagFromDriver = false
    private var fmDeviceId = "Startek Eng-Inc."
    private var fmDeviceId2 = "Startek Eng-Inc.\u0000"
    private var fmDeviceId3 = "Startek Eng. Inc."
    private var fmDeviceId4 = "Startek"
    private var bankIINNumber = ""
    private var bankName = ""
    private var flagNameRdService = ""
    private var driverActivity: Class<*>? = null
    private var aadharNumberMain = ""
    private var gpsTracker: GpsTracker? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var latLong = ""
    private val sharedPrefs = "LastLatlong"
    private var lastLatlong: String? = null
    private var locationManager: LocationManager? = null
    private var gatewayPriority = 0
    private var encodedUrl = ""
    private val transactionTypeCW = "Cash Withdrawal"
    private val transactionTypeBE = "Request Balance"
    private val baseurlSdkUser = "https://aeps-prod-gateway-as1-5pwajhaz.ts.gateway.dev/"
    private lateinit var unifiedAepsViewModel: UnifiedAepsViewModel

    private val registerActivity =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ ->

        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUnifiedAepsBinding.inflate(inflater, container, false)
        val unifiedAepsRepo = (requireActivity().application as AppController).unifiedAepsRepo
        unifiedAepsViewModel = ViewModelProvider(
            requireActivity(),
            UnifiedAepsViewModelFactory(unifiedAepsRepo)
        )[UnifiedAepsViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            takeMultiplePermission()
        }
        bindObserver()
        getAndSetData()
        getLocation()
        getBankListData()
        getRDServiceClass()
        init()
        onClickListener()

    }

    private fun init() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            })

//        flagFromDriver = true;
        binding.fingerprint.isEnabled = false
        binding.fingerprint.isClickable = false
        locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (SdkConstants.applicationType.equals("CORE", ignoreCase = true)) {
            SdkConstants.isSl = false
        } else {
            SdkConstants.isSl = true
            binding.terms.visibility = View.GONE
        }
        if (SdkConstants.transactionType.equals(
                SdkConstants.balanceEnquiry,
                ignoreCase = true
            )
        ) {
            binding.amountEnter.isVisible = false
            binding.tvTxnType.text = getString(R.string.requestBalance)
        } else if (SdkConstants.transactionType.equals(
                SdkConstants.miniStatement,
                ignoreCase = true
            )
        ) {
            binding.amountEnter.isVisible = false
            binding.tvTxnType.text = getString(R.string.miniStatement)
        } else {
            binding.amountEnter.isVisible = true
            binding.tvTxnType.text = getString(R.string.cashWithdrawal)
            binding.amountEnter.setText(SdkConstants.transactionAmount)
            binding.amountEnter.isEnabled = false
        }
        if (SdkConstants.FAILEDVALUE.equals("FAILEDDATA", ignoreCase = true)) {
            binding.aadharNumber.setText(makePrettyString(SdkConstants.AADHAAR_NUMBER))
            binding.bankspinner.setText(SdkConstants.BANK_NAME)
            binding.mobileNumber.setText(SdkConstants.MOBILENUMBER)
            bankIINNumber = SdkConstants.bankIIN
            binding.aadharNumber.isEnabled = false
            binding.aadharNumber.setTextColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.grey
                )
            )
            binding.mobileNumber.isEnabled = false
            binding.mobileNumber.clearFocus()
            binding.bankspinner.isEnabled = false
            binding.fingerprint.isEnabled = true
            binding.fingerprint.isClickable = true
            binding.fingerprint.setColorFilter(
                ContextCompat.getColor(requireActivity(), R.color.colorPrimary),
                PorterDuff.Mode.SRC_IN
            )
            SdkConstants.RECEIVE_DRIVER_DATA = ""
            binding.depositBar.visibility = View.GONE
            binding.depositNote.visibility = View.GONE
            binding.submitButton.background =
                ContextCompat.getDrawable(requireActivity(), R.drawable.button_submit)
            gatewayPriority += 1
        }

    }

    private fun onClickListener() {
        binding.aadhaar.setOnClickListener {
            aadhaarNoTransaction()
        }
        binding.virtualID.setOnClickListener {
            vidTransaction()
        }
        binding.bankspinner.setOnClickListener {
            findNavController().navigate(R.id.action_unifiedAepsFragment_to_bankSpinnerFragment)
        }
        binding.aadharNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!mKeyListenerSet) {
                    binding.aadharNumber.setOnKeyListener { _, keyCode, _ ->
                        try {
                            mWannaDeleteHyphen =
                                keyCode == KeyEvent.KEYCODE_DEL && binding.aadharNumber.selectionEnd - binding.aadharNumber.selectionStart <= 1 && binding.aadharNumber.selectionStart > 0 && binding.aadharNumber.text
                                    .toString()[binding.aadharNumber.selectionEnd - 1] == '-'
                        } catch (e: IndexOutOfBoundsException) {
                            // never to happen because of checks
                        }
                        false
                    }
                    mKeyListenerSet = true
                }
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (mInside) // to avoid recursive calls
                    return
                mInside = true
                val currentPos: Int = binding.aadharNumber.selectionStart
                val string: String =
                    binding.aadharNumber.text.toString().uppercase(Locale.getDefault())
                val newString: String = makePrettyString(string)
                binding.aadharNumber.setText(newString)
                try {
                    binding.aadharNumber.setSelection(
                        getCursorPos(
                            string,
                            newString,
                            currentPos,
                            mWannaDeleteHyphen
                        )
                    )
                } catch (e: java.lang.IndexOutOfBoundsException) {
                    binding.aadharNumber.setSelection(binding.aadharNumber.length()) // last resort never to happen
                }
                mWannaDeleteHyphen = false
                mInside = false
            }

            override fun afterTextChanged(s: Editable?) {
                if (s!!.isNotEmpty()) {
                    binding.aadharNumber.error = null
                    var aadhaarNo = binding.aadharNumber.text.toString()
                    if (aadhaarNo.contains("-")) {
                        aadhaarNo = aadhaarNo.replace("-".toRegex(), "").trim { it <= ' ' }
                        balanceInquiryAadhaarNo = aadhaarNo
                        if (balanceInquiryAadhaarNo.length >= 12) {
                            if (!Util.validateAadharNumber(aadhaarNo)) {
                                binding.aadharNumber.error = "Enter Your Correct Aadhar No"
                            } else {
                                binding.fingerprint.isEnabled = true
                                binding.fingerprint.isClickable = true
                                binding.fingerprint.setColorFilter(
                                    ContextCompat.getColor(requireActivity(), R.color.colorPrimary),
                                    PorterDuff.Mode.SRC_IN
                                )
                                binding.mobileNumber.requestFocus()
                            }
                        }

                    }
                }
            }

        })
        binding.aadharNumber.transformationMethod = ChangeTransformationMethod()
        binding.aadharVirtualID.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!mKeyListenerSet) {
                    binding.aadharVirtualID.setOnKeyListener { _, keyCode, _ ->
                        try {
                            mWannaDeleteHyphen =
                                keyCode == KeyEvent.KEYCODE_DEL && binding.aadharVirtualID.selectionEnd - binding.aadharVirtualID.selectionStart <= 1 && binding.aadharVirtualID.selectionStart > 0 && binding.aadharVirtualID.text
                                    .toString()[binding.aadharVirtualID.selectionEnd - 1] == '-'
                        } catch (e: java.lang.IndexOutOfBoundsException) {
                            // never to happen because of checks
                        }
                        false
                    }
                    mKeyListenerSet = true
                }
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (mInside) // to avoid recursive calls
                    return
                mInside = true
                val currentPos: Int = binding.aadharVirtualID.selectionStart
                val string: String =
                    binding.aadharVirtualID.text.toString().uppercase(Locale.getDefault())
                val newString = makePrettyString(string)
                binding.aadharVirtualID.setText(newString)
                try {
                    binding.aadharVirtualID.setSelection(
                        getCursorPos(
                            string,
                            newString,
                            currentPos,
                            mWannaDeleteHyphen
                        )
                    )
                } catch (e: java.lang.IndexOutOfBoundsException) {
                    binding.aadharVirtualID.setSelection(binding.aadharVirtualID.length()) // last resort never to happen
                }
                mWannaDeleteHyphen = false
                mInside = false
            }

            override fun afterTextChanged(s: Editable?) {
                if (s!!.isEmpty()) {
                    binding.aadharVirtualID.error = "Enter Your Virtual ID"
                }
                if (s.isNotEmpty()) {
                    binding.aadharVirtualID.error = null
                    var aadharNo: String = binding.aadharVirtualID.text.toString()
                    if (aadharNo.contains("-")) {
                        aadharNo = aadharNo.replace("-".toRegex(), "").trim { it <= ' ' }
                        balanceInquiryAadhaarNo = aadharNo
                        if (balanceInquiryAadhaarNo.length >= 16) {
                            if (!Util.validateAadharVID(aadharNo)) {
                                binding.aadharVirtualID.error = "Enter Your Correct Virtual ID"
                            } else {
                                binding.aadharVirtualID.clearFocus()
                                binding.mobileNumber.requestFocus()
                            }
                        }
                    }
                }
            }
        })
        binding.mobileNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                var number = ""
                if (s!!.isNotEmpty() && s.length < 9) {
                    binding.mobileNumber.error = null
                    number = s.toString()
                    if (number.startsWith("0")) {
                        binding.mobileNumber.error = resources.getString(R.string.mobilevaliderror)
                    }
                }
                if (s.length == 10) {
                    if (!Util.isValidMobile(binding.mobileNumber.text.toString())) {
                        if (number.startsWith("0")) {
                            binding.mobileNumber.error =
                                resources.getString(R.string.mobilevaliderror)
                        } else {
                            binding.mobileNumber.error =
                                resources.getString(R.string.mobilevaliderror)
                        }
                    } else {
                        binding.mobileNumber.clearFocus()
                    }
                }

            }

        })
        binding.fingerprint.setOnClickListener {
            showLoader(requireActivity())
            try {
                binding.fingerprint.isEnabled = false
                binding.fingerprint.setColorFilter(
                    ContextCompat.getColor(requireActivity(), R.color.colorGrey),
                    PorterDuff.Mode.SRC_IN
                )
                flagFromDriver = true
                if (SdkConstants.MANUFACTURE_FLAG.equals(
                        fmDeviceId,
                        ignoreCase = true
                    ) || SdkConstants.MANUFACTURE_FLAG.contains(fmDeviceId4) || SdkConstants.MANUFACTURE_FLAG.equals(
                        fmDeviceId2,
                        ignoreCase = true
                    ) || SdkConstants.MANUFACTURE_FLAG.equals(fmDeviceId3, ignoreCase = true)
                ) {
                    SdkConstants.aadharNumberValue = binding.aadharNumber.text.toString()
                    SdkConstants.mobileNumberValue = binding.mobileNumber.text.toString()
                    SdkConstants.bankValue = binding.bankspinner.text.toString()
                    bankIINNumber = SdkConstants.bankIIN
                    if (binding.mobileNumber.text.toString().isEmpty() || binding.bankspinner.text
                            .toString().isEmpty()
                    ) {
                        Toast.makeText(
                            requireActivity(),
                            "Fill up details",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        binding.fingerprint.isEnabled = false
                        binding.aadharNumber.isEnabled = false
                        binding.aadharNumber.setTextColor(
                            ContextCompat.getColor(
                                requireActivity(),
                                R.color.grey
                            )
                        )
                        binding.mobileNumber.isEnabled = false
                        binding.mobileNumber.clearFocus()
                        binding.bankspinner.isEnabled = false
                        SdkConstants.OnBackpressedValue = false
                        binding.fingerprint.setColorFilter(R.color.colorGrey)
                        if (SdkConstants.onDriverDataListener != null) {
                            SdkConstants.onDriverDataListener!!.onFingerClick(
                                balanceInquiryAadhaarNo,
                                binding.mobileNumber.text.toString(),
                                binding.bankspinner.text.toString(),
                                flagNameRdService,
                                this@UnifiedAepsFragment
                            )
                            requireActivity().finish()
                        }
                    }
                } else {
                    val launchIntent = Intent(requireActivity(), driverActivity)
                    launchIntent.putExtra("driverFlag", flagNameRdService)
                    launchIntent.putExtra("AadharNo", balanceInquiryAadhaarNo)
                    startActivityForResult(launchIntent,50)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        binding.bankspinner.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0!!.isEmpty()) {
                    binding.bankspinner.error = resources.getString(R.string.select_bank_error)
                    binding.mobileNumber.clearFocus()
                }
                if (p0.isNotEmpty()) {
                    binding.bankspinner.error = null
                }
            }
        })
        binding.terms.setOnCheckedChangeListener { _, _ ->
            SdkConstants.firstCheck = true
            showTermsDetails(requireActivity())
        }
        binding.submitButton.setOnClickListener {

            var balanceaadharNo: String
            var balanceaadharVid: String
            balanceaadharNo = binding.aadharNumber.text.toString()
            if (aadhaarBool) {
                if (balanceaadharNo.contains("-")) {
                    balanceaadharNo = balanceaadharNo.replace("-".toRegex(), "").trim { it <= ' ' }
                }
                if (!Util.validateAadharNumber(balanceaadharNo)) {
                    binding.aadharVirtualID.error = resources.getString(R.string.valid_aadhar_error)
                    return@setOnClickListener
                }
            } else if (virtualBool) {
                balanceaadharVid = binding.aadharVirtualID.text.toString().trim { it <= ' ' }
                if (balanceaadharVid.contains("-")) {
                    balanceaadharVid =
                        balanceaadharVid.replace("-".toRegex(), "").trim { it <= ' ' }
                }
                if (!Util.validateAadharVID(balanceaadharVid)) {
                    binding.aadharVirtualID.error = resources.getString(R.string.valid_aadhar_error)
                    return@setOnClickListener
                }
            }
            if (!flagFromDriver) {
                Toast.makeText(
                    requireActivity(),
                    "Please do Biometric Verification",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            } else {
                try {
                    val respObj = JSONObject(SdkConstants.RECEIVE_DRIVER_DATA)
                    val scoreStr = respObj.getString("pidata_qscore")
                    if (scoreStr.toFloat() <= 40) {
                        showAlert("Bad Fingerprint Strength, Please try Again !", requireActivity())
                        return@setOnClickListener
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            if (binding.mobileNumber.text == null || !Util.isValidMobile(
                    binding.mobileNumber.text.toString().trim { it <= ' ' })
            ) {
                binding.mobileNumber.error = resources.getString(R.string.mobileerror)
                return@setOnClickListener
            }
            SdkConstants.AADHAAR_NUMBER = binding.aadharNumber.text.toString().trim { it <= ' ' }
            aadharNumberMain = SdkConstants.AADHAAR_NUMBER
            if (!Util.validateAadharNumber(balanceInquiryAadhaarNo)) {
                binding.aadharNumber.error = resources.getString(R.string.Validaadhaarerror)
                return@setOnClickListener
            }
            SdkConstants.MOBILENUMBER = binding.mobileNumber.text.toString().trim { it <= ' ' }
            val panAadhaar: String = binding.mobileNumber.text.toString().trim { it <= ' ' }

            if (panAadhaar.contains(" ") && panAadhaar.length != 10) {
                binding.mobileNumber.error = resources.getString(R.string.mobileerror)
                return@setOnClickListener
            }
            if (binding.bankspinner.text == null) {
                binding.bankspinner.error = resources.getString(R.string.select_bank_error)
                return@setOnClickListener
            }
            SdkConstants.BANK_NAME = binding.bankspinner.text.toString().trim { it <= ' ' }
            if (SdkConstants.firstCheck) {
                showLoader(requireActivity())
                try {
                    if (binding.tvTxnType.text.toString() == transactionTypeCW) {
                        if (SdkConstants.applicationType.equals("CORE", ignoreCase = true)) {
                            unifiedAepsViewModel.getCahWithdrawalEncodedUrl()
                        } else {
                            encodedUrl = "$baseurlSdkUser/api/cashWithdrawal"
                        }
                    } else if (binding.tvTxnType.text.toString() == transactionTypeBE) {
                        if (SdkConstants.applicationType.equals("CORE", ignoreCase = true)) {
                            unifiedAepsViewModel.getBalanceEnqEncodedUrl()
                        } else {
                            encodedUrl = "$baseurlSdkUser/api/balanceEnquiry"
                        }
                    } else {
                        if (SdkConstants.applicationType.equals("CORE", ignoreCase = true)) {
                            unifiedAepsViewModel.getMiniStatementEncodedUrl()
                        } else {
                            encodedUrl = "$baseurlSdkUser/api/miniStatement"
                        }
                    }
                    val respObj = JSONObject(SdkConstants.RECEIVE_DRIVER_DATA)
                    var pidData = respObj.getString(
                        "base64pidData"
                    )
                    pidData =
                        if (SdkConstants.internalFPName.equals("wiseasy", ignoreCase = true)) {
                            pidData.replace("\\n+".toRegex(), "").trim()
                        } else {
                            pidData.replace("\\R+".toRegex(), "").trim()
                        }
                    val userType: String =
                        if (!SdkConstants.applicationType.equals(
                                "CORE",
                                ignoreCase = true
                            )
                        ) {
                            "APIUSER"
                        } else {
                            "ANDROIDUSER"
                        }
                    val transactionRequest = TransactionRequest(
                        aadharNo = balanceInquiryAadhaarNo,
                        amount = binding.amountEnter.text.toString(),
                        apiUser = userType,
                        apiUserName = SdkConstants.API_USER_NAME_VALUE,
                        bankName = binding.bankspinner.text.toString(),
                        gatewayPriority = gatewayPriority,
                        iin = bankIINNumber,
                        latLong = lastLatlong!!,
                        mobileNumber = binding.mobileNumber.text.toString(),
                        paramA = SdkConstants.paramA,
                        paramB = SdkConstants.paramB,
                        paramC = SdkConstants.paramC,
                        pidData = pidData,
                        retailer = SdkConstants.userNameFromCoreApp
                    )
                    if (SdkConstants.applicationType.equals("CORE", ignoreCase = true)) {
                        Handler(Looper.myLooper()!!).postDelayed({
                            encryptBalanceEnquiry(
                                encodedUrl,
                                transactionRequest,
                                SdkConstants.tokenFromCoreApp
                            )
                        }, 1000)

                    } else {
                        encryptBalanceEnquiry(
                            encodedUrl,
                            transactionRequest,
                            "Bearer ${SdkConstants.tokenFromCoreApp}"
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun bindObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                unifiedAepsViewModel.getEncodedUrlLiveData.collectLatest {
//                hideLoader()
                    when (it) {
                        is NetworkResults.Success -> {
                            val responseUrl = it.data?.hello
                            val url = Base64.decode(responseUrl, Base64.DEFAULT)
                            encodedUrl = String(url)
                        }
                        is NetworkResults.Loading -> {
                            showLoader(requireActivity())
                        }
                        is NetworkResults.Error -> {
                            Toast.makeText(requireActivity(), it.message, Toast.LENGTH_LONG)
                                .show()
                            Log.i("urlerror", it.message.toString())
                        }
                    }

                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                unifiedAepsViewModel.getTransactionStatusLiveData.collectLatest { networkResults ->
                    hideLoader()
                    when (networkResults) {
                        is NetworkResults.Success -> {
                            hideLoader()
                            networkResults.data?.let {
                                if (!SdkConstants.skipReceiptPart) {
                                    if (findNavController().currentDestination?.id == R.id.unifiedAepsFragment) {
                                        val bundle = Bundle()
                                        val gson = Gson()
                                        val txnData = gson.toJson(it)
                                        bundle.putString("txnData", txnData)
                                        val transactionMode = it.transactionMode
                                        val status = it.status
                                        if (status.equals("SUCCESS", ignoreCase = true)) {
                                            if (transactionMode.equals(
                                                    "AEPS_MINI_STATEMENT",
                                                    ignoreCase = true
                                                )
                                            ) {
                                                findNavController().navigate(
                                                    R.id.action_unifiedAepsFragment_to_unifiedAepsMiniStatementFragment,
                                                    bundle
                                                )
                                            } else {
                                                findNavController().navigate(
                                                    R.id.action_unifiedAepsFragment_to_unifiedAepsTransactionStatusFragment,
                                                    bundle
                                                )
                                            }
                                        } else {
                                            findNavController().navigate(
                                                R.id.action_unifiedAepsFragment_to_unifiedAepsTransactionStatusFragment,
                                                bundle
                                            )
                                        }
                                    }
                                } else {
                                    val gson = Gson()
                                    val getData: String = gson.toJson(it)
                                    SdkConstants.onFinishListener!!.onSdkFinish(it.status,SdkConstants.paramA,it.balance,getData)
                                    requireActivity().finish()
                                }
                            }
                        }
                        is NetworkResults.Loading -> {
                            showLoader(requireActivity())
                        }
                        is NetworkResults.Error -> {
                            Toast.makeText(
                                requireActivity(),
                                networkResults.message,
                                Toast.LENGTH_LONG
                            )
                                .show()
                        }
                    }
                }
            }
        }
    }


    private fun encryptBalanceEnquiry(
        encodedUrl: String,
        transactionRequest: TransactionRequest,
        tokenFromCoreApp: String,
    ) {
        unifiedAepsViewModel.getTransactionStatus(tokenFromCoreApp, encodedUrl, transactionRequest)
    }

    private fun getBankListData() {
        setFragmentResultListener("requestKey") { _, bundle ->
            val result = bundle.getString("bankData")
            val `object` = JSONObject(result!!)
            binding.bankspinner.setText(`object`.getString("bankName"))
            bankIINNumber = `object`.getString("iin")
            bankName = `object`.getString("bankName")
            SdkConstants.bankIIN = bankIINNumber
            SdkConstants.BANK_NAME = bankName
            checkValidation()
        }
    }


    private fun
            aadhaarNoTransaction() {
        binding.aadhaar.setImageResource(R.drawable.ic_fingerprint_blue)
        binding.virtualID.isEnabled = true
        binding.aadhaar.isEnabled = false
        binding.aadharNumber.isVisible = true
        binding.aadharVirtualID.isVisible = false
        virtualBool = false
        aadhaarBool = true
        binding.aadharText.setTextColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.colorPrimary
            )
        )
        binding.virtualID.setImageResource(R.drawable.ic_language)
        binding.virtualidText.setTextColor(ContextCompat.getColor(requireActivity(), R.color.grey))
    }

    private fun vidTransaction() {
        binding.virtualID.setImageResource(R.drawable.ic_language_blue)
        binding.virtualID.isEnabled = false
        binding.aadhaar.isEnabled = true
        binding.aadharNumber.isVisible = false
        binding.aadharVirtualID.isVisible = true
        virtualBool = true
        aadhaarBool = false
        binding.virtualidText.setTextColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.colorPrimary
            )
        )
        binding.aadhaar.setImageResource(R.drawable.ic_fingerprint_grey)
        binding.aadharText.setTextColor(ContextCompat.getColor(requireActivity(), R.color.grey))
    }


    private fun getCursorPos(
        oldString: String,
        newString: String,
        oldPos: Int,
        isDeleteHyphen: Boolean
    ): Int {
        val cursorPos = newString.length
        if (oldPos != oldString.length) {
            val stringWithMarker =
                oldString.substring(0, oldPos) + marker + oldString.substring(oldPos)
            var cursorPos1 = (makePrettyString(stringWithMarker)).indexOf(marker)
            if (isDeleteHyphen)
                cursorPos1 -= 1
        }
        return cursorPos

    }

    private fun makePrettyString(string: String): String {
        val number = string.replace("-".toRegex(), "")
        val isEndHyphen = string.endsWith("-") && number.length % 4 == 0
        return number.replace("(.{4}(?!$))".toRegex(), "$1-") + if (isEndHyphen) "-" else ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        if (aadhaarBool) {
            aadhaarNoTransaction()
        } else {
            vidTransaction()
        }
        if (flagFromDriver) {
            if (SdkConstants.RECEIVE_DRIVER_DATA.isEmpty() || SdkConstants.RECEIVE_DRIVER_DATA == ""
            ) {
                binding.fingerprint.setColorFilter(
                    ContextCompat.getColor(requireActivity(), R.color.colorPrimary),
                    PorterDuff.Mode.SRC_IN
                )
                binding.fingerprint.isEnabled = true
                binding.fingerprint.isClickable = true
                binding.submitButton.setBackgroundResource(R.drawable.button_submit)
                binding.submitButton.isEnabled = false
            } else if (balanceInquiryAadhaarNo.equals(
                    "",
                    ignoreCase = true
              ) || balanceInquiryAadhaarNo.isEmpty()
            ) {
                binding.aadharNumber.error = "Enter Aadhar No."
                fingerStrength()
            } else if (binding.mobileNumber.text.toString().isEmpty() || binding.mobileNumber.text
                    .toString().equals("", ignoreCase = true)
            ) {
                binding.mobileNumber.error = "Enter mobile no."
                fingerStrength()
            } else if (binding.bankspinner.text.toString().isEmpty() || binding.bankspinner.text
                    .toString().trim { it <= ' ' }
                    .equals("", ignoreCase = true)
            ) {
                binding.bankspinner.error = "Choose your bank."
                fingerStrength()
            } else {
                fingerStrength()
                //fingerprint.setImageDrawable(getResources().getDrawable(R.drawable.F));
                binding.fingerprint.setColorFilter(
                    ContextCompat.getColor(requireActivity(), R.color.colorGrey),
                    PorterDuff.Mode.SRC_IN
                )
                binding.fingerprint.isEnabled = false
            }
        }
        /*if (requireArguments().getString("FAILEDVALUE") != null && requireArguments().getString("FAILEDVALUE")
                .equals("FAILEDDATA", ignoreCase = true)
        ) {
            binding.aadharNumber.setText(makePrettyString(SdkConstants.AADHAAR_NUMBER))
            binding.bankspinner.setText(SdkConstants.BANK_NAME)
            binding.mobileNumber.setText(SdkConstants.MOBILENUMBER)
            bankIINNumber = SdkConstants.bankIIN
            binding.aadharNumber.isEnabled = false
            binding.aadharNumber.setTextColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.grey
                )
            )
            binding.mobileNumber.isEnabled = false
            binding.mobileNumber.clearFocus()
            binding.bankspinner.isEnabled = false
            binding.fingerprint.isEnabled = true
            binding.fingerprint.isClickable = true
            binding.fingerprint.setColorFilter(
                ContextCompat.getColor(requireActivity(), R.color.colorPrimary),
                PorterDuff.Mode.SRC_IN
            )
            SdkConstants.RECEIVE_DRIVER_DATA = ""
            binding.depositBar.visibility = View.GONE
            binding.depositNote.visibility = View.GONE
            binding.submitButton.background =
                ContextCompat.getDrawable(requireActivity(), R.drawable.button_submit)
            gatewayPriority += 1
        }*/


    }

    private fun fingerStrength() {
        try {
            val respObj = JSONObject(SdkConstants.RECEIVE_DRIVER_DATA)
            val scoreStr = respObj.getString("pidata_qscore")
            if (scoreStr.contains(",")) {
                hideLoader()
                showAlert("Invalid Fingerprint Data", requireActivity())
            } else {
                binding.submitButton.isEnabled = true
                binding.submitButton.setBackgroundResource(R.drawable.button_submit_blue)
                if (scoreStr.toFloat() <= 40) {
                    binding.depositBar.visibility = View.VISIBLE
                    binding.depositBar.progress = scoreStr.toFloat()
                    binding.depositBar.setProgressTextMoved(true)
                    binding.depositBar.setEndColor(
                        ContextCompat.getColor(
                            requireActivity(),
                            R.color.red
                        )
                    )
                    binding.depositBar.setStartColor(
                        ContextCompat.getColor(
                            requireActivity(),
                            R.color.red
                        )
                    )
                    binding.depositNote.visibility = View.VISIBLE
                    binding.fingerprintStrengthDeposit.visibility = View.VISIBLE
                } else {
                    binding.depositBar.visibility = View.VISIBLE
                    binding.depositBar.progress = scoreStr.toFloat()
                    binding.depositBar.setProgressTextMoved(true)
                    binding.depositBar.setEndColor(
                        ContextCompat.getColor(
                            requireActivity(),
                            R.color.green
                        )
                    )
                    binding.depositBar.setStartColor(
                        ContextCompat.getColor(
                            requireActivity(),
                            R.color.green
                        )
                    )
                    binding.depositNote.visibility = View.VISIBLE
                    binding.fingerprintStrengthDeposit.visibility = View.VISIBLE
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showAlert("Invalid Fingerprint Data", requireActivity())
        }
    }


    private fun checkValidation() {
        if (SdkConstants.transactionType.equals(SdkConstants.cashWithdrawal, ignoreCase = true)) {
            if (binding.amountEnter.text != null) {
                inputValidation()
            }
        } else {
            inputValidation()
        }
    }

    private fun inputValidation() {
        if (binding.mobileNumber.text != null && Util.isValidMobile(
                binding.mobileNumber.text.toString()
                    .trim { it <= ' ' }) && binding.bankspinner.text != null
        ) {
            if (aadhaarBool) {
                var aadharNo: String = binding.aadharNumber.text.toString()
                if (aadharNo.contains("-")) {
                    aadharNo = aadharNo.replace("-".toRegex(), "").trim { it <= ' ' }
                }
            } else if (virtualBool) {
                var aadharVid: String = binding.aadharVirtualID.text.toString()
                if (aadharVid.contains("-")) {
                    aadharVid = aadharVid.replace("-".toRegex(), "").trim { it <= ' ' }
                }
            }
        } else {
            binding.submitButton.isEnabled = false
            binding.submitButton.background =
                ContextCompat.getDrawable(requireActivity(), R.drawable.button_submit)
        }
    }

    private fun getRDServiceClass() {
        val accessClassName: String =
            SdkConstants.DRIVER_ACTIVITY //getIntent().getStringExtra("activity");
        flagNameRdService =
            SdkConstants.MANUFACTURE_FLAG //getIntent().getStringExtra("driverFlag");
        try {
            val targetActivity = Class.forName(accessClassName).asSubclass(
                Activity::class.java
            )
            driverActivity = targetActivity
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }


    }

    override fun onFingerClick(
        aadharNo: String?,
        mobileNumber: String?,
        bankName: String?,
        driverFlag: String?,
        listener: OnDriverDataListener?
    ) {
        TODO("Not yet implemented")
    }

    private fun getLocation() {
        gpsTracker = GpsTracker(requireActivity())
        if (gpsTracker!!.canGetLocation()) {
            latitude = gpsTracker!!.getLatitude()
            longitude = gpsTracker!!.getLongitude()
            latLong = latitude.toString() + "," + longitude
        } else {
            gpsTracker!!.showSettingsAlert()
        }
        saveLocation()
    }

    private fun saveLocation() {
        val sharedPreferences: SharedPreferences =
            requireActivity().getSharedPreferences(sharedPrefs, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        if (latLong.isNotEmpty()) {
            lastLatlong = latLong
        }
        editor.putString(lastLatlong, latLong)
        editor.apply()
    }

    @RequiresApi(Build.VERSION_CODES.S)
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
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    requireActivity(),
                    ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    requireActivity(),
                    ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                registerActivity.launch(
                    arrayOf(
                        READ_EXTERNAL_STORAGE,
                        BLUETOOTH_CONNECT,
                        BLUETOOTH,
                        BLUETOOTH_SCAN,
                        BLUETOOTH_CONNECT,
                        ACCESS_FINE_LOCATION,
                        ACCESS_COARSE_LOCATION
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
                    BLUETOOTH_CONNECT,
                    ACCESS_FINE_LOCATION,
                    ACCESS_COARSE_LOCATION
                )
            )

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 50) {
            hideLoader()
        }
    }

}