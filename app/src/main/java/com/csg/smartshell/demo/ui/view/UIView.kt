package com.csg.smartshell.demo.ui.view

import android.content.Context
import android.os.Handler
import android.widget.Toast

val handler = Handler()

public fun Context.toast(string: String?){
    handler.post {
        Toast.makeText(this , string , Toast.LENGTH_SHORT).show()
    }
}