package com.example.aeps_sdk

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.aeps_sdk.callbacks.OnFinishListener
import com.example.aeps_sdk.databinding.ActivityMainBinding
import com.example.aeps_sdk.matm.MatmMainActivity
import com.example.aeps_sdk.utils.SdkConstants
import com.example.irctc_library.DashBoardActivity
import com.google.gson.Gson
import org.json.JSONObject

class MainActivity : AppCompatActivity(), OnFinishListener {
    private lateinit var binding: ActivityMainBinding
    private var skipReceipt: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnAeps.setOnClickListener {
            callAepsSDKApp()
        }
        binding.btnMatm.setOnClickListener {
            callMatmSdkApp()
        }
        binding.rgTransType.setOnCheckedChangeListener(object: RadioGroup.OnCheckedChangeListener {
            override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
//              Toast.makeText(this@MainActivity.applicationContext,"${R.id.rb_be==checkedId}",Toast.LENGTH_SHORT).show()
                if(R.id.rb_be==checkedId||R.id.rb_mini==checkedId){
                    binding.etAmount.visibility= View.INVISIBLE
                }else{
                    binding.etAmount.visibility= View.VISIBLE
                }
            }

        })
    }

    private fun callMatmSdkApp() {
        val dataModel = DataModel()
        dataModel.DEVICE_TYPE = SdkConstants.pax
        dataModel.transactionType = SdkConstants.cashWithdrawal
        dataModel.transactionAmount = "100"
        dataModel.paramA = "123456789"
        dataModel.paramB = "branch1"
        dataModel.paramC = "loanID1234"
        dataModel.paramC = "7978628756"
//        dataModel.loginID="aepsTestR"
//        dataModel.encryptedData="cssC%2BcHGxugRFLTjpk%2BJN2Hbbo%2F%2BDokPsBwb9uFdXebdGg%2FEaqOvFXBEoU7ve%2FAP6rabeaskLloqjx6bF6tCcw%3D%3D"
        dataModel.userNameFromCoreApp="itpl"
        dataModel.tokenFromCoreApp="eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJpdHBsIiwiYXVkaWVuY2UiOiJ3ZWIiLCJjcmVhdGVkIjoxNjY0NzkyNzIyOTk2LCJleHAiOjE2NjQ3OTYzMjJ9.zBz8831NppkC1ikud15yB_0q8HoH5gs305d9LzCFiednKsLaeO3W-uGMGdKu59LkZFw0_3jrVR-vRV_6TYVe9A"
        dataModel.DEVICE_NAME=SdkConstants.pax
        dataModel.applicationType="CORE"
        SdkConstants.onFinishListener=this
        dataModel.IS_BETA_USER=true
        val intent = Intent(this, MatmMainActivity::class.java)
        val gson = Gson()
        val getData: String = gson.toJson(dataModel)
        intent.putExtra("data", getData)
        startActivity(intent)
    }

    private fun callAepsSDKApp() {
        skipReceipt = binding.skipReceiptCB.isChecked
        val txnAmount = binding.etAmount.text.toString().trim()
        val dataModel = DataModel()
        if (binding.rbMini.isChecked) {
            dataModel.transactionType = "2"
        } else if (binding.rbBe.isChecked) {
            dataModel.transactionType = "0"
        } else if (binding.rbCw.isChecked) {
            dataModel.transactionType = "1"
        } else if (binding.rbAdhaarpay.isChecked) {
            dataModel.transactionType = "3"
        }
        dataModel.transactionAmount = txnAmount
        dataModel.paramA = "test"
        dataModel.paramB = "BLS1"
        dataModel.paramC = "loanID"

//        For SDK CLients
        dataModel.applicationType = ""
        dataModel.tokenFromCoreApp =
            "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJhbm5peWFtLWFlcHNAY3JlZGl0YXBwLTI5YmYyLmlhbS5nc2VydmljZWFjY291bnQuY29tIiwiYXVkIjoiMTE1OTQyODI3NDI0OTQzNTQzNjE4IiwiZXhwIjoxNjkyOTcyNzk1LCJpYXQiOjE2NjE0MzY3OTUsInN1YiI6ImFubml5YW0tYWVwc0BjcmVkaXRhcHAtMjliZjIuaWFtLmdzZXJ2aWNlYWNjb3VudC5jb20iLCJlbWFpbCI6ImFubml5YW0tYWVwc0BjcmVkaXRhcHAtMjliZjIuaWFtLmdzZXJ2aWNlYWNjb3VudC5jb20ifQ.WTMYIq6O8p_Dqyg1XrLt1LYCyXQBfPCvXUGW8G4irj7X7nCsEOVfgnjymLfETQNLLTHnmVdS-J4BW9SLqYQDsIv2opPAZVzHxRom3NNWZZfLhBhWUobTyq8it5wh4VQ8osCFYmFHZzlRYpiBY-xFaFXUB1LULAQizm95LtqrinyOVTc1p0w1nQc4LQiPQCtGfvfufadtaVF4Y4z4UMCmzyppOGjH_eh-O9_-AyHN72Au9oF2_277dfLnlu0JmPWwr56EKtUkboLqe77xSiR-sUl_Nj-Zsn1hcLbt5xRo4V8Ebnd_QQunXjXOojNmXvubmd6Zzm9irZdyJzhA9FjDMQ"
        dataModel.userNameFromCoreApp = "Anniyamtest"
        dataModel.API_USER_NAME_VALUE = "anniyamapi"

        //For pro app user
        /*dataModel.tokenFromCoreApp="eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJpdHBsIiwiYXVkaWVuY2UiOiJ3ZWIiLCJjcmVhdGVkIjoxNjY0MTc2MjM3NDQ1LCJleHAiOjE2NjQxNzk4Mzd9.VxAQKqILipyZYNu-JgcJvrpRTy4TJnp8lusdsz1y115jpIHIF9eXEXv6glxLQ00AG1Umaht3CiAitjwfxCAKnw"
        dataModel.applicationType="CORE"
        dataModel.userNameFromCoreApp="itpl"
        dataModel.API_USER_NAME_VALUE=""*/


        dataModel.DRIVER_ACTIVITY =
            "com.example.aeps_sdk.DriverActivity"
        dataModel.BRAND_NAME = "iServeU Technology"
        dataModel.SHOP_NAME = "itpl"
        dataModel.skipReceiptPart = skipReceipt
        SdkConstants.onFinishListener = this
//        dataModel.internalFPName = "wiseasy"
        if (binding.rbCw.isChecked) {
            if (binding.etAmount.text.toString() == "") {
                binding.etAmount.error = "Please enter amount"
                return
            }
        }
        val intent = Intent(this, AepsMainActivity::class.java)
        val gson = Gson()
        val getData: String = gson.toJson(dataModel)
        intent.putExtra("data", getData)
        startActivity(intent)
    }

    override fun onSdkFinish(
        statusTxt: String,
        paramA: String,
        statusDesc: String,
        jsonString: String?
    ) {
        Toast.makeText(this, jsonString.toString(), Toast.LENGTH_SHORT).show()
        try{
            val jsonObject = JSONObject(jsonString.toString())
        }catch (e:Exception){
            Log.d("jsonException", e.printStackTrace().toString())
        }
    }

}