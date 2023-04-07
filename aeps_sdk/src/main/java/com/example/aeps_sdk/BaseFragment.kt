package com.example.aeps_sdk

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.example.aeps_sdk.utils.SdkConstants
import org.json.JSONObject

open class BaseFragment : Fragment() {
    var loadingView: ProgressDialog? = null
    private lateinit var dataObject:String
    private lateinit var `object`:JSONObject

    fun getAndSetData() {
        if(requireActivity().intent.hasExtra("data")){
            dataObject= requireActivity().intent.getStringExtra("data").toString()
            `object` = JSONObject(dataObject)
        }
        if(`object`.has("transactionType")){
            SdkConstants.transactionType = `object`.getString("transactionType").toString()
        }
        if(`object`.has("tokenFromCoreApp")){
            SdkConstants.tokenFromCoreApp = `object`.getString("tokenFromCoreApp").toString()
        }
        if(`object`.has("applicationType")){
            SdkConstants.applicationType = `object`.getString("applicationType").toString()
        }
        if(`object`.has("userNameFromCoreApp")){
            SdkConstants.userNameFromCoreApp = `object`.getString("userNameFromCoreApp").toString()
        }
        if(`object`.has("DRIVER_ACTIVITY")){
            SdkConstants.DRIVER_ACTIVITY = `object`.getString("DRIVER_ACTIVITY").toString()
        }
        if(`object`.has("transactionAmount")){
            SdkConstants.transactionAmount = `object`.getString("transactionAmount").toString()
        }
        if(`object`.has("paramA")){
            SdkConstants.paramA=`object`.getString("paramA").toString()
        }
        if(`object`.has("paramB")){
            SdkConstants.paramB=`object`.getString("paramB").toString()
        }
        if(`object`.has("paramC")){
            SdkConstants.paramC=`object`.getString("paramC").toString()
        }
        if(`object`.has("API_USER_NAME_VALUE")){
            SdkConstants.API_USER_NAME_VALUE=`object`.getString("API_USER_NAME_VALUE").toString()
        }
        if(`object`.has("SHOP_NAME")){
            SdkConstants.SHOP_NAME=`object`.getString("SHOP_NAME").toString()
        }
        if(`object`.has("skipReceiptPart")){
            SdkConstants.skipReceiptPart=`object`.getString("skipReceiptPart").toBoolean()
        }
        if(`object`.has("internalFPName")){
            SdkConstants.internalFPName=`object`.getString("internalFPName")
        }
        if(`object`.has("DEVICE_TYPE")){
            SdkConstants.DEVICE_TYPE=`object`.getString("DEVICE_TYPE")
        }
        if(`object`.has("loginID")){
            SdkConstants.loginID=`object`.getString("loginID")
        }
        if(`object`.has("encryptedData")){
            SdkConstants.encryptedData=`object`.getString("encryptedData")
        }
        if(`object`.has("DEVICE_NAME")){
            SdkConstants.DEVICE_NAME=`object`.getString("DEVICE_NAME")
        }
        if(`object`.has("USER_MOBILE_NO")){
            SdkConstants.USER_MOBILE_NO=`object`.getString("USER_MOBILE_NO")
        }
        if(`object`.has("IS_BETA_USER")){
            SdkConstants.IS_BETA_USER=`object`.getString("IS_BETA_USER").toBoolean()
        }

    }

    open fun showLoader(activity: Activity) {
        try {
            if (loadingView == null) {
                loadingView = ProgressDialog(activity)
                loadingView!!.setCancelable(false)
                loadingView!!.setMessage("Please Wait..")
            }
            loadingView!!.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    open fun hideLoader() {
        try {
            loadingView?.dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    open fun showAlertBioAuth(msg: String?, showAlert_context: Context?) {
        try {
            val builder = AlertDialog.Builder(
                showAlert_context!!
            )
            builder.setTitle("Alert!!")
            builder.setMessage(msg)
            builder.setPositiveButton(
                "OK"
            ) { dialog, _ ->
                dialog.dismiss()
                requireActivity().finish()
            }
            val dialog = builder.create()
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    open fun showAlert(msg: String?, showAlert_context: Context?) {
        try {
            val builder = AlertDialog.Builder(
                showAlert_context!!
            )
            builder.setTitle("Alert!!")
            builder.setMessage(msg)
            builder.setPositiveButton(
                "OK"
            ) { dialog, _ -> dialog.dismiss() }
            val dialog = builder.create()
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showTermsDetails(activity: Activity?) {
        try {
            val dialog = Dialog(requireActivity())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.activity_terms_conditions)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            val firstText = dialog.findViewById<View>(R.id.firststText) as TextView
            val secondText = dialog.findViewById<View>(R.id.secondstText) as TextView
            val switchCompat = dialog.findViewById<View>(R.id.swOnOff) as SwitchCompat
            switchCompat.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    firstText.text = resources.getString(R.string.hinditm1)
                    secondText.text = resources.getString(R.string.hinditm2)
                } else {
                    firstText.text = resources.getString(R.string.term1)
                    secondText.text = resources.getString(R.string.term2)
                }
            }
            val dialogBtnClose = dialog.findViewById<View>(R.id.close_Btn) as Button
            dialogBtnClose.setOnClickListener { dialog.cancel() }
            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(activity, "Error$e", Toast.LENGTH_SHORT).show()
        }
    }


}