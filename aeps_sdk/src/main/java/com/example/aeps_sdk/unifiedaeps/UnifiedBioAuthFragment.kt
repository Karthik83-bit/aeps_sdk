package com.example.aeps_sdk.unifiedaeps

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.aeps_sdk.BaseFragment
import com.example.aeps_sdk.R
import com.example.aeps_sdk.application.AppController
import com.example.aeps_sdk.utils.Util
import com.example.aeps_sdk.databinding.FragmentUnifiedBioAuthBinding
import com.example.aeps_sdk.unifiedaeps.models.req.PinRequest
import com.example.aeps_sdk.unifiedaeps.models.req.BioAuthSubmitRequest
import com.example.aeps_sdk.unifiedaeps.models.req.EncodedUrlRequest
import com.example.aeps_sdk.unifiedaeps.models.req.SetAddressRequest
import com.example.aeps_sdk.utils.NetworkResults
import com.example.aeps_sdk.utils.SdkConstants
import com.example.aeps_sdk.unifiedaeps.viewmodel.BioAuthViewModel
import com.example.aeps_sdk.unifiedaeps.viewmodel.BioAuthViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class UnifiedBioAuthFragment : BaseFragment(){
    private var _binding: FragmentUnifiedBioAuthBinding? = null
    private val binding get() = _binding!!
    private var driverActivity: Class<*>? = null
    private var flagNameRdService = ""
    private var balanceInquiryAadhaarNo: String? = null
    private var mKeyListenerSet = false
    private var mWannaDeleteHyphen = false
    private var mInside = false
    private val marker: String = "|" // filtered in layout not to be in the string
    private val REQUEST_CAMERA_PERMISSIONS = 931
    private var mylocation: Location? = null
    private var location_flag = false
    private var postalCode = "751012"
    private var userNameStr = ""
    private var isSL = false
    private lateinit var encodedUrl: String
    private lateinit var setAddressRequest: SetAddressRequest
    private var tokenWithBearer: String? = null
    private var pincode = 751017
    private var flagFromDriver = false
    private lateinit var bioAuthSubmitRequest: BioAuthSubmitRequest
    private lateinit var bioAuthViewModel: BioAuthViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUnifiedBioAuthBinding.inflate(inflater, container, false)
        val bioAuthRepo = (requireActivity().application as AppController).bioAuthRepository
        bioAuthViewModel = ViewModelProvider(
            requireActivity(),
            BioAuthViewModelFactory(bioAuthRepo)
        )[BioAuthViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showLoader(requireActivity())
        getAndSetData()
        init()
        bindObserver()
        onClickListener()
    }

    private fun init() {
        SdkConstants.RECEIVE_DRIVER_DATA = ""
        binding.conLayout.isVisible = false
        if (SdkConstants.applicationType.equals("CORE", ignoreCase = true)) {
            userNameStr = SdkConstants.applicationUserName
            checkAddressStatus()
            isSL = false
        } else {
            isSL = true
            checkAddressStatus()
        }
        getRDServiceClass()
        binding.twoFactFingerprint.isEnabled = false
        binding.twoFactFingerprint.isClickable = false
        binding.depositBar.isVisible = false
    }

    private fun onClickListener() {
        binding.balanceAadharNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!mKeyListenerSet) {
                    binding.balanceAadharNumber.setOnKeyListener { _, keyCode, _ ->
                        try {
                            mWannaDeleteHyphen =
                                keyCode == KeyEvent.KEYCODE_DEL && binding.balanceAadharNumber.selectionEnd - binding.balanceAadharNumber.selectionStart <= 1 && binding.balanceAadharNumber.selectionStart > 0 && binding.balanceAadharNumber.text.toString()[binding.balanceAadharNumber.selectionEnd - 1] == '-'
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
                val currentPos: Int = binding.balanceAadharNumber.selectionStart
                val string: String =
                    binding.balanceAadharNumber.text.toString().uppercase(Locale.getDefault())
                val newString: String = makePrettyString(string)
                binding.balanceAadharNumber.setText(newString)
                try {
                    binding.balanceAadharNumber.setSelection(
                        getCursorPos(
                            string, newString, currentPos, mWannaDeleteHyphen
                        )
                    )
                } catch (e: java.lang.IndexOutOfBoundsException) {
                    binding.balanceAadharNumber.setSelection(binding.balanceAadharNumber.length()) // last resort never to happen
                }
                mWannaDeleteHyphen = false
                mInside = false
            }

            @RequiresApi(Build.VERSION_CODES.M)
            override fun afterTextChanged(s: Editable?) {
                if (s!!.isEmpty()) {
                    binding.balanceAadharNumber.error = resources.getString(R.string.aadhaarnumber)
                }
                if (s.isNotEmpty()) {
                    binding.balanceAadharNumber.error = null
                    var aadhaarNo = binding.balanceAadharNumber.text.toString()
                    if (aadhaarNo.contains("-")) {
                        aadhaarNo = aadhaarNo.replace("-".toRegex(), "").trim { it <= ' ' }
                        balanceInquiryAadhaarNo = aadhaarNo
                        if (balanceInquiryAadhaarNo!!.length >= 12 && Util.validateAadharNumber(
                                balanceInquiryAadhaarNo!!
                            )
                        ) {
                            binding.twoFactFingerprint.isEnabled = true
                            binding.twoFactFingerprint.isClickable = true
                            binding.twoFactFingerprint.setColorFilter(
                                ContextCompat.getColor(requireActivity(), R.color.buttonSolidColor),
                                PorterDuff.Mode.SRC_IN
                            )
                        } else {
                            binding.balanceAadharNumber.error =
                                resources.getString(R.string.Validaadhaarerror)
                            binding.twoFactFingerprint.isEnabled = false
                            binding.twoFactFingerprint.isClickable = false
                            binding.twoFactFingerprint.setColorFilter(requireActivity().getColor(R.color.grey),PorterDuff.Mode.SRC_IN)

                        }

                    } else {
                        balanceInquiryAadhaarNo = aadhaarNo
                        if (!Util.validateAadharNumber(aadhaarNo)) {
                            binding.balanceAadharNumber.error =
                                resources.getString(R.string.Validaadhaarerror)
                            binding.twoFactFingerprint.isEnabled = false
                            binding.twoFactFingerprint.isClickable = false
                            binding.twoFactFingerprint.setColorFilter(requireActivity().getColor(R.color.grey),PorterDuff.Mode.SRC_IN)

                        } else {
                            Log.d("TAG", "afterTextChanged: ${flagFromDriver}")
                            binding.twoFactFingerprint.isEnabled = true
                            binding.twoFactFingerprint.isClickable = true
                            binding.twoFactFingerprint.setColorFilter(
                                ContextCompat.getColor(requireActivity(), R.color.colorPrimary),
                                PorterDuff.Mode.SRC_IN
                            )
                        }
                    }
                }
            }

        })
        binding.twoFactFingerprint.setOnClickListener {
            showLoader(requireActivity())
            if (balanceInquiryAadhaarNo!!.isNotEmpty() && Util.validateAadharNumber(
                    balanceInquiryAadhaarNo!!
                )
            ) {
                flagFromDriver = true
                val launchIntent = Intent(requireActivity(), driverActivity)
                launchIntent.putExtra("driverFlag", flagNameRdService)
                launchIntent.putExtra("AadharNo", balanceInquiryAadhaarNo)
                startActivityForResult(launchIntent, 50)
            } else {
                hideLoader()
                Toast.makeText(requireActivity(), R.string.Validaadhaarerror, Toast.LENGTH_SHORT)
                    .show()
            }
        }
        binding.twoFactSubmitButton.setOnClickListener {
            if (binding.balanceAadharNumber.text.toString() == "") {
                Toast.makeText(
                    requireActivity(), R.string.aadhaarnumber, Toast.LENGTH_SHORT
                ).show()
            } else {
                if (!Util.validateAadharNumber(balanceInquiryAadhaarNo!!)) {
                    Toast.makeText(
                        requireActivity(), R.string.Validaadhaarerror, Toast.LENGTH_SHORT
                    ).show()
                } else {
                    if (!flagFromDriver) {
                        Toast.makeText(
                            requireActivity(), "Please do Biometric Verification", Toast.LENGTH_LONG
                        ).show()
                        return@setOnClickListener
                    } else {
                        try {
                            val respObj = JSONObject(SdkConstants.RECEIVE_DRIVER_DATA)
                            val scoreStr = respObj.getString("pidata_qscore")
                            if (scoreStr.toFloat() <= 40) {
                                Toast.makeText(
                                    requireActivity(),
                                    "Bad Fingerprint Strength, Please try Again !",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@setOnClickListener
                            } else {
                                try {
//                                        JSONObject respObj = new JSONObject(SdkConstants.RECEIVE_DRIVER_DATA);
                                    val CI = respObj.getString("CI")
                                    val DC = respObj.getString("DC")
                                    val DPID = respObj.getString("DPID")
                                    val DATAVALUE = respObj.getString("DATAVALUE")
                                    val HMAC = respObj.getString("HMAC")
                                    val MI = respObj.getString("MI")
                                    val MC = respObj.getString("MC")
                                    val RDSID = respObj.getString("RDSID")
                                    val RDSVER = respObj.getString("RDSVER")
                                    val value = respObj.getString("value")
                                    bioAuthSubmitRequest = BioAuthSubmitRequest(
                                        aadharNo = balanceInquiryAadhaarNo!!,
                                        apiUserName = SdkConstants.API_USER_NAME_VALUE,
                                        ci = CI,
                                        dc = DC,
                                        dpId = DPID,
                                        encryptedPID = DATAVALUE,
                                        hMac = HMAC,
                                        isSL = isSL,
                                        mcData = MC,
                                        mi = MI,
                                        operation = "",
                                        rdsId = RDSID,
                                        rdsVer = RDSVER,
                                        retailer = SdkConstants.userNameFromCoreApp,
                                        sKey = value
                                    )
                                    getEncodedUrlForSubmitBioAuth()
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    private fun getEncodedUrlForSubmitBioAuth() {
        bioAuthViewModel.getEncodedUrlForSubmitBioAuth()
    }

    private fun submitBioAuth(encodedUrl: String, bioAuthSubmitRequest: BioAuthSubmitRequest) {
        tokenWithBearer = if (SdkConstants.applicationType == "CORE") {
            SdkConstants.tokenFromCoreApp
        } else {
            "Bearer " + SdkConstants.tokenFromCoreApp
        }
        bioAuthViewModel.submitBioAuth(tokenWithBearer!!, encodedUrl, bioAuthSubmitRequest)
    }

    private fun updateUserPropAddress(encodedUrl: String, addressRequest: SetAddressRequest) {
        tokenWithBearer = if (SdkConstants.applicationType == "CORE") {
            SdkConstants.tokenFromCoreApp
        } else {
            "Bearer " + SdkConstants.tokenFromCoreApp
        }
        bioAuthViewModel.updateUserPropAddress(tokenWithBearer!!, encodedUrl, addressRequest)
    }

    private fun setAddress() {
        val encodedUrlRequest = EncodedUrlRequest(userNameStr)
        bioAuthViewModel.getEncodedUrlForSetAddress(encodedUrlRequest)
    }

    private fun getAddressFromPin(pinRequest: PinRequest) {
        bioAuthViewModel.getAddressFromPin(pinRequest)
    }

    private fun checkAddressStatus() {
        try {
            bioAuthViewModel.getEncodedUrlForAddressStatus()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun viewUserPropAddress(token: String, url: String) {
        bioAuthViewModel.viewUserPropAddress(token, url)
    }


    private fun bindObserver() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                bioAuthViewModel.getEncodedUrlLiveData.collectLatest {
                    hideLoader()
                    when (it) {
                        is NetworkResults.Success -> {
                            if (SdkConstants.applicationType == "CORE") {
                                val responseUrl = it.data?.hello
                                val url = Base64.decode(responseUrl, Base64.DEFAULT)
                                encodedUrl = String(url)
                                viewUserPropAddress(SdkConstants.tokenFromCoreApp, encodedUrl)
                            } else {
                                encodedUrl =
                                    "https://aeps-prod-gateway-as1-5pwajhaz.ts.gateway.dev" + "/api/viewUserPropAddress/" + SdkConstants.userNameFromCoreApp
                                viewUserPropAddress(
                                    "Bearer " + SdkConstants.tokenFromCoreApp, encodedUrl
                                )
                            }
                        }
                        is NetworkResults.Loading -> {
                            showLoader(requireActivity())
                        }
                        is NetworkResults.Error -> {
                            showAlertBioAuth(it.message, requireContext())
                        }
                    }

                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                bioAuthViewModel.viewUserPropAddressLiveData.collectLatest {
                    hideLoader()
                    when (it) {
                        is NetworkResults.Success -> {
                            var bioAuthStatus: Boolean = it.data!!.response.bioauth
                            bioAuthStatus=true
                            if (!bioAuthStatus) {
                                if (postalCode.isNotEmpty()) {
                                    pincode = Integer.valueOf(postalCode)
                                }
                                val pinRequest = PinRequest(pincode)
                                getAddressFromPin(pinRequest)
                            } else {
                                binding.conLayout.isVisible = false
                                findNavController().navigate(R.id.action_unifiedBioAuthFragment_to_unifiedAepsFragment)
                            }
                        }
                        is NetworkResults.Loading -> {
                            showLoader(requireActivity())
                        }
                        is NetworkResults.Error -> {
                            showAlertBioAuth(it.message, requireContext())

                        }
                        else -> {}
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                bioAuthViewModel.getAddressFromPinLiveData.collectLatest { networkResults ->
                    hideLoader()
                    when (networkResults) {
                        is NetworkResults.Success -> {
                            networkResults.data?.let {
                                if (it.data.status.equals("success", ignoreCase = true)) {
                                    val pinCode = it.data.data.pincode.toString()
                                    val state = it.data.data.state
                                    val shortState = Util.getShortState(state).toString()
                                    val city = it.data.data.city
                                    val lat = "0.0"
                                    val lng = "0.0"
                                    setAddressRequest = SetAddressRequest(
                                        apiUserName = SdkConstants.API_USER_NAME_VALUE,
                                        city = city,
                                        latLong = "$lat,$lng",
                                        pincode = pinCode,
                                        state = shortState
                                    )
                                    setAddress()
                                } else {
                                    hideLoader()
                                    showAlertBioAuth(
                                        "Invalid area pin, please try after sometimes",
                                        requireActivity()
                                    )
                                }
                            }


                        }
                        is NetworkResults.Loading -> {
                            showLoader(requireActivity())
                        }
                        is NetworkResults.Error -> {
                            showAlertBioAuth(networkResults.message, requireContext())
                        }
                    }

                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                bioAuthViewModel.getEncodedUrlLiveDataSetAddress.collectLatest { networkResults ->
                    hideLoader()
                    when (networkResults) {
                        is NetworkResults.Success -> {
                            networkResults.data?.let {
                                val responseUrl = it.hello
                                val url = Base64.decode(responseUrl, Base64.DEFAULT)
                                if (SdkConstants.applicationType.equals(
                                        "CORE", ignoreCase = true
                                    )
                                ) {
                                    encodedUrl = String(url)
                                    updateUserPropAddress(encodedUrl, setAddressRequest)
                                } else {
                                    encodedUrl =
                                        "https://aeps-prod-gateway-as1-5pwajhaz.ts.gateway.dev" + "/api/updateUserPropAddress/" + SdkConstants.userNameFromCoreApp
                                    updateUserPropAddress(encodedUrl, setAddressRequest)
                                }
                            }
                        }
                        is NetworkResults.Loading -> {
                            showLoader(requireActivity())
                        }
                        is NetworkResults.Error -> {
                            showAlertBioAuth(networkResults.message, requireContext())
                        }
                    }

                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                bioAuthViewModel.updateUserPropAddressLiveData.collectLatest { networkResults ->
                    hideLoader()
                    when (networkResults) {
                        is NetworkResults.Success -> {
                            networkResults.data?.let {
                                val status = it.status
                                val statusDesc = it.statusDesc
                                if (status == "0") {
                                    binding.conLayout.isVisible = true
                                } else {
                                    showAlertBioAuth(statusDesc, requireActivity())
                                }
                            }
                        }
                        is NetworkResults.Loading -> {
                            showLoader(requireActivity())
                        }
                        is NetworkResults.Error -> {
                            showAlertBioAuth(networkResults.message, requireContext())

                        }
                        else -> {}
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                bioAuthViewModel.getEncodedUrlLiveDataSubmitBioAuth.collectLatest { networkResults ->
                    hideLoader()
                    when (networkResults) {
                        is NetworkResults.Success -> {
                            networkResults.data?.let {
                                val responseUrl = it.hello
                                val url = Base64.decode(responseUrl, Base64.DEFAULT)
                                if (SdkConstants.applicationType.equals(
                                        "CORE", ignoreCase = true
                                    )
                                ) {
                                    encodedUrl = String(url)
                                    submitBioAuth(encodedUrl, bioAuthSubmitRequest)
                                } else {
                                    encodedUrl =
                                        "https://aeps-prod-gateway-as1-5pwajhaz.ts.gateway.dev" + "/api/bioAuth"
                                    submitBioAuth(encodedUrl, bioAuthSubmitRequest)
                                }
                            }
                        }
                        is NetworkResults.Loading -> {
                            showLoader(requireActivity())
                        }
                        is NetworkResults.Error -> {
                            showAlertBioAuth(networkResults.message, requireContext())
                        }
                    }

                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                bioAuthViewModel.submitBioAuthLiveData.collectLatest { networkResults ->
                    hideLoader()
                    when (networkResults) {
                        is NetworkResults.Success -> {
                            networkResults.data?.let {
                                val status = it.status
                                if (status.equals("0", ignoreCase = true)) {
                                    Toast.makeText(
                                        requireActivity(),
                                        "SUCCESS",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    findNavController().navigate(R.id.action_unifiedBioAuthFragment_to_unifiedAepsFragment)
                                } else {
                                    Toast.makeText(
                                        requireActivity(),
                                        "FAILURE",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        is NetworkResults.Loading -> {
                            showLoader(requireActivity())
                        }
                        is NetworkResults.Error -> {
                            showAlertBioAuth(networkResults.message, requireContext())

                        }
                        else -> {}
                    }
                }
            }
        }


    }


    private fun makePrettyString(string: String): String {
        val number = string.replace("-".toRegex(), "")
        val isEndHyphen = string.endsWith("-") && number.length % 4 == 0
        return number.replace("(.{4}(?!$))".toRegex(), "$1-") + if (isEndHyphen) "-" else ""
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
            var cursorpos = (makePrettyString(stringWithMarker)).indexOf(marker)
            if (isDeleteHyphen) cursorpos -= 1
        }
        return cursorPos

    }

    private fun getRDServiceClass() {
        val accessClassName: String =
            SdkConstants.DRIVER_ACTIVITY
        flagNameRdService =
            SdkConstants.MANUFACTURE_FLAG
        try {
            val targetActivity = Class.forName(accessClassName).asSubclass(
                Activity::class.java
            )
            driverActivity = targetActivity
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }


    }
    @RequiresApi(Build.VERSION_CODES.M)
    fun checkPermission() {
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val permissionsToRequest: MutableList<String> = ArrayList()
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(
                    requireActivity(), permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(permission)
            }
        }
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), REQUEST_CAMERA_PERMISSIONS
            )
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        if (flagFromDriver) {
            if (SdkConstants.RECEIVE_DRIVER_DATA.isEmpty() || SdkConstants.RECEIVE_DRIVER_DATA.equals(
                    ""
                )
            ) {
                binding.twoFactFingerprint.isEnabled = true
                binding.twoFactSubmitButton.isEnabled = false
            } else if (balanceInquiryAadhaarNo.equals(
                    "",
                    ignoreCase = true
                ) || balanceInquiryAadhaarNo!!.isEmpty()
            ) {
                binding.balanceAadharNumber.error = "Enter Aadhar No."
                fingerStrength()
            } else {
                fingerStrength()
                binding.twoFactFingerprint.isEnabled = false
                binding.twoFactSubmitButton.isEnabled = true
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkPermission()
            }
        }
    }

    private fun fingerStrength() {
        try {
            val respObj = JSONObject(SdkConstants.RECEIVE_DRIVER_DATA)
            val scoreStr = respObj.getString("pidata_qscore")
            if (scoreStr.toFloat() <= 40) {
                binding.depositBar.visibility = View.VISIBLE
                binding.depositBar.progress = scoreStr.toFloat()
                binding.depositBar.setProgressTextMoved(true)
                binding.depositBar.setEndColor(resources.getColor(R.color.red))
                binding.depositBar.setStartColor(resources.getColor(R.color.red))
            } else {
                binding.depositBar.visibility = View.VISIBLE
                binding.depositBar.progress = scoreStr.toFloat()
                binding.depositBar.setProgressTextMoved(true)
                binding.depositBar.setEndColor(resources.getColor(R.color.green))
                binding.depositBar.setStartColor(resources.getColor(R.color.green))
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val permissionLocation = ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
//            getMyLocation()
        } else {
            Toast.makeText(
                requireActivity(),
                "Please accept the location permission",
                Toast.LENGTH_SHORT
            ).show()
            if (!requireActivity().isFinishing) {
                requireActivity().finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 50) {
            hideLoader()
        }
    }

}