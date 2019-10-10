package com.smartshell.infrared.ui.view

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun Context.toast(string: String?) {
    GlobalScope.launch(Dispatchers.Main) {
        Toast.makeText(this@toast, string, Toast.LENGTH_SHORT).show()
    }
}