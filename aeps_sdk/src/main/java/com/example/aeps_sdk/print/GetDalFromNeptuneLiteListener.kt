package com.example.aeps_sdk.print

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.pax.dal.IDAL
import com.pax.neptunelite.api.NeptuneLiteUser

class GetDalFromNeptuneLiteListener : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        dal = getDal()
    }

    companion object {
        private var dal: IDAL? = null
        private var appContext: Context? = null
        fun getDal(): IDAL? {
            if (dal == null) {
                try {
                    val start = System.currentTimeMillis()
                    dal = NeptuneLiteUser.getInstance().getDal(appContext)
                    Log.i("Test", "get dal cost:" + (System.currentTimeMillis() - start) + " ms")
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(appContext, "error occurred,DAL is null.", Toast.LENGTH_LONG)
                        .show()
                }
            }
            return dal
        }
    }
}
