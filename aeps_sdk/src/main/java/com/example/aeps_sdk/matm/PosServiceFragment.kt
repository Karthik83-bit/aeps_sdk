package com.example.aeps_sdk.matm

import android.R.style
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.example.aeps_sdk.BaseFragment
import com.example.aeps_sdk.R
import com.example.aeps_sdk.R.string
import com.example.aeps_sdk.databinding.FragmentPosServiceBinding
import com.example.aeps_sdk.location.GpsTracker
import com.example.aeps_sdk.utils.SdkConstants
import com.example.aeps_sdk.utils.SdkConstants.Companion.A910
import com.example.aeps_sdk.utils.SdkConstants.Companion.DEVICE_TYPE
import com.example.aeps_sdk.utils.SdkConstants.Companion.Newland
import com.example.aeps_sdk.utils.SdkConstants.Companion.POS
import com.example.aeps_sdk.utils.SdkConstants.Companion.Wiseasy
import com.example.aeps_sdk.utils.SdkConstants.Companion.integratedpos
import com.example.aeps_sdk.utils.SdkConstants.Companion.mATM2
import com.example.aeps_sdk.utils.SdkConstants.Companion.matmservice
import com.example.aeps_sdk.utils.SdkConstants.Companion.matmservice_1
import com.example.aeps_sdk.utils.SdkConstants.Companion.morefun
import com.example.aeps_sdk.utils.SdkConstants.Companion.pax

class PosServiceFragment : BaseFragment() {
    private var _binding: FragmentPosServiceBinding? = null
    private val binding get() = _binding!!
    private var gpsTracker: GpsTracker? = null
    private var latLong = ""
    private var latitude: Double? = null
    private var longitude: Double? = null

    private val registerActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val data = result.data!!
                if (data.hasExtra("error1Response")) {
                    val bundle = Bundle()
                    bundle.putInt("errorResponse", data.getIntExtra("error1Response", 0))
                    findNavController().navigate(R.id.action_posServiceFragment_to_errorFragment,bundle)
                } else if (data.hasExtra("errorResponse")) {
                    val bundle = Bundle()
                    bundle.putInt("errorResponse", data.getIntExtra("errorResponse", 0))
                    findNavController().navigate(R.id.action_posServiceFragment_to_error2Fragment)
                } else {
                    val bundle=Bundle()
                    bundle.putString("flag",data.getStringExtra("flag"))
                    bundle.putString("TRANSACTION_ID", data.getStringExtra("TRANSACTION_ID"))
                    bundle.putString("TRANSACTION_TYPE", data.getStringExtra("TRANSACTION_TYPE"))
                    bundle.putString("TRANSACTION_AMOUNT", data.getStringExtra("TRANSACTION_AMOUNT"))
                    bundle.putString("RRN_NO", data.getStringExtra("RRN_NO"))
                    bundle.putString("RESPONSE_CODE", data.getStringExtra("RESPONSE_CODE"))
                    bundle.putString("APP_NAME", data.getStringExtra("APP_NAME"))
                    bundle.putString("AID", data.getStringExtra("AID"))
                    bundle.putString("AMOUNT", data.getStringExtra("AMOUNT"))
                    bundle.putString("MID", data.getStringExtra("MID"))
                    bundle.putString("TID", data.getStringExtra("TID"))
                    bundle.putString("TXN_ID", data.getStringExtra("TXN_ID"))
                    bundle.putString("INVOICE", data.getStringExtra("INVOICE"))
                    bundle.putString("CARD_TYPE", data.getStringExtra("CARD_TYPE"))
                    bundle.putString("APPR_CODE", data.getStringExtra("APPR_CODE"))
                    bundle.putString("CARD_NUMBER", data.getStringExtra("CARD_NUMBER"))
                    bundle.putString("CARD_HOLDERNAME", data.getStringExtra("CARD_HOLDERNAME"))
                    bundle.putString("status_code", data.getStringExtra("status_code"))
                    Log.d("bundleData", bundle.toString())
                    findNavController().navigate(R.id.action_posServiceFragment_to_transactionStatusFragment,bundle)
                }
            } else {
                requireActivity().finish()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPosServiceBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        getAndSetData()
        getLocation()
        if (DEVICE_TYPE.equals(
                Wiseasy,
                ignoreCase = true
            ) && SdkConstants.transactionType.equals(POS, ignoreCase = true)
        ) {
            checkAppInstalledOrNot(integratedpos)
        } else if (SdkConstants.IS_BETA_USER) {
            checkAppInstalledOrNot(matmservice_1)
        } else {
            checkAppInstalledOrNot(matmservice)
        }
    }

    private fun checkAppInstalledOrNot(packageName: String) {
        val installed = appInstalledOrNot(packageName)
        try {
            if (installed) {
                if (DEVICE_TYPE.equals(morefun, ignoreCase = true)) {
                    sendDataToService(packageName, morefun)
                } else if (DEVICE_TYPE.equals(Wiseasy, ignoreCase = true)) {
                    sendDataToService(packageName, Wiseasy)
                } else if (DEVICE_TYPE.equals(A910, ignoreCase = true)) {
                    sendDataToService(packageName, A910)
                } else if (DEVICE_TYPE.equals(Newland, ignoreCase = true)) {
                    sendDataToService(packageName, Newland)
                } else if (DEVICE_TYPE.equals(pax, ignoreCase = true)) {
                    sendDataToService(packageName, mATM2)
                } else {
                    showDeviceTypeAlertMsg(requireActivity())
                }
            } else {
                /*It will show if the app is not install in your phone*/
                when (packageName) {
                    integratedpos -> {
                        showAlertPOSWiseasy(requireActivity(), packageName)
                    }
                    matmservice_1 -> {
                        showAlert(requireActivity())
                    }
                    else -> {
                        showMatmAlert(requireActivity())
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showMatmAlert(context: Context) {
        try {
            val alertbuilderupdate: AlertDialog.Builder =
                AlertDialog.Builder(context, style.Theme_Material_Light_Dialog_Alert)
            alertbuilderupdate.setCancelable(false)
            val message = string.downloadMatmService_1
            alertbuilderupdate.setTitle("Alert")
                .setMessage(message)
                .setPositiveButton(
                    "Download Now"
                ) { _, _ ->
                    redirectToMatm2PlayStore()
                    requireActivity().finish()
                }
                .setNegativeButton("Not Now") { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                    requireActivity().finish()
                }
            val alert11 = alertbuilderupdate.create()
            alert11.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun redirectToMatm2PlayStore() {
        val uri =
            Uri.parse("https://play.google.com/store/apps/details?id=com.matm.matmservice&hl=en_US")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        goToMarket.addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=com.matm.matmservice&hl=en_US")
                )
            )
        }
    }


    private fun showAlert(context: Context) {
        try {
            val alertbuilderupdate: AlertDialog.Builder =
                AlertDialog.Builder(context, style.Theme_Material_Light_Dialog_Alert)
            alertbuilderupdate.setCancelable(false)
            val message = string.downloadMatmService
            alertbuilderupdate.setTitle("Alert")
                .setMessage(message)
                .setPositiveButton(
                    "Download Now"
                ) { _, _ ->
                    redirectToPlayStore()
                    requireActivity().finish()
                }
                .setNegativeButton(
                    "Not Now"
                ) { dialog, _ ->
                    dialog.dismiss()
                    requireActivity().finish()
                }
            val alert11 = alertbuilderupdate.create()
            alert11.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun redirectToPlayStore() {
        val uri =
            Uri.parse("https://play.google.com/store/apps/details?id=com.matm.matmservice_1&hl=en_US")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        goToMarket.addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=com.matm.matmservice_1&hl=en_US")
                )
            )
        }
    }


    private fun showAlertPOSWiseasy(context: Context, packageName: String) {
        try {
            val alertbuilderupdate =
                AlertDialog.Builder(context, style.Theme_Material_Light_Dialog_Alert)
            alertbuilderupdate.setCancelable(false)
            val message = string.downloadPosService
            alertbuilderupdate.setTitle("Alert")
                .setMessage(message)
                .setPositiveButton("Download Now") { _, _ ->
                    redirectToPlayStorePOSWiseasy(packageName)
                    requireActivity().finish()
                }
                .setNegativeButton("Not Now") { dialog, _ ->
                    dialog.dismiss()
                    requireActivity().finish()
                }
            val dialog = alertbuilderupdate.create()
            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun redirectToPlayStorePOSWiseasy(packageName: String) {
        val uri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName&hl=en_US")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        goToMarket.addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName&hl=en_US")
                )
            )
        }
    }

    private fun showDeviceTypeAlertMsg(context: Context) {
        try {
            val alertbuilderupdate =
                AlertDialog.Builder(context, style.Theme_Material_Light_Dialog_Alert)
            alertbuilderupdate.setCancelable(false)
            val message = string.deviceNotFound
            alertbuilderupdate.setTitle("Alert")
                .setMessage(message)
                .setPositiveButton("Ok") { dialog, _ ->
                    dialog.dismiss()
                    requireActivity().finish()
                }
            val alertDialog = alertbuilderupdate.create()
            alertDialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun sendDataToService(packageName: String, activityName: String) {
        val manager: PackageManager = requireActivity().packageManager
        val sIntent = manager.getLaunchIntentForPackage(packageName)
        sIntent!!.flags = 0
        sIntent.putExtra("ActivityName", activityName)
        if (SdkConstants.applicationType == "CORE") {
            sIntent.putExtra("UserName", SdkConstants.userNameFromCoreApp)
            sIntent.putExtra("UserToken", SdkConstants.tokenFromCoreApp)
            sIntent.putExtra("ApplicationType", "CORE")
        } else {
            sIntent.putExtra("LoginID", SdkConstants.loginID)
            sIntent.putExtra("EncryptedData", SdkConstants.encryptedData)
            sIntent.putExtra("ApplicationType", "")
        }
        sIntent.putExtra("ParamA", SdkConstants.paramA)
        sIntent.putExtra("ParamB", SdkConstants.paramB)
        sIntent.putExtra("ParamC", SdkConstants.paramC)
        sIntent.putExtra("latitude", latitude)
        sIntent.putExtra("longitude", longitude)
        sIntent.putExtra("Amount", SdkConstants.transactionAmount)
        sIntent.putExtra("TransactionType", SdkConstants.transactionType)
        sIntent.putExtra("deviceName", SdkConstants.DEVICE_NAME)
        sIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        registerActivity.launch(sIntent)
    }

    private fun appInstalledOrNot(uri: String): Boolean {
        val pm: PackageManager = requireActivity().packageManager
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return false
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
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}