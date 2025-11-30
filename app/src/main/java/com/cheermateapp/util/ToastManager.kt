package com.cheermateapp.util

import android.content.Context
import android.widget.Toast

object ToastManager {
    private var toast: Toast? = null

    fun showToast(context: Context, message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
        toast?.cancel()
        toast = Toast.makeText(context, message, duration)
        toast?.show()
    }
}
