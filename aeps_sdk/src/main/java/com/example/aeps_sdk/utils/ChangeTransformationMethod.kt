package com.example.aeps_sdk.utils

import android.text.method.PasswordTransformationMethod
import android.view.View

class ChangeTransformationMethod: PasswordTransformationMethod() {
    override fun getTransformation(source: CharSequence?, view: View?): CharSequence {
        return PasswordCharSequence(source!!)
    }
    private class PasswordCharSequence(private val mSource: CharSequence) :
        CharSequence {
        override val length: Int
            get() =mSource.length

        override fun get(index: Int): Char {
            return if (index <= 8 && mSource.length > 9) 'X' else mSource[index]
        }
        override fun subSequence(start: Int, end: Int): CharSequence {
            return mSource.subSequence(start, end)
        }
    }


}