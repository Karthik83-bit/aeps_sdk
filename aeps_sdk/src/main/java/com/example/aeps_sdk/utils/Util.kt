package com.example.aeps_sdk.utils

import android.util.Patterns
import com.example.aeps.utils.Verhoff
import java.util.regex.Pattern

class Util {
    companion object {
        fun validateAadharNumber(aadharNumber: String): Boolean {
            val aadharPattern = Pattern.compile("\\d{12}")
            var isValidAadhar = aadharPattern.matcher(aadharNumber).matches()
            if (isValidAadhar) {
                isValidAadhar = Verhoff.validateVerhoeff(aadharNumber)
            }
            return isValidAadhar
        }
        fun validateAadharVID(aadharNumber: String): Boolean {
            val aadharPattern = Pattern.compile("\\d{16}")
            var isValidAadhar = aadharPattern.matcher(aadharNumber).matches()
            if (isValidAadhar) {
                isValidAadhar = Verhoff.validateVerhoeff(aadharNumber)
            }
            return isValidAadhar
        }
        fun isValidMobile(phone: String): Boolean {
            var ph = phone
            var check = false
            if (ph.contains("+")) {
                ph = ph.replace("+", "")
            }
            if (ph.contains(" ")) {
                ph = ph.replace(" ", "")
            }
            if (ph.length in 10..12) {
                check = Patterns.PHONE.matcher(ph).matches()
            }
            return check
        }
        fun getShortState(state: String): String? {
            return try {
                if (state.equals("Andhra Pradesh", ignoreCase = true) || state.equals(
                        "AP",
                        ignoreCase = true
                    )
                ) {
                    "AP"
                } else if (state.equals(
                        "Arunachal Pradesh",
                        ignoreCase = true
                    ) || state.equals("AR", ignoreCase = true)
                ) {
                    "AR"
                } else if (state.equals("Assam", ignoreCase = true) || state.equals(
                        "AS",
                        ignoreCase = true
                    )
                ) {
                    "AS"
                } else if (state.equals("Bihar", ignoreCase = true) || state.equals(
                        "BR",
                        ignoreCase = true
                    )
                ) {
                    "BR"
                } else if (state.equals("Chandigarh", ignoreCase = true) || state.equals(
                        "CH",
                        ignoreCase = true
                    )
                ) {
                    "CH"
                } else if (state.equals("Chhattisgarh", ignoreCase = true) || state.equals(
                        "CG",
                        ignoreCase = true
                    )
                ) {
                    "CG"
                } else if (state.equals(
                        "Dadra and Nagar Haveli",
                        ignoreCase = true
                    ) || state.equals("DN", ignoreCase = true)
                ) {
                    "DN"
                } else if (state.equals("Daman & Diu", ignoreCase = true) || state.equals(
                        "DD",
                        ignoreCase = true
                    )
                ) {
                    "DD"
                } else if (state.equals("Delhi", ignoreCase = true) || state.equals(
                        "DL",
                        ignoreCase = true
                    )
                ) {
                    "DL"
                } else if (state.equals("Goa", ignoreCase = true) || state.equals(
                        "GA",
                        ignoreCase = true
                    )
                ) {
                    "GA"
                } else if (state.equals("Gujarat", ignoreCase = true) || state.equals(
                        "GJ",
                        ignoreCase = true
                    )
                ) {
                    "GJ"
                } else if (state.equals("Haryana", ignoreCase = true) || state.equals(
                        "HR",
                        ignoreCase = true
                    )
                ) {
                    "HR"
                } else if (state.equals("Himachal Pradesh", ignoreCase = true) || state.equals(
                        "HP",
                        ignoreCase = true
                    )
                ) {
                    "HP"
                } else if (state.equals("Jammu & Kashmir", ignoreCase = true) || state.equals(
                        "JK",
                        ignoreCase = true
                    )
                ) {
                    "JK"
                } else if (state.equals("Karnataka", ignoreCase = true) || state.equals(
                        "KA",
                        ignoreCase = true
                    )
                ) {
                    "KA"
                } else if (state.equals("Kerala", ignoreCase = true) || state.equals(
                        "KL",
                        ignoreCase = true
                    )
                ) {
                    "KL"
                } else if (state.equals("Lakshadweep", ignoreCase = true) || state.equals(
                        "LD",
                        ignoreCase = true
                    )
                ) {
                    "LD"
                } else if (state.equals("Madhya Pradesh", ignoreCase = true) || state.equals(
                        "MP",
                        ignoreCase = true
                    )
                ) {
                    "MP"
                } else if (state.equals("Maharashtra", ignoreCase = true) || state.equals(
                        "MH",
                        ignoreCase = true
                    )
                ) {
                    "MH"
                } else if (state.equals("Manipur", ignoreCase = true) || state.equals(
                        "MN",
                        ignoreCase = true
                    )
                ) {
                    "MN"
                } else if (state.equals("Meghalaya", ignoreCase = true) || state.equals(
                        "ML",
                        ignoreCase = true
                    )
                ) {
                    "ML"
                } else if (state.equals("Mizoram", ignoreCase = true) || state.equals(
                        "MZ",
                        ignoreCase = true
                    )
                ) {
                    "MZ"
                } else if (state.equals("Nagaland", ignoreCase = true) || state.equals(
                        "NL",
                        ignoreCase = true
                    )
                ) {
                    "NL"
                } else if (state.equals("Orissa", ignoreCase = true) || state.equals(
                        "OR",
                        ignoreCase = true
                    )
                ) {
                    "OR"
                } else if (state.equals("Odisha", ignoreCase = true) || state.equals(
                        "OR",
                        ignoreCase = true
                    )
                ) {
                    "OR"
                } else if (state.equals("Puducherry", ignoreCase = true) || state.equals(
                        "PY",
                        ignoreCase = true
                    )
                ) {
                    "PY"
                } else if (state.equals("Punjab", ignoreCase = true) || state.equals(
                        "PB",
                        ignoreCase = true
                    )
                ) {
                    "PB"
                } else if (state.equals("Rajasthan", ignoreCase = true) || state.equals(
                        "RJ",
                        ignoreCase = true
                    )
                ) {
                    "RJ"
                } else if (state.equals("Sikkim", ignoreCase = true) || state.equals(
                        "SK",
                        ignoreCase = true
                    )
                ) {
                    "SK"
                } else if (state.equals("Tamil Nadu", ignoreCase = true) || state.equals(
                        "TN",
                        ignoreCase = true
                    )
                ) {
                    "TN"
                } else if (state.equals("Telangana", ignoreCase = true) || state.equals(
                        "TG",
                        ignoreCase = true
                    )
                ) {
                    "TG"
                } else if (state.equals("Tripura", ignoreCase = true) || state.equals(
                        "TR",
                        ignoreCase = true
                    )
                ) {
                    "TR"
                } else if (state.equals("Uttar Pradesh", ignoreCase = true) || state.equals(
                        "UP",
                        ignoreCase = true
                    )
                ) {
                    "UP"
                } else if (state.equals(
                        "Uttarakhand (Uttranchal)",
                        ignoreCase = true
                    ) || state.equals("UK", ignoreCase = true)
                ) {
                    "UK"
                } else if (state.equals("West Bengal", ignoreCase = true) || state.equals(
                        "WB",
                        ignoreCase = true
                    )
                ) {
                    "WB"
                } else {
                    "BR"
                }
            } catch (e: Exception) {
                "BR"
            }
        }

    }
}